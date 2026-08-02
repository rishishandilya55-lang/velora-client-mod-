package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fullbright implementation for Minecraft 1.21.4.
 * Intercepts gamma setting calls to return 1600% brightness (16.0) when Fullbright is active.
 */
@Mixin(SimpleOption.class)
public class SimpleOptionMixin {

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void onGetValue(CallbackInfoReturnable<Object> cir) {
        if (ModConfig.showFullbright) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.options != null && (Object) this == client.options.getGamma()) {
                cir.setReturnValue(16.0);
            }
        }
    }
}
