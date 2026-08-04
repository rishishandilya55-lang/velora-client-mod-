package com.velora.client.mixin;

import com.velora.client.client.CpsClient;
import com.velora.client.client.ZoomClient;
import com.velora.client.config.ModConfig;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    /**
     * Captures native GLFW mouse click events for 100% accurate CPS registration without missing fast clicks.
     */
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                CpsClient.registerLeftClick();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                CpsClient.registerRightClick();
            }
        }
    }

    /**
     * Intercepts Mouse Scroll Wheel events during Zooming to adjust zoom level dynamically.
     */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ModConfig.showZoom && ZoomClient.isZooming) {
            if (vertical != 0.0) {
                ZoomClient.onMouseScroll(vertical);
                ci.cancel(); // Prevent hotbar slot scrolling while zooming!
            }
        }
    }
}
