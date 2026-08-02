package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
    private void forceCapePartVisible(PlayerModelPart modelPart, CallbackInfoReturnable<Boolean> cir) {
        if (modelPart == PlayerModelPart.CAPE && ModConfig.enableCape) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            boolean isLocalPlayer = (player == MinecraftClient.getInstance().player);
            if (!ModConfig.capeOnlyLocal || isLocalPlayer) {
                cir.setReturnValue(true);
            }
        }
    }
}
