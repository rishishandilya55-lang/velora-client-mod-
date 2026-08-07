package com.velora.client.mixin;

import com.velora.client.client.FreeLookClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onEntityChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (FreeLookClient.active && mc != null && mc.player != null && (Object) this == mc.player) {
            FreeLookClient.updateRotation(cursorDeltaX, cursorDeltaY);
            ci.cancel();
        }
    }
}
