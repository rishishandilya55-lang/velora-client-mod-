package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class BlockInfoMod implements ClientModInitializer {
    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showBlockInfo) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || client.crosshairTarget == null || client.options.hudHidden) return;

            if (client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                BlockState state = client.world.getBlockState(hitResult.getBlockPos());
                String blockName = state.getBlock().getName().getString();

                TextRenderer textRenderer = client.textRenderer;

                drawContext.getMatrices().push();
                drawContext.getMatrices().scale(ModConfig.blockInfoScale, ModConfig.blockInfoScale, 1.0f);

                int x = (int) (ModConfig.blockInfoX / ModConfig.blockInfoScale);
                int y = (int) (ModConfig.blockInfoY / ModConfig.blockInfoScale);

                int textWidth = textRenderer.getWidth(blockName);
                drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
                drawContext.drawText(textRenderer, blockName, x, y, 0xFFFFFFFF, true);

                drawContext.getMatrices().pop();
            }
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
