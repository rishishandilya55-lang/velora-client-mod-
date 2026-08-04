package com.velora.client.gui.cosmetic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

import java.util.Optional;

/**
 * 3D Isometric Mini-Display Renderer for Mannequin Cards in Velora Client.
 * Renders small, compact 3D mannequin player models wearing cosmetics.
 * Includes precise 64x32 cape UV mapping to display velora_cape.png and mojang_cape.png textures inside card viewports.
 */
public class MannequinModelRenderer {

    /**
     * Renders a 3D isometric mannequin mini-display displaying a cosmetic item.
     *
     * @param context DrawContext for rendering
     * @param x       Top-left X coordinate of card viewport
     * @param y       Top-left Y coordinate of card viewport
     * @param width   Viewport width
     * @param height  Viewport height
     * @param item    The cosmetic item to display
     * @param hovered Whether card is currently hovered
     */
    public static void renderMannequinCard(DrawContext context, int x, int y, int width, int height, CosmeticItem item, boolean hovered) {
        int centerX = x + width / 2;
        int centerY = y + height / 2 + 4;

        // Viewport Inner Dark Box (#14161F)
        context.fill(x, y, x + width, y + height, 0xFF14161F);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 150.0f);

        // Small, compact scale (16.0f) tuned for 106px height viewport
        float scale = 16.0f;
        matrices.scale(scale, -scale, scale);

        // Angle tilt: Capes rendered from isometric back view (~165°)
        boolean isBackView = item.getType() == CosmeticItem.CosmeticType.CAPE || item.getType() == CosmeticItem.CosmeticType.WINGS;
        float rotY = isBackView ? 165.0f : 15.0f;
        if (hovered) {
            rotY += (float) (Math.sin(System.currentTimeMillis() / 220.0) * 10.0);
        }

        Quaternionf rotation = new Quaternionf()
                .rotationX((float) Math.toRadians(12.0))
                .rotateY((float) Math.toRadians(rotY));
        matrices.multiply(rotation);

        // Render Mannequin Body
        renderMannequinBase(context, matrices);

        // Render Cosmetic Attachment with Safe Texture Validation
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

        // Outer subtle viewport border
        context.drawBorder(x, y, width, height, hovered ? 0xFF3B82F6 : 0xFF232734);
    }

    private static void renderMannequinBase(DrawContext context, MatrixStack matrices) {
        int bodyColor = 0xFF475569;
        int headColor = 0xFF64748B;
        int standColor = 0xFF334155;

        // Base Pedestal
        drawCube(context, matrices, -0.4f, -1.1f, -0.4f, 0.8f, 0.1f, 0.8f, standColor);

        // Legs / Lower Body
        drawCube(context, matrices, -0.22f, -0.6f, -0.1f, 0.2f, 0.6f, 0.2f, bodyColor);
        drawCube(context, matrices, 0.02f, -0.6f, -0.1f, 0.2f, 0.6f, 0.2f, bodyColor);

        // Torso / Chest
        drawCube(context, matrices, -0.25f, 0.0f, -0.12f, 0.5f, 0.7f, 0.24f, 0xFF526075);

        // Shoulders & Arms
        drawCube(context, matrices, -0.4f, 0.1f, -0.1f, 0.12f, 0.55f, 0.2f, bodyColor);
        drawCube(context, matrices, 0.28f, 0.1f, -0.1f, 0.12f, 0.55f, 0.2f, bodyColor);

        // Head
        drawCube(context, matrices, -0.2f, 0.72f, -0.2f, 0.4f, 0.4f, 0.4f, headColor);
    }

    private static void renderCapeOnMannequin(DrawContext context, MatrixStack matrices, Identifier texture) {
        matrices.push();
        try {
            matrices.translate(0.0f, 0.68f, -0.13f);
            matrices.multiply(new Quaternionf().rotationX((float) Math.toRadians(-8.0)));

            if (CosmeticTextureCache.isTextureValid(texture)) {
                int[] dim = getTextureDimensions(texture);
                context.drawTexture(texture, -10, -28, 0.0f, 0.0f, 20, 32, dim[0], dim[1]);
            } else {
                renderFallbackCape(context, matrices);
            }
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
                context.drawTexture(texture, -30, -22, 0.0f, 0.0f, 26, 36, dim[0], dim[1]);
                context.drawTexture(texture, 4, -22, 0.0f, 0.0f, 26, 36, dim[0], dim[1]);
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
            if (mc != null && mc.getTextureManager() != null) {
                AbstractTexture abstractTexture = mc.getTextureManager().getTexture(texture);
                if (abstractTexture != null) {
                    Optional<NativeImage> image = abstractTexture.getImage();
                    if (image.isPresent()) {
                        return new int[]{image.get().getWidth(), image.get().getHeight()};
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return new int[]{64, 32};
    }

    private static void renderFallbackCape(DrawContext context, MatrixStack matrices) {
        // Fallback gray mannequin block (Hex: 0xFF334155)
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
