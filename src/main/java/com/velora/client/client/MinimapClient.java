package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimapClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static final int MAP_SIZE = 96;
    private static NativeImage nativeImage;
    private static NativeImageBackedTexture mapTexture;
    private static Identifier textureId;
    private static long lastUpdateMs = 0;
    private static int lastPlayerX = Integer.MIN_VALUE;
    private static int lastPlayerZ = Integer.MIN_VALUE;
    private static int lastPlayerY = Integer.MIN_VALUE;
    private static int lastZoomHash = 0;

    public static void init() {
        LOGGER.info("[Velora] Minimap HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showMinimap) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.options.hudHidden) return;

            ensureTextureRegistered(client);
            updateMapTextureIfNeeded(client);
            renderMinimap(drawContext, client);
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    private static void ensureTextureRegistered(MinecraftClient client) {
        if (mapTexture == null) {
            nativeImage = new NativeImage(MAP_SIZE, MAP_SIZE, false);
            mapTexture = new NativeImageBackedTexture(nativeImage);
            textureId = Identifier.of("velora", "textures/gui/dynamic_minimap.png");
            client.getTextureManager().registerTexture(textureId, mapTexture);
        }
    }

    public static void close() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (textureId != null && mc != null) {
            mc.getTextureManager().destroyTexture(textureId);
        }
        if (nativeImage != null) {
            nativeImage.close();
            nativeImage = null;
        }
        mapTexture = null;
        textureId = null;
    }

    private static void updateMapTextureIfNeeded(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        int playerX = MathHelper.floor(client.player.getX());
        int playerY = MathHelper.floor(client.player.getY());
        int playerZ = MathHelper.floor(client.player.getZ());

        int zoomHash = Float.floatToIntBits(ModConfig.minimapZoom);
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs < 100 && playerX == lastPlayerX && playerZ == lastPlayerZ
                && Math.abs(playerY - lastPlayerY) < 3 && zoomHash == lastZoomHash) {
            return;
        }

        lastUpdateMs = now;
        lastPlayerX = playerX;
        lastPlayerY = playerY;
        lastPlayerZ = playerZ;
        lastZoomHash = zoomHash;

        int halfSize = MAP_SIZE / 2;
        float zoom = Math.max(0.5f, Math.min(3.0f, ModConfig.minimapZoom));
        int worldRadius = (int) (halfSize / zoom);

        ClientWorld world = client.world;
        boolean hasCeiling = world.getDimension().hasCeiling();
        boolean isCircle = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape);

        for (int px = 0; px < MAP_SIZE; px++) {
            for (int pz = 0; pz < MAP_SIZE; pz++) {
                int dx = px - halfSize;
                int dz = pz - halfSize;

                if (isCircle && (dx * dx + dz * dz > (halfSize - 2) * (halfSize - 2))) {
                    nativeImage.setColorArgb(px, pz, 0x00000000);
                    continue;
                }

                int worldX = playerX + (int) (dx / zoom);
                int worldZ = playerZ + (int) (dz / zoom);

                int color = getPixelColor(world, worldX, playerY, worldZ, hasCeiling);
                nativeImage.setColorArgb(px, pz, color);
            }
        }

        mapTexture.upload();
    }

    private static int getPixelColor(ClientWorld world, int x, int playerY, int z, boolean hasCeiling) {
        int y;
        if (hasCeiling) {
            int startY = Math.min(playerY + 8, 120);
            int minY = Math.max(world.getBottomY(), playerY - 20);
            y = startY;
            BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
            while (y > minY && world.getBlockState(mutable).isAir()) {
                y--;
                mutable.set(x, y, z);
            }
        } else {
            y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        }

        BlockPos pos = new BlockPos(x, Math.max(world.getBottomY(), y - 1), z);
        BlockState state = world.getBlockState(pos);

        if (state.isOf(Blocks.WATER) || state.isOf(Blocks.BUBBLE_COLUMN)) {
            int depth = 1;
            while (depth < 8 && world.getBlockState(pos.down(depth)).isOf(Blocks.WATER)) {
                depth++;
            }
            int waterBlue = Math.max(120, 240 - depth * 15);
            int waterGreen = Math.max(80, 180 - depth * 12);
            return (0xFF << 24) | (15 << 16) | (waterGreen << 8) | waterBlue;
        }

        if (state.isOf(Blocks.LAVA)) {
            return 0xFFFF5500;
        }

        MapColor mapColor = state.getMapColor(world, pos);
        int baseColor = (mapColor != null && mapColor.color != 0) ? mapColor.color : 0x557733;

        int northY = hasCeiling ? y : world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z - 1);
        int heightDiff = y - northY;

        float shadowFactor = 1.0f;
        if (heightDiff > 0) {
            shadowFactor = 1.18f;
        } else if (heightDiff < 0) {
            shadowFactor = 0.82f;
        }

        int r = MathHelper.clamp((int) (((baseColor >> 16) & 0xFF) * shadowFactor), 0, 255);
        int g = MathHelper.clamp((int) (((baseColor >> 8) & 0xFF) * shadowFactor), 0, 255);
        int b = MathHelper.clamp((int) ((baseColor & 0xFF) * shadowFactor), 0, 255);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public static void renderMinimap(DrawContext drawContext, MinecraftClient client) {
        if (textureId == null || client.player == null) return;

        drawContext.getMatrices().push();
        drawContext.getMatrices().scale(ModConfig.minimapScale, ModConfig.minimapScale, 1.0f);

        int baseX = (int) (ModConfig.minimapX / ModConfig.minimapScale);
        int baseY = (int) (ModConfig.minimapY / ModConfig.minimapScale);

        int size = MAP_SIZE;
        int halfSize = size / 2;
        int centerX = baseX + halfSize;
        int centerY = baseY + halfSize;
        boolean isCircle = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape);

        int bgPadding = isCircle ? 5 : 4;
        if (isCircle) {
            drawContext.fill(baseX - bgPadding, baseY - bgPadding, baseX + size + bgPadding, baseY + size + bgPadding, 0xBB08080A);
        } else {
            drawContext.fill(baseX - bgPadding, baseY - bgPadding, baseX + size + bgPadding, baseY + size + bgPadding, 0xCC08080A);
        }

        drawContext.drawTexture(
            RenderLayer::getGuiTextured,
            textureId,
            baseX, baseY,
            0.0f, 0.0f,
            size, size,
            size, size
        );

        int borderColor = isCircle ? 0xFF292930 : 0xFFA78BFA;
        if (isCircle) {
            drawContext.drawBorder(baseX - 2, baseY - 2, size + 4, size + 4, 0xFF1D1D22);
            drawContext.drawBorder(baseX - 1, baseY - 1, size + 2, size + 2, borderColor);
        } else {
            drawContext.drawBorder(baseX - 2, baseY - 2, size + 4, size + 4, 0xFF1D1D22);
            drawContext.drawBorder(baseX - 1, baseY - 1, size + 2, size + 2, borderColor);
            drawContext.fill(baseX, baseY - 2, baseX + size, baseY - 1, 0xFFA78BFA);
        }

        float playerYaw = MathHelper.wrapDegrees(client.player.getYaw());
        float compassAngleOffset = ModConfig.minimapRotateMap ? playerYaw : 0.0f;
        drawCompassLabels(drawContext, client.textRenderer, centerX, centerY, halfSize + 3, compassAngleOffset);

        if (ModConfig.minimapShowEntities && client.world != null) {
            renderEntityBlips(drawContext, client, centerX, centerY, halfSize - 4);
        }

        renderWaypointBlips(drawContext, client, centerX, centerY, halfSize - 4);

        renderPlayerArrow(drawContext, centerX, centerY, playerYaw);

        if (ModConfig.minimapShowBiome && client.world != null) {
            String biomeName = getFormattedBiomeName(client);
            String heading = getDirectionHeading(playerYaw);
            String headerText = heading + " | " + biomeName;
            drawContext.drawCenteredTextWithShadow(client.textRenderer, headerText, centerX, baseY - 12, 0xFFE4E4E7);
        }

        if (ModConfig.minimapShowCoordinates) {
            int px = MathHelper.floor(client.player.getX());
            int py = MathHelper.floor(client.player.getY());
            int pz = MathHelper.floor(client.player.getZ());
            String coordsText = String.format("X:%d Y:%d Z:%d", px, py, pz);
            drawContext.drawCenteredTextWithShadow(client.textRenderer, coordsText, centerX, baseY + size + 5, 0xFF71717A);
        }

        drawContext.getMatrices().pop();
    }

    private static void renderPlayerArrow(DrawContext context, int cx, int cy, float yaw) {
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw + 180.0f));

        context.fill(-2, 5, 2, 6, 0xFF1D1D22);
        context.fill(-3, 2, 3, 5, 0xFFA78BFA);
        context.fill(-2, -1, 2, 2, 0xFFC4B5FD);
        context.fill(-1, -4, 1, -1, 0xFFEDE9FE);

        context.getMatrices().pop();
    }

    private static void drawCompassLabels(DrawContext context, TextRenderer font, int cx, int cy, int radius, float angleOffset) {
        double radN = Math.toRadians(-angleOffset - 180);
        double radE = Math.toRadians(-angleOffset - 90);
        double radS = Math.toRadians(-angleOffset);
        double radW = Math.toRadians(-angleOffset + 90);

        int nx = (int) (cx + Math.sin(radN) * radius);
        int ny = (int) (cy - Math.cos(radN) * radius);
        context.drawText(font, "N", nx - 2, ny - 2, 0xFFEF4444, true);

        int ex = (int) (cx + Math.sin(radE) * radius);
        int ey = (int) (cy - Math.cos(radE) * radius);
        context.drawText(font, "E", ex - 2, ey - 2, 0xFFA1A1AA, true);

        int sx = (int) (cx + Math.sin(radS) * radius);
        int sy = (int) (cy - Math.cos(radS) * radius);
        context.drawText(font, "S", sx - 2, sy - 2, 0xFFA1A1AA, true);

        int wx = (int) (cx + Math.sin(radW) * radius);
        int wy = (int) (cy - Math.cos(radW) * radius);
        context.drawText(font, "W", wx - 2, wy - 2, 0xFFA1A1AA, true);
    }

    private static void renderEntityBlips(DrawContext context, MinecraftClient client, int cx, int cy, int maxRadius) {
        if (client.world == null || client.player == null) return;

        float zoom = Math.max(0.5f, Math.min(3.0f, ModConfig.minimapZoom));
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;

            double dx = entity.getX() - playerX;
            double dz = entity.getZ() - playerZ;
            double dy = entity.getY() - playerY;

            double worldRadius = maxRadius / zoom;
            if (Math.abs(dx) > worldRadius || Math.abs(dz) > worldRadius) continue;

            int blipX = (int) (cx + dx * zoom);
            int blipY = (int) (cy + dz * zoom);

            if (entity instanceof PlayerEntity player) {
                renderPlayerHeadIcon(context, client, blipX, blipY, player, dy);
                if (ModConfig.minimapShowEntityNames) {
                    String name = player.getName().getString();
                    int nameW = client.textRenderer.getWidth(name);
                    context.fill(blipX - nameW / 2 - 2, blipY - 13, blipX + nameW / 2 + 2, blipY - 3, 0xCC0F0F12);
                    context.drawText(client.textRenderer, name, blipX - nameW / 2, blipY - 12, 0xFFF4F4F5, true);
                }
            } else if (entity instanceof HostileEntity) {
                renderMobDiamond(context, blipX, blipY, 0xFFEF4444, 3);
            } else if (entity instanceof ItemEntity) {
                renderMobDiamond(context, blipX, blipY, 0xFFFBBF24, 2);
            } else {
                context.fill(blipX, blipY, blipX + 1, blipY + 1, 0xFF34D399);
            }

            if (dy > 4.0) {
                context.drawText(client.textRenderer, "+", blipX + 4, blipY - 3, 0xFF34D399, false);
            } else if (dy < -4.0) {
                context.drawText(client.textRenderer, "-", blipX + 4, blipY - 3, 0xFFEF4444, false);
            }
        }
    }

    private static void renderWaypointBlips(DrawContext context, MinecraftClient client, int cx, int cy, int maxRadius) {
        if (!ModConfig.minimapShowWaypoints || client.player == null) return;

        java.util.List<com.velora.client.waypoints.Waypoint> waypoints = com.velora.client.waypoints.WaypointManager.getVisibleWaypointsForCurrentDimension();
        if (waypoints.isEmpty()) return;

        float zoom = Math.max(0.5f, Math.min(3.0f, ModConfig.minimapZoom));
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();

        for (com.velora.client.waypoints.Waypoint wp : waypoints) {
            double dx = wp.x - playerX;
            double dz = wp.z - playerZ;

            double dist = Math.sqrt(dx * dx + dz * dz);
            double clampedDist = Math.min(dist, maxRadius / zoom);

            double angle = Math.atan2(dz, dx);
            int blipX = (int) (cx + Math.cos(angle) * clampedDist * zoom);
            int blipY = (int) (cy + Math.sin(angle) * clampedDist * zoom);

            int color = wp.color | 0xFF000000;
            String firstLetter = (wp.name != null && !wp.name.trim().isEmpty())
                ? String.valueOf(Character.toUpperCase(wp.name.trim().charAt(0)))
                : "W";

            // Clean circular/square mini badge with waypoint color border
            context.fill(blipX - 4, blipY - 4, blipX + 5, blipY + 5, 0xEE0F0F12);
            context.drawBorder(blipX - 4, blipY - 4, 9, 9, color);

            // Centered first letter
            int letterW = client.textRenderer.getWidth(firstLetter);
            context.drawText(client.textRenderer, firstLetter, blipX - letterW / 2 + 1, blipY - 3, color, true);
        }
    }

    private static void renderPlayerHeadIcon(DrawContext context, MinecraftClient client, int x, int y, PlayerEntity player, double heightDiff) {
        int s = 5;
        context.fill(x - s, y - s, x + s, y + s, 0xFF0F0F12);
        int accent = 0xFFA78BFA;
        context.fill(x - s, y - s, x + s, y - s + 1, accent);
        context.fill(x - s, y + s - 1, x + s, y + s, accent);
        context.fill(x - s, y - s, x - s + 1, y + s, accent);
        context.fill(x + s - 1, y - s, x + s, y + s, accent);
        context.fill(x - 3, y - 3, x - 1, y - 1, 0xFFF4F4F5);
        context.fill(x + 1, y - 3, x + 3, y - 1, 0xFFF4F4F5);
        context.fill(x - 3, y, x + 3, y + 2, 0xFFA1A1AA);
        context.fill(x - 2, y + 3, x + 2, y + 4, 0xFFF4F4F5);
    }

    private static void renderMobDiamond(DrawContext context, int x, int y, int color, int size) {
        context.fill(x, y - size, x + 1, y - size + 1, 0xFF0F0F12);
        context.fill(x - 1, y - size + 1, x + 2, y - size + 2, 0xFF0F0F12);
        context.fill(x - 2, y - size + 2, x + 3, y - size + 3, 0xFF0F0F12);
        context.fill(x - 3, y - size + 3, x + 4, y - size + 4, 0xFF0F0F12);
        context.fill(x - 2, y - size + 4, x + 3, y - size + 5, 0xFF0F0F12);
        context.fill(x - 1, y - size + 5, x + 2, y - size + 6, 0xFF0F0F12);
        context.fill(x, y - size + 6, x + 1, y - size + 7, 0xFF0F0F12);
        context.fill(x, y - size + 1, x + 1, y - size + 2, color);
        context.fill(x - 1, y - size + 2, x + 2, y - size + 3, color);
        context.fill(x - 2, y - size + 3, x + 3, y - size + 4, color);
        context.fill(x - 1, y - size + 4, x + 2, y - size + 5, color);
        context.fill(x, y - size + 5, x + 1, y - size + 6, color);
    }

    private static String getFormattedBiomeName(MinecraftClient client) {
        if (client.world == null || client.player == null) return "Unknown";
        BlockPos pos = client.player.getBlockPos();
        RegistryEntry<Biome> biomeEntry = client.world.getBiome(pos);
        String biomeKey = biomeEntry.getKey().map(key -> key.getValue().getPath()).orElse("biome");

        String[] words = biomeKey.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static String getDirectionHeading(float yaw) {
        float wrapped = MathHelper.wrapDegrees(yaw);
        if (wrapped >= -22.5f && wrapped < 22.5f) return "S";
        if (wrapped >= 22.5f && wrapped < 67.5f) return "SW";
        if (wrapped >= 67.5f && wrapped < 112.5f) return "W";
        if (wrapped >= 112.5f && wrapped < 157.5f) return "NW";
        if (wrapped >= 157.5f || wrapped < -157.5f) return "N";
        if (wrapped >= -157.5f && wrapped < -112.5f) return "NE";
        if (wrapped >= -112.5f && wrapped < -67.5f) return "E";
        return "SE";
    }

    public static int getMinimapWidth() {
        return MAP_SIZE + 10;
    }

    public static int getMinimapHeight() {
        return MAP_SIZE + 24;
    }
}
