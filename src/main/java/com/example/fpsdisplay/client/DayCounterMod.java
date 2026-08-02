package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class DayCounterMod implements ClientModInitializer {
    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showDayCounter) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            long day = client.world.getTimeOfDay() / 24000L;
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
