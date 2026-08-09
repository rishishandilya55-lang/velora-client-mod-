package com.velora.client.util;

import java.awt.Color;

public class HudColorHelper {

    public static int getRainbowColor() {
        float hue = (System.currentTimeMillis() % 4000L) / 4000.0f;
        int rgb = Color.HSBtoRGB(hue, 0.85f, 1.0f);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    public static int getEffectiveColor(int configColor, boolean rainbow) {
        if (rainbow) {
            return getRainbowColor();
        }
        return 0xFF000000 | (configColor & 0x00FFFFFF);
    }

    public static String getColorName(int color) {
        int c = color & 0x00FFFFFF;
        if (c == 0xFFFFFF) return "White";
        if (c == 0xFFD700) return "Gold";
        if (c == 0x55FF55) return "Lime";
        if (c == 0x55FFFF) return "Aqua";
        if (c == 0xFF55FF) return "Pink";
        if (c == 0xA78BFA) return "Purple";
        if (c == 0xFF5555) return "Red";
        if (c == 0xFFAA00) return "Orange";
        if (c == 0xFFFF55) return "Yellow";
        if (c == 0x00AAAA) return "Cyan";
        return String.format("#%06X", c);
    }

    public static int cycleColor(int currentColor) {
        int c = currentColor & 0x00FFFFFF;
        if (c == 0xFFFFFF) return 0xFFFFD700; // Gold
        if (c == 0xFFD700) return 0xFF55FF55; // Lime
        if (c == 0x55FF55) return 0xFF55FFFF; // Aqua
        if (c == 0x55FFFF) return 0xFFFF55FF; // Pink
        if (c == 0xFF55FF) return 0xFFA78BFA; // Purple
        if (c == 0xA78BFA) return 0xFFFF5555; // Red
        if (c == 0xFF5555) return 0xFFFFAA00; // Orange
        if (c == 0xFFAA00) return 0xFFFFFF55; // Yellow
        if (c == 0xFFFF55) return 0xFF00AAAA; // Cyan
        return 0xFFFFFFFF;                    // White
    }
}
