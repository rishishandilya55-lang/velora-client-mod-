package com.velora.client.client;

import com.velora.client.config.ModConfig;
import com.velora.client.waypoints.Waypoint;
import com.velora.client.waypoints.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Waypoints in-world and HUD rendering module.
 *
 * Strategy:
 *   • Thin 3D vertical beam → rendered in WorldRenderEvents.LAST
 *   • 3D to 2D projection   → projects the exact world-space anchor (wp.x, wp.y + 2.0, wp.z)
 *   • Crisp HUD label       → drawn via DrawContext in HudRenderCallback centered over the beam
 */
public class WaypointsMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    /** Projected HUD entries, populated during world render, consumed by HUD render. */
    private record HudEntry(String label, float screenX, float screenY, int color) {}
    private static final List<HudEntry> hudEntries = new ArrayList<>();

    public static void init() {
        WaypointManager.init();
        LOGGER.info("[Velora] Waypoints module initialized");

        // 1. World render: thin beam + 3D→2D projection
        WorldRenderEvents.LAST.register(WaypointsMod::renderWorld);

        // 2. HUD render: draw projected text labels
        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager) ->
                renderHud(drawContext));
    }

    // ── World render phase ───────────────────────────────────────────────────
    private static void renderWorld(WorldRenderContext ctx) {
        hudEntries.clear();
        if (!ModConfig.showWaypoints) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null || mc.options.hudHidden) return;
        if (mc.getWindow() == null) return;

        Camera camera = ctx.camera();
        if (camera == null) return;

        Vec3d cam = camera.getPos();

        // Combined view-projection matrix for 3D→2D projection
        Matrix4f viewProj = new Matrix4f(ctx.projectionMatrix()).mul(ctx.positionMatrix());

        // Get vertex consumers for the beam
        VertexConsumerProvider ctxConsumers = ctx.consumers();
        VertexConsumerProvider.Immediate immediate;
        if (ctxConsumers instanceof VertexConsumerProvider.Immediate imm) {
            immediate = imm;
        } else {
            immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        }

        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        long worldTime  = mc.world.getTime();

        List<Waypoint> waypoints = WaypointManager.getVisibleWaypointsForCurrentDimension();

        for (Waypoint wp : waypoints) {
            double dx = wp.x - cam.x;
            double dy = wp.y - cam.y;
            double dz = wp.z - cam.z;

            double horizDist = Math.sqrt(dx * dx + dz * dz);
            double dist3d    = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist3d < 0.2) continue;

            int wpColor = wp.color | 0xFF000000;

            // ── Thin vertical beacon beam ────────────────────────────────────
            if (ModConfig.waypointsBeaconBeams && immediate != null) {
                MatrixStack beamMs = new MatrixStack();
                // BeaconBlockEntityRenderer.renderBeam internally offsets by (+0.5, 0.0, +0.5).
                // To center the beam on exact (wp.x, wp.z), offset by (dx - 0.5, -cam.y, dz - 0.5).
                beamMs.translate(dx - 0.5, -cam.y, dz - 0.5);
                BeaconBlockEntityRenderer.renderBeam(
                    beamMs, immediate,
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    tickDelta, 1.0f, worldTime,
                    (int) Math.max(-64, wp.y), 320,
                    wpColor,
                    0.05f,  // inner radius
                    0.08f   // outer radius
                );
                immediate.draw();
            }

            // ── Project waypoint label position to 2D screen space ───────────
            // Floating label position: exactly at (wp.x, wp.y + 2.0, wp.z)
            double labelY = (wp.y + 2.0) - cam.y;
            Vector4f clip = new Vector4f((float) dx, (float) labelY, (float) dz, 1.0f);
            clip.mul(viewProj);

            if (clip.w <= 0f) continue; // behind camera

            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;

            // Skip if off-screen
            if (ndcX < -1.2f || ndcX > 1.2f || ndcY < -1.2f || ndcY > 1.2f) continue;

            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();

            float sx = (ndcX + 1.0f) * 0.5f * sw;
            float sy = (1.0f - ndcY) * 0.5f * sh;

            // Build label string
            String label = wp.name;
            if (ModConfig.waypointsShowDistance) {
                label += " [" + (int) Math.round(horizDist) + "m]";
            }

            hudEntries.add(new HudEntry(label, sx, sy, wpColor));
        }
    }

    // ── HUD render phase ─────────────────────────────────────────────────────
    private static void renderHud(DrawContext context) {
        if (!ModConfig.showWaypoints) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;

        for (HudEntry entry : hudEntries) {
            int textW = tr.getWidth(entry.label());
            int x = (int) entry.screenX() - textW / 2;
            int y = (int) entry.screenY() - 5;

            // Dark semi-transparent pill with waypoint color border
            context.fill(x - 4, y - 2, x + textW + 4, y + 11, 0xCC090A0F);
            context.drawBorder(x - 4, y - 2, textW + 8, 13, entry.color());

            // White text with shadow — perfectly centered and aligned
            context.drawText(tr, entry.label(), x, y, 0xFFFFFFFF, true);
        }
    }

    /** Called from WaypointRenderMixin — kept as no-op stub. */
    public static void renderWaypoints(Camera camera) { }

    @Override
    public void onInitializeClient() {
        init();
    }
}
