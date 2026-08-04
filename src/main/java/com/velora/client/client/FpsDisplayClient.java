package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class FpsDisplayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Load saved JSON configuration from disk
        ModConfig.loadConfig();

        // Initialize all Velora Client sub-modules
        ModKeybindings.init();
        KeystrokesClient.init();
        CpsClient.init();
        ZoomClient.init();
        ToggleSprintClient.init();
        ToggleSneakMod.init();
        FullbrightClient.init();
        ArmorMod.init();
        NoHurtCamMod.init();
        CoordinatesMod.init();
        DayCounterMod.init();
        BlockInfoMod.init();
        FreeLookClient.init();
        SnapLookClient.init();
        PingMod.init();
        MinimapClient.init();

        // Clean up MinimapClient native resources on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MinimapClient.close();
            FullbrightClient.onToggleOff();
        });

        // Fullbright gamma tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FullbrightClient.tick();
        });

        // Register FPS Display HUD Render Callback
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
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
