package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.CosmeticsLockerScreen;
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

    private static final Identifier VELORA_CAPE = Identifier.of("velora", "textures/cape/velora_cape.png");
    private static final Identifier ROSE_CAPE = Identifier.of("velora", "textures/cape/rose.png");
    private static final Identifier PURPLE_ROSE_CAPE = Identifier.of("velora", "textures/cape/purple_rose.png");
    private static final Identifier WITHERED_ROSE_CAPE = Identifier.of("velora", "textures/cape/withered_rose.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injectVeloraCape(CallbackInfoReturnable<SkinTextures> cir) {
        boolean previewingCape = CosmeticsLockerScreen.isPreviewingCape();
        int capeChoice = previewingCape ? CosmeticsLockerScreen.getPreviewingCapeIndex() : (ModConfig.enableCape ? ModConfig.selectedCape : -1);

        // If cape is disabled or capeChoice is -1 (Mojang default), do not override
        if (capeChoice < 0 || (!ModConfig.enableCape && !previewingCape) || (!ModConfig.overrideDefaultCape && !previewingCape)) {
            return;
        }

        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        boolean isLocalPlayer = (player == MinecraftClient.getInstance().player);

        if (previewingCape || !ModConfig.capeOnlyLocal || isLocalPlayer) {
            SkinTextures original = cir.getReturnValue();
            if (original != null) {
                Identifier capeTexture = switch (capeChoice) {
                    case 0 -> VELORA_CAPE;
                    case 1 -> ROSE_CAPE;
                    case 2 -> PURPLE_ROSE_CAPE;
                    case 3 -> WITHERED_ROSE_CAPE;
                    default -> null;
                };

                if (capeTexture != null) {
                    cir.setReturnValue(new SkinTextures(
                        original.texture(),
                        original.textureUrl(),
                        capeTexture,
                        original.elytraTexture(),
                        original.model(),
                        original.secure()
                    ));
                }
            }
        }
    }
}
