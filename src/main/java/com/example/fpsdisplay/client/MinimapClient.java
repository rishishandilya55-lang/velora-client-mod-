package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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

public class MinimapClient implements ClientModInitializer {

    public static final int MAP_SIZE = 128;
    private static NativeImage nativeImage;
    private static NativeImageBackedTexture mapTexture;
    private static Identifier textureId;
    private static long lastUpdateMs = 0;
    private static int lastPlayerX = Integer.MIN_VALUE;
    private static int lastPlayerZ = Integer.MIN_VALUE;
    private static int lastPlayerY = Integer.MIN_VALUE;

    public static void init() {
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
            textureId = Identifier.of("fpsdisplay", "textures/gui/dynamic_minimap.png");
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

        long now = System.currentTimeMillis();
        // Update map texture every 100ms or when player moves position
        if (now - lastUpdateMs < 100 && playerX == lastPlayerX && playerZ == lastPlayerZ && Math.abs(playerY - lastPlayerY) < 3) {
            return;
        }

        lastUpdateMs = now;
        lastPlayerX = playerX;
        lastPlayerY = playerY;
        lastPlayerZ = playerZ;

        int halfSize = MAP_SIZE / 2;
        ClientWorld world = client.world;
        boolean hasCeiling = world.getDimension().hasCeiling();
        boolean isCircle = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape);

        for (int px = 0; px < MAP_SIZE; px++) {
            for (int pz = 0; pz < MAP_SIZE; pz++) {
                int dx = px - halfSize;
                int dz = pz - halfSize;

                // Mask circular bounds if circle mode selected
                if (isCircle && (dx * dx + dz * dz > (halfSize - 2) * (halfSize - 2))) {
                    nativeImage.setColorArgb(px, pz, 0x00000000);
                    continue;
                }

                int worldX = playerX + dx;
                int worldZ = playerZ + dz;

                int color = getPixelColor(world, worldX, playerY, worldZ, hasCeiling);
                nativeImage.setColorArgb(px, pz, color);
            }
        }

