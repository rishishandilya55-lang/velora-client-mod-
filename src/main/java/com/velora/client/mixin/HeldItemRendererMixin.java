package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
        method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD")
    )
    private void velora_applyViewModelTransforms(
        AbstractClientPlayerEntity player,
        float tickDelta,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!ModConfig.showViewModel) return;

        boolean isMainHand = (hand == Hand.MAIN_HAND);

        // 1. Position Translation (X, Y, Z offsets)
        float posX = isMainHand ? ModConfig.viewModelMainHandX : ModConfig.viewModelOffHandX;
        float posY = isMainHand ? ModConfig.viewModelMainHandY : ModConfig.viewModelOffHandY;
        float posZ = isMainHand ? ModConfig.viewModelMainHandZ : ModConfig.viewModelOffHandZ;

        if (posX != 0.0f || posY != 0.0f || posZ != 0.0f) {
            matrices.translate(posX, posY, posZ);
        }

        // 2. Rotations (Pitch, Yaw, Roll)
        if (ModConfig.viewModelPitch != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ModConfig.viewModelPitch));
        }
        if (ModConfig.viewModelYaw != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ModConfig.viewModelYaw));
        }
        if (ModConfig.viewModelRoll != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ModConfig.viewModelRoll));
        }

        // 3. Hand Scale
        float handScale = isMainHand ? ModConfig.viewModelMainHandScale : ModConfig.viewModelOffHandScale;
        if (handScale != 1.0f && handScale > 0.0f) {
            matrices.scale(handScale, handScale, handScale);
        }
    }

    @Inject(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD")
    )
    private void velora_applyItemScaleDirectly(
        LivingEntity entity,
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!ModConfig.showViewModel || stack == null || stack.isEmpty()) return;

        float itemScale = 1.0f;
        if (renderMode.isFirstPerson()) {
            itemScale = ModConfig.getItemScale(stack);
        } else if (renderMode == ModelTransformationMode.GROUND) {
            itemScale = ModConfig.getItemGroundScale(stack);
        } else if (renderMode == ModelTransformationMode.GUI) {
            itemScale = ModConfig.getItemGuiScale(stack);
        }

        if (itemScale != 1.0f && itemScale > 0.0f) {
            matrices.scale(itemScale, itemScale, itemScale);
        }
    }
}
