package com.velora.client.gui;

import net.minecraft.client.gui.DrawContext;

public class VeloraRenderUtil {

    public static void drawSolidPanel(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor) {
        context.fill(x, y, x + w, y + h, bgColor);
        context.drawBorder(x, y, w, h, borderColor);
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
}
