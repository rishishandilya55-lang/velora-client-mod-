package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;

/**
 * PingMod — renders the server ping (latency) on the HUD.
 *
 * Uses the player's own PlayerListEntry.getLatency() which is the same
 * value shown in the tab list — updated by the server every few seconds.
 */
public class PingMod {

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showPing) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;
            if (client.getNetworkHandler() == null) return;

            // Get the player's own ping from the player list
            PlayerListEntry entry = client.getNetworkHandler()
                    .getPlayerListEntry(client.player.getUuid());
            if (entry == null) return;

            int ping = entry.getLatency();

            // Choose color based on ping quality
            int pingColor;
            if (ping < 80)        pingColor = 0xFF55FF55;  // Green  — great
            else if (ping < 150)  pingColor = 0xFFFFFF55;  // Yellow — okay
            else if (ping < 250)  pingColor = 0xFFFF9922;  // Orange — poor
            else                  pingColor = 0xFFFF5555;  // Red    — bad

            String pingText = ping + " ms";

            TextRenderer textRenderer = client.textRenderer;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.pingScale, ModConfig.pingScale, 1.0f);

            int x = (int) (ModConfig.pingX / ModConfig.pingScale);
            int y = (int) (ModConfig.pingY / ModConfig.pingScale);

            // Semi-transparent background pill
            int textWidth = textRenderer.getWidth(pingText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, pingText, x, y, pingColor, true);

            drawContext.getMatrices().pop();
        });
    }
}
