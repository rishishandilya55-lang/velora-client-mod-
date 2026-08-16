package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class NametagHudMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
        at = @At("TAIL")
    )
    private void velora_renderSelfNametag(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;
            if (!ModConfig.showNametag) return;
            if (!mc.options.getPerspective().isFirstPerson()) return;

            String username = mc.getSession().getUsername();
            RankSystem.Rank rank = RankSystem.getRankForPlayer(username);
            if (rank == RankSystem.Rank.DEFAULT) return;

            int centerX = context.getScaledWindowWidth() / 2;
            drawRankedNametag(context, mc, username, rank, centerX, 10);
        } catch (Exception e) {
            LOGGER.error("[Velora] Self nametag rendering failed", e);
        }
    }

    private static void drawRankedNametag(DrawContext context, MinecraftClient mc, String username, RankSystem.Rank rank, int centerX, int topY) {
        TextRenderer textRenderer = mc.textRenderer;

        String rankPrefix = "[" + rank.getDisplayName() + "] ";
        int rankWidth = textRenderer.getWidth(rankPrefix);
        int nameWidth = textRenderer.getWidth(username);

        Identifier badgeTex = ModConfig.nametagShowBadge ? RankSystem.getBadgeTexture(rank) : null;
        int badgeArea = badgeTex != null ? 18 : 0;
        int textWidth = rankWidth + nameWidth;
        int boxPadding = 4;
        int boxWidth = badgeArea + textWidth + boxPadding * 2;
        int boxHeight = 18;
        int boxX = centerX - boxWidth / 2;

        context.fill(boxX, topY, boxX + boxWidth, topY + boxHeight, 0x66000000);

        int cursor = boxX + boxPadding;
        if (badgeTex != null) {
            context.drawTexture(RenderLayer::getGuiTextured, badgeTex, cursor, topY + 1, 0.0f, 0.0f, 16, 16, 16, 16);
            cursor += 18;
        }

        context.drawTextWithShadow(textRenderer, rankPrefix, cursor, topY + 5, rank.getColor());
        cursor += rankWidth;
        context.drawTextWithShadow(textRenderer, username, cursor, topY + 5, 0xFFFFFFFF);
    }
}
