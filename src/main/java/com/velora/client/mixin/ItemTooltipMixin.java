package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ItemTooltipMixin {

    @Inject(method = "drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"))
    private void velora_enhancedTooltip(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ModConfig.showItemTooltips) return;
    }
}
