package com.velora.client.mixin;

import com.velora.client.gui.VeloraMainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Immediately replaces the vanilla TitleScreen with our fully standalone VeloraMainMenuScreen.
 * We extend Screen so the `client` field is directly accessible.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void redirectToVeloraMenu(CallbackInfo ci) {
        if (this.client != null) {
            this.client.setScreen(new VeloraMainMenuScreen());
            ci.cancel();
        }
    }
}
