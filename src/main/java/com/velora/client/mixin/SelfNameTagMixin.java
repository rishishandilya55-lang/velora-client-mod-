package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class SelfNameTagMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    private static boolean selfLabelLogged = false;

    @Inject(
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velora_allowSelfLabel(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || entity != mc.player) return;
            if (!ModConfig.showNametag) return;
            if (!selfLabelLogged) {
                selfLabelLogged = true;
                LOGGER.info("[Velora] Self nametag allowed for {}", mc.getSession().getUsername());
            }
            cir.setReturnValue(true);
        } catch (Exception e) {
            LOGGER.error("[Velora] SelfNameTag mixin failed", e);
        }
    }

    @Redirect(
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"
            )
    )
    private static Entity velora_getCameraEntity(MinecraftClient instance) {
        return null;
    }
}
