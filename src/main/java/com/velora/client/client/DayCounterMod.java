package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DayCounterMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Day counter HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showDayCounter) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            long day = client.world.getTimeOfDay() / 24000L + 1;
            LOGGER.trace("[Velora] Day calculation: day={}", day);
            String dayText = "Day: " + day;

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.dayScale, ModConfig.dayScale, 1.0f);

            int x = (int) (ModConfig.dayX / ModConfig.dayScale);
            int y = (int) (ModConfig.dayY / ModConfig.dayScale);

            int textWidth = textRenderer.getWidth(dayText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, dayText, x, y, 0xFFFFFFFF, true);

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
