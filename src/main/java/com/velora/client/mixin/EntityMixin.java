package com.velora.client.mixin;

import com.velora.client.client.FreeLookClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts look direction changes on the player entity during FreeLook.
 *
 * When FreeLook is active:
 * - Redirects mouse delta to update FreeLookClient camera rotation angles (cameraYaw / cameraPitch).
 * - Cancels the call to changeLookDirection so the player entity's yaw and pitch DO NOT turn.
 * - This allows the player to move in the direction the character is facing while freely orbiting the camera!
 */
@Mixin(Entity.class)
public class EntityMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onEntityChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (FreeLookClient.active && mc != null && mc.player != null && (Object) this == mc.player) {
            LOGGER.debug("[Velora] FreeLook intercepting look direction: dx={}, dy={}", cursorDeltaX, cursorDeltaY);
            FreeLookClient.updateRotation(cursorDeltaX, cursorDeltaY);
            ci.cancel(); // Lock entity body yaw/pitch so character moves in facing direction
        }
    }
}
