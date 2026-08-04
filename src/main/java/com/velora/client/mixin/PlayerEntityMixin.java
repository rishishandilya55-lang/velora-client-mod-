package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
    private void forceCapePartVisible(PlayerModelPart modelPart, CallbackInfoReturnable<Boolean> cir) {
        if (modelPart == PlayerModelPart.CAPE && ModConfig.enableCape && ModConfig.overrideDefaultCape) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            MinecraftClient mc = MinecraftClient.getInstance();
            boolean isLocalPlayer = (mc != null && mc.player != null && player == mc.player);
            if (!ModConfig.capeOnlyLocal || isLocalPlayer) {
                LOGGER.debug("[Velora] Cape visibility override: forcing cape visible for {}", player.getName().getString());
                cir.setReturnValue(true);
            }
        }
    }
}
