package com.velora.client.mixin;

import com.velora.client.client.HitColorMod;
import com.velora.client.config.ModConfig;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

    @Redirect(
        method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")
    )
    private void redirectModelRender(Model model, MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        int finalOverlay = HitColorMod.getArmorOverlay(overlay);
        int finalColor = color;
        if (ModConfig.showHitColor && ModConfig.hitColorShowOnArmor && HitColorMod.isCurrentEntityHurt()) {
            finalColor = HitColorMod.getCurrentColorArgb();
        }
        model.render(matrices, vertexConsumer, light, finalOverlay, finalColor);
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V")
    )
    private void redirectTrimModelRender(Model model, MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay) {
        int finalOverlay = HitColorMod.getArmorOverlay(overlay);
        model.render(matrices, vertexConsumer, light, finalOverlay);
    }
}
