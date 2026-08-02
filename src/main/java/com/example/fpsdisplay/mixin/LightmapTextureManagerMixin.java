package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright implementation for Minecraft 1.21.4.
 * Overrides lightmap texture updates to force maximum brightness (100% white) across all sky and block light levels.
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Shadow @Final private NativeImage image;
    @Shadow @Final private NativeImageBackedTexture texture;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(float delta, CallbackInfo ci) {
        if (ModConfig.showFullbright) {
            for (int b = 0; b < 16; b++) {
                for (int s = 0; s < 16; s++) {
                    this.image.setColorArgb(s, b, 0xFFFFFFFF);
                }
            }
            this.texture.upload();
            ci.cancel();
        }
    }
}


