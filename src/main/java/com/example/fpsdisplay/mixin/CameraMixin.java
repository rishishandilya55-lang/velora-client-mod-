package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.client.FreeLookClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * FreeLook camera override for Minecraft 1.21.4.
 *
 * Modifies parameters in setRotation(yaw, pitch) when FreeLook is active.
 * Overrides camera yaw and pitch with FreeLookClient's decoupled camera rotation,
 * allowing Camera to calculate exact rotation quaternions and plane vectors for free 3D camera movement around the player.
 */
@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), ordinal = 0)
    private float modifyYaw(float yaw) {
        return FreeLookClient.active ? FreeLookClient.cameraYaw : yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), ordinal = 1)
    private float modifyPitch(float pitch) {
        return FreeLookClient.active ? FreeLookClient.cameraPitch : pitch;
    }
}

