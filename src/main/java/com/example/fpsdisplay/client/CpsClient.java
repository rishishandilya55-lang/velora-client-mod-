package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CpsClient implements ClientModInitializer {
    private static final List<Long> leftclicks = Collections.synchronizedList(new ArrayList<>());
    private static final List<Long> rightclicks = Collections.synchronizedList(new ArrayList<>());

    public static void registerLeftClick() {
        leftclicks.add(System.currentTimeMillis());
    }

    public static void registerRightClick() {
        rightclicks.add(System.currentTimeMillis());
    }

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showCps) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            long time = System.currentTimeMillis();
            int leftCps;
            int rightCps;
            synchronized (leftclicks) {
                leftclicks.removeIf(clickTime -> clickTime < time - 1000);
                leftCps = leftclicks.size();
            }
            synchronized (rightclicks) {
                rightclicks.removeIf(clickTime -> clickTime < time - 1000);
                rightCps = rightclicks.size();
            }

            String cpsText;
            if (ModConfig.showRightCps) {
                cpsText = "CPS: " + leftCps + " | " + rightCps;
            } else {
                cpsText = "CPS: " + leftCps;
            }

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.cpsScale, ModConfig.cpsScale, 1.0f);

            int x = (int) (ModConfig.cpsX / ModConfig.cpsScale);
            int y = (int) (ModConfig.cpsY / ModConfig.cpsScale);

            int textWidth = textRenderer.getWidth(cpsText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, cpsText, x, y, 0xFFFFFFFF, true);

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
