package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public class SimpleOptionMixin<T> {

    @SuppressWarnings("unchecked")
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void velora_overrideGamma(CallbackInfoReturnable<T> cir) {
        if (ModConfig.showFullbright) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.options != null && (Object) this == mc.options.getGamma()) {
                cir.setReturnValue((T) Double.valueOf(16.0));
            }
        }
    }
}
