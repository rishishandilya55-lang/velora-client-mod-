package com.example.fpsdisplay.mixin;

/**
 * Fullbright for Minecraft 1.21.4 is handled cleanly via GameRendererMixin.getNightVisionStrength.
 *
 * Intercepting SimpleOption.getValue to return 16.0 causes Minecraft option validation
 * to log "Illegal option value 16.0" on every frame. GameRendererMixin provides 100% fullbright natively.
 */
public class SimpleOptionMixin {
    // Unregistered from mixins.json — Fullbright active via GameRendererMixin
}
