package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.CosmeticsLockerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    private static final Identifier VELORA_CAPE = Identifier.of("velora", "textures/cape/velora_cape.png");
    private static final Identifier CLASSIC_CAPE = Identifier.of("velora", "textures/cape/classic_cape.png");
    private static final Identifier WAVE_CAPE = Identifier.of("velora", "textures/cape/wave_cape.png");
    private static final Identifier ROSE_CAPE = Identifier.of("velora", "textures/cape/rose.png");
    private static final Identifier PURPLE_ROSE_CAPE = Identifier.of("velora", "textures/cape/purple_rose.png");
    private static final Identifier WITHERED_ROSE_CAPE = Identifier.of("velora", "textures/cape/withered_rose.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injectVeloraCape(CallbackInfoReturnable<SkinTextures> cir) {
        boolean previewingCape = CosmeticsLockerScreen.isPreviewingCape();
        if ((ModConfig.enableCape && ModConfig.overrideDefaultCape) || previewingCape) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
            boolean isLocalPlayer = (player == MinecraftClient.getInstance().player);
            String playerName = player.getName().getString();

            if (previewingCape || !ModConfig.capeOnlyLocal || isLocalPlayer) {
                SkinTextures original = cir.getReturnValue();
                if (original != null) {
                    int capeChoice = CosmeticsLockerScreen.getPreviewingCapeIndex() >= 0
                            ? CosmeticsLockerScreen.getPreviewingCapeIndex()
                            : ModConfig.selectedCape;

                    Identifier capeTexture = switch (capeChoice) {
                        case 1 -> CLASSIC_CAPE;
                        case 2 -> WAVE_CAPE;
                        case 3 -> ROSE_CAPE;
                        case 4 -> PURPLE_ROSE_CAPE;
                        case 5 -> WITHERED_ROSE_CAPE;
                        default -> VELORA_CAPE;
                    };

                    LOGGER.debug("[Velora] Cape injection: player={}, choice={}, texture={}, preview={}",
                            playerName, capeChoice, capeTexture, previewingCape);

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
