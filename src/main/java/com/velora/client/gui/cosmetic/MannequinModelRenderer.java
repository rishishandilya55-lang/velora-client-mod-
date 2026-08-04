package com.velora.client.gui.cosmetic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class MannequinModelRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void renderMannequinCard(DrawContext context, int x, int y, int width, int height, CosmeticItem item, boolean hovered) {
        LOGGER.trace("[Velora] Rendering mannequin card: name={}, type={}", item.getName(), item.getType());
        int centerX = x + width / 2;
        int centerY = y + height / 2 + 4;

        context.fill(x, y, x + width, y + height, 0xFF14161F);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 150.0f);

        float scale = 16.0f;
        matrices.scale(scale, -scale, scale);

        boolean isBackView = item.getType() == CosmeticItem.CosmeticType.CAPE || item.getType() == CosmeticItem.CosmeticType.WINGS;
        float rotY = isBackView ? 165.0f : 15.0f;
        if (hovered) {
            rotY += (float) (Math.sin(System.currentTimeMillis() / 220.0) * 10.0);
        }

        Quaternionf rotation = new Quaternionf()
                .rotationX((float) Math.toRadians(12.0))
                .rotateY((float) Math.toRadians(rotY));
        matrices.multiply(rotation);

        renderMannequinBase(context, matrices);

        Identifier validTexture = CosmeticTextureCache.getValidTextureOrFallback(item.getTexture());
        try {
            switch (item.getType()) {
                case CAPE -> renderCapeOnMannequin(context, matrices, validTexture);
                case WINGS -> renderWingsOnMannequin(context, matrices, validTexture);
                case HAT -> renderHatOnMannequin(context, matrices, validTexture);
                case FACE -> renderFaceOnMannequin(context, matrices, validTexture);
                case AURA -> renderAuraOnMannequin(context, matrices, validTexture);
            }
        } catch (Exception e) {
            renderFallbackCape(context, matrices);
        }

        matrices.pop();

        context.drawBorder(x, y, width, height, hovered ? 0xFF3B82F6 : 0xFF232734);
    }

    private static void renderMannequinBase(DrawContext context, MatrixStack matrices) {
        int bodyColor = 0xFF475569;
        int headColor = 0xFF64748B;
        int standColor = 0xFF334155;

        drawCube(context, matrices, -0.4f, -1.1f, -0.4f, 0.8f, 0.1f, 0.8f, standColor);
        drawCube(context, matrices, -0.22f, -0.6f, -0.1f, 0.2f, 0.6f, 0.2f, bodyColor);
        drawCube(context, matrices, 0.02f, -0.6f, -0.1f, 0.2f, 0.6f, 0.2f, bodyColor);
        drawCube(context, matrices, -0.25f, 0.0f, -0.12f, 0.5f, 0.7f, 0.24f, 0xFF526075);
        drawCube(context, matrices, -0.4f, 0.1f, -0.1f, 0.12f, 0.55f, 0.2f, bodyColor);
        drawCube(context, matrices, 0.28f, 0.1f, -0.1f, 0.12f, 0.55f, 0.2f, bodyColor);
        drawCube(context, matrices, -0.2f, 0.72f, -0.2f, 0.4f, 0.4f, 0.4f, headColor);
    }

    private static void renderCapeOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 0.68f, -0.13f);
            matrices.multiply(new Quaternionf().rotationX((float) Math.toRadians(-8.0)));

            if (CosmeticTextureCache.isTextureValid(texture)) {
                int[] dim = getTextureDimensions(texture);
                LOGGER.trace("[Velora] Cape texture dimensions: {}x{} for {}", dim[0], dim[1], texture);
                context.drawTexture(RenderLayer::getGuiTextured, texture, -10, -28, 0.0f, 0.0f, 20, 32, dim[0], dim[1]);
            } else {
                LOGGER.debug("[Velora] Cape texture invalid, using fallback: {}", texture);
                renderFallbackCape(context, matrices);
            }
        } catch (Exception e) {
            LOGGER.error("[Velora] Exception rendering cape texture: {}", texture, e);
            renderFallbackCape(context, matrices);
        } finally {
            matrices.pop();
        }
    }

    private static void renderWingsOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 0.35f, -0.14f);

            if (CosmeticTextureCache.isTextureValid(texture)) {
                int[] dim = getTextureDimensions(texture);
                context.drawTexture(RenderLayer::getGuiTextured, texture, -30, -22, 0.0f, 0.0f, 26, 36, dim[0], dim[1]);
                context.drawTexture(RenderLayer::getGuiTextured, texture, 4, -22, 0.0f, 0.0f, 26, 36, dim[0], dim[1]);
            } else {
                drawCube(context, matrices, -0.6f, -0.2f, 0.0f, 1.2f, 0.8f, 0.05f, 0xFF475569);
            }
        } finally {
            matrices.pop();
        }
    }

    private static void renderHatOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 1.15f, 0.0f);
            drawCube(context, matrices, -0.25f, 0.0f, -0.25f, 0.5f, 0.22f, 0.5f, 0xFFFFD700);
        } finally {
            matrices.pop();
        }
    }

    private static void renderFaceOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 0.9f, 0.22f);
            drawCube(context, matrices, -0.25f, -0.05f, 0.0f, 0.5f, 0.15f, 0.05f, 0xFF38BDF8);
        } finally {
            matrices.pop();
        }
    }

    private static void renderAuraOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 0.4f, 0.0f);
            drawCube(context, matrices, -0.45f, -0.05f, -0.45f, 0.9f, 0.1f, 0.9f, 0xAA7C3AED);
        } finally {
            matrices.pop();
        }
    }

    private static int[] getTextureDimensions(Identifier texture) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.getResourceManager() != null) {
                java.util.Optional<Resource> resource = mc.getResourceManager().getResource(texture);
                if (resource.isPresent()) {
                    try (InputStream stream = resource.get().getInputStream()) {
                        NativeImage image = NativeImage.read(stream);
                        int w = image.getWidth();
                        int h = image.getHeight();
                        image.close();
                        return new int[]{w, h};
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return new int[]{64, 32};
    }

    private static void renderFallbackCape(DrawContext context, MatrixStack matrices) {
        drawCube(context, matrices, -0.22f, -0.7f, -0.02f, 0.44f, 0.75f, 0.04f, 0xFF334155);
    }

    private static void drawCube(DrawContext context, MatrixStack matrices, float x, float y, float z, float w, float h, float d, int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;

        int sideColor = argb;
        int topColor = ((a & 0xFF) << 24) | ((Math.min(255, (int)(r * 1.15))) << 16) | ((Math.min(255, (int)(g * 1.15))) << 8) | (Math.min(255, (int)(b * 1.15)));

        int x1 = (int)(x * 40), y1 = (int)(- (y + h) * 40);
        int w1 = (int)(w * 40), h1 = (int)(h * 40);

        context.fill(x1, y1, x1 + w1, y1 + h1, sideColor);
        context.drawBorder(x1, y1, w1, h1, topColor);
    }
}
