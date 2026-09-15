package com.velora.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public abstract class AnimatedOwoScreen extends BaseOwoScreen<FlowLayout> {

    private float animationProgress = 0.0f;
    private boolean isClosing = false;
    private boolean isFullyClosed = false;
    private boolean isRenderingBackground = false;

    protected int getFadeColor(int color) {
        int alpha = (color >> 24) & 0xFF;
        int newAlpha = (int) (alpha * animationProgress);
        return (newAlpha << 24) | (color & 0x00FFFFFF);
    }

    protected float getAnimationProgress() {
        return animationProgress;
    }

    protected AnimatedOwoScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Animation update
        float speed = 0.12f; // Fast and snappy
        if (isClosing) {
            animationProgress = Math.max(0.0f, animationProgress - speed);
            if (animationProgress <= 0.0f) {
                isFullyClosed = true;
                super.close();
                return;
            }
        } else {
            animationProgress = Math.min(1.0f, animationProgress + speed);
        }

        // Render Background (Unscaled)
        isRenderingBackground = true;
        this.renderBackground(context, mouseX, mouseY, delta);
        isRenderingBackground = false;

        // Render UI (Scaled)
        float scale = getEasing(animationProgress);

        context.getMatrices().push();
        // Scale from center of screen
        context.getMatrices().translate(this.width / 2.0f, this.height / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-this.width / 2.0f, -this.height / 2.0f, 0);

        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().pop();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Only render the background when we explicitly ask for it to avoid scaling it.
        // BaseOwoScreen/Screen will call this inside super.render(), but we intercept it.
        if (isRenderingBackground) {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void close() {
        if (!isClosing) {
            isClosing = true;
        } else if (isFullyClosed) {
            super.close();
        }
    }

    private float getEasing(float t) {
        // Cubic ease out
        t -= 1.0f;
        return t * t * t + 1.0f;
    }
}
