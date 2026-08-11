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
 * Waypoints rendering module.
 *
 * Strategy:
 *   • Thin 3D beam  → drawn via BeaconBlockEntityRenderer in WorldRenderEvents.LAST
 *   • Text label    → projected from 3D world space to 2D screen coordinates,
 *                     then drawn in HudRenderCallback (always reliable).
 *
 * Why HUD for text?
 *   TextRenderer.draw() with a world-space MatrixStack is unreliable at the
 *   LAST render phase in Fabric 1.21.x — the GL state / render layers are not
 *   set up for it at that point.  HudRenderCallback + DrawContext.drawText()
 *   always works and gives a clean, crisp label.
 */
public class WaypointsMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    /** Projected HUD entries, populated during world render, consumed by HUD render. */
    private record HudEntry(String label, float screenX, float screenY) {}
    private static final List<HudEntry> hudEntries = new ArrayList<>();

    // ────────────────────────────────────────────────────────────────────────
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
            double dx = (wp.x + 0.5) - cam.x;
            double dy = (wp.y + 1.5) - cam.y;
            double dz = (wp.z + 0.5) - cam.z;

            double horizDist = Math.sqrt(dx * dx + dz * dz);
            double dist3d    = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist3d < 0.5) continue;

            int wpColor = wp.color | 0xFF000000;

            // ── Thin vertical beam ───────────────────────────────────────────
            if (ModConfig.waypointsBeaconBeams && immediate != null) {
                MatrixStack beamMs = new MatrixStack();
                beamMs.translate(dx, -cam.y, dz);
                BeaconBlockEntityRenderer.renderBeam(
                    beamMs, immediate,
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    tickDelta, 1.0f, worldTime,
                    (int) Math.max(-64, wp.y), 320,
                    wpColor,
                    0.04f,  // inner radius — nice and thin
                    0.06f   // outer radius — nice and thin
                );
                immediate.draw();
            }

            // ── Project waypoint label position to 2D screen space ───────────
            Vector4f clip = new Vector4f((float) dx, (float) dy, (float) dz, 1.0f);
            clip.mul(viewProj);

            if (clip.w <= 0f) continue; // behind camera

            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;

            // Skip if far off-screen (allow slight overflow for edge labels)
            if (ndcX < -1.4f || ndcX > 1.4f || ndcY < -1.4f || ndcY > 1.4f) continue;

            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();

            float sx = (ndcX + 1.0f) * 0.5f * sw;
            float sy = (1.0f - ndcY) * 0.5f * sh;

            // Build label string
            String label = wp.name;
            if (ModConfig.waypointsShowDistance) {
                label += " [" + (int) Math.round(horizDist) + "m]";
            }

            hudEntries.add(new HudEntry(label, sx, sy));
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
            int y = (int) entry.screenY();

            // Dark semi-transparent background pill
            context.fill(x - 4, y - 2, x + textW + 4, y + 10, 0x90000000);

            // White text with shadow — always readable
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
