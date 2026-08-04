package com.velora.client.mixin;

import com.velora.client.client.ZoomClient;
import com.velora.client.config.ModConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    /**
     * Handles NoHurtCam: Disables or scales camera tilt/shake on player damage without touching player hurtTime.
     */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModConfig.showNoHurtCam) {
            if (ModConfig.hurtCamIntensity <= 0.0f) {
                LOGGER.debug("[Velora] Hurt cam cancelled (intensity={})", ModConfig.hurtCamIntensity);
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
                LOGGER.debug("[Velora] Zoom FOV applied: baseFov={}, zoomedFov={}", baseFov, zoomedFov);
                ci.setReturnValue(zoomedFov);
            }
        }
    }
}

