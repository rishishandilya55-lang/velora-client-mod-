package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

public class SnapLookClient implements ClientModInitializer {
    public static boolean active = false;
    private static Perspective previousPerspective = Perspective.FIRST_PERSON;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !ModConfig.showSnapLook) {
                if (active) disable(client);
                return;
            }

            boolean isPressed = ModKeybindings.snapLookKey != null && ModKeybindings.snapLookKey.isPressed();
            if (isPressed) {
                if (!active) {
                    active = true;
                    previousPerspective = client.options.getPerspective();
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                }
            } else {
                if (active) {
                    disable(client);
                }
            }
        });
    }

    private static void disable(MinecraftClient client) {
        active = false;
        if (client.options != null) {
            client.options.setPerspective(previousPerspective);
        }
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
