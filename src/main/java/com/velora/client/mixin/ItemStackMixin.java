package com.velora.client.mixin;

import com.velora.client.config.ModConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void velora_appendEnhancedTooltips(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (!ModConfig.showItemTooltips) return;

        List<Text> originalList = cir.getReturnValue();
        if (originalList == null) return;

        // Create mutable list to avoid UnsupportedOperationException on ImmutableCollections
        List<Text> tooltipList = new ArrayList<>(originalList);

        ItemStack stack = (ItemStack) (Object) this;

        // 1. Exact Durability
        if (ModConfig.tooltipShowDurability && stack.isDamageable() && stack.getMaxDamage() > 0) {
            int maxDur = stack.getMaxDamage();
            int curDur = maxDur - stack.getDamage();
            int percent = (int) Math.round(((double) curDur / maxDur) * 100);

            Formatting durColor = Formatting.GREEN;
            if (percent < 25) durColor = Formatting.RED;
            else if (percent < 50) durColor = Formatting.GOLD;
            else if (percent < 75) durColor = Formatting.YELLOW;

            tooltipList.add(Text.literal("Durability: ").formatted(Formatting.GRAY)
                .append(Text.literal(curDur + " / " + maxDur + " (" + percent + "%)").formatted(durColor)));
        }

        // 2. Food Nutrition / Saturation
        if (ModConfig.tooltipShowFood) {
            FoodComponent food = stack.get(DataComponentTypes.FOOD);
            if (food != null) {
                float saturation = food.saturation();
                int nutrition = food.nutrition();
                tooltipList.add(Text.literal("Food: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + nutrition + " Hunger ").formatted(Formatting.GOLD))
                    .append(Text.literal("⚡ +" + String.format("%.1f", saturation) + " Saturation").formatted(Formatting.YELLOW)));
            }
        }

        // 3. Item Identifier / Registry ID
        if (ModConfig.tooltipShowId) {
            Identifier id = Registries.ITEM.getId(stack.getItem());
            if (id != null) {
                tooltipList.add(Text.literal(id.toString()).formatted(Formatting.DARK_GRAY));
            }
        }

        cir.setReturnValue(tooltipList);
    }
}
