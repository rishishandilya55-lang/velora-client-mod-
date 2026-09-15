package com.velora.client.gui;

import net.minecraft.client.gui.DrawContext;

public class VeloraVectorIcons {

    public static void drawIcon(DrawContext ctx, String modName, int x, int y, int size) {
        int color = 0xFFFFFFFF; // Clean monochrome white
        int cx = x + size / 2;
        int cy = y + size / 2;

        switch (modName) {
            case "FPS Display" -> drawGauge(ctx, cx, cy, color);
            case "WASD Keys" -> drawWASD(ctx, cx, cy, color);
            case "Ping Display" -> drawSignalBars(ctx, cx, cy, color);
            case "CPS Counter" -> drawMouse(ctx, cx, cy, color);
            case "Armor Status" -> drawShield(ctx, cx, cy, color);
            case "Coordinates" -> drawMapPin(ctx, cx, cy, color);
            case "Day Counter" -> drawSun(ctx, cx, cy, color);
            case "Block Info" -> drawCube(ctx, cx, cy, color);
            case "Toggle Sprint" -> drawSprint(ctx, cx, cy, color);
            case "Toggle Sneak" -> drawSneak(ctx, cx, cy, color);
            case "Zoom Mod" -> drawMagnifier(ctx, cx, cy, color);
            case "Free Look" -> drawEye(ctx, cx, cy, color);
            case "Snap Look" -> drawRefresh(ctx, cx, cy, color);
            case "Fullbright" -> drawSun(ctx, cx, cy, color);
            case "No Hurt Cam" -> drawShield(ctx, cx, cy, color);
            case "Minimap" -> drawRadar(ctx, cx, cy, color);
            case "Waypoints" -> drawBeacon(ctx, cx, cy, color);
            case "Chat Colors" -> drawChatBubble(ctx, cx, cy, color);
            case "Item Tooltips" -> drawTag(ctx, cx, cy, color);
            case "Hit Color" -> drawPulse(ctx, cx, cy, color);
            case "Item Physics" -> drawCube(ctx, cx, cy, color);
            case "Potion Status" -> drawFlask(ctx, cx, cy, color);
            case "Crosshair" -> drawCrosshair(ctx, cx, cy, color);
            case "Item Model" -> drawFrame(ctx, cx, cy, color);
            default -> drawGauge(ctx, cx, cy, color);
        }
    }

