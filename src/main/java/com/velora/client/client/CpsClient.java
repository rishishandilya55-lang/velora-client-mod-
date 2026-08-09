package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CpsClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    private static final List<Long> leftclicks = Collections.synchronizedList(new ArrayList<>());
    private static final List<Long> rightclicks = Collections.synchronizedList(new ArrayList<>());

    public static void registerLeftClick() {
        leftclicks.add(System.currentTimeMillis());
    }

    public static void registerRightClick() {
        rightclicks.add(System.currentTimeMillis());
    }

    public static void init() {
        LOGGER.info("[Velora] CPS HUD registered");
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

            String cpsText = ModConfig.showRightCps
                ? "CPS: " + leftCps + " | " + rightCps
                : "CPS: " + leftCps;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.cpsScale, ModConfig.cpsScale, 1.0f);

            int x = (int) (ModConfig.cpsX / ModConfig.cpsScale);
            int y = (int) (ModConfig.cpsY / ModConfig.cpsScale);

            int textWidth = textRenderer.getWidth(cpsText);
            if (ModConfig.cpsBackground && ModConfig.hudShowBackground) {
                int bg = (ModConfig.hudBackgroundOpacity << 24) | 0x000000;
                drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, bg);
            }

            int color = HudColorHelper.getEffectiveColor(ModConfig.cpsTextColor, ModConfig.cpsTextRainbow);
            drawContext.drawText(textRenderer, cpsText, x, y, color, ModConfig.hudTextShadow);

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
