package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
        LOGGER.info("[Velora] All modules initialized");

        // Clean up MinimapClient native resources on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.debug("[Velora] Disconnect event fired, cleaning up");
            MinimapClient.close();
            FullbrightClient.onToggleOff();
        });
        LOGGER.debug("[Velora] Disconnect handler registered");

        // Fullbright gamma tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FullbrightClient.tick();
        });
        LOGGER.debug("[Velora] Fullbright tick registered");

        // Register FPS Display HUD Render Callback
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            LOGGER.trace("[Velora] HUD render tick");
            if (!ModConfig.showFps) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;
            int fps = client.getCurrentFps();
            String fpsText = "FPS: " + fps;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.fpsScale, ModConfig.fpsScale, 1.0f);

            int x = (int) (ModConfig.fpsX / ModConfig.fpsScale);
            int y = (int) (ModConfig.fpsY / ModConfig.fpsScale);

            int textWidth = textRenderer.getWidth(fpsText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, fpsText, x, y, 0xFFFFFFFF, true);

            drawContext.getMatrices().pop();
        });
    }
}
