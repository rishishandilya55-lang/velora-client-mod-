package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import com.example.fpsdisplay.gui.ModMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    public static KeyBinding freeLookKey;
    public static KeyBinding snapLookKey;

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new ModMenuScreen());
            }
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
