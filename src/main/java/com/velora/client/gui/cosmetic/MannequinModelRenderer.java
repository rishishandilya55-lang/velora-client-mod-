package com.velora.client.gui.cosmetic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class MannequinModelRenderer {

    private static final int BODY_COLOR = 0xFF475569;
    private static final int HEAD_COLOR = 0xFF64748B;
    private static final int STAND_COLOR = 0xFF334155;
    private static final int FACE_COLOR = 0xFF38BDF8;
    private static final int HAT_COLOR = 0xFFFFD700;
    private static final int AURA_COLOR = 0x557C3AED;

    private static PlayerEntityModel skinModel;
    private static PlayerEntityModel slimSkinModel;

    public static void renderMannequinCard(DrawContext context, int x, int y, int width, int height, CosmeticItem item, boolean hovered) {
        context.fill(x, y, x + width, y + height, 0xFF14161F);

        float rotY = 180.0f + (float) (Math.sin(System.currentTimeMillis() / 700.0) * 15.0);
        if (hovered) {
            rotY = 180.0f + (float) (Math.sin(System.currentTimeMillis() / 350.0) * 20.0);
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + width / 2.0f, y + height / 2.0f + 0.94f * 38.0f, 150.0f);
        matrices.scale(38.0f, -38.0f, 38.0f);
        matrices.multiply(new Quaternionf()
                .rotationX((float) Math.toRadians(10.0))
                .rotateY((float) Math.toRadians(rotY)));

        context.draw(vcp -> renderPreview(matrices, vcp, item));

        matrices.pop();

        context.drawBorder(x, y, width, height, hovered ? 0xFF3B82F6 : 0xFF232734);
    }

    public static void renderPreviewLarge(DrawContext context, int x, int y, int width, int height, CosmeticItem item) {
        float rotY = 180.0f + (float) (Math.sin(System.currentTimeMillis() / 1600.0) * 30.0);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + width / 2.0f, y + height / 2.0f + 0.94f * 52.0f, 150.0f);
        matrices.scale(52.0f, -52.0f, 52.0f);
        matrices.multiply(new Quaternionf()
                .rotationX((float) Math.toRadians(8.0))
                .rotateY((float) Math.toRadians(rotY)));

        context.draw(vcp -> renderPreview(matrices, vcp, item));

        matrices.pop();
    }

    private static void renderPreview(MatrixStack matrices, VertexConsumerProvider vcp, CosmeticItem item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        AbstractClientPlayerEntity player = mc.player;

        if (player == null) {
            renderMannequinBase(matrices, vcp);
            renderCosmeticFallback(matrices, vcp, item);
            return;
        }

        SkinTextures skinTextures = player.getSkinTextures();
        PlayerEntityModel model = getSkinModel(skinTextures.model() == SkinTextures.Model.SLIM);
        PlayerEntityRenderState state = new PlayerEntityRenderState();
        populateState(state, skinTextures);

        model.setAngles(state);

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(skinTextures.texture()));

        matrices.push();
        matrices.scale(-1.0f, -1.0f, 1.0f);
        matrices.translate(0.0f, -1.501f, 0.0f);
        model.render(matrices, vc, 0xF000F0, OverlayTexture.DEFAULT_UV, -1);
        matrices.pop();

        renderCosmetic(matrices, vcp, item);
    }

    private static PlayerEntityModel getSkinModel(boolean slim) {
        if (slim) {
            if (slimSkinModel == null) {
                slimSkinModel = buildSkinModel(true);
            }
            return slimSkinModel;
        }
        if (skinModel == null) {
            skinModel = buildSkinModel(false);
        }
        return skinModel;
    }

    private static PlayerEntityModel buildSkinModel(boolean thinArms) {
        ModelPart part = TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, thinArms), 64, 64).createModel();
        return new PlayerEntityModel(part, thinArms);
    }

    private static void populateState(PlayerEntityRenderState state, SkinTextures skinTextures) {
        state.skinTextures = skinTextures;
        state.spectator = false;
        state.invisible = false;
        state.sneaking = false;
        state.hatVisible = true;
        state.jacketVisible = true;
        state.leftPantsLegVisible = true;
        state.rightPantsLegVisible = true;
        state.leftSleeveVisible = true;
        state.rightSleeveVisible = true;
        state.capeVisible = true;
        state.playerName = Text.empty();
        state.name = "";
        state.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
        state.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
        state.mainArm = Arm.RIGHT;
        state.preferredArm = Arm.RIGHT;
        state.activeHand = Hand.MAIN_HAND;
        state.isUsingItem = false;
        state.isGliding = false;
        state.hasVehicle = false;
        state.leaningPitch = 0.0f;
        state.limbAmplitudeInverse = 1.0f;
        state.limbFrequency = 0.0f;
        state.limbAmplitudeMultiplier = 0.0f;
        state.baseScale = 1.0f;
        state.ageScale = 1.0f;
        state.pitch = 0.0f;
        state.yawDegrees = 0.0f;
        state.bodyYaw = 0.0f;
        state.equippedHeadStack = ItemStack.EMPTY;
        state.equippedChestStack = ItemStack.EMPTY;
        state.equippedLegsStack = ItemStack.EMPTY;
        state.equippedFeetStack = ItemStack.EMPTY;
    }

    private static void renderCosmetic(MatrixStack matrices, VertexConsumerProvider vcp, CosmeticItem item) {
        if (item == null) return;
        Identifier texture = CosmeticTextureCache.getValidTextureOrFallback(item.getTexture());
        switch (item.getType()) {
            case CAPE -> renderCape(matrices, vcp, texture);
            case WINGS -> renderWings(matrices, vcp, texture);
            case HAT -> renderHat(matrices, vcp);
            case FACE -> renderFace(matrices, vcp);
            case AURA -> renderAura(matrices, vcp);
        }
    }

    private static void renderCape(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture) {
        if (!CosmeticTextureCache.isTextureValid(texture)) return;
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGuiTextured(texture));
        capeQuad(vc, matrices, -0.24f, 1.55f, -0.165f, 0.24f, 1.18f, 0.0f, 1.0f, 0.0f, 0.34f);
        capeQuad(vc, matrices, -0.26f, 1.18f, -0.185f, 0.26f, 0.88f, 0.0f, 1.0f, 0.34f, 0.68f);
        capeQuad(vc, matrices, -0.30f, 0.88f, -0.205f, 0.30f, 0.66f, 0.0f, 1.0f, 0.68f, 1.0f);
    }

    private static void renderWings(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture) {
        if (!CosmeticTextureCache.isTextureValid(texture)) return;
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGuiTextured(texture));
        texturedQuad(vc, matrices, -0.62f, 1.40f, -0.22f, -0.10f, 0.85f);
        texturedQuad(vc, matrices, 0.10f, 1.40f, -0.22f, 0.62f, 0.85f);
    }

    private static void renderHat(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.30f, 1.80f, -0.24f, 0.60f, 0.08f, 0.48f, HAT_COLOR);
        drawBox(vcp, matrices, -0.20f, 1.88f, -0.16f, 0.40f, 0.22f, 0.32f, 0xFFEAB308);
    }

    private static void renderFace(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.16f, 1.54f, 0.30f, 0.32f, 0.16f, 0.06f, FACE_COLOR);
    }

    private static void renderAura(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.45f, 0.15f, -0.45f, 0.90f, 1.60f, 0.90f, AURA_COLOR);
    }

    private static void renderCosmeticFallback(MatrixStack matrices, VertexConsumerProvider vcp, CosmeticItem item) {
        if (item == null) return;
        Identifier texture = CosmeticTextureCache.getValidTextureOrFallback(item.getTexture());
        switch (item.getType()) {
            case CAPE -> renderCapeOnMannequin(matrices, vcp, texture);
            case WINGS -> renderWingsOnMannequin(matrices, vcp, texture);
            case HAT -> renderHatOnMannequin(matrices, vcp);
            case FACE -> renderFaceOnMannequin(matrices, vcp);
            case AURA -> renderAuraOnMannequin(matrices, vcp);
        }
    }

    private static void renderMannequinBase(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.42f, -1.05f, -0.42f, 0.84f, 0.10f, 0.84f, STAND_COLOR);
        drawBox(vcp, matrices, -0.22f, -0.62f, -0.10f, 0.20f, 0.62f, 0.20f, BODY_COLOR);
        drawBox(vcp, matrices, 0.02f, -0.62f, -0.10f, 0.20f, 0.62f, 0.20f, BODY_COLOR);
        drawBox(vcp, matrices, -0.25f, 0.0f, -0.12f, 0.50f, 0.70f, 0.24f, 0xFF526075);
        drawBox(vcp, matrices, -0.40f, 0.12f, -0.10f, 0.12f, 0.53f, 0.20f, BODY_COLOR);
        drawBox(vcp, matrices, 0.28f, 0.12f, -0.10f, 0.12f, 0.53f, 0.20f, BODY_COLOR);
        drawBox(vcp, matrices, -0.20f, 0.72f, -0.20f, 0.40f, 0.40f, 0.40f, HEAD_COLOR);
    }

    private static void renderCapeOnMannequin(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture) {
        if (!CosmeticTextureCache.isTextureValid(texture)) {
            drawBox(vcp, matrices, -0.22f, -0.60f, -0.14f, 0.44f, 0.70f, 0.04f, 0xFF334155);
            return;
        }
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGuiTextured(texture));
        capeQuad(vc, matrices, -0.24f, 0.70f, -0.135f, 0.24f, 0.48f, 0.0f, 1.0f, 0.0f, 0.34f);
        capeQuad(vc, matrices, -0.26f, 0.48f, -0.150f, 0.26f, 0.24f, 0.0f, 1.0f, 0.34f, 0.68f);
        capeQuad(vc, matrices, -0.30f, 0.24f, -0.165f, 0.30f, -0.02f, 0.0f, 1.0f, 0.68f, 1.0f);
    }

    private static void renderWingsOnMannequin(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture) {
        if (!CosmeticTextureCache.isTextureValid(texture)) {
            drawBox(vcp, matrices, -0.28f, -0.20f, -0.12f, 0.56f, 0.60f, 0.05f, 0xFF475569);
            return;
        }
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGuiTextured(texture));
        texturedQuad(vc, matrices, -0.62f, 0.70f, -0.22f, -0.08f, -0.05f);
        texturedQuad(vc, matrices, 0.08f, 0.70f, -0.22f, 0.62f, -0.05f);
    }

    private static void renderHatOnMannequin(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.30f, 1.10f, -0.24f, 0.60f, 0.08f, 0.48f, HAT_COLOR);
        drawBox(vcp, matrices, -0.20f, 1.18f, -0.16f, 0.40f, 0.22f, 0.32f, 0xFFEAB308);
    }

    private static void renderFaceOnMannequin(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.16f, 0.84f, 0.20f, 0.32f, 0.16f, 0.06f, FACE_COLOR);
    }

    private static void renderAuraOnMannequin(MatrixStack matrices, VertexConsumerProvider vcp) {
        drawBox(vcp, matrices, -0.45f, -0.55f, -0.45f, 0.90f, 1.60f, 0.90f, AURA_COLOR);
    }

    private static void drawBox(VertexConsumerProvider vcp, MatrixStack matrices, float x, float y, float z, float w, float h, float d, int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGui());

        solidFace(vc, matrices, x, y, z, x + w, y, z, x + w, y + h, z, x, y + h, z, r, g, b, a, 0.72f);
        solidFace(vc, matrices, x, y, z + d, x + w, y, z + d, x + w, y + h, z + d, x, y + h, z + d, r, g, b, a, 0.92f);
        solidFace(vc, matrices, x, y, z + d, x, y, z, x, y + h, z, x, y + h, z + d, r, g, b, a, 0.80f);
        solidFace(vc, matrices, x + w, y, z, x + w, y, z + d, x + w, y + h, z + d, x + w, y + h, z, r, g, b, a, 0.84f);
        solidFace(vc, matrices, x, y + h, z, x + w, y + h, z, x + w, y + h, z + d, x, y + h, z + d, r, g, b, a, 1.0f);
        solidFace(vc, matrices, x, y, z + d, x + w, y, z + d, x + w, y, z, x, y, z, r, g, b, a, 0.60f);
    }

    private static void solidFace(VertexConsumer vc, MatrixStack matrices, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a, float shade) {
        int sr = (int) (r * shade);
        int sg = (int) (g * shade);
        int sb = (int) (b * shade);
        vertex(vc, matrices, x0, y0, z0, sr, sg, sb, a);
        vertex(vc, matrices, x1, y1, z1, sr, sg, sb, a);
        vertex(vc, matrices, x2, y2, z2, sr, sg, sb, a);
        vertex(vc, matrices, x3, y3, z3, sr, sg, sb, a);
    }

    private static void vertex(VertexConsumer vc, MatrixStack matrices, float x, float y, float z, int r, int g, int b, int a) {
        vc.vertex(matrices.peek().getPositionMatrix(), x, y, z).color(r, g, b, a).texture(0.0f, 0.0f).light(0xF000F0);
    }

    private static void texturedQuad(VertexConsumer vc, MatrixStack matrices, float x0, float yTop, float z, float x1, float yBottom) {
        capeQuad(vc, matrices, x0, yTop, z, x1, yBottom, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    private static void capeQuad(VertexConsumer vc, MatrixStack matrices, float x0, float yTop, float z, float x1, float yBottom, float u0, float u1, float v0, float v1) {
        vc.vertex(matrices.peek().getPositionMatrix(), x0, yTop, z).color(255, 255, 255, 255).texture(u0, v0).light(0xF000F0);
        vc.vertex(matrices.peek().getPositionMatrix(), x1, yTop, z).color(255, 255, 255, 255).texture(u1, v0).light(0xF000F0);
        vc.vertex(matrices.peek().getPositionMatrix(), x1, yBottom, z).color(255, 255, 255, 255).texture(u1, v1).light(0xF000F0);
        vc.vertex(matrices.peek().getPositionMatrix(), x0, yBottom, z).color(255, 255, 255, 255).texture(u0, v1).light(0xF000F0);
    }
}
