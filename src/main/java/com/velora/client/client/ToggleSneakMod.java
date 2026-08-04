package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class ToggleSneakMod implements ClientModInitializer {
    public static boolean toggleSneakActive = false;
    private static boolean wasSneakKeyPressed = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            boolean isSneakKeyPressed = client.options.sneakKey.isPressed();
            if (isSneakKeyPressed && !wasSneakKeyPressed) {
                toggleSneakActive = !toggleSneakActive;
            }
            wasSneakKeyPressed = isSneakKeyPressed;

            if (ModConfig.showToggleSneak && toggleSneakActive) {
                if (client.currentScreen == null && !client.player.getAbilities().flying) {
                    client.options.sneakKey.setPressed(true);
                }
            }
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
