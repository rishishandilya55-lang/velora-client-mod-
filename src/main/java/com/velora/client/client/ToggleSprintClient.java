package com.velora.client.client;

import com.velora.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleSprintClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static boolean toggleSprintActive = false;
    private static boolean wasSprintKeyPressed = false;

    public static void init() {
        // 1. Listen for user's Sprint Key to toggle Auto-Sprint ON / OFF!
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            // Detect Sprint Key press transition
            boolean isSprintKeyPressed = client.options.sprintKey.isPressed();
            if (isSprintKeyPressed && !wasSprintKeyPressed) {
                toggleSprintActive = !toggleSprintActive;
                LOGGER.info("[Velora] Sprint toggle: {}", toggleSprintActive ? "ON" : "OFF");
            }
            wasSprintKeyPressed = isSprintKeyPressed;

            // Auto-Sprint when active and moving forward
            if (ModConfig.showToggleSprint && toggleSprintActive) {
                if (client.options.forwardKey.isPressed()
                        && !client.player.isSneaking()
                        && !client.player.isSubmergedInWater()
                        && client.player.getHungerManager().getFoodLevel() > 6) {
                    client.player.setSprinting(true);
                }
            }
        });

        // 2. Renders Movement Status HUD ALL THE TIME!
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.showToggleSprint) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            TextRenderer textRenderer = client.textRenderer;

            // Determine Movement Status String (Shows ALL THE TIME!)
            String statusText;
            int statusColor;

            if (client.player.getAbilities().flying) {
                statusText = "[ Flying ]";
                statusColor = 0xFF38BDF8; // Light Blue
            } else if (client.player.isSneaking()) {
                statusText = "[ Sneaking ]";
                statusColor = 0xFFFACC15; // Yellow
            } else if (client.player.isSprinting()) {
                if (toggleSprintActive) {
                    statusText = "[ Sprinting (Toggled) ]";
                    statusColor = 0xFF00FF88; // Emerald Green
                } else {
                    statusText = "[ Sprinting (Key Held) ]";
                    statusColor = 0xFF6EE7B7; // Soft Mint Green
                }
            } else if (client.options.forwardKey.isPressed() || client.options.backKey.isPressed() ||
                       client.options.leftKey.isPressed() || client.options.rightKey.isPressed()) {
                statusText = toggleSprintActive ? "[ Walking (Sprint Toggled) ]" : "[ Walking ]";
                statusColor = 0xFFD1D5DB; // Silver Gray
            } else {
                statusText = toggleSprintActive ? "[ Auto-Sprint: ON ]" : "[ Sprint: OFF ]";
                statusColor = toggleSprintActive ? 0xFF00FF88 : 0xFF9CA3AF;
            }

            // Apply Scale Transform to actual rendering!
            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(ModConfig.sprintScale, ModConfig.sprintScale, 1.0f);

            int x = (int) (ModConfig.sprintX / ModConfig.sprintScale);
            int y = (int) (ModConfig.sprintY / ModConfig.sprintScale);

            // Background Box & Status Text
            int textWidth = textRenderer.getWidth(statusText);
            drawContext.fill(x - 4, y - 4, x + textWidth + 4, y + 12, 0x80000000);
            drawContext.drawText(textRenderer, statusText, x, y, statusColor, true);

            drawContext.getMatrices().pop();
        });
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}
