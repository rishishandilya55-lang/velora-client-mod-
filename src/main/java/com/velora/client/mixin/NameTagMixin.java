package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class NameTagMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    private static boolean selfDrawnLogged = false;
    private static long lastSelfLog = 0;

    @Inject(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
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
            if (state.nameLabelPos == null) return;

            String username = state.playerName != null ? state.playerName.getString() : name.getString();
            RankSystem.Rank rank = RankSystem.getRankForPlayer(username);
            if (rank == RankSystem.Rank.DEFAULT) return;

            ci.cancel();
            renderRankedNametag(state, username, rank, matrices, vertexConsumers, light);

            if (username.equals(mc.getSession().getUsername())) {
                if (!selfDrawnLogged) {
                    selfDrawnLogged = true;
                    LOGGER.info("[Velora] Self ranked nametag drawn for {}", username);
                }
                long now = System.currentTimeMillis();
                if (now - lastSelfLog > 3000) {
                    lastSelfLog = now;
                    Vec3d labelWorld = new Vec3d(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
                    Vec3d camPos = mc.gameRenderer.getCamera().getPos();
                    Vector3f view = new Vector3f((float) (labelWorld.x - camPos.x), (float) (labelWorld.y - camPos.y), (float) (labelWorld.z - camPos.z));
                    view.rotate(mc.gameRenderer.getCamera().getRotation().conjugate());
                    LOGGER.info("[Velora] Self label world={} camView={} perspective={}", labelWorld, view, mc.options.getPerspective());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Velora] NameTag rendering failed", e);
        }
    }

    private static void renderRankedNametag(
            PlayerEntityRenderState state,
            String username,
            RankSystem.Rank rank,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d nameLabelPos = state.nameLabelPos;
        boolean sneaking = state.sneaking;
        TextRenderer textRenderer = mc.textRenderer;

        String rankPrefix = "[" + rank.getDisplayName() + "] ";
        Text rankText = Text.literal(rankPrefix).withColor(rank.getColor());
        int rankWidth = textRenderer.getWidth(rankPrefix);
        int nameWidth = textRenderer.getWidth(username);
        float textX = -(rankWidth + nameWidth) / 2.0f;
        float textY = sneaking ? -18.0f : 0.0f;

        int bgOpacity = (int) (mc.options.getTextBackgroundOpacity(0.25f) * 255.0f);
        int bgColor = bgOpacity << 24;
        TextRenderer.TextLayerType layerType = sneaking
                ? TextRenderer.TextLayerType.SEE_THROUGH
                : TextRenderer.TextLayerType.NORMAL;

        matrices.push();
        matrices.translate(nameLabelPos.x, nameLabelPos.y + 0.5, nameLabelPos.z);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        Matrix4f mat = matrices.peek().getPositionMatrix();

        textRenderer.draw(rankText, textX, textY, -1, false, mat, vertexConsumers, layerType, bgColor, light);
        textRenderer.draw(username, textX + rankWidth, textY, -1, false, mat, vertexConsumers, layerType, bgColor, light);

        if (sneaking) {
            textRenderer.draw(rankText, textX, textY, -1, false, mat, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            textRenderer.draw(username, textX + rankWidth, textY, -1, false, mat, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }

        Identifier badgeTex = ModConfig.nametagShowBadge ? RankSystem.getBadgeTexture(rank) : null;
        if (badgeTex != null) {
            float badgeX = textX - 18.0f;
            float badgeY = textY - 4.0f;
            VertexConsumer badgeConsumer = vertexConsumers.getBuffer(RenderLayer.getText(badgeTex));
            badgeConsumer.vertex(mat, badgeX, badgeY + 16.0f, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).light(light);
            badgeConsumer.vertex(mat, badgeX + 16.0f, badgeY + 16.0f, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).light(light);
            badgeConsumer.vertex(mat, badgeX + 16.0f, badgeY, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).light(light);
            badgeConsumer.vertex(mat, badgeX, badgeY, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).light(light);
        }

        matrices.pop();
    }
}
