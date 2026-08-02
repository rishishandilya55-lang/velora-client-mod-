package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomClient implements ClientModInitializer {
    public static KeyBinding zoomKey;
    public static boolean isZooming = false;
    private static float currentZoomFactor = 1.0f; // 1.0 = normal, higher = zoomed in
    private static float targetZoomFactor = 1.0f;

    public static void init() {
        // Register Key 'C' for Zoom
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fpsdisplay.zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.fpsdisplay.general"
        ));
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    /**
     * Calculates the zoomed FOV frame-by-frame for GameRendererMixin.
     */
    public static float getCalculatedFov(float baseFov, float tickDelta) {
        if (!ModConfig.showZoom) return baseFov;

        MinecraftClient client = MinecraftClient.getInstance();
        boolean keyPressed = false;

        if (client != null && client.currentScreen == null) {
            if (zoomKey != null && zoomKey.isPressed()) {
                keyPressed = true;
            } else if (client.getWindow() != null && client.getWindow().getHandle() != 0) {
                keyPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_C);
            }
        }

        if (keyPressed) {
            if (!isZooming) {
                isZooming = true;
                ModConfig.currentZoomScroll = 0.0f;
            }
            float baseZoom = Math.max(1.5f, 70.0f / Math.max(5.0f, ModConfig.zoomAmount));
            targetZoomFactor = baseZoom + (ModConfig.currentZoomScroll * 0.5f);
            targetZoomFactor = Math.max(1.2f, Math.min(25.0f, targetZoomFactor));
        } else {
            if (isZooming) {
                isZooming = false;
                ModConfig.currentZoomScroll = 0.0f;
            }
            targetZoomFactor = 1.0f;
        }

        if (ModConfig.zoomSmooth) {
            currentZoomFactor += (targetZoomFactor - currentZoomFactor) * (0.25f * (tickDelta + 1.0f));
        } else {
            currentZoomFactor = targetZoomFactor;
        }

        if (Math.abs(currentZoomFactor - 1.0f) < 0.01f && !isZooming) {
            currentZoomFactor = 1.0f;
            return baseFov;
        }

        return Math.max(2.0f, baseFov / currentZoomFactor);
    }

    /**
     * Called by MouseMixin when scrolling mouse wheel while zooming.
     */
    public static void onMouseScroll(double vertical) {
        if (vertical > 0) {
            ModConfig.currentZoomScroll += 1.0f;
        } else if (vertical < 0) {
            ModConfig.currentZoomScroll = Math.max(-2.0f, ModConfig.currentZoomScroll - 1.0f);
        }
    }

    public static float getCurrentZoomFactor() {
        return currentZoomFactor;
    }
}
