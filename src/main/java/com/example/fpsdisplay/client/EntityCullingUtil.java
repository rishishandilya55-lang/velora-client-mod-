package com.example.fpsdisplay.client;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class EntityCullingUtil {

    /**
     * Determines whether an entity should be rendered or culled based on distance & block occlusion.
     */
    public static boolean shouldRenderEntity(Entity entity, Camera camera) {
        if (entity == null || camera == null) return true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return true;

        // Never cull the local player
        if (entity == client.player) return true;

        // Never cull removed, spectator, dead, or submerged entities
        if (entity.isRemoved() || entity.isSpectator() || !entity.isAlive() || entity.isSubmergedInWater()) {
            return true;
        }

        Vec3d cameraPos = camera.getPos();
        Vec3d entityPos = entity.getPos();
        double distSq = cameraPos.squaredDistanceTo(entityPos);

        // Always render nearby entities (< 8 blocks away) for instant responsiveness
        if (distSq <= 64.0) return true;

        Box box = entity.getBoundingBox();

        // Check 1: Raycast to the center of the entity
        Vec3d centerTarget = box.getCenter();
        if (hasLineOfSight(client, cameraPos, centerTarget, entity)) {
            return true;
        }

        // Check 2: Raycast to the top of the entity (prevents pop-in at wall tops)
        Vec3d topTarget = new Vec3d(centerTarget.x, box.maxY, centerTarget.z);
        if (hasLineOfSight(client, cameraPos, topTarget, entity)) {
            return true;
        }

        // Both raycasts blocked by solid blocks -> Cull entity!
        return false;
    }

    private static boolean hasLineOfSight(MinecraftClient client, Vec3d from, Vec3d to, Entity entity) {
        BlockHitResult result = client.world.raycast(new RaycastContext(
            from,
            to,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            entity
        ));

        return result.getType() != HitResult.Type.BLOCK;
    }
}