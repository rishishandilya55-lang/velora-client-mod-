package com.velora.client.mixin;

import com.velora.client.client.WaypointsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into the very end of WorldRenderer.render() to draw waypoint labels.
 *
 * WHY NO METHOD PARAMETERS?
 * WorldRenderer.render() has a different signature in different MC builds.
 * Declaring the target method's parameters in the @Inject method must match
 * exactly or Mixin throws InvalidInjectionException at runtime.
 * By declaring ONLY CallbackInfo, Mixin doesn't validate the target params at
 * all and the injection works across any signature variant.
 * We obtain the Camera via MinecraftClient.getInstance() instead.
 */
@Mixin(WorldRenderer.class)
public class WaypointRenderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void velora$renderWaypoints(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.gameRenderer == null) return;
        Camera camera = mc.gameRenderer.getCamera();
        WaypointsMod.renderWaypoints(camera);
    }
}
