package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.CosmeticsLockerScreen;
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
        boolean previewingCape = CosmeticsLockerScreen.isPreviewingCape();
        int capeChoice = previewingCape ? CosmeticsLockerScreen.getPreviewingCapeIndex() : (ModConfig.enableCape ? ModConfig.selectedCape : -1);

        if (modelPart == PlayerModelPart.CAPE && ((ModConfig.enableCape && ModConfig.overrideDefaultCape && capeChoice >= 0) || (previewingCape && capeChoice >= 0))) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            MinecraftClient mc = MinecraftClient.getInstance();
            boolean isLocalPlayer = (mc != null && mc.player != null && player == mc.player);
            if (!ModConfig.capeOnlyLocal || isLocalPlayer || previewingCape) {
                cir.setReturnValue(true);
            }
        }
    }
}
