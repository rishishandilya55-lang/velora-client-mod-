package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright for Minecraft 1.21.4.
 *
 * Instead of @Shadow-ing the NativeImage field (which was restructured in 1.21.4),
 * we temporarily override the gamma option to 16.0 before LightmapTextureManager
 * runs its update — making the entire lightmap compute at maximum brightness.
 * When fullbright is turned off we restore the previous gamma value.
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    /** Stores the user's real gamma so we can restore it when fullbright is disabled. */
    private double velora$savedGamma = -1.0;

    @Inject(method = "update", at = @At("HEAD"))
    private void velora$onUpdateHead(float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        if (ModConfig.showFullbright) {
            // First time enabling: remember the real gamma
            if (velora$savedGamma < 0) {
                velora$savedGamma = client.options.getGamma().getValue();
            }
            // Force gamma to max so every lightmap pixel renders at full brightness
            client.options.getGamma().setValue(16.0);
        } else if (velora$savedGamma >= 0) {
            // Fullbright just turned off — restore the saved gamma
            client.options.getGamma().setValue(velora$savedGamma);
            velora$savedGamma = -1.0;
        }
    }
}
