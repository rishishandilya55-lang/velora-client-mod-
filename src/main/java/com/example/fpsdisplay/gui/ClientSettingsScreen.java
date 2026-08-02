package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClientSettingsScreen extends Screen {
    public ClientSettingsScreen() {
        super(Text.literal("Client Settings"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int panelW = 480;
        int panelH = 320;
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        // Dark floating panel
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC1A1A2E);

        // Header Title
        context.fill(panelX, panelY, panelX + panelW, panelY + 36, 0xEE222240);
        context.drawText(this.textRenderer, "Velora Client - Performance & Client Settings", panelX + 14, panelY + 12, 0xFFFFFFFF, true);

        // Close X button
        int xBtnX = panelX + panelW - 26;
        int xBtnY = panelY + 8;
        boolean xHov = mouseX >= xBtnX && mouseX <= xBtnX + 18 && mouseY >= xBtnY && mouseY <= xBtnY + 18;
        if (xHov) context.fill(xBtnX - 2, xBtnY - 2, xBtnX + 20, xBtnY + 20, 0x55FF4444);
        context.drawCenteredTextWithShadow(this.textRenderer, "X", xBtnX + 9, xBtnY + 5, 0xFFFFFFFF);

        // Optimization Options List
        int startY = panelY + 46;
        int rowH = 30;
        int spacing = 6;

        // 1. Fast Math Optimization
        drawSettingRow(context, mouseX, mouseY, panelX + 20, startY, panelW - 40, rowH, "Fast Math & Lightweight Render", ModConfig.optiFastMath);

        // 2. Low Memory Overhead Mode
        drawSettingRow(context, mouseX, mouseY, panelX + 20, startY + (rowH + spacing), panelW - 40, rowH, "Low Memory Garbage Mode", ModConfig.optiLowMemoryMode);

        // 3. Particle Limiter
        drawSettingRow(context, mouseX, mouseY, panelX + 20, startY + (rowH + spacing) * 2, panelW - 40, rowH, "Particle Limiter (FPS Boost)", ModConfig.optiLimitParticles);

        // 4. Disable Terrain Fog
        drawSettingRow(context, mouseX, mouseY, panelX + 20, startY + (rowH + spacing) * 3, panelW - 40, rowH, "Disable Terrain Fog", ModConfig.optiDisableFog);

        // 5. Entity Culling
        drawSettingRow(context, mouseX, mouseY, panelX + 20, startY + (rowH + spacing) * 4, panelW - 40, rowH, "Entity Culling (Occlusion & FPS Boost)", ModConfig.optiEntityCulling);
    }

    private void drawSettingRow(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String label, boolean enabled) {
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hov ? 0x55FFFFFF : 0x33FFFFFF);
        context.drawText(this.textRenderer, label, x + 14, y + (h - 8) / 2, 0xFFFFFFFF, false);

        int sw = 32; int sh = 16;
        int sx = x + w - sw - 14;
        int sy = y + (h - sh) / 2;
        context.fill(sx, sy, sx + sw, sy + sh, enabled ? 0xFF22C55E : 0xFF71717A);
        int kx = enabled ? sx + sw - 14 : sx + 2;
        context.fill(kx, sy + 2, kx + 12, sy + sh - 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = 480;
        int panelH = 320;
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        int xBtnX = panelX + panelW - 26;
        int xBtnY = panelY + 8;
        if (mouseX >= xBtnX && mouseX <= xBtnX + 18 && mouseY >= xBtnY && mouseY <= xBtnY + 18) {
            this.close();
            return true;
        }

        int startY = panelY + 46;
        int rowH = 30;
        int spacing = 6;
        int innerX = panelX + 20;
        int innerW = panelW - 40;

        if (mouseX >= innerX && mouseX <= innerX + innerW) {
            if (mouseY >= startY && mouseY <= startY + rowH) {
                ModConfig.optiFastMath = !ModConfig.optiFastMath;
                return true;
            }
            if (mouseY >= startY + (rowH + spacing) && mouseY <= startY + (rowH + spacing) + rowH) {
                ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode;
                return true;
            }
            if (mouseY >= startY + (rowH + spacing) * 2 && mouseY <= startY + (rowH + spacing) * 2 + rowH) {
                ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles;
                return true;
            }
            if (mouseY >= startY + (rowH + spacing) * 3 && mouseY <= startY + (rowH + spacing) * 3 + rowH) {
                ModConfig.optiDisableFog = !ModConfig.optiDisableFog;
                return true;
            }
            if (mouseY >= startY + (rowH + spacing) * 4 && mouseY <= startY + (rowH + spacing) * 4 + rowH) {
                ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
