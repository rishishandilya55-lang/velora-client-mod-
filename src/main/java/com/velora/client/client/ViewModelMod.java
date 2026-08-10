package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewModelMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] View Model & Item Scale mod initialized");
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    /**
     * Gets the effective composite scale (hand scale * item-specific scale).
     */
    public static float getEffectiveScale(ItemStack stack, Hand hand) {
        if (!ModConfig.showViewModel) return 1.0f;
        float handScale = (hand == Hand.MAIN_HAND) ? ModConfig.viewModelMainHandScale : ModConfig.viewModelOffHandScale;
        float itemScale = ModConfig.getItemScale(stack);
        return Math.max(0.01f, handScale * itemScale);
    }
}
