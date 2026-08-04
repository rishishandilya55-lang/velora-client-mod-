package com.velora.client.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class CapePhysicsUtil {

    public static class CapeState {
        public float pitch;
        public float roll;
        public float wave;
    }

    public static CapeState computeCapePhysics(AbstractClientPlayerEntity player, float tickDelta) {
        CapeState state = new CapeState();
        if (player == null) return state;

        // Interpolated player displacement
        double deltaX = MathHelper.lerp((double) tickDelta, player.prevCapeX, player.capeX) - MathHelper.lerp((double) tickDelta, player.prevX, player.getX());
        double deltaY = MathHelper.lerp((double) tickDelta, player.prevCapeY, player.capeY) - MathHelper.lerp((double) tickDelta, player.prevY, player.getY());
        double deltaZ = MathHelper.lerp((double) tickDelta, player.prevCapeZ, player.capeZ) - MathHelper.lerp((double) tickDelta, player.prevZ, player.getZ());

        float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
        double sinYaw = MathHelper.sin(bodyYaw * (float) (Math.PI / 180.0));
        double cosYaw = -MathHelper.cos(bodyYaw * (float) (Math.PI / 180.0));

        // Pitch rotation (momentum backward when running)
        float pitchRaw = (float) (deltaX * sinYaw + deltaZ * cosYaw) * 100.0f;
        pitchRaw = MathHelper.clamp(pitchRaw, 0.0f, 150.0f);

        // Roll rotation (side to side swaying)
        float rollRaw = (float) (deltaX * cosYaw - deltaZ * sinYaw) * 100.0f;
        rollRaw = MathHelper.clamp(rollRaw, -35.0f, 35.0f);

        // Speed & stride wave flapping
        double vx = player.getVelocity().x;
        double vz = player.getVelocity().z;
        float hSpeed = (float) Math.sqrt(vx * vx + vz * vz);
        float waveTime = (player.age + tickDelta) * 0.35f;
        float waveAnim = MathHelper.sin(waveTime + hSpeed * 5.0f) * 12.0f * Math.min(1.0f, hSpeed * 3.0f);

        // Vertical movement angle adjust
        float pitchY = (float) deltaY * 10.0f;
        pitchY = MathHelper.clamp(pitchY, -12.0f, 35.0f);

        state.pitch = pitchRaw + waveAnim + pitchY;
        state.roll = rollRaw;
        state.wave = waveAnim;

        if (player.isInSneakingPose()) {
            state.pitch += 22.0f;
        }

        return state;
    }
}
