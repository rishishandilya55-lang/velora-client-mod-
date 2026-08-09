package com.velora.client.mixin;

import com.velora.client.client.CustomCrosshairMod;
import com.velora.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void velora_customCrosshairRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModConfig.enableCustomCrosshair) {
            ci.cancel();
            CustomCrosshairMod.render(context, tickCounter);
        }
    }
}
