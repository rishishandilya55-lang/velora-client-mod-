package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Shadow
    @Final
    private Random random;

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void velora_renderItemPhysics(ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!ModConfig.showItemPhysics || state.itemRenderState.isEmpty()) {
            return;
        }

        ci.cancel();

        matrices.push();

        boolean hasDepth = state.itemRenderState.hasDepth();
        float scaleY = state.itemRenderState.getTransformation().scale.y();

        // Realistic ground placement
        if (hasDepth) {
            // 3D Block items sit flat on the floor
            matrices.translate(0.0f, 0.05f * scaleY + 0.05f, 0.0f);
            float rotation = (float) ((state.seed & 0xFFFF) % 360);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        } else {
            // 2D Flat items lay flat on the ground
            matrices.translate(0.0f, 0.02f, 0.0f);
            float rotation = (float) ((state.seed & 0xFFFF) % 360);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        }

        // Render item stack
        ItemEntityRenderer.renderStack(matrices, vertexConsumers, light, state, this.random);

        matrices.pop();
    }
}
