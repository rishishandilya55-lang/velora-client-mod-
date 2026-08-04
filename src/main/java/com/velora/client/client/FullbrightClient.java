package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullbrightClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        ModConfig.showFullbright = false;
        LOGGER.info("[Velora] Fullbright module initialized (forced OFF)");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ModKeybindings.fullbrightKey != null && ModKeybindings.fullbrightKey.wasPressed()) {
                ModConfig.showFullbright = !ModConfig.showFullbright;
                ModConfig.saveConfig();
                LOGGER.info("[Velora] Fullbright toggled: {}", ModConfig.showFullbright ? "ON" : "OFF");
            }
        });
    }

    public static void onToggleOff() {
        ModConfig.showFullbright = false;
        ModConfig.saveConfig();
        LOGGER.debug("[Velora] Fullbright reset to OFF on disconnect");
    }
}
