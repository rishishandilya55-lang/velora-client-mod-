package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinatesMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Coordinates HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showCoordinates) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            int px = (int) Math.floor(client.player.getX());
            int py = (int) Math.floor(client.player.getY());
            int pz = (int) Math.floor(client.player.getZ());

            Direction facing = client.player.getHorizontalFacing();
            String dirLetter = switch (facing) {
                case NORTH -> "N";
                case SOUTH -> "S";
                case WEST  -> "W";
                case EAST  -> "E";
                default    -> "";
            };
            String axisSign = switch (facing) {
                case NORTH -> "-";
                case SOUTH -> "+";
                case WEST  -> "-";
                case EAST  -> "+";
                default    -> "";
            };

            boolean isNether = (client.world.getRegistryKey() == World.NETHER)
                || client.world.getRegistryKey().getValue().getPath().contains("nether");
            boolean isOverworld = (client.world.getRegistryKey() == World.OVERWORLD)
                || client.world.getRegistryKey().getValue().getPath().contains("overworld");

            String lineX = "X: " + px;
            String lineY = "Y: " + py;
            String lineZ = "Z: " + pz;

            String netherLine = null;
            if (ModConfig.coordsShowNether) {
                if (isNether) {
                    int owX = (int) Math.floor(client.player.getX() * 8.0);
                    int owZ = (int) Math.floor(client.player.getZ() * 8.0);
                    netherLine = "Overworld: " + owX + ", " + owZ;
                } else if (isOverworld) {
                    int nX = (int) Math.floor(client.player.getX() / 8.0);
                    int nZ = (int) Math.floor(client.player.getZ() / 8.0);
                    netherLine = "Nether: " + nX + ", " + nZ;
                }
            }

            String biomeName = null;
            if (ModConfig.coordsShowBiome) {
                RegistryEntry<Biome> biomeEntry = client.world.getBiome(client.player.getBlockPos());
                if (biomeEntry != null && biomeEntry.getKey().isPresent()) {
                    String raw = biomeEntry.getKey().get().getValue().getPath().replace('_', ' ');
                    biomeName = capitalizeWords(raw);
                }
            }

            // Calculate card dimensions
            int rightColWidth = ModConfig.coordsShowDirection ? Math.max(textRenderer.getWidth(dirLetter), textRenderer.getWidth(axisSign)) + 10 : 0;
            int maxLeftWidth = Math.max(textRenderer.getWidth(lineX), Math.max(textRenderer.getWidth(lineY), textRenderer.getWidth(lineZ)));
            if (netherLine != null) {
                maxLeftWidth = Math.max(maxLeftWidth, textRenderer.getWidth(netherLine));
            }
            if (biomeName != null) {
                maxLeftWidth = Math.max(maxLeftWidth, textRenderer.getWidth("Biome: " + biomeName));
            }

            int cardWidth = maxLeftWidth + rightColWidth + 10;
            int lineHeight = 10;
            int totalLines = 3 + (netherLine != null ? 1 : 0) + (biomeName != null ? 1 : 0);
            int cardHeight = totalLines * lineHeight + 4;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.coordsScale, ModConfig.coordsScale, 1.0f);

            int x = (int) (ModConfig.coordsX / ModConfig.coordsScale);
            int y = (int) (ModConfig.coordsY / ModConfig.coordsScale);

            if (ModConfig.coordsBackground && ModConfig.hudShowBackground) {
                int bgAlpha = Math.max(0, Math.min(255, ModConfig.hudBackgroundOpacity));
                int bg = (bgAlpha << 24) | 0x000000;
                drawContext.fill(x - 3, y - 3, x + cardWidth + 3, y + cardHeight + 1, bg);
            }

            int textColor = HudColorHelper.getEffectiveColor(ModConfig.coordsTextColor, ModConfig.coordsTextRainbow);
            int textX = x;
            int curY = y;

            // 1. Line X
            drawContext.drawText(textRenderer, lineX, textX, curY, textColor, ModConfig.hudTextShadow);
            curY += lineHeight;

            // 2. Line Y (+ Direction letter on the right, e.g. S)
            drawContext.drawText(textRenderer, lineY, textX, curY, textColor, ModConfig.hudTextShadow);
            if (ModConfig.coordsShowDirection && !dirLetter.isEmpty()) {
                int dirX = x + cardWidth - textRenderer.getWidth(dirLetter);
                drawContext.drawText(textRenderer, dirLetter, dirX, curY, textColor, ModConfig.hudTextShadow);
            }
            curY += lineHeight;

            // 3. Line Z (+ Axis sign on the right, e.g. +)
            drawContext.drawText(textRenderer, lineZ, textX, curY, textColor, ModConfig.hudTextShadow);
            if (ModConfig.coordsShowDirection && !axisSign.isEmpty()) {
                int signX = x + cardWidth - textRenderer.getWidth(axisSign);
                drawContext.drawText(textRenderer, axisSign, signX, curY, textColor, ModConfig.hudTextShadow);
            }
            curY += lineHeight;

            // 4. Nether / Overworld conversion
            if (netherLine != null) {
                drawContext.drawText(textRenderer, netherLine, textX, curY, textColor, ModConfig.hudTextShadow);
                curY += lineHeight;
            }

            // 5. Biome line with green biome name
            if (biomeName != null) {
                int labelWidth = textRenderer.getWidth("Biome: ");
                drawContext.drawText(textRenderer, "Biome: ", textX, curY, textColor, ModConfig.hudTextShadow);
                drawContext.drawText(textRenderer, biomeName, textX + labelWidth, curY, 0xFF55FF55, ModConfig.hudTextShadow);
            }

            drawContext.getMatrices().pop();
        });
    }

    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return "";
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                result.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}

