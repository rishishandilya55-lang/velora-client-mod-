package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class NameTagMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void velora_customNametag(
            PlayerEntityRenderState state,
            Text name,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || mc.world == null) return;
            if (!ModConfig.showNametag) return;

            String playerName = state.name != null ? state.name : name.getString();
            RankSystem.Rank rank = RankSystem.getRankForPlayer(playerName);
            if (rank == RankSystem.Rank.DEFAULT) return;

            Vec3d nameLabelPos = state.nameLabelPos;
            if (nameLabelPos == null) return;

            ci.cancel();

            boolean sneaking = state.sneaking;
            TextRenderer textRenderer = mc.textRenderer;

            String rankPrefix = "[" + rank.getDisplayName() + "] ";
            Text rankText = Text.literal(rankPrefix).withColor(rank.getColor());

            int rankWidth = textRenderer.getWidth(rankPrefix);
            int nameWidth = textRenderer.getWidth(playerName);
            int totalWidth = rankWidth + nameWidth;

            float textX = -totalWidth / 2.0f;
            float textY = sneaking ? -18.0f : 0.0f;

            int bgOpacity = (int) (mc.options.getTextBackgroundOpacity(0.25f) * 255.0f);
            int bgColor = bgOpacity << 24;

            matrices.push();
            matrices.translate(nameLabelPos.x, nameLabelPos.y + 0.5, nameLabelPos.z);
            matrices.multiply(mc.gameRenderer.getCamera().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);

            Matrix4f mat = matrices.peek().getPositionMatrix();

            textRenderer.draw(
                    rankText,
                    textX,
                    textY,
                    -1,
                    false,
                    mat,
                    vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    bgColor,
                    light
            );

            textRenderer.draw(
                    playerName,
                    textX + rankWidth,
                    textY,
                    -1,
                    false,
                    mat,
                    vertexConsumers,
                    sneaking ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                    bgColor,
                    light
            );

            if (sneaking) {
                textRenderer.draw(
                        Text.literal(playerName),
                        textX + rankWidth,
                        textY,
                        -1,
                        false,
                        mat,
                        vertexConsumers,
                        TextRenderer.TextLayerType.NORMAL,
                        0,
                        light
                );
            }

            Identifier badgeTex = ModConfig.nametagShowBadge ? RankSystem.getBadgeTexture(rank) : null;
            if (badgeTex != null) {
                matrices.push();
                matrices.translate(textX - 12, textY - 1, 0);
                matrices.scale(0.06f, 0.06f, 1.0f);
                VertexConsumer badgeConsumer = vertexConsumers.getBuffer(RenderLayer.getText(badgeTex));
                Matrix4f badgeMat = matrices.peek().getPositionMatrix();
                badgeConsumer.vertex(badgeMat, 0, 16, 0).color(255, 255, 255, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light);
                badgeConsumer.vertex(badgeMat, 16, 16, 0).color(255, 255, 255, 255).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light);
                badgeConsumer.vertex(badgeMat, 16, 0, 0).color(255, 255, 255, 255).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light);
                badgeConsumer.vertex(badgeMat, 0, 0, 0).color(255, 255, 255, 255).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light);
                matrices.pop();
            }

            matrices.pop();
        } catch (Exception e) {
            LOGGER.error("[Velora] NameTag rendering failed", e);
        }
    }
}
