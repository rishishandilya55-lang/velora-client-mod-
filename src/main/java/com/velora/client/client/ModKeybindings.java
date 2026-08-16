package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.ModMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModKeybindings implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static KeyBinding openMenuKey;
    public static KeyBinding freeLookKey;
    public static KeyBinding snapLookKey;
    public static KeyBinding fullbrightKey;
    public static KeyBinding waypointKey;
    public static KeyBinding nametagKey;

    public static void init() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.velora.general"
        ));

        freeLookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.freelook",
            InputUtil.Type.KEYSYM,
            ModConfig.freeLookKey,
            "category.velora.general"
        ));

        snapLookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.snaplook",
            InputUtil.Type.KEYSYM,
            ModConfig.snapLookKey,
            "category.velora.general"
        ));

        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.fullbright",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "category.velora.general"
        ));

        waypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.waypoints",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.velora.general"
        ));

        nametagKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.velora.nametag",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.velora.general"
        ));

        LOGGER.info("[Velora] Keybindings registered: menu=RIGHT_SHIFT, freelook={}, snaplook={}, fullbright=F6, waypoints=U, nametag=P",
                ModConfig.freeLookKey, ModConfig.snapLookKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                LOGGER.debug("[Velora] Open menu key pressed");
                client.setScreen(new ModMenuScreen());
            }

            while (waypointKey.wasPressed()) {
                if (client.world != null) {
                    client.setScreen(new com.velora.client.gui.WaypointManagerScreen(null));
                }
            }

            while (nametagKey.wasPressed()) {
                ModConfig.showNametag = !ModConfig.showNametag;
                ModConfig.saveConfig();
                LOGGER.info("[Velora] Nametag toggled to {}", ModConfig.showNametag);
            }
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
