package com.velora.client.mixin;

import com.velora.client.client.HitColorMod;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
        at = @At("HEAD")
    )
    private void onRenderHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState state, float f, float g, CallbackInfo ci) {
        HitColorMod.setCurrentEntityHurt(state.hurt || state.deathTime > 0);
    }

    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
        at = @At("RETURN")
    )
    private void onRenderReturn(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState state, float f, float g, CallbackInfo ci) {
        HitColorMod.setCurrentEntityHurt(false);
    }
}
