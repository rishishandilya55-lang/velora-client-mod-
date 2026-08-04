package com.example.fpsdisplay.mixin;

/**
 * Fullbright for Minecraft 1.21.4 is handled cleanly via GameRendererMixin.getNightVisionStrength.
 *
 * Modifying SimpleOption<Double> gamma to 16.0 causes Minecraft 1.21.4 option validation
 * to log "Illegal option value 16.0" on every frame. Using GameRendererMixin provides
 * 100% fullbright night-vision lighting natively with zero log errors.
 */
public class LightmapTextureManagerMixin {
    // Unregistered from mixins.json — Fullbright active via GameRendererMixin
}
