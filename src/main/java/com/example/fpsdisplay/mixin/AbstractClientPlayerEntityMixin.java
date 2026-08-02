package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import com.example.fpsdisplay.gui.CosmeticsLockerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    private static final Identifier VELORA_CAPE = Identifier.of("fpsdisplay", "textures/cape/velora_cape.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injectVeloraCape(CallbackInfoReturnable<SkinTextures> cir) {
        boolean previewingCape = CosmeticsLockerScreen.isPreviewingCape();
        if (ModConfig.enableCape || previewingCape) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
            boolean isLocalPlayer = (player == MinecraftClient.getInstance().player);

            if (previewingCape || !ModConfig.capeOnlyLocal || isLocalPlayer) {
                SkinTextures original = cir.getReturnValue();
                if (original != null) {
                    cir.setReturnValue(new SkinTextures(
                        original.texture(),
                        original.textureUrl(),
                        VELORA_CAPE,
                        original.elytraTexture(),
                        original.model(),
                        original.secure()
                    ));
                }
            }
        }
    }
}

