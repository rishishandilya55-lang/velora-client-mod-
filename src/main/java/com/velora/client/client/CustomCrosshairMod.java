package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCrosshairMod {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    public static final int GRID_SIZE = 15; // 15x15 pixel canvas

    public static void init() {
        LOGGER.info("[Velora] Custom Crosshair module initialized");
    }

    public static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.options.hudHidden) return;

        boolean isFirstPerson = mc.options.getPerspective().isFirstPerson();
        if (!isFirstPerson && !ModConfig.crosshairThirdPerson) {
            return;
        }

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;

        boolean isTargeting = false;
        if (ModConfig.crosshairHighlightEntity || ModConfig.crosshairEnemyCrosshair) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                isTargeting = true;
            }
        }

        float attackCooldown = 1.0f;
        if (mc.player != null) {
            attackCooldown = mc.player.getAttackCooldownProgress(0.0f);
        }

        float dynamicOffset = 0.0f;
        if (ModConfig.crosshairDynamic && mc.player != null) {
            if (mc.player.isSprinting()) dynamicOffset = 3.0f;
            else if (mc.player.getVelocity().horizontalLengthSquared() > 0.001) dynamicOffset = 1.5f;
            if (!mc.player.isOnGround()) dynamicOffset += 2.0f;
        }

        renderCrosshair(context, cx, cy, dynamicOffset, attackCooldown, isTargeting);
    }

    /**
     * Core renderer used for in-game crosshair, HUD editor, and crosshair preview
     */
    public static void renderCrosshair(
        DrawContext context,
        int cx,
        int cy,
        float dynamicOffset,
        float attackCooldown,
        boolean isTargetingEntity
    ) {
        float scale = getEffectiveScale();

        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        if (scale != 1.0f) {
            context.getMatrices().scale(scale, scale, 1.0f);
        }

        int mainColor;
        boolean isEnemyHitbox = isTargetingEntity && (ModConfig.crosshairEnemyCrosshair || ModConfig.crosshairHighlightEntity);

        if (isEnemyHitbox) {
            mainColor = ModConfig.crosshairEnemyColor;
        } else {
            mainColor = HudColorHelper.getEffectiveColor(ModConfig.crosshairColor, ModConfig.crosshairRainbow);
        }

        int outlineColor = ModConfig.crosshairOutlineColor;
        boolean hasOutline = ModConfig.crosshairOutline;

        String preset = ModConfig.crosshairPreset != null ? ModConfig.crosshairPreset.toUpperCase() : "CLASSIC_CROSS";
        int size = Math.max(1, ModConfig.crosshairSize);
        int gap = Math.max(0, (int)(ModConfig.crosshairGap + dynamicOffset));
        if (isEnemyHitbox && "CROSS_EXPAND".equalsIgnoreCase(ModConfig.crosshairEnemyMode)) {
            gap += 3;
        }
        int thickness = Math.max(1, ModConfig.crosshairThickness);
        int halfThick = thickness / 2;

        switch (preset) {
            case "DOT" -> {
                int dotS = Math.max(1, ModConfig.crosshairDotSize);
                drawDot(context, 0, 0, dotS, mainColor, hasOutline, outlineColor);
            }
            case "CIRCLE" -> {
                drawCircle(context, 0, 0, size + gap, thickness, mainColor, hasOutline, outlineColor);
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "SQUARE" -> {
                drawSquare(context, 0, 0, size + gap, thickness, mainColor, hasOutline, outlineColor);
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "CHEVRON" -> {
                drawChevron(context, 0, 0, size, thickness, gap, mainColor, hasOutline, outlineColor);
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "DIAMOND" -> {
                drawDiamond(context, 0, 0, size + gap, thickness, mainColor, hasOutline, outlineColor);
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "T_SHAPE" -> {
                drawLine(context, -gap - size, -halfThick, size, thickness, mainColor, hasOutline, outlineColor); // Left
                drawLine(context, gap + 1, -halfThick, size, thickness, mainColor, hasOutline, outlineColor); // Right
                drawLine(context, -halfThick, gap + 1, thickness, size, mainColor, hasOutline, outlineColor); // Bottom
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "BOX_FEET" -> {
                drawBoxWithFeet(context, 0, 0, size + gap + 2, thickness, mainColor, hasOutline, outlineColor);
                if (ModConfig.crosshairShowDot) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
            case "CUSTOM_DRAWN", "DRAWABLE", "CUSTOM" -> {
                drawCustomGrid(context, 0, 0, mainColor, hasOutline, outlineColor);
            }
            default -> {
                // Classic 4-arm crosshair
                drawLine(context, -gap - size, -halfThick, size, thickness, mainColor, hasOutline, outlineColor); // Left
                drawLine(context, gap + 1, -halfThick, size, thickness, mainColor, hasOutline, outlineColor); // Right
                drawLine(context, -halfThick, -gap - size, thickness, size, mainColor, hasOutline, outlineColor); // Top
                drawLine(context, -halfThick, gap + 1, thickness, size, mainColor, hasOutline, outlineColor); // Bottom

                if (ModConfig.crosshairShowDot || "CROSS_DOT".equals(preset)) {
                    drawDot(context, 0, 0, ModConfig.crosshairDotSize, mainColor, hasOutline, outlineColor);
                }
            }
        }

        // Special Enemy Crosshair overlay modes
        if (isEnemyHitbox && ModConfig.crosshairEnemyCrosshair) {
            if ("TARGET_LOCK_BOX".equalsIgnoreCase(ModConfig.crosshairEnemyMode)) {
                drawTargetLockBrackets(context, 0, 0, size + gap + 5, ModConfig.crosshairEnemyColor);
            } else if ("RED_DOT".equalsIgnoreCase(ModConfig.crosshairEnemyMode)) {
                drawDot(context, 0, 0, 3, ModConfig.crosshairEnemyColor, true, 0xFF000000);
            }
        }

        // Weapon Attack Cooldown Indicator
        if (ModConfig.crosshairAttackIndicator && attackCooldown < 1.0f) {
            int arcRadius = size + gap + 5;
            int barWidth = (int) (16 * attackCooldown);
            int barY = arcRadius + 2;

            if (hasOutline) {
                context.fill(-9, barY - 1, 9, barY + 3, outlineColor);
            }
            context.fill(-8, barY, 8, barY + 2, 0x66000000);
            context.fill(-8, barY, -8 + barWidth, barY + 2, mainColor);
        }

        context.getMatrices().pop();
    }

    private static float getEffectiveScale() {
        if (!ModConfig.crosshairUseCustomScale) return 1.0f;
        String mode = ModConfig.crosshairScaleMode != null ? ModConfig.crosshairScaleMode.toUpperCase() : "NORMAL";
        return switch (mode) {
            case "SMALL" -> 0.75f;
            case "LARGE" -> 1.5f;
            case "AUTO" -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                int guiScale = mc != null ? mc.options.getGuiScale().getValue() : 2;
                yield guiScale >= 3 ? 0.8f : 1.2f;
            }
            default -> ModConfig.crosshairScaleFactor > 0.1f ? ModConfig.crosshairScaleFactor : 1.0f;
        };
    }

    private static void drawLine(
        DrawContext context,
        int x,
        int y,
        int w,
        int h,
        int color,
        boolean outline,
        int outlineColor
    ) {
        if (outline) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, outlineColor);
        }
        context.fill(x, y, x + w, y + h, color);
    }

    private static void drawDot(
        DrawContext context,
        int cx,
        int cy,
        int size,
        int color,
        boolean outline,
        int outlineColor
    ) {
        int half = size / 2;
        int x1 = cx - half;
        int y1 = cy - half;
        int x2 = x1 + size;
        int y2 = y1 + size;

        if (outline) {
            context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, outlineColor);
        }
        context.fill(x1, y1, x2, y2, color);
    }

    private static void drawSquare(
        DrawContext context,
        int cx,
        int cy,
        int radius,
        int thickness,
        int color,
        boolean outline,
        int outlineColor
    ) {
        int x1 = cx - radius;
        int y1 = cy - radius;
        int x2 = cx + radius;
        int y2 = cy + radius;

        if (outline) {
            context.fill(x1 - 1 - thickness, y1 - 1 - thickness, x2 + 1 + thickness, y1, outlineColor);
            context.fill(x1 - 1 - thickness, y2, x2 + 1 + thickness, y2 + 1 + thickness, outlineColor);
            context.fill(x1 - 1 - thickness, y1, x1, y2, outlineColor);
            context.fill(x2, y1, x2 + 1 + thickness, y2, outlineColor);
        }

        context.fill(x1 - thickness, y1 - thickness, x2 + thickness, y1, color);
        context.fill(x1 - thickness, y2, x2 + thickness, y2 + thickness, color);
        context.fill(x1 - thickness, y1, x1, y2, color);
        context.fill(x2, y1, x2 + thickness, y2, color);
    }

    private static void drawBoxWithFeet(
        DrawContext context,
        int cx,
        int cy,
        int radius,
        int thickness,
        int color,
        boolean outline,
        int outlineColor
    ) {
        drawSquare(context, cx, cy, radius, thickness, color, outline, outlineColor);
        // Feet at bottom left and bottom right (like the image screenshot!)
        int footLen = Math.max(2, radius / 3);
        int footOffset = radius - 2;
        drawLine(context, cx - footOffset, cy + radius, thickness, footLen, color, outline, outlineColor);
        drawLine(context, cx + footOffset - thickness + 1, cy + radius, thickness, footLen, color, outline, outlineColor);
    }

    private static void drawTargetLockBrackets(DrawContext context, int cx, int cy, int radius, int color) {
        int cornerLen = 4;
        int x1 = cx - radius;
        int y1 = cy - radius;
        int x2 = cx + radius;
        int y2 = cy + radius;

        // Outline background
        int out = 0xAA000000;
        // Top-left bracket
        context.fill(x1 - 1, y1 - 1, x1 + cornerLen + 1, y1 + 1, out);
        context.fill(x1 - 1, y1 - 1, x1 + 1, y1 + cornerLen + 1, out);
        // Top-right bracket
        context.fill(x2 - cornerLen - 1, y1 - 1, x2 + 1, y1 + 1, out);
        context.fill(x2 - 1, y1 - 1, x2 + 1, y1 + cornerLen + 1, out);
        // Bottom-left bracket
        context.fill(x1 - 1, y2 - 1, x1 + cornerLen + 1, y2 + 1, out);
        context.fill(x1 - 1, y2 - cornerLen - 1, x1 + 1, y2 + 1, out);
        // Bottom-right bracket
        context.fill(x2 - cornerLen - 1, y2 - 1, x2 + 1, y2 + 1, out);
        context.fill(x2 - 1, y2 - cornerLen - 1, x2 + 1, y2 + 1, out);

        // Core lines
        context.fill(x1, y1, x1 + cornerLen, y1 + 1, color);
        context.fill(x1, y1, x1 + 1, y1 + cornerLen, color);
        context.fill(x2 - cornerLen, y1, x2, y1 + 1, color);
        context.fill(x2 - 1, y1, x2, y1 + cornerLen, color);
        context.fill(x1, y2 - 1, x1 + cornerLen, y2, color);
        context.fill(x1, y2 - cornerLen, x1 + 1, y2, color);
        context.fill(x2 - cornerLen, y2 - 1, x2, y2, color);
        context.fill(x2 - 1, y2 - cornerLen, x2, y2, color);
    }

    private static void drawCircle(
        DrawContext context,
        int cx,
        int cy,
        int radius,
        int thickness,
        int color,
        boolean outline,
        int outlineColor
    ) {
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dy = -radius - 1; dy <= radius + 1; dy++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist >= radius - 0.5 && dist <= radius + thickness - 0.5) {
                    if (outline) {
                        context.fill(cx + dx - 1, cy + dy - 1, cx + dx + 2, cy + dy + 2, outlineColor);
                    }
                }
            }
        }
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dy = -radius - 1; dy <= radius + 1; dy++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist >= radius - 0.5 && dist <= radius + thickness - 0.5) {
                    context.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    private static void drawDiamond(
        DrawContext context,
        int cx,
        int cy,
        int radius,
        int thickness,
        int color,
        boolean outline,
        int outlineColor
    ) {
        for (int i = 0; i <= radius; i++) {
            int d = radius - i;
            drawDot(context, cx + i, cy - d, thickness, color, outline, outlineColor);
            drawDot(context, cx - i, cy - d, thickness, color, outline, outlineColor);
            drawDot(context, cx + i, cy + d, thickness, color, outline, outlineColor);
            drawDot(context, cx - i, cy + d, thickness, color, outline, outlineColor);
        }
    }

    private static void drawChevron(
        DrawContext context,
        int cx,
        int cy,
        int size,
        int thickness,
        int gap,
        int color,
        boolean outline,
        int outlineColor
    ) {
        int tipY = cy - gap;
        for (int i = 0; i < size; i++) {
            drawDot(context, cx - i, tipY + i, thickness, color, outline, outlineColor);
            drawDot(context, cx + i, tipY + i, thickness, color, outline, outlineColor);
        }
    }

    private static void drawCustomGrid(
        DrawContext context,
        int cx,
        int cy,
        int color,
        boolean outline,
        int outlineColor
    ) {
        boolean[] grid = ModConfig.crosshairGrid;
        if (grid == null || grid.length < GRID_SIZE * GRID_SIZE) {
            grid = getDefaultGrid();
            ModConfig.crosshairGrid = grid;
        }

        int half = GRID_SIZE / 2; // 7

        if (outline) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    if (grid[row * GRID_SIZE + col]) {
                        int px = cx + (col - half);
                        int py = cy + (row - half);
                        context.fill(px - 1, py - 1, px + 2, py + 2, outlineColor);
                    }
                }
            }
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row * GRID_SIZE + col]) {
                    int px = cx + (col - half);
                    int py = cy + (row - half);
                    context.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    public static boolean[] getDefaultGrid() {
        boolean[] grid = new boolean[GRID_SIZE * GRID_SIZE];
        int center = GRID_SIZE / 2; // 7
        // Default Plus Shape
        for (int i = 2; i < GRID_SIZE - 2; i++) {
            grid[i * GRID_SIZE + center] = true;
            grid[center * GRID_SIZE + i] = true;
        }
        grid[center * GRID_SIZE + center] = false; // center gap
        return grid;
    }

    public static boolean[] getBoxFeetTemplate() {
        boolean[] grid = new boolean[GRID_SIZE * GRID_SIZE];
        int min = 2, max = 12;
        for (int i = min; i <= max; i++) {
            grid[min * GRID_SIZE + i] = true; // top
            grid[max * GRID_SIZE + i] = true; // bottom
            grid[i * GRID_SIZE + min] = true; // left
            grid[i * GRID_SIZE + max] = true; // right
        }
        // Feet
        grid[13 * GRID_SIZE + (min + 2)] = true;
        grid[14 * GRID_SIZE + (min + 2)] = true;
        grid[13 * GRID_SIZE + (max - 2)] = true;
        grid[14 * GRID_SIZE + (max - 2)] = true;
        return grid;
    }

    public static boolean[] getCircleTemplate() {
        boolean[] grid = new boolean[GRID_SIZE * GRID_SIZE];
        int c = GRID_SIZE / 2;
        double r = 5.0;
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                double dist = Math.sqrt((x - c) * (x - c) + (y - c) * (y - c));
                if (dist >= r - 0.7 && dist <= r + 0.7) {
                    grid[y * GRID_SIZE + x] = true;
                }
            }
        }
        grid[c * GRID_SIZE + c] = true;
        return grid;
    }

    public static boolean[] getDiamondTemplate() {
        boolean[] grid = new boolean[GRID_SIZE * GRID_SIZE];
        int c = GRID_SIZE / 2;
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (Math.abs(x - c) + Math.abs(y - c) == 5) {
                    grid[y * GRID_SIZE + x] = true;
                }
            }
        }
        grid[c * GRID_SIZE + c] = true;
        return grid;
    }

    public static boolean[] getHeartTemplate() {
        boolean[] grid = new boolean[GRID_SIZE * GRID_SIZE];
        int c = GRID_SIZE / 2;
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                double nx = (x - c) / 5.0;
                double ny = (c - y) / 5.0;
                double eq = Math.pow(nx * nx + ny * ny - 1, 3) - nx * nx * ny * ny * ny;
                if (eq <= 0.05 && eq >= -0.2) {
                    grid[y * GRID_SIZE + x] = true;
                }
            }
        }
        return grid;
    }
}
