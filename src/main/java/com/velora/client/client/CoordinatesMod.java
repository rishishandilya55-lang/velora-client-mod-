package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinatesMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    private static int tickCounter = 0;

    public static void init() {
        LOGGER.info("[Velora] Coordinates HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showCoordinates) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            int px = (int) Math.floor(client.player.getX());
            int py = (int) Math.floor(client.player.getY());
            int pz = (int) Math.floor(client.player.getZ());
            String coordsText = "XYZ: " + px + ", " + py + ", " + pz;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.coordsScale, ModConfig.coordsScale, 1.0f);

            int x = (int) (ModConfig.coordsX / ModConfig.coordsScale);
            int y = (int) (ModConfig.coordsY / ModConfig.coordsScale);

            int textWidth = textRenderer.getWidth(coordsText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, coordsText, x, y, 0xFFFFFFFF, true);

            drawContext.getMatrices().pop();
            tickCounter++;
            if (tickCounter % 60 == 0) {
                LOGGER.trace("[Velora] Coords HUD: {},{},{}", px, py, pz);
            }
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