        mapTexture.upload();
    }

    private static int getPixelColor(ClientWorld world, int x, int playerY, int z, boolean hasCeiling) {
        int y;
        if (hasCeiling) {
            // Nether / Ceiling Dimension: Scan down from near player Y level to find walking floor
            int startY = Math.min(playerY + 8, 120);
            int minY = Math.max(world.getBottomY(), playerY - 20);
            y = startY;
            BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
            while (y > minY && world.getBlockState(mutable).isAir()) {
                y--;
                mutable.set(x, y, z);
            }
        } else {
            // Overworld / End: Surface top heightmap
            y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        }

        BlockPos pos = new BlockPos(x, Math.max(world.getBottomY(), y - 1), z);
        BlockState state = world.getBlockState(pos);

        if (state.isOf(Blocks.WATER) || state.isOf(Blocks.BUBBLE_COLUMN)) {
            // Water Depth Gradient Shading
            int depth = 1;
            while (depth < 8 && world.getBlockState(pos.down(depth)).isOf(Blocks.WATER)) {
                depth++;
            }
            int waterBlue = Math.max(120, 240 - depth * 15);
            int waterGreen = Math.max(80, 180 - depth * 12);
            return (0xFF << 24) | (15 << 16) | (waterGreen << 8) | waterBlue;
        }

        if (state.isOf(Blocks.LAVA)) {
            return 0xFFFF5500; // Glowing lava orange
        }

        MapColor mapColor = state.getMapColor(world, pos);
        int baseColor = (mapColor != null && mapColor.color != 0) ? mapColor.color : 0x557733;

        // Hillshading: Calculate slope height difference relative to north block
        int northY = hasCeiling ? y : world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z - 1);
        int heightDiff = y - northY;

        float shadowFactor = 1.0f;
        if (heightDiff > 0) {
            shadowFactor = 1.18f; // Ridgeline highlight
        } else if (heightDiff < 0) {
            shadowFactor = 0.82f; // Valley shadow
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

        // 1. Sleek Outer Dark Glass Background Container
        if (isCircle) {
            drawContext.fill(baseX - 4, baseY - 4, baseX + size + 4, baseY + size + 4, 0xAA080A12);
        } else {
            drawContext.fill(baseX - 3, baseY - 3, baseX + size + 3, baseY + size + 3, 0xEE0B0D17);
        }

        // 2. Render Dynamic Terrain Texture Map
        drawContext.drawTexture(
            textureId,
            baseX, baseY,
            0.0f, 0.0f,
            size, size,
            size, size
        );

        // 3. Glowing Purple Bezel Border
        int borderAlpha = 0xFF;
        int borderColor = (borderAlpha << 24) | 0xA855F7;
        if (!isCircle) {
            drawContext.drawBorder(baseX - 2, baseY - 2, size + 4, size + 4, borderColor);
            drawContext.drawBorder(baseX - 3, baseY - 3, size + 6, size + 6, 0x44A855F7);
        } else {
            drawContext.drawBorder(baseX - 1, baseY - 1, size + 2, size + 2, borderColor);
        }

        // 4. Compass Cardinal Directions (N, E, S, W) on Bezel
        float playerYaw = MathHelper.wrapDegrees(client.player.getYaw());
        float compassAngleOffset = ModConfig.minimapRotateMap ? playerYaw : 0.0f;
        drawCompassLabels(drawContext, client.textRenderer, centerX, centerY, halfSize + 4, compassAngleOffset);

        // 5. Entity Radar Overlays with Height Markers
        if (ModConfig.minimapShowEntities && client.world != null) {
            renderEntityBlips(drawContext, client, centerX, centerY, halfSize - 6);
        }

        // 6. Center Player Rotating Directional Arrow Icon (▲)
        renderPlayerDirectionalArrow(drawContext, centerX, centerY, playerYaw);

        // 7. Info Header (Biome & Heading) & Footer (Coordinates)
        int infoY = baseY + size + 6;
        if (ModConfig.minimapShowBiome && client.world != null) {
            String biomeName = getFormattedBiomeName(client);
            String heading = getDirectionHeading(playerYaw);
            String headerText = heading + " • " + biomeName;
            drawContext.drawCenteredTextWithShadow(client.textRenderer, headerText, centerX, baseY - 12, 0xFFE9D5FF);
        }

        if (ModConfig.minimapShowCoordinates) {
            int px = MathHelper.floor(client.player.getX());
            int py = MathHelper.floor(client.player.getY());
            int pz = MathHelper.floor(client.player.getZ());
            String coordsText = String.format("X: %d  Y: %d  Z: %d", px, py, pz);
            drawContext.drawCenteredTextWithShadow(client.textRenderer, coordsText, centerX, infoY, 0xFF38BDF8);
        }

        drawContext.getMatrices().pop();
    }

    private static void renderPlayerDirectionalArrow(DrawContext context, int cx, int cy, float yaw) {
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        // Rotate arrow matching player facing yaw
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw + 180.0f));

        // Sharp Cyan Directional Triangle Arrow Pointer
        context.fill(-3, 4, 3, 5, 0xFF0284C7);
        context.fill(-2, 1, 2, 4, 0xFF38BDF8);
        context.fill(-1, -2, 1, 1, 0xFF7DD3FC);
        context.fill(0, -4, 1, -2, 0xFFFFFFFF);

        context.getMatrices().pop();
    }

    private static void drawCompassLabels(DrawContext context, TextRenderer font, int cx, int cy, int radius, float angleOffset) {
        double radN = Math.toRadians(-angleOffset - 180);
        double radE = Math.toRadians(-angleOffset - 90);
        double radS = Math.toRadians(-angleOffset);
        double radW = Math.toRadians(-angleOffset + 90);

        int nx = (int) (cx + Math.sin(radN) * radius);
        int ny = (int) (cy - Math.cos(radN) * radius);
        context.drawText(font, "N", nx - 3, ny - 3, 0xFFEF4444, true);

        int ex = (int) (cx + Math.sin(radE) * radius);
        int ey = (int) (cy - Math.cos(radE) * radius);
        context.drawText(font, "E", ex - 3, ey - 3, 0xFFFFFFFF, true);

        int sx = (int) (cx + Math.sin(radS) * radius);
        int sy = (int) (cy - Math.cos(radS) * radius);
        context.drawText(font, "S", sx - 3, sy - 3, 0xFFFFFFFF, true);

        int wx = (int) (cx + Math.sin(radW) * radius);
        int wy = (int) (cy - Math.cos(radW) * radius);
        context.drawText(font, "W", wx - 3, wy - 3, 0xFFFFFFFF, true);
    }

    private static void renderEntityBlips(DrawContext context, MinecraftClient client, int cx, int cy, int maxRadius) {
        if (client.world == null || client.player == null) return;

        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;

            double dx = entity.getX() - playerX;
            double dz = entity.getZ() - playerZ;
            double dy = entity.getY() - playerY;

            if (Math.abs(dx) > maxRadius || Math.abs(dz) > maxRadius) continue;

            int blipX = (int) (cx + dx);
            int blipY = (int) (cy + dz);

            int color = 0xFF22C55E; // Green for passive mobs
            if (entity instanceof HostileEntity) {
                color = 0xFFEF4444; // Red for hostile mobs
            } else if (entity instanceof PlayerEntity) {
                color = 0xFF38BDF8; // Cyan for players
            } else if (entity instanceof ItemEntity) {
                color = 0xFFEAB308; // Gold for dropped items
            }

            // Draw blip dot with crisp dark border
            context.fill(blipX - 2, blipY - 2, blipX + 3, blipY + 3, 0xFF080A12);
            context.fill(blipX - 1, blipY - 1, blipX + 2, blipY + 2, color);

            // Relative Height Indicator (+) for above, (-) for below
            if (dy > 4.0) {
                context.drawText(client.textRenderer, "+", blipX + 2, blipY - 4, 0xFFFFFFFF, false);
            } else if (dy < -4.0) {
                context.drawText(client.textRenderer, "-", blipX + 2, blipY - 4, 0xFFCBD5E1, false);
            }
        }
    }

    private static String getFormattedBiomeName(MinecraftClient client) {
        if (client.world == null || client.player == null) return "Unknown";
        BlockPos pos = client.player.getBlockPos();
        RegistryEntry<Biome> biomeEntry = client.world.getBiome(pos);
        String biomeKey = biomeEntry.getKey().map(key -> key.getValue().getPath()).orElse("biome");
        
        // Format biome string e.g. "cherry_grove" -> "Cherry Grove"
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
        return MAP_SIZE + 8;
    }

    public static int getMinimapHeight() {
        return MAP_SIZE + 24;
    }
}
