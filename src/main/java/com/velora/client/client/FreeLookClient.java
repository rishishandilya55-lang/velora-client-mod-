package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreeLookClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static boolean active = false;
    public static float cameraYaw = 0.0f;
    public static float cameraPitch = 0.0f;
    private static Perspective previousPerspective = Perspective.FIRST_PERSON;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !ModConfig.showFreeLook) {
                if (active) disable(client);
                return;
            }

            boolean isPressed = ModKeybindings.freeLookKey != null && ModKeybindings.freeLookKey.isPressed();
            if (isPressed) {
                if (!active && !SnapLookClient.active) {
                    active = true;
                    LOGGER.debug("[Velora] FreeLook activated");
                    previousPerspective = client.options.getPerspective();
                    cameraYaw = client.player.getYaw();
                    cameraPitch = client.player.getPitch();
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                }
            } else {
                if (active) {
                    LOGGER.debug("[Velora] FreeLook deactivated");
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

    public static void updateRotation(double cursorDeltaX, double cursorDeltaY) {
        if (!active) return;
        cameraYaw += (float) (cursorDeltaX * 0.15f);
        cameraPitch += (float) (cursorDeltaY * 0.15f);
        cameraPitch = Math.max(-90.0f, Math.min(90.0f, cameraPitch));
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
