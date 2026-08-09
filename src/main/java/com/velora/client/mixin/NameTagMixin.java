package com.velora.client.mixin;

import com.velora.client.client.FreeLookClient;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("HEAD"),
        cancellable = true
    )
    private void velora_customNametag(
        EntityRenderState state,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!ModConfig.showNametag) {
            return;
        }

        // Only customize player overhead nametags
        if (!(state instanceof PlayerEntityRenderState playerState)) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        Vec3d nameLabelPos = state.nameLabelPos;
        if (nameLabelPos == null) return;

        String nameStr = playerState.name != null && !playerState.name.isEmpty()
            ? playerState.name
            : (text != null ? text.getString() : "");

        if (nameStr == null || nameStr.isEmpty()) return;

        boolean isLocalPlayer = mc.player.getName().getString().equalsIgnoreCase(nameStr);
        boolean isFirstPerson = mc.options.getPerspective().isFirstPerson() && !FreeLookClient.active;

        // Cancel vanilla nametag rendering
        ci.cancel();

        // Do not render on self in first-person mode
        if (isLocalPlayer && isFirstPerson) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        if (textRenderer == null) return;

        // Find the player entity for health and distance calculation
        AbstractClientPlayerEntity targetPlayer = null;
        if (mc.world != null) {
            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(nameStr)) {
                    targetPlayer = p;
                    break;
                }
            }
        }

        // Build rich nametag text
        MutableText fullTag;
        if (text != null) {
            fullTag = text.copy();
        } else if (state.displayName != null) {
            fullTag = state.displayName.copy();
        } else {
            fullTag = Text.literal(nameStr);
        }

        // 1. Health indicator
        if (ModConfig.nametagShowHealth && targetPlayer != null) {
            int hp = (int) Math.ceil(targetPlayer.getHealth());
            Formatting hpColor = (hp >= 15) ? Formatting.GREEN : (hp >= 8) ? Formatting.YELLOW : Formatting.RED;
            fullTag.append(Text.literal(" " + hp + "❤").formatted(hpColor));
        }

        // 2. Distance indicator
        if (ModConfig.nametagShowDistance && targetPlayer != null) {
            Entity cam = (mc.cameraEntity != null) ? mc.cameraEntity : mc.player;
            int dist = (int) Math.sqrt(targetPlayer.squaredDistanceTo(cam));
            fullTag.append(Text.literal(" [" + dist + "m]").formatted(Formatting.GRAY));
        }

        matrices.push();
        matrices.translate(nameLabelPos.x, nameLabelPos.y + 0.5, nameLabelPos.z);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float textWidth = textRenderer.getWidth(fullTag);
        float x = -textWidth / 2.0f;
        float y = 0.0f;

        int userOpacity = Math.max(0, Math.min(255, ModConfig.nametagBackgroundOpacity));
        int bgColor = (userOpacity << 24) | 0x000000;

        if (state.sneaking) {
            // Sneaking: see-through layer so player can be seen faintly through walls
            textRenderer.draw(
                fullTag,
                x,
                y,
                0x20FFFFFF,
                false,
                matrix,
                vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                bgColor,
                light
            );
            textRenderer.draw(
                fullTag,
                x,
                y,
                0xFFFFFFFF,
                false,
                matrix,
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                light
            );
        } else {
            // Normal: crisp, vibrant nametag with background plate
            textRenderer.draw(
                fullTag,
                x,
                y,
                0xFFFFFFFF,
                false,
                matrix,
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                bgColor,
                light
            );
        }

        matrices.pop();
    }
}
