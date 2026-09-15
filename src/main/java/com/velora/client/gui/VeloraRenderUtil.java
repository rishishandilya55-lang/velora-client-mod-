package com.velora.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class VeloraRenderUtil {

    private static final Identifier TEX_WINDOW          = Identifier.of("velora", "textures/gui/nineslice/window.png");
    private static final Identifier TEX_CARD            = Identifier.of("velora", "textures/gui/nineslice/card.png");
    private static final Identifier TEX_CARD_HOVER      = Identifier.of("velora", "textures/gui/nineslice/card_hover.png");
    private static final Identifier TEX_BUTTON_ACTIVE   = Identifier.of("velora", "textures/gui/nineslice/button_active.png");
    private static final Identifier TEX_BUTTON_INACTIVE = Identifier.of("velora", "textures/gui/nineslice/button_inactive.png");
    private static final Identifier TEX_SEARCH          = Identifier.of("velora", "textures/gui/nineslice/search.png");
    private static final Identifier TEX_PILL_ON         = Identifier.of("velora", "textures/gui/nineslice/pill_on.png");
    private static final Identifier TEX_PILL_OFF        = Identifier.of("velora", "textures/gui/nineslice/pill_off.png");

    public static void drawNineSlicePadded(DrawContext ctx, Identifier id, int x, int y, int w, int h, int c, int p, int texR, int s) {
        int texMid = s - p * 2 - texR * 2;
        int midW = w - c * 2;
        int midH = h - c * 2;

        float uLeft = (float) p;
        float uRight = (float) (s - p - texR);
        float vTop = (float) p;
        float vBottom = (float) (s - p - texR);

        float uMid = (float) (p + texR);
        float vMid = (float) (p + texR);

        // 4 Corners (Sampled strictly within [16.0f, 240.0f], preventing wrap-around!)
        ctx.drawTexture(RenderLayer::getGuiTextured, id, x, y, uLeft, vTop, c, c, texR, texR, s, s);
        ctx.drawTexture(RenderLayer::getGuiTextured, id, x + w - c, y, uRight, vTop, c, c, texR, texR, s, s);
        ctx.drawTexture(RenderLayer::getGuiTextured, id, x, y + h - c, uLeft, vBottom, c, c, texR, texR, s, s);
        ctx.drawTexture(RenderLayer::getGuiTextured, id, x + w - c, y + h - c, uRight, vBottom, c, c, texR, texR, s, s);

        // 4 Edges
        if (midW > 0) {
            ctx.drawTexture(RenderLayer::getGuiTextured, id, x + c, y, uMid, vTop, midW, c, texMid, texR, s, s);
            ctx.drawTexture(RenderLayer::getGuiTextured, id, x + c, y + h - c, uMid, vBottom, midW, c, texMid, texR, s, s);
        }
        if (midH > 0) {
            ctx.drawTexture(RenderLayer::getGuiTextured, id, x, y + c, uLeft, vMid, c, midH, texR, texMid, s, s);
            ctx.drawTexture(RenderLayer::getGuiTextured, id, x + w - c, y + c, uRight, vMid, c, midH, texR, texMid, s, s);
        }
        // Center
        if (midW > 0 && midH > 0) {
            ctx.drawTexture(RenderLayer::getGuiTextured, id, x + c, y + c, uMid, vMid, midW, midH, texMid, texMid, s, s);
        }
    }

    public static void drawSmoothWindow(DrawContext ctx, int x, int y, int w, int h) {
        drawNineSlicePadded(ctx, TEX_WINDOW, x, y, w, h, 10, 0, 24, 256);
    }

    public static void drawSmoothCard(DrawContext ctx, int x, int y, int w, int h, boolean hovered) {
        drawNineSlicePadded(ctx, hovered ? TEX_CARD_HOVER : TEX_CARD, x, y, w, h, 8, 0, 20, 256);
    }

    public static void drawSmoothButton(DrawContext ctx, int x, int y, int w, int h, boolean active) {
        drawNineSlicePadded(ctx, active ? TEX_BUTTON_ACTIVE : TEX_BUTTON_INACTIVE, x, y, w, h, 6, 0, 16, 256);
    }

    public static void drawSmoothSearch(DrawContext ctx, int x, int y, int w, int h) {
        drawNineSlicePadded(ctx, TEX_SEARCH, x, y, w, h, 6, 0, 16, 256);
    }

    public static void drawPillPng(DrawContext ctx, int x, int y, int w, int h, boolean active) {
        Identifier pillTex = active ? TEX_PILL_ON : TEX_PILL_OFF;
        ctx.drawTexture(RenderLayer::getGuiTextured, pillTex, x, y, 0.0f, 0.0f, w, h, 256, 128, 256, 128);
    }

    // ── Primitive Helper methods ──────────────────────────────────────
    public static void drawSolidPanel(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor) {
        context.fill(x, y, x + w, y + h, bgColor);
        if ((borderColor & 0xFF000000) != 0) {
            context.drawBorder(x, y, w, h, borderColor);
        }
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        context.fill(x, y, x + w, y + h, color);
    }

    public static void drawRoundedBorder(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        context.drawBorder(x, y, w, h, color);
    }

    public static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int i = -radius; i <= radius; i++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, radius * radius - i * i)));
            context.fill(cx - dx, cy + i, cx + dx + 1, cy + i + 1, color);
        }
    }

    public static void drawPill(DrawContext context, int x, int y, int w, int h, int color) {
        int radius = h / 2;
        context.fill(x + radius, y, x + w - radius, y + h, color);
        drawCircle(context, x + radius, y + radius, radius, color);
        drawCircle(context, x + w - radius, y + radius, radius, color);
    }

    public static void drawToggleSwitch(DrawContext context, int x, int y, int w, int h, boolean enabled) {
        drawPillPng(context, x, y, w, h, enabled);
    }

    public static void drawDivider(DrawContext context, int x, int y, int w) {
        context.fill(x, y, x + w, y + 1, VeloraColors.DIVIDER);
    }
}
