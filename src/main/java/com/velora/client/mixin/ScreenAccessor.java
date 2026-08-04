package com.velora.client.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Accessor mixin that exposes the private Screen.drawables list so that
 * TitleScreenMixin can manually render child widgets without calling
 * super.render() (which would re-invoke renderBackground / panorama).
 */
@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("drawables")
    List<Drawable> velora_getDrawables();
}
