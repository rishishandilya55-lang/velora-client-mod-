package com.velora.client.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

public class HitColorMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    public static final int DEFAULT_VANILLA_HURT = -1291911168; // 0xB3FF0000 (Vanilla ~70% red overlay)

    private static NativeImageBackedTexture overlayTextureInstance = null;
    private static int lastAppliedColor = 0;
    private static final ThreadLocal<Boolean> CURRENT_ENTITY_HURT = ThreadLocal.withInitial(() -> false);

    public static void init() {
        LOGGER.info("[Velora] HitColor module initialized");
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    public static void setOverlayTextureInstance(NativeImageBackedTexture texture) {
        overlayTextureInstance = texture;
        markDirty();
    }

    public static void setCurrentEntityHurt(boolean hurt) {
        CURRENT_ENTITY_HURT.set(hurt);
    }

    public static boolean isCurrentEntityHurt() {
        return CURRENT_ENTITY_HURT.get();
    }

    public static int getArmorOverlay(int defaultOverlay) {
        if (ModConfig.showHitColor && ModConfig.hitColorShowOnArmor && isCurrentEntityHurt()) {
            return OverlayTexture.packUv(OverlayTexture.getU(0), OverlayTexture.getV(true));
        }
        return defaultOverlay;
    }

    public static int getCurrentColorArgb() {
        if (!ModConfig.showHitColor) {
            return DEFAULT_VANILLA_HURT;
        }

        // In Minecraft entity shaders: color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
        // overlayColor.a = 0.0 means 100% overlay color, overlayColor.a = 1.0 means 0% overlay color.
        // Therefore, shaderAlpha = 255 - userOpacity!
        int userOpacity = Math.max(0, Math.min(255, ModConfig.hitColorAlpha));
        int shaderAlpha = Math.max(0, Math.min(255, 255 - userOpacity));

        int r, g, b;
        if (ModConfig.hitColorRainbow) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0f;
            int rgb = Color.HSBtoRGB(hue, 0.85f, 1.0f);
            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = rgb & 0xFF;
        } else {
            r = Math.max(0, Math.min(255, ModConfig.hitColorRed));
            g = Math.max(0, Math.min(255, ModConfig.hitColorGreen));
            b = Math.max(0, Math.min(255, ModConfig.hitColorBlue));
        }

        return (shaderAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static void onRenderTick(GameRenderer gameRenderer) {
        if (overlayTextureInstance == null) return;

        int targetColor = ModConfig.showHitColor ? getCurrentColorArgb() : DEFAULT_VANILLA_HURT;
        if (targetColor != lastAppliedColor) {
            lastAppliedColor = targetColor;
            updateTexture(overlayTextureInstance, targetColor);
        }
    }

    public static void updateTexture(NativeImageBackedTexture texture, int colorArgb) {
        if (texture == null) return;
        NativeImage image = texture.getImage();
        if (image != null) {
            image.fillRect(0, 0, 16, 8, colorArgb);
            RenderSystem.activeTexture(33985); // GL_TEXTURE1
            texture.bindTexture();
            image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false);
            RenderSystem.activeTexture(33984); // GL_TEXTURE0
        }
    }

    public static void markDirty() {
        lastAppliedColor = 0;
        if (overlayTextureInstance != null) {
            int targetColor = ModConfig.showHitColor ? getCurrentColorArgb() : DEFAULT_VANILLA_HURT;
            lastAppliedColor = targetColor;
            updateTexture(overlayTextureInstance, targetColor);
        }
    }
}
