package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
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

            long timeOfDay = client.world.getTimeOfDay();
            long day = (timeOfDay / 24000L) + 1;

            StringBuilder sb = new StringBuilder("Day: ").append(day);
            if (ModConfig.dayShowTime) {
                long dayTicks = (timeOfDay % 24000L + 6000L) % 24000L;
                int hours = (int) (dayTicks / 1000L);
                int minutes = (int) ((dayTicks % 1000L) * 60 / 1000L);
                sb.append(String.format(" (%02d:%02d)", hours, minutes));
            }

            String dayText = sb.toString();

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.dayScale, ModConfig.dayScale, 1.0f);

            int x = (int) (ModConfig.dayX / ModConfig.dayScale);
            int y = (int) (ModConfig.dayY / ModConfig.dayScale);

            int textWidth = textRenderer.getWidth(dayText);
            if (ModConfig.dayBackground && ModConfig.hudShowBackground) {
                int bg = (ModConfig.hudBackgroundOpacity << 24) | 0x000000;
                drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, bg);
            }

            int color = HudColorHelper.getEffectiveColor(ModConfig.dayTextColor, ModConfig.dayTextRainbow);
            drawContext.drawText(textRenderer, dayText, x, y, color, ModConfig.hudTextShadow);

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
