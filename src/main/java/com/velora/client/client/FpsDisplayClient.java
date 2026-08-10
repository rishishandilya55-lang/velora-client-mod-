package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FpsDisplayClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Velora] Initializing Velora Client...");

        // Load saved JSON configuration from disk
        ModConfig.loadConfig();
        LOGGER.info("[Velora] Config loaded");

        // Initialize all Velora Client sub-modules
        LOGGER.debug("[Velora] Initializing ModKeybindings");
        ModKeybindings.init();
        LOGGER.debug("[Velora] Initializing KeystrokesClient");
        KeystrokesClient.init();
        LOGGER.debug("[Velora] Initializing CpsClient");
        CpsClient.init();
        LOGGER.debug("[Velora] Initializing ZoomClient");
        ZoomClient.init();
        LOGGER.debug("[Velora] Initializing ToggleSprintClient");
        ToggleSprintClient.init();
        LOGGER.debug("[Velora] Initializing ToggleSneakMod");
        ToggleSneakMod.init();
        LOGGER.debug("[Velora] Initializing FullbrightClient");
        FullbrightClient.init();
        LOGGER.debug("[Velora] Initializing ArmorMod");
        ArmorMod.init();
        LOGGER.debug("[Velora] Initializing NoHurtCamMod");
        NoHurtCamMod.init();
        LOGGER.debug("[Velora] Initializing CoordinatesMod");
        CoordinatesMod.init();
        LOGGER.debug("[Velora] Initializing DayCounterMod");
        DayCounterMod.init();
        LOGGER.debug("[Velora] Initializing BlockInfoMod");
        BlockInfoMod.init();
        LOGGER.debug("[Velora] Initializing FreeLookClient");
        FreeLookClient.init();
        LOGGER.debug("[Velora] Initializing SnapLookClient");
        SnapLookClient.init();
        LOGGER.debug("[Velora] Initializing PingMod");
        PingMod.init();
        LOGGER.debug("[Velora] Initializing MinimapClient");
        MinimapClient.init();
        LOGGER.debug("[Velora] Initializing ChatColorsClient");
        ChatColorsClient.init();
        LOGGER.debug("[Velora] Initializing ItemTooltipsClient");
        ItemTooltipsClient.init();
        LOGGER.debug("[Velora] Initializing HitColorMod");
        HitColorMod.init();
        LOGGER.debug("[Velora] Initializing PotionHudMod");
        PotionHudMod.init();
        LOGGER.debug("[Velora] Initializing CustomCrosshairMod");
        CustomCrosshairMod.init();
        LOGGER.debug("[Velora] Initializing ViewModelMod");
        ViewModelMod.init();
        LOGGER.debug("[Velora] Initializing WaypointsMod");
        WaypointsMod.init();
        LOGGER.info("[Velora] All modules initialized");

        // Clean up MinimapClient native resources on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.debug("[Velora] Disconnect event fired, cleaning up");
            MinimapClient.close();
            FullbrightClient.onToggleOff();
        });
        LOGGER.debug("[Velora] Disconnect handler registered");

        // Register FPS Display HUD Render Callback
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showFps) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;
            int fps = client.getCurrentFps();
            String fpsText = ModConfig.fpsShowPrefix ? ("FPS: " + fps) : (fps + " FPS");

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.fpsScale, ModConfig.fpsScale, 1.0f);

            int x = (int) (ModConfig.fpsX / ModConfig.fpsScale);
            int y = (int) (ModConfig.fpsY / ModConfig.fpsScale);

            int textWidth = textRenderer.getWidth(fpsText);
            if (ModConfig.fpsBackground && ModConfig.hudShowBackground) {
                int bg = (ModConfig.hudBackgroundOpacity << 24) | 0x000000;
                drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, bg);
            }

            int color = com.velora.client.util.HudColorHelper.getEffectiveColor(ModConfig.fpsTextColor, ModConfig.fpsTextRainbow);
            drawContext.drawText(textRenderer, fpsText, x, y, color, ModConfig.hudTextShadow);

            drawContext.getMatrices().pop();
        });
    }
}
