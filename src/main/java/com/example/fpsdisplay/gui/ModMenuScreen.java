package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuScreen extends Screen {

    // Panel dimensions
    private static final int panelW = 540;
    private static final int panelH = 370;
    private static final int sideW = 50;
    private static final int titleH = 38;

    public ModMenuScreen() {
        super(Text.literal("Velora Client"));
    }

    // Block panorama completely with solid dark fill
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF080A12);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Container Window with Glowing Electric Purple Border
        VeloraRenderUtil.drawSolidPanel(context, panelX, panelY, panelW, panelH, 0xFF141726, 0xFFA855F7);
        VeloraRenderUtil.drawGlowBorder(context, panelX, panelY, panelW, panelH, 0x88A855F7, 3);

        // Left mini sidebar (#10121C)
        VeloraRenderUtil.drawSolidPanel(context, panelX, panelY, sideW, panelH, 0xFF10121C, 0xFF2D334A);

        // Sidebar Icon 1: Mods Grid
        boolean modeHov = mouseX >= panelX && mouseX <= panelX + sideW && mouseY >= panelY + 8 && mouseY <= panelY + 48;
        if (modeHov) context.fill(panelX + 4, panelY + 8, panelX + sideW - 4, panelY + 48, 0x33A855F7);
        int gx = panelX + 16; int gy = panelY + 14;
        context.fill(gx, gy, gx + 8, gy + 8, 0xFFA855F7);
        context.fill(gx + 10, gy, gx + 18, gy + 8, 0xFFA855F7);
        context.fill(gx, gy + 10, gx + 8, gy + 18, 0xFFA855F7);
        context.fill(gx + 10, gy + 10, gx + 18, gy + 18, 0xFFA855F7);
        context.drawCenteredTextWithShadow(this.textRenderer, "Mods", panelX + sideW / 2, panelY + 36, 0xFFE2E8F0);

        // Sidebar Icon 2: Cosmetics Locker
        boolean cosHov = mouseX >= panelX && mouseX <= panelX + sideW && mouseY >= panelY + 52 && mouseY <= panelY + 92;
        if (cosHov) context.fill(panelX + 4, panelY + 52, panelX + sideW - 4, panelY + 92, 0x33A855F7);
        context.drawCenteredTextWithShadow(this.textRenderer, "✦", panelX + sideW / 2, panelY + 58, 0xFFD8B4FE);
        context.drawCenteredTextWithShadow(this.textRenderer, "Locker", panelX + sideW / 2, panelY + 80, 0xFFE2E8F0);

        // Sidebar Icon 3: Settings
        boolean settHov = mouseX >= panelX && mouseX <= panelX + sideW && mouseY >= panelY + 96 && mouseY <= panelY + 136;
        if (settHov) context.fill(panelX + 4, panelY + 96, panelX + sideW - 4, panelY + 136, 0x33A855F7);
        context.drawCenteredTextWithShadow(this.textRenderer, "⚙", panelX + sideW / 2, panelY + 102, 0xFF94A3B8);
        context.drawCenteredTextWithShadow(this.textRenderer, "Config", panelX + sideW / 2, panelY + 124, 0xFFE2E8F0);

        // Main panel body (#181B2C)
        VeloraRenderUtil.drawSolidPanel(context, panelX + sideW, panelY, panelW - sideW, panelH, 0xFF181B2C, 0xFF2D334A);

        // Title Bar
        context.fill(panelX + sideW, panelY, panelX + panelW, panelY + titleH, 0xFF0F111D);
        context.fill(panelX + sideW, panelY + titleH - 1, panelX + panelW, panelY + titleH, 0xFFA855F7);
        context.drawText(this.textRenderer, "✦ VELORA CLIENT MODS", panelX + sideW + 14, panelY + 13, 0xFFFFFFFF, true);

        // Top-Right Locker & HUD Editor Buttons
        int cosBtnX = panelX + panelW - 176;
        int cosBtnY = panelY + 7;
        boolean cosBtnHov = mouseX >= cosBtnX && mouseX <= cosBtnX + 76 && mouseY >= cosBtnY && mouseY <= cosBtnY + 22;
        VeloraRenderUtil.drawPillButton(context, cosBtnX, cosBtnY, 76, 22, "Cosmetics", cosBtnHov, 0xFF16A34A, this.textRenderer);

        int hudBtnX = panelX + panelW - 92;
        int hudBtnY = panelY + 7;
        boolean hudHov = mouseX >= hudBtnX && mouseX <= hudBtnX + 64 && mouseY >= hudBtnY && mouseY <= hudBtnY + 22;
        VeloraRenderUtil.drawPillButton(context, hudBtnX, hudBtnY, 64, 22, "Edit HUD", hudHov, 0xFF8B5CF6, this.textRenderer);

        // X Close button
        int xBtnX = panelX + panelW - 22; int xBtnY = panelY + 9;
        boolean xHov = mouseX >= xBtnX && mouseX <= xBtnX + 14 && mouseY >= xBtnY && mouseY <= xBtnY + 16;
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", xBtnX + 7, xBtnY + 2, xHov ? 0xFFEF4444 : 0xFF94A3B8);

        // Module Grid (4 columns)
        int gridX = panelX + sideW + 12;
        int gridY = panelY + titleH + 10;
        int cellW = (panelW - sideW - 24) / 4 - 6;
        int cellH = 66;
        int gap = 6;

        String[] modules = {
            "FPS Display", "WASD Keys", "Ping Display", "CPS Counter",
            "Zoom Mod", "Toggle Sprint", "Toggle Sneak", "Fullbright",
            "Free Look", "Snap Look", "Armor Status", "Coordinates",
            "Day Counter", "Block Info", "No Hurt Cam", "Capes Locker", "Minimap"
        };

        boolean[] enabled = {
            ModConfig.showFps, ModConfig.showKeystrokes, ModConfig.showPing, ModConfig.showCps,
            ModConfig.showZoom, ModConfig.showToggleSprint, ModConfig.showToggleSneak, ModConfig.showFullbright,
            ModConfig.showFreeLook, ModConfig.showSnapLook, ModConfig.showArmorStatus, ModConfig.showCoordinates,
            ModConfig.showDayCounter, ModConfig.showBlockInfo, ModConfig.showNoHurtCam, true, ModConfig.showMinimap
        };

        String[] iconBadges = {
            "FPS", "WASD", "MS", "CPS",
            "ZOOM", "SPR", "SNK", "BRT",
            "LOOK", "SNAP", "ARM", "XYZ",
            "DAY", "BLK", "CAM", "CAPE", "MAP"
        };

        int[] badgeColors = {
            0xFF22C55E, 0xFF06B6D4, 0xFFEAB308, 0xFFEC4899,
            0xFFA855F7, 0xFF3B82F6, 0xFF8B5CF6, 0xFFF59E0B,
            0xFF10B981, 0xFF14B8A6, 0xFF6366F1, 0xFF84CC16,
            0xFFF97316, 0xFF06B6D4, 0xFFEF4444, 0xFFA855F7, 0xFF3B82F6
        };

        for (int i = 0; i < modules.length; i++) {
            int col = i % 4;
            int row = i / 4;
            int cx2 = gridX + col * (cellW + gap);
            int cy2 = gridY + row * (cellH + gap);

            boolean isEn = enabled[i];
            boolean cardHov = mouseX >= cx2 && mouseX <= cx2 + cellW && mouseY >= cy2 && mouseY <= cy2 + cellH;

            // Card background & high-contrast border
            int cardBg = isEn ? (cardHov ? 0xFF2E1C54 : 0xFF241544) : (cardHov ? 0xFF282C42 : 0xFF1E2235);
            int cardBorder = isEn ? 0xFFA855F7 : (cardHov ? 0xFF64748B : 0xFF333A54);

            VeloraRenderUtil.drawSolidPanel(context, cx2, cy2, cellW, cellH, cardBg, cardBorder);

            // Left indicator accent line
            if (isEn) {
                context.fill(cx2, cy2, cx2 + 3, cy2 + cellH, 0xFF22C55E);
            }

            // Top Badge Icon
            int badgeColor = badgeColors[i % badgeColors.length];
            context.fill(cx2 + 8, cy2 + 6, cx2 + 38, cy2 + 20, (badgeColor & 0x00FFFFFF) | 0x33000000);
            context.drawBorder(cx2 + 8, cy2 + 6, 30, 14, badgeColor);
            context.drawCenteredTextWithShadow(this.textRenderer, iconBadges[i], cx2 + 23, cy2 + 9, badgeColor);

            // Title
            context.drawText(this.textRenderer, modules[i], cx2 + 8, cy2 + 26, isEn ? 0xFFFFFFFF : 0xFFCBD5E1, true);

            // Toggle switch (bottom right of card)
            boolean swHov = mouseX >= cx2 + cellW - 32 && mouseX <= cx2 + cellW - 6 && mouseY >= cy2 + cellH - 18 && mouseY <= cy2 + cellH - 6;
            VeloraRenderUtil.drawSwitch(context, cx2 + cellW - 32, cy2 + cellH - 18, 26, 12, isEn, swHov);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // X Close
        if (mx >= panelX + panelW - 24 && mx <= panelX + panelW - 6 && my >= panelY + 6 && my <= panelY + 24) {
            this.close();
            return true;
        }

        // Top Cosmetics Button
        int cosBtnX = panelX + panelW - 176;
        if (mx >= cosBtnX && mx <= cosBtnX + 76 && my >= panelY + 7 && my <= panelY + 29) {
            if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen());
            return true;
        }

        // Top HUD Editor Button
        int hudBtnX = panelX + panelW - 92;
        if (mx >= hudBtnX && mx <= hudBtnX + 64 && my >= panelY + 7 && my <= panelY + 29) {
            if (this.client != null) this.client.setScreen(new HudEditorScreen());
            return true;
        }

        // Sidebar Locker Button
        if (mx >= panelX && mx <= panelX + sideW && my >= panelY + 52 && my <= panelY + 92) {
            if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen());
            return true;
        }

        // Module Grid clicks
        int gridX = panelX + sideW + 12;
        int gridY = panelY + titleH + 10;
        int cellW = (panelW - sideW - 24) / 4 - 6;
        int cellH = 66;
        int gap = 6;

        for (int i = 0; i < 17; i++) {
            int col = i % 4;
            int row = i / 4;
            int cx2 = gridX + col * (cellW + gap);
            int cy2 = gridY + row * (cellH + gap);

            if (mx >= cx2 && mx <= cx2 + cellW && my >= cy2 && my <= cy2 + cellH) {
                toggleModule(i);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleModule(int i) {
        switch (i) {
            case 0 -> ModConfig.showFps = !ModConfig.showFps;
            case 1 -> ModConfig.showKeystrokes = !ModConfig.showKeystrokes;
            case 2 -> ModConfig.showPing = !ModConfig.showPing;
            case 3 -> ModConfig.showCps = !ModConfig.showCps;
            case 4 -> ModConfig.showZoom = !ModConfig.showZoom;
            case 5 -> ModConfig.showToggleSprint = !ModConfig.showToggleSprint;
            case 6 -> ModConfig.showToggleSneak = !ModConfig.showToggleSneak;
            case 7 -> ModConfig.showFullbright = !ModConfig.showFullbright;
            case 8 -> ModConfig.showFreeLook = !ModConfig.showFreeLook;
            case 9 -> ModConfig.showSnapLook = !ModConfig.showSnapLook;
            case 10 -> ModConfig.showArmorStatus = !ModConfig.showArmorStatus;
            case 11 -> ModConfig.showCoordinates = !ModConfig.showCoordinates;
            case 12 -> ModConfig.showDayCounter = !ModConfig.showDayCounter;
            case 13 -> ModConfig.showBlockInfo = !ModConfig.showBlockInfo;
            case 14 -> ModConfig.showNoHurtCam = !ModConfig.showNoHurtCam;
            case 15 -> { if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen()); }
            case 16 -> ModConfig.showMinimap = !ModConfig.showMinimap;
        }
        ModConfig.saveConfig();
    }
}
