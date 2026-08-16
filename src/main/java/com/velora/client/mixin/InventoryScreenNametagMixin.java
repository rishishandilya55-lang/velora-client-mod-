package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenNametagMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Redirect(
        method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V"
        )
    )
    private static void velora_drawEntityWithNametag(DrawContext context, int x1, int y1, int x2, int y2, int size, float alpha, float mouseX, float mouseY, LivingEntity entity) {
        InventoryScreen.drawEntity(context, x1, y1, x2, y2, size, alpha, mouseX, mouseY, entity);
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            if (!ModConfig.showNametag) return;

            String username = mc.getSession().getUsername();
            RankSystem.Rank rank = RankSystem.getRankForPlayer(username);
            if (rank == RankSystem.Rank.DEFAULT) return;

            drawPreviewLabel(context, mc, username, rank, x1 + 25, y1 - 4);
        } catch (Exception e) {
            LOGGER.error("[Velora] Inventory nametag rendering failed", e);
        }
    }

    private static boolean labelLogged = false;

    private static void drawPreviewLabel(DrawContext context, MinecraftClient mc, String username, RankSystem.Rank rank, int centerX, int boxBottom) {
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
        int boxTop = boxBottom - boxHeight;

        context.fill(boxX - 1, boxTop - 1, boxX + boxWidth + 1, boxBottom + 1, 0xFF000000);
        context.fill(boxX, boxTop, boxX + boxWidth, boxBottom, 0x99000000);

        int cursor = boxX + boxPadding;
        if (badgeTex != null) {
            context.drawTexture(RenderLayer::getGuiTextured, badgeTex, cursor, boxTop + 1, 0.0f, 0.0f, 16, 16, 16, 16);
            cursor += 18;
        }

        context.drawTextWithShadow(textRenderer, rankPrefix, cursor, boxTop + 5, rank.getColor());
        cursor += rankWidth;
        context.drawTextWithShadow(textRenderer, username, cursor, boxTop + 5, 0xFFFFFFFF);

        if (!labelLogged) {
            labelLogged = true;
            LOGGER.info("[Velora] Inventory nametag drawn for {} at ({},{})", username, centerX, boxBottom);
        }
    }
}
