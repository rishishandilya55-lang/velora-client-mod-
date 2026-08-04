package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class KeystrokesClient implements ClientModInitializer {
    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showKeystrokes) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            // Apply Scale Transform to actual rendering!
            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.keystrokesScale, ModConfig.keystrokesScale, 1.0f);

            int x = (int) (ModConfig.keystrokesX / ModConfig.keystrokesScale);
            int y = (int) (ModConfig.keystrokesY / ModConfig.keystrokesScale);

            // 1. Key & Mouse Press States
            boolean wPressed = client.options.forwardKey.isPressed();
            boolean aPressed = client.options.leftKey.isPressed();
            boolean sPressed = client.options.backKey.isPressed();
            boolean dPressed = client.options.rightKey.isPressed();
            boolean spacePressed = client.options.jumpKey.isPressed();
            boolean lmbPressed = client.options.attackKey.isPressed();
            boolean rmbPressed = client.options.useKey.isPressed();

            TextRenderer textRenderer = client.textRenderer;

            // 2. Dynamic Opacity Background Color
            int alpha = (ModConfig.keystrokesOpacity & 0xFF) << 24;
            int defaultBg = alpha | 0x000000;

            int wColor = wPressed ? 0xFFFFFFFF : defaultBg;
            int aColor = aPressed ? 0xFFFFFFFF : defaultBg;
            int sColor = sPressed ? 0xFFFFFFFF : defaultBg;
            int dColor = dPressed ? 0xFFFFFFFF : defaultBg;
            int spaceColor = spacePressed ? 0xFFFFFFFF : defaultBg;
            int lmbColor = lmbPressed ? 0xFFFFFFFF : defaultBg;
            int rmbColor = rmbPressed ? 0xFFFFFFFF : defaultBg;

            // 3. W Key
            drawContext.fill(x + 17, y, x + 32, y + 15, wColor);
            drawContext.drawText(textRenderer, "W", x + 22, y + 4, wPressed ? 0xFF000000 : 0xFFFFFFFF, false);

            // 4. A Key
            drawContext.fill(x, y + 17, x + 15, y + 32, aColor);
            drawContext.drawText(textRenderer, "A", x + 5, y + 21, aPressed ? 0xFF000000 : 0xFFFFFFFF, false);

            // 5. S Key
            drawContext.fill(x + 17, y + 17, x + 32, y + 32, sColor);
            drawContext.drawText(textRenderer, "S", x + 22, y + 21, sPressed ? 0xFF000000 : 0xFFFFFFFF, false);

            // 6. D Key
            drawContext.fill(x + 34, y + 17, x + 49, y + 32, dColor);
            drawContext.drawText(textRenderer, "D", x + 39, y + 21, dPressed ? 0xFF000000 : 0xFFFFFFFF, false);

            // 7. Space Bar
            drawContext.fill(x, y + 34, x + 49, y + 47, spaceColor);
            int spaceBarLineColor = spacePressed ? 0xFF000000 : 0xFFFFFFFF;
            drawContext.fill(x + 10, y + 39, x + 39, y + 41, spaceBarLineColor);

            // 8. Mouse Strokes (LMB & RMB Boxes)
            if (ModConfig.showMouseStrokes) {
                drawContext.fill(x, y + 50, x + 23, y + 65, lmbColor);
                drawContext.drawText(textRenderer, "LMB", x + 3, y + 54, lmbPressed ? 0xFF000000 : 0xFFFFFFFF, false);

                drawContext.fill(x + 26, y + 50, x + 49, y + 65, rmbColor);
                drawContext.drawText(textRenderer, "RMB", x + 29, y + 54, rmbPressed ? 0xFF000000 : 0xFFFFFFFF, false);
            }

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
