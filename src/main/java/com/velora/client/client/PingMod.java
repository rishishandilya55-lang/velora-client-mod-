package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingMod {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Ping HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showPing) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;
            if (client.getNetworkHandler() == null) return;

            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry == null) return;

            int ping = entry.getLatency();
            String pingText = ping + " ms";

            int pingColor;
            if (ModConfig.pingCustomColor || ModConfig.pingTextRainbow) {
                pingColor = HudColorHelper.getEffectiveColor(ModConfig.pingTextColor, ModConfig.pingTextRainbow);
            } else {
                if (ping < 80)        pingColor = 0xFF55FF55;  // Green
                else if (ping < 150)  pingColor = 0xFFFFFF55;  // Yellow
                else if (ping < 250)  pingColor = 0xFFFF9922;  // Orange
                else                  pingColor = 0xFFFF5555;  // Red
            }

            TextRenderer textRenderer = client.textRenderer;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.pingScale, ModConfig.pingScale, 1.0f);

            int x = (int) (ModConfig.pingX / ModConfig.pingScale);
            int y = (int) (ModConfig.pingY / ModConfig.pingScale);

            int textWidth = textRenderer.getWidth(pingText);
            if (ModConfig.pingBackground && ModConfig.hudShowBackground) {
                int bg = (ModConfig.hudBackgroundOpacity << 24) | 0x000000;
                drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, bg);
            }

            drawContext.drawText(textRenderer, pingText, x, y, pingColor, ModConfig.hudTextShadow);

            drawContext.getMatrices().pop();
        });
    }
}