    private static void drawGauge(DrawContext ctx, int cx, int cy, int color) {
        // Smooth speedometer gauge
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 7, color);
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 5, 0x00000000);
        ctx.fill(cx - 8, cy + 2, cx + 9, cy + 8, 0x00000000); // Cut bottom arc
        // Needle pointing up-right
        ctx.fill(cx - 1, cy - 1, cx + 4, cy - 4, color);
        ctx.fill(cx, cy, cx + 1, cy + 1, color);
    }

    private static void drawWASD(DrawContext ctx, int cx, int cy, int color) {
        // Top W key box
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 3, cy - 7, 7, 7, 1, color);
        // A, S, D key boxes
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 10, cy + 1, 7, 7, 1, color);
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 3, cy + 1, 7, 7, 1, color);
        VeloraRenderUtil.drawRoundedBorder(ctx, cx + 4, cy + 1, 7, 7, 1, color);
    }

    private static void drawSignalBars(DrawContext ctx, int cx, int cy, int color) {
        ctx.fill(cx - 7, cy + 3, cx - 5, cy + 7, color);
        ctx.fill(cx - 3, cy, cx - 1, cy + 7, color);
        ctx.fill(cx + 1, cy - 3, cx + 3, cy + 7, color);
        ctx.fill(cx + 5, cy - 6, cx + 7, cy + 7, color);
    }

    private static void drawMouse(DrawContext ctx, int cx, int cy, int color) {
        // Mouse body outline
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 5, cy - 7, 11, 15, 3, color);
        // Center scroll wheel divider
        ctx.fill(cx, cy - 7, cx + 1, cy - 3, color);
    }

    private static void drawShield(DrawContext ctx, int cx, int cy, int color) {
        // Shield top bar
        ctx.fill(cx - 6, cy - 7, cx + 7, cy - 5, color);
        // Shield sides
        ctx.fill(cx - 6, cy - 5, cx - 4, cy + 1, color);
        ctx.fill(cx + 5, cy - 5, cx + 7, cy + 1, color);
        ctx.fill(cx - 4, cy + 1, cx - 2, cy + 5, color);
        ctx.fill(cx + 3, cy + 1, cx + 5, cy + 5, color);
        ctx.fill(cx - 2, cy + 5, cx + 3, cy + 7, color);
    }

    private static void drawMapPin(DrawContext ctx, int cx, int cy, int color) {
        // Top circle pin
        VeloraRenderUtil.drawCircle(ctx, cx, cy - 3, 5, color);
        // Bottom tip
        ctx.fill(cx - 2, cy + 2, cx + 3, cy + 6, color);
        ctx.fill(cx - 1, cy + 6, cx + 2, cy + 8, color);
        // Center hole
        VeloraRenderUtil.drawCircle(ctx, cx, cy - 3, 2, 0xFF000000);
    }

    private static void drawSun(DrawContext ctx, int cx, int cy, int color) {
        // Center sun disk
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 3, color);
        // Rays
        ctx.fill(cx, cy - 7, cx + 1, cy - 4, color);
        ctx.fill(cx, cy + 5, cx + 1, cy + 8, color);
        ctx.fill(cx - 7, cy, cx - 4, cy + 1, color);
        ctx.fill(cx + 5, cy, cx + 8, cy + 1, color);
    }

    private static void drawCube(DrawContext ctx, int cx, int cy, int color) {
        // Wireframe isometric cube box
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 6, cy - 6, 13, 13, 2, color);
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 3, cy - 3, 7, 7, 1, color);
    }

    private static void drawSprint(DrawContext ctx, int cx, int cy, int color) {
        // Fast running shoe icon
        ctx.fill(cx - 6, cy - 3, cx + 5, cy, color);
        ctx.fill(cx - 3, cy, cx + 7, cy + 3, color);
        ctx.fill(cx - 6, cy + 3, cx + 8, cy + 6, color);
        // Trailing lines
        ctx.fill(cx - 9, cy - 2, cx - 7, cy - 1, color);
        ctx.fill(cx - 9, cy + 2, cx - 7, cy + 3, color);
    }

    private static void drawSneak(DrawContext ctx, int cx, int cy, int color) {
        // Boot/Leg sneaking
        ctx.fill(cx - 3, cy - 6, cx + 1, cy + 2, color);
        ctx.fill(cx - 3, cy + 2, cx + 5, cy + 6, color);
    }

    private static void drawMagnifier(DrawContext ctx, int cx, int cy, int color) {
        // Lens circle
        VeloraRenderUtil.drawCircle(ctx, cx - 3, cy - 3, 5, color);
        VeloraRenderUtil.drawCircle(ctx, cx - 3, cy - 3, 3, 0x00000000);
        // Handle
        ctx.fill(cx + 2, cy + 2, cx + 7, cy + 7, color);
    }

    private static void drawEye(DrawContext ctx, int cx, int cy, int color) {
        // Eye oval
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 6, color);
        ctx.fill(cx - 7, cy - 7, cx + 8, cy - 3, 0x00000000);
        ctx.fill(cx - 7, cy + 4, cx + 8, cy + 8, 0x00000000);
        // Pupil
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 2, color);
    }

    private static void drawRefresh(DrawContext ctx, int cx, int cy, int color) {
        // Circular loop
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 6, color);
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 4, 0x00000000);
        ctx.fill(cx + 1, cy - 2, cx + 6, cy + 3, 0x00000000);
    }

    private static void drawRadar(DrawContext ctx, int cx, int cy, int color) {
        // Radar circle with grid lines
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 7, color);
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 6, 0x00000000);
        ctx.fill(cx, cy - 6, cx + 1, cy + 7, color);
        ctx.fill(cx - 6, cy, cx + 7, cy + 1, color);
    }

    private static void drawBeacon(DrawContext ctx, int cx, int cy, int color) {
        // Diamond marker beam
        ctx.fill(cx, cy - 7, cx + 1, cy - 6, color);
        ctx.fill(cx - 2, cy - 5, cx + 3, cy - 2, color);
        ctx.fill(cx - 5, cy - 2, cx + 6, cy + 3, color);
        ctx.fill(cx - 2, cy + 3, cx + 3, cy + 6, color);
        ctx.fill(cx, cy + 6, cx + 1, cy + 7, color);
    }

    private static void drawChatBubble(DrawContext ctx, int cx, int cy, int color) {
        // Speech bubble
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 7, cy - 5, 15, 10, 2, color);
        // Tail
        ctx.fill(cx - 4, cy + 5, cx - 2, cy + 7, color);
        // Lines
        ctx.fill(cx - 4, cy - 2, cx + 4, cy - 1, color);
        ctx.fill(cx - 4, cy + 1, cx + 1, cy + 2, color);
    }

    private static void drawTag(DrawContext ctx, int cx, int cy, int color) {
        // Tag outline
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 6, cy - 5, 13, 10, 2, color);
        ctx.fill(cx - 3, cy - 2, cx - 1, cy, color);
    }

    private static void drawPulse(DrawContext ctx, int cx, int cy, int color) {
        // Pulse wave zig-zag line
        ctx.fill(cx - 7, cy, cx - 3, cy + 1, color);
        ctx.fill(cx - 3, cy - 4, cx - 2, cy + 1, color);
        ctx.fill(cx - 2, cy - 4, cx + 1, cy - 3, color);
        ctx.fill(cx + 1, cy - 4, cx + 2, cy + 5, color);
        ctx.fill(cx + 2, cy + 4, cx + 5, cy + 5, color);
        ctx.fill(cx + 5, cy, cx + 8, cy + 1, color);
    }

    private static void drawFlask(DrawContext ctx, int cx, int cy, int color) {
        // Potion bottle
        ctx.fill(cx - 2, cy - 7, cx + 3, cy - 4, color);
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 5, cy - 4, 11, 11, 2, color);
    }

    private static void drawCrosshair(DrawContext ctx, int cx, int cy, int color) {
        // Target reticle
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 3, color);
        VeloraRenderUtil.drawCircle(ctx, cx, cy, 1, 0x00000000);
        ctx.fill(cx, cy - 7, cx + 1, cy - 3, color);
        ctx.fill(cx, cy + 4, cx + 1, cy + 8, color);
        ctx.fill(cx - 7, cy, cx - 3, cy + 1, color);
        ctx.fill(cx + 4, cy, cx + 8, cy + 1, color);
    }

    private static void drawFrame(DrawContext ctx, int cx, int cy, int color) {
        // Picture frame
        VeloraRenderUtil.drawRoundedBorder(ctx, cx - 6, cy - 6, 13, 13, 2, color);
        ctx.fill(cx, cy - 2, cx + 1, cy + 3, color);
        ctx.fill(cx - 2, cy, cx + 3, cy + 1, color);
    }
}
