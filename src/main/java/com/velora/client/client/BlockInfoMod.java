package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockInfoMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] Block info HUD registered");
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showBlockInfo) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || client.crosshairTarget == null || client.options.hudHidden) return;

            if (client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                BlockState state = client.world.getBlockState(hitResult.getBlockPos());
                String blockName = state.getBlock().getName().getString();

                StringBuilder sb = new StringBuilder(blockName);

                if (ModConfig.blockInfoShowTool) {
                    String toolHint = getHarvestToolName(state);
                    if (!toolHint.isEmpty()) {
                        sb.append(" [").append(toolHint).append("]");
                    }
                }

                String displayText = sb.toString();

                TextRenderer textRenderer = client.textRenderer;

                drawContext.getMatrices().push();
                drawContext.getMatrices().scale(ModConfig.blockInfoScale, ModConfig.blockInfoScale, 1.0f);

                int x = (int) (ModConfig.blockInfoX / ModConfig.blockInfoScale);
                int y = (int) (ModConfig.blockInfoY / ModConfig.blockInfoScale);

                int textWidth = textRenderer.getWidth(displayText);
                if (ModConfig.blockInfoBackground && ModConfig.hudShowBackground) {
                    int bg = (ModConfig.hudBackgroundOpacity << 24) | 0x000000;
                    drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, bg);
                }

                int color = HudColorHelper.getEffectiveColor(ModConfig.blockInfoTextColor, ModConfig.blockInfoTextRainbow);
                drawContext.drawText(textRenderer, displayText, x, y, color, ModConfig.hudTextShadow);

                drawContext.getMatrices().pop();
            }
        });
    }

    private static String getHarvestToolName(BlockState state) {
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) return "Pickaxe ⛏";
        if (state.isIn(BlockTags.AXE_MINEABLE)) return "Axe 🪓";
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) return "Shovel ⛏";
        if (state.isIn(BlockTags.HOE_MINEABLE)) return "Hoe";
        return "";
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
