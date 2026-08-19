package com.velora.client.gui;

import net.minecraft.client.gui.DrawContext;

public class VeloraRenderUtil {

    public static void drawSolidPanel(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor) {
        context.fill(x, y, x + w, y + h, bgColor);
        if ((borderColor & 0xFF000000) != 0) {
            context.drawBorder(x, y, w, h, borderColor);
        }
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        if (radius <= 0) {
            context.fill(x, y, x + w, y + h, color);
            return;
        }
        context.fill(x + 2, y, x + w - 2, y + h, color);
        context.fill(x, y + 2, x + w, y + h - 2, color);
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    public static void drawPill(DrawContext context, int x, int y, int w, int h, int color) {
        drawRoundedRect(context, x, y, w, h, 2, color);
    }

    public static void drawToggleSwitch(DrawContext context, int x, int y, int w, int h, boolean enabled) {
        int trackBg = enabled ? VeloraColors.GREEN_D : VeloraColors.SURF4;
        int trackBorder = enabled ? VeloraColors.GREEN_S : VeloraColors.BORDER_S;
        int knobColor = enabled ? 0xFFFFFFFF : VeloraColors.TEXT_M;

        drawRoundedRect(context, x, y, w, h, 2, trackBg);
        context.drawBorder(x, y, w, h, trackBorder);

        int knobW = h - 4;
        int knobX = enabled ? (x + w - knobW - 2) : (x + 2);
        int knobY = y + 2;
        drawRoundedRect(context, knobX, knobY, knobW, knobW, 1, knobColor);
    }

    public static void drawDivider(DrawContext context, int x, int y, int w) {
        context.fill(x, y, x + w, y + 1, VeloraColors.DIVIDER);
    }
}
