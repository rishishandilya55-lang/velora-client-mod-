package com.velora.client.mixin;

import com.velora.client.client.ZoomClient;
import com.velora.client.config.ModConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Handles NoHurtCam: Disables or scales camera tilt/shake on player damage without touching player hurtTime.
     */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModConfig.showNoHurtCam) {
            if (ModConfig.hurtCamIntensity <= 0.0f) {
                // Completely disable hurt camera wobble
                ci.cancel();
            }
        }
    }

    /**
     * Handles Zoom Mod: Smoothly applies zoomed FOV without modifying client options.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> ci) {
        if (ModConfig.showZoom) {
            float baseFov = ci.getReturnValue();
            float zoomedFov = ZoomClient.getCalculatedFov(baseFov, tickDelta);
            if (zoomedFov != baseFov) {
                ci.setReturnValue(zoomedFov);
            }
        }
    }

    /**
     * Handles Fullbright: Returns 1.0f night vision strength when Fullbright is enabled.
     */
    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void onGetNightVisionStrength(net.minecraft.entity.LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.showFullbright) {
            cir.setReturnValue((float) ModConfig.fullbrightGamma);
        }
    }
}

