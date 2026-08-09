package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullbrightClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Fullbright module initialized (status: {})", ModConfig.showFullbright ? "ON" : "OFF");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ModKeybindings.fullbrightKey != null && ModKeybindings.fullbrightKey.wasPressed()) {
                ModConfig.showFullbright = !ModConfig.showFullbright;
                ModConfig.saveConfig();

                if (client.player != null) {
                    Text msg = Text.literal("Fullbright: ")
                        .formatted(Formatting.GRAY)
                        .append(ModConfig.showFullbright
                            ? Text.literal("ON").formatted(Formatting.GREEN, Formatting.BOLD)
                            : Text.literal("OFF").formatted(Formatting.RED, Formatting.BOLD));
                    client.player.sendMessage(msg, true);
                }

                LOGGER.info("[Velora] Fullbright toggled: {}", ModConfig.showFullbright ? "ON" : "OFF");
            }
        });
    }

    public static void onToggleOff() {
        // No-op: preserve user fullbright preference across sessions
    }
}

