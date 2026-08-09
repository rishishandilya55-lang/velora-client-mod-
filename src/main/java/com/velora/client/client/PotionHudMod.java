package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionHudMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Potion HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showPotionHud) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
            if (effects.isEmpty()) return;

            List<StatusEffectInstance> sortedEffects = new ArrayList<>(effects);
            sortedEffects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration));

            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.potionHudScale, ModConfig.potionHudScale, 1.0f);

            int startX = (int) (ModConfig.potionHudX / ModConfig.potionHudScale);
            int startY = (int) (ModConfig.potionHudY / ModConfig.potionHudScale);

            renderPotionList(drawContext, client, sortedEffects, startX, startY);

            drawContext.getMatrices().pop();
        });
    }

    private static void renderPotionList(
        DrawContext drawContext,
        MinecraftClient client,
        List<StatusEffectInstance> effects,
        int x,
        int y
    ) {
        TextRenderer textRenderer = client.textRenderer;
        StatusEffectSpriteManager spriteManager = client.getStatusEffectSpriteManager();
        int curY = y;
        int rowHeight = 22;

        int textColor = HudColorHelper.getEffectiveColor(ModConfig.potionHudTextColor, ModConfig.potionHudTextRainbow);
        int bgAlpha = Math.max(0, Math.min(255, ModConfig.hudBackgroundOpacity));
        int bgColor = (bgAlpha << 24) | 0x000000;

        for (StatusEffectInstance instance : effects) {
            RegistryEntry<StatusEffect> effectEntry = instance.getEffectType();
            StatusEffect effect = effectEntry.value();

            String name = effect.getName().getString();
            int amp = instance.getAmplifier();
            String ampStr = (amp > 0) ? " " + toRoman(amp + 1) : "";
            String title = name + ampStr;

            int durationTicks = instance.getDuration();
            int totalSec = durationTicks / 20;
            int min = totalSec / 60;
            int sec = totalSec % 60;
            String durStr = instance.isInfinite() ? "**:**" : String.format("%02d:%02d", min, sec);

            int titleW = textRenderer.getWidth(title);
            int durW = textRenderer.getWidth(durStr);
            int textW = Math.max(titleW, durW);
            int rowW = 22 + textW + 6;

            // Background plate
            if (ModConfig.potionHudBackground && ModConfig.hudShowBackground) {
                drawContext.fill(x - 2, curY - 2, x + rowW, curY + 20, bgColor);
            }

            // Sprite Icon
            if (ModConfig.potionHudShowIcon && spriteManager != null) {
                Sprite sprite = spriteManager.getSprite(effectEntry);
                if (sprite != null) {
                    drawContext.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x, curY + 1, 18, 18);
                }
            }

            int textX = ModConfig.potionHudShowIcon ? x + 22 : x + 2;

            // Potion Name
            drawContext.drawText(textRenderer, title, textX, curY + 1, textColor, ModConfig.hudTextShadow);

            // Duration (Red if flashing / low time, otherwise subtle gray)
            int durColor = (durationTicks <= 200 && (durationTicks % 20 < 10)) ? 0xFFEF4444 : 0xFFA1A1AA;
            drawContext.drawText(textRenderer, durStr, textX, curY + 11, durColor, ModConfig.hudTextShadow);

            curY += rowHeight;
        }
    }

    /**
     * Renders sample preview potion effects for the HUD editor
     */
    public static void renderPreview(DrawContext drawContext, int x, int y, TextRenderer textRenderer) {
        String[][] previewData = new String[][] {
            {"Speed II", "01:45", "0xFF38BDF8"},
            {"Strength I", "02:30", "0xFFEF4444"},
            {"Fire Resistance", "03:15", "0xFFFB923C"}
        };

        int curY = y;
        int rowHeight = 22;
        int textColor = HudColorHelper.getEffectiveColor(ModConfig.potionHudTextColor, ModConfig.potionHudTextRainbow);
        int bgAlpha = Math.max(0, Math.min(255, ModConfig.hudBackgroundOpacity));
        int bgColor = (bgAlpha << 24) | 0x000000;

        for (String[] data : previewData) {
            String title = data[0];
            String durStr = data[1];
            int dotColor = (int) Long.parseLong(data[2].substring(2), 16) | 0xFF000000;

            int titleW = textRenderer.getWidth(title);
            int durW = textRenderer.getWidth(durStr);
            int textW = Math.max(titleW, durW);
            int rowW = 22 + textW + 6;

            if (ModConfig.potionHudBackground && ModConfig.hudShowBackground) {
                drawContext.fill(x - 2, curY - 2, x + rowW, curY + 20, bgColor);
            }

            // Preview icon (bullet / potion badge)
            drawContext.fill(x + 2, curY + 3, x + 16, curY + 17, 0x33FFFFFF);
            drawContext.fill(x + 5, curY + 6, x + 13, curY + 14, dotColor);

            int textX = x + 22;
            drawContext.drawText(textRenderer, title, textX, curY + 1, textColor, ModConfig.hudTextShadow);
            drawContext.drawText(textRenderer, durStr, textX, curY + 11, 0xFFA1A1AA, ModConfig.hudTextShadow);

            curY += rowHeight;
        }
    }

    public static int getPreviewWidth() {
        return 110;
    }

    public static int getPreviewHeight() {
        return 66;
    }

    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
