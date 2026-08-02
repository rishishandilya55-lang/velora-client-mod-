package com.example.fpsdisplay.mixin;

import com.example.fpsdisplay.client.EntityCullingUtil;
import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
        method = "renderEntity",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderEntity(
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        CallbackInfo ci
    ) {
        if (ModConfig.optiEntityCulling) {
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            if (camera != null && !EntityCullingUtil.shouldRenderEntity(entity, camera)) {
                ci.cancel();
            }
        }
    }
}
