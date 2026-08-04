package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public class FullbrightClient {

    private static boolean wasEnabled = false;
    private static double savedGamma = 0.0;

    public static void init() {
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;

        if (ModConfig.showFullbright && !wasEnabled) {
            savedGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(ModConfig.fullbrightGamma);
            wasEnabled = true;
        } else if (!ModConfig.showFullbright && wasEnabled) {
            mc.options.getGamma().setValue(savedGamma);
            wasEnabled = false;
        } else if (ModConfig.showFullbright && wasEnabled) {
            mc.options.getGamma().setValue(ModConfig.fullbrightGamma);
        }
    }

    public static void onToggleOff() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.options != null && wasEnabled) {
            mc.options.getGamma().setValue(savedGamma);
            wasEnabled = false;
        }
    }
}
