package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.waypoints.Waypoint;
import com.velora.client.waypoints.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WaypointsMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        WaypointManager.init();
        LOGGER.info("[Velora] Waypoints module initialized");

        WorldRenderEvents.AFTER_ENTITIES.register(WaypointsMod::renderInWorldWaypoints);
    }

    private static void renderInWorldWaypoints(WorldRenderContext context) {
        if (!ModConfig.showWaypoints) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null || mc.options.hudHidden) return;

        Camera camera = context.camera();
        if (camera == null) return;

        Vec3d camPos = camera.getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        TextRenderer textRenderer = mc.textRenderer;

        if (matrices == null || consumers == null || textRenderer == null) return;

        List<Waypoint> visibleWaypoints = WaypointManager.getVisibleWaypointsForCurrentDimension();
        if (visibleWaypoints.isEmpty()) return;

        for (Waypoint wp : visibleWaypoints) {
            double dx = (wp.x + 0.5) - camPos.x;
            double dy = (wp.y + 1.5) - camPos.y;
            double dz = (wp.z + 0.5) - camPos.z;

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.5) continue; // Don't render inside the player's face

            int wpColor = wp.color | 0xFF000000;

            // 1. Render Beacon Beam Column
            if (ModConfig.waypointsBeaconBeams) {
                matrices.push();
                matrices.translate((wp.x + 0.5) - camPos.x, -camPos.y, (wp.z + 0.5) - camPos.z);
                BeaconBlockEntityRenderer.renderBeam(
                    matrices,
                    consumers,
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    context.tickCounter().getTickDelta(true),
                    1.0f,
                    mc.world.getTime(),
                    (int) Math.max(-64, wp.y),
                    320,
                    wpColor,
                    0.2f,
                    0.25f
                );
                matrices.pop();
            }

            // Tag scaling: scale smoothly with distance so it stays prominent and readable across the map
            float baseScale = 0.035f;
            float scale = (float) (dist > 6.0 ? baseScale * (dist / 6.0) : baseScale);
            scale = Math.min(scale, 0.65f); // Clamp maximum scale

            String tagStr = wp.name;
            if (ModConfig.waypointsShowDistance) {
                tagStr += " [" + (int) Math.round(dist) + "m]";
            }
            Text tagText = Text.literal(tagStr);

            matrices.push();
            matrices.translate(dx, dy, dz);
            matrices.multiply(camera.getRotation());
            matrices.scale(-scale, -scale, scale);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float textWidth = textRenderer.getWidth(tagText);
            float textX = -textWidth / 2.0f;
            float textY = 0.0f;

            int bgColor = 0x90000000; // Dark translucent pill background matching screenshot

            // Draw see-through layer so waypoints are visible through walls and terrain
            textRenderer.draw(
                tagText,
                textX,
                textY,
                0xFFFFFFFF,
                false,
                matrix,
                consumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                bgColor,
                0xF000F0
            );

            // Draw normal crisp layer on top
            textRenderer.draw(
                tagText,
                textX,
                textY,
                0xFFFFFFFF,
                false,
                matrix,
                consumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                0xF000F0
            );

            matrices.pop();

            // Flush immediate vertex consumer to ensure OpenGL renders billboard tag
            if (consumers instanceof VertexConsumerProvider.Immediate imm) {
                imm.draw();
            }
        }
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
