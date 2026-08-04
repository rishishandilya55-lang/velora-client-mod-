package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void velora_customNametag(
            net.minecraft.client.render.entity.state.EntityRenderState state,
            Text name,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!ModConfig.showNametag) return;

        String playerName = name.getString();
        RankSystem.Rank rank = RankSystem.getRankForPlayer(playerName);
        if (rank == RankSystem.Rank.DEFAULT) return;

        ci.cancel();

        net.minecraft.util.math.Vec3d nameLabelPos = state.nameLabelPos;
        if (nameLabelPos == null) return;

        boolean sneaking = state.sneaking;

        TextRenderer textRenderer = mc.textRenderer;
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        String rankPrefix = "[" + rank.getDisplayName() + "] ";
        Text rankText = Text.literal(rankPrefix).withColor(rank.getColor());

        int rankWidth = textRenderer.getWidth(rankPrefix);
        int nameWidth = textRenderer.getWidth(playerName);
        int totalWidth = rankWidth + nameWidth;

        float textX = -totalWidth / 2.0f;
        float textY = sneaking ? -18.0f : 0.0f;

        int bgOpacity = (int) (mc.options.getTextBackgroundOpacity(0.25f) * 255.0f);
        int bgColor = bgOpacity << 24;

        TextRenderer.TextLayerType layerType = sneaking ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

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
                layerType,
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

        matrices.pop();
    }
}
