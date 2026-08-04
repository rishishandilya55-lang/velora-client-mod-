package com.velora.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class VeloraRenderUtil {

    /**
     * Fills a rectangle with a smooth vertical linear gradient.
     */
    public static void drawGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        context.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    /**
     * Draws a solid, high-contrast rectangle with a crisp 1px border.
     */
    public static void drawSolidPanel(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor) {
        // High contrast fill
        context.fill(x, y, x + w, y + h, bgColor);
        // Crisp border outline around panel
        context.drawBorder(x, y, w, h, borderColor);
    }

    /**
     * Draws a smooth rounded rectangle with solid fill.
     */
    public static void drawRoundedRect(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        if (radius <= 0) {
            context.fill(x, y, x + w, y + h, color);
            return;
        }
        context.fill(x + 2, y, x + w - 2, y + h, color);
        context.fill(x, y + 2, x + w, y + h - 2, color);
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    /**
     * Draws multi-layer neon glow ambient borders for maximum visibility.
     */
    public static void drawGlowBorder(DrawContext context, int x, int y, int w, int h, int glowColor, int spread) {
        int baseAlpha = (glowColor >> 24) & 0xFF;
        if (baseAlpha == 0) baseAlpha = 0xFF;
        int rgb = glowColor & 0x00FFFFFF;

        for (int i = 0; i <= spread; i++) {
            int alpha = Math.max(0, (int) (baseAlpha * (1.0f - (float) i / (spread + 1))));
            int color = (alpha << 24) | rgb;
            context.drawBorder(x - i, y - i, w + i * 2, h + i * 2, color);
        }
    }

    /**
     * Draws a high-contrast Lunar glass container panel with solid background, accent bar, and glowing border.
     */
    public static void drawGlassPanel(DrawContext context, int x, int y, int w, int h, int bgColor, int accentColor) {
        // Solid high-contrast background
        context.fill(x, y, x + w, y + h, bgColor);

        // Top accent line
        context.fill(x, y, x + w, y + 2, accentColor);

        // Crisp border outline
        context.drawBorder(x, y, w, h, accentColor);

        // Glow outline
        drawGlowBorder(context, x, y, w, h, (accentColor & 0x00FFFFFF) | 0x66000000, 2);
    }

    /**
     * Draws a modern Lunar-style pill button with high contrast and pure white text.
     */
    public static void drawPillButton(DrawContext context, int x, int y, int w, int h, String text, boolean hovered, int accentColor, TextRenderer font) {
        int topColor = hovered ? 0xFF9333EA : 0xFF2D1554;
        int bottomColor = hovered ? 0xFF6B21A8 : 0xFF1E0B38;

        // Gradient background
        drawGradient(context, x, y, x + w, y + h, topColor, bottomColor);

        // Crisp border outline
        context.drawBorder(x, y, w, h, hovered ? 0xFFD8B4FE : accentColor);

        // Pure white text with shadow
        context.drawCenteredTextWithShadow(font, text, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    /**
     * Draws a modern toggle switch widget with high contrast colors.
     */
    public static void drawSwitch(DrawContext context, int x, int y, int w, int h, boolean enabled, boolean hovered) {
        int trackColor = enabled ? (hovered ? 0xFF16A34A : 0xFF22C55E) : (hovered ? 0xFF4B5563 : 0xFF374151);
        context.fill(x, y, x + w, y + h, trackColor);
        context.drawBorder(x, y, w, h, enabled ? 0xFF86EFAC : 0xFF9CA3AF);

        int knobSize = h - 4;
        int knobX = enabled ? x + w - knobSize - 2 : x + 2;
        int knobY = y + 2;

        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
    }
}
