package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorMod implements ClientModInitializer {

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showArmorStatus) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            renderArmorHud(drawContext, client);
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    public static void renderArmorHud(DrawContext drawContext, MinecraftClient client) {
        if (client.player == null) return;
        TextRenderer textRenderer = client.textRenderer;
        PlayerInventory inventory = client.player.getInventory();

        List<ItemStack> items = getEquippedItems(client, inventory);
        if (items.isEmpty()) return;

        drawContext.getMatrices().push();
        drawContext.getMatrices().scale(ModConfig.armorScale, ModConfig.armorScale, 1.0f);

        int baseX = (int) (ModConfig.armorX / ModConfig.armorScale);
        int baseY = (int) (ModConfig.armorY / ModConfig.armorScale);

        boolean isHorizontal = "HORIZONTAL".equalsIgnoreCase(ModConfig.armorOrientation);
        boolean isCompact = "COMPACT".equalsIgnoreCase(ModConfig.armorBackgroundStyle);
        boolean isTransparent = "TRANSPARENT".equalsIgnoreCase(ModConfig.armorBackgroundStyle);

        int currX = baseX;
        int currY = baseY;

        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time / 150.0) * 0.5 + 0.5);

        for (ItemStack stack : items) {
            String labelText;
            int textColor;
            float durRatio = 1.0f;
            boolean isDamageable = stack.isDamageable();

            if (isDamageable) {
                int maxDamage = stack.getMaxDamage();
                int currentDur = maxDamage - stack.getDamage();
                durRatio = (float) currentDur / (float) maxDamage;

                if (durRatio > 0.60f) {
                    textColor = 0xFF22C55E; // Emerald Green
                } else if (durRatio > 0.30f) {
                    textColor = 0xFFFACC15; // Amber Gold
                } else if (durRatio > 0.15f) {
                    textColor = 0xFFF97316; // Vibrant Orange
                } else {
                    int alpha = (int) (180 + pulse * 75);
                    textColor = (alpha << 24) | 0xEF4444; // Pulsing Red
                }

                if ("PERCENT".equalsIgnoreCase(ModConfig.armorDurabilityMode)) {
                    labelText = (int) (durRatio * 100) + "%";
                } else if ("VALUE".equalsIgnoreCase(ModConfig.armorDurabilityMode)) {
                    labelText = String.valueOf(currentDur);
                } else {
                    labelText = currentDur + "/" + maxDamage;
                }
            } else {
                int totalCount = stack.getCount();
                if (ModConfig.armorShowCount) {
                    totalCount = countTotalItemsInInventory(inventory, stack);
                }
                labelText = "x" + totalCount;
                textColor = 0xFF38BDF8; // Soft Cyan
            }

            int textWidth = textRenderer.getWidth(labelText);

            if (isCompact) {
                // Compact Mode: 20x20 slot with durability bar overlay
                int itemW = 20;
                int itemH = 20;

                drawContext.fill(currX, currY, currX + itemW, currY + itemH, 0x88111122);
                drawContext.fill(currX, currY, currX + itemW, currY + 1, 0x55A855F7);
                drawContext.drawItem(stack, currX + 2, currY + 2);

                if (isDamageable) {
                    int barW = 16;
                    int fillW = Math.max(1, Math.round(barW * durRatio));
                    drawContext.fill(currX + 2, currY + 17, currX + 2 + barW, currY + 19, 0xFF1E293B);
                    drawContext.fill(currX + 2, currY + 17, currX + 2 + fillW, currY + 19, textColor | 0xFF000000);
                }

                if (isHorizontal) {
                    currX += itemW + 4;
                } else {
                    currY += itemH + 4;
                }
            } else {
                // Modern / Transparent Mode
                int itemW = 20 + textWidth + 8;
                int itemH = 20;

                if (!isTransparent) {
                    // Modern Dark Panel
                    drawContext.fill(currX, currY, currX + itemW, currY + itemH, 0xAA0F172A);
                    drawContext.fill(currX, currY, currX + itemW, currY + 1, 0x66A855F7);
                    drawContext.fill(currX, currY + itemH - 1, currX + itemW, currY + itemH, 0x33A855F7);
                }

                drawContext.drawItem(stack, currX + 2, currY + 2);

                if (isDamageable) {
                    int barW = 16;
                    int fillW = Math.max(1, Math.round(barW * durRatio));
                    drawContext.fill(currX + 2, currY + 17, currX + 2 + barW, currY + 19, 0xFF1E293B);
                    drawContext.fill(currX + 2, currY + 17, currX + 2 + fillW, currY + 19, textColor | 0xFF000000);
                }

                drawContext.drawText(textRenderer, labelText, currX + 20, currY + 6, textColor, true);

                if (isHorizontal) {
                    currX += itemW + 4;
                } else {
                    currY += itemH + 4;
                }
            }
        }

        drawContext.getMatrices().pop();
    }

    public static List<ItemStack> getEquippedItems(MinecraftClient client, PlayerInventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        if (inventory == null) return items;

        // Top to Bottom: Helmet (39), Chestplate (38), Leggings (37), Boots (36)
        ItemStack helmet = inventory.getStack(39);
        ItemStack chest = inventory.getStack(38);
        ItemStack legs = inventory.getStack(37);
        ItemStack boots = inventory.getStack(36);

        if (!helmet.isEmpty()) items.add(helmet);
        if (!chest.isEmpty()) items.add(chest);
        if (!legs.isEmpty()) items.add(legs);
        if (!boots.isEmpty()) items.add(boots);

        // Hand items
        if (client.player != null) {
            ItemStack mainhand = client.player.getMainHandStack();
            if (!mainhand.isEmpty()) items.add(mainhand);

            if (ModConfig.armorShowOffhand) {
                ItemStack offhand = client.player.getOffHandStack();
                if (!offhand.isEmpty()) items.add(offhand);
            }
        }

        return items;
    }

    private static int countTotalItemsInInventory(PlayerInventory inventory, ItemStack target) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, target)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int getEquippedItemCount() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return 4;
        return Math.max(1, getEquippedItems(client, client.player.getInventory()).size());
    }

    public static int getArmorWidth() {
        boolean isHorizontal = "HORIZONTAL".equalsIgnoreCase(ModConfig.armorOrientation);
        boolean isCompact = "COMPACT".equalsIgnoreCase(ModConfig.armorBackgroundStyle);
        int count = getEquippedItemCount();

        if (isCompact) {
            return isHorizontal ? (count * 24) : 24;
        }

        return isHorizontal ? (count * 72) : 76;
    }

    public static int getArmorHeight() {
        boolean isHorizontal = "HORIZONTAL".equalsIgnoreCase(ModConfig.armorOrientation);
        boolean isCompact = "COMPACT".equalsIgnoreCase(ModConfig.armorBackgroundStyle);
        int count = getEquippedItemCount();

        if (isCompact) {
            return isHorizontal ? 24 : (count * 24);
        }

        return isHorizontal ? 24 : (count * 24);
    }
}
