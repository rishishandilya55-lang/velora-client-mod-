package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fullbright implementation for Minecraft 1.21.4.
 * Overrides lightmap brightness calculations to return maximum brightness (1.0f).
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (ModConfig.showFullbright) {
            cir.setReturnValue(1.0f);
        }
    }
}

