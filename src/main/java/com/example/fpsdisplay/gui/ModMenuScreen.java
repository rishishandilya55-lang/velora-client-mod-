package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Velora Client – Mod Menu Screen
 * High-contrast purple/black Feather-style layout.
 * Properly visible card backgrounds, toggle switches, and category pills.
 */
public class ModMenuScreen extends Screen {

    // Layout
    private static final int PANEL_W = 600;
    private static final int PANEL_H = 400;
    private static final int SIDE_W  = 60;
    private static final int HDR_H   = 36;

    // Categories
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private int selCat = 0;
    private String search = "";

    // Modules table: {name, category, badge, color}
    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "FPS",  0xFF22D3EE},
        {"WASD Keys",     "HUD",      "KEYS", 0xFF818CF8},
        {"Ping Display",  "HUD",      "MS",   0xFFFBBF24},
        {"CPS Counter",   "HUD",      "CPS",  0xFFF472B6},
        {"Armor Status",  "HUD",      "ARM",  0xFF34D399},
        {"Coordinates",   "HUD",      "XYZ",  0xFF86EFAC},
        {"Day Counter",   "HUD",      "DAY",  0xFFFB923C},
        {"Block Info",    "HUD",      "BLK",  0xFF67E8F9},
        {"Toggle Sprint", "Movement", "SPR",  0xFF60A5FA},
        {"Toggle Sneak",  "Movement", "SNK",  0xFFA78BFA},
        {"Zoom Mod",      "Movement", "ZOOM", 0xFFC084FC},
        {"Free Look",     "Movement", "LOOK", 0xFF4ADE80},
        {"Snap Look",     "Movement", "SNAP", 0xFF2DD4BF},
        {"Fullbright",    "Visual",   "BRT",  0xFFFDE68A},
        {"No Hurt Cam",   "Visual",   "CAM",  0xFFF87171},
        {"Minimap",       "Visual",   "MAP",  0xFF93C5FD},
        {"Capes Locker",  "Visual",   "CAPE", 0xFFE879F9},
    };

    public ModMenuScreen() {
        super(Text.literal("Velora Client – Mods"));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        // Dark overlay behind menu
        ctx.fill(0, 0, this.width, this.height, 0xCC04040C);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        // ── Outer panel ──────────────────────────────────────────────────────
        // Shadow
        ctx.fill(px + 4, py + 4, px + PANEL_W + 4, py + PANEL_H + 4, 0x66000000);
        // Background
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, 0xFF0D0D1A);
        // Purple border
        ctx.drawBorder(px, py, PANEL_W, PANEL_H, 0xFF8B21F7);
        // Inner border inset
        ctx.drawBorder(px + 1, py + 1, PANEL_W - 2, PANEL_H - 2, 0x335533AA);

        // ── Sidebar ───────────────────────────────────────────────────────────
        ctx.fill(px, py, px + SIDE_W, py + PANEL_H, 0xFF090915);
        ctx.fill(px + SIDE_W - 1, py, px + SIDE_W, py + PANEL_H, 0xFF5B21B6);

        // Sidebar items
        drawSideItem(ctx, mx, my, px, py + 10,  "▦",  "MODS",   true);
        drawSideItem(ctx, mx, my, px, py + 70,  "✦",  "LOOKS",  false);
        drawSideItem(ctx, mx, my, px, py + 130, "⊞",  "HUD",    false);
        drawSideItem(ctx, mx, my, px, py + PANEL_H - 55, "⚙", "CFG", false);

        // ── Header ────────────────────────────────────────────────────────────
        int hx = px + SIDE_W;
        ctx.fill(hx, py, px + PANEL_W, py + HDR_H, 0xFF0A0A18);
        ctx.fill(hx, py + HDR_H - 1, px + PANEL_W, py + HDR_H, 0xFF7C3AED);

        // Title
        ctx.drawText(this.textRenderer, "Mod Menu", hx + 10, py + 12, 0xFFA855F7, true);

        // Category pills
        int pillX = hx + 72;
        for (int i = 0; i < CATS.length; i++) {
            boolean sel = (i == selCat);
            boolean hov = inBox(mx, my, pillX, py + 9, 44, 18);
            ctx.fill(pillX, py + 9, pillX + 44, py + 27,
                    sel ? 0xFF6D28D9 : (hov ? 0xFF1E1840 : 0xFF110E24));
            ctx.drawBorder(pillX, py + 9, 44, 18,
                    sel ? 0xFFA855F7 : (hov ? 0xFF4C1D95 : 0xFF2A1A4A));
            ctx.drawCenteredTextWithShadow(this.textRenderer, CATS[i],
                    pillX + 22, py + 13, sel ? 0xFFFFFFFF : (hov ? 0xFFD8B4FE : 0xFF7C6699));
            pillX += 50;
        }

        // Close button
        int cx2 = px + PANEL_W - 20, cy2 = py + 10;
        boolean xHov = inBox(mx, my, cx2, cy2, 14, 16);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "✕", cx2 + 7, cy2 + 2,
                xHov ? 0xFFFF4444 : 0xFF554466);

        // ── Content area ──────────────────────────────────────────────────────
        int cx = hx;
        int cy = py + HDR_H;
        int cw = PANEL_W - SIDE_W;
        int ch = PANEL_H - HDR_H;
        ctx.fill(cx, cy, cx + cw, cy + ch, 0xFF0C0C1E);

        // Draw grid
        drawGrid(ctx, mx, my, cx, cy, cw, ch);

        super.render(ctx, mx, my, delta);
    }

    // ── Sidebar item ─────────────────────────────────────────────────────────
    private void drawSideItem(DrawContext ctx, int mx, int my,
                              int px, int y, String icon, String lbl, boolean active) {
        boolean hov = inBox(mx, my, px, y, SIDE_W, 52);
        if (active) {
            ctx.fill(px, y, px + SIDE_W, y + 52, 0xFF18103A);
            ctx.fill(px, y, px + 3, y + 52, 0xFFA855F7);
        } else if (hov) {
            ctx.fill(px, y, px + SIDE_W, y + 52, 0xFF120C2A);
        }
        int ic = active ? 0xFFA855F7 : (hov ? 0xFFD8B4FE : 0xFF554466);
        int tc = active ? 0xFFE2D9FF : (hov ? 0xFFBBAFDD : 0xFF554466);
        ctx.drawCenteredTextWithShadow(this.textRenderer, icon, px + SIDE_W / 2, y + 10, ic);
        ctx.drawCenteredTextWithShadow(this.textRenderer, lbl,  px + SIDE_W / 2, y + 26, tc);
    }

    // ── Module grid ──────────────────────────────────────────────────────────
    private void drawGrid(DrawContext ctx, int mx, int my,
                          int areaX, int areaY, int areaW, int areaH) {
        int cols = 3;
        int padX = 10, padY = 10, gapX = 8, gapY = 8;
        int cellW = (areaW - padX * 2 - gapX * (cols - 1)) / cols;  // ~170px
        int cellH = 80;

        int col = 0, row = 0;
        for (int i = 0; i < MODS.length; i++) {
            String name  = (String) MODS[i][0];
            String cat   = (String) MODS[i][1];
            String badge = (String) MODS[i][2];
            int    bClr  = (int)    MODS[i][3];
            boolean en   = isEnabled(i);

            if (selCat != 0 && !cat.equals(CATS[selCat])) continue;
            if (!search.isEmpty() && !name.toLowerCase().contains(search)) continue;

            int cx = areaX + padX + col * (cellW + gapX);
            int cy = areaY + padY + row * (cellH + gapY);

            if (cy + cellH > areaY + areaH - 4) break; // overflow guard

            drawCard(ctx, mx, my, cx, cy, cellW, cellH, name, badge, bClr, en);

            col++;
            if (col >= cols) { col = 0; row++; }
        }
    }

    private void drawCard(DrawContext ctx, int mx, int my,
                          int x, int y, int w, int h,
                          String name, String badge, int bClr, boolean en) {
        boolean hov = inBox(mx, my, x, y, w, h);

        // ── Card background (clearly visible) ─────────────────────────────────
        int bg = en
                ? (hov ? 0xFF241A48 : 0xFF1B1238)   // enabled: rich purple-dark
                : (hov ? 0xFF1A1830 : 0xFF131224);   // disabled: near-black blue
        ctx.fill(x, y, x + w, y + h, bg);

        // ── Card border ────────────────────────────────────────────────────────
        int border = en ? 0xFF8B21F7 : (hov ? 0xFF3D2A6A : 0xFF22203A);
        ctx.drawBorder(x, y, w, h, border);

        // ── Left accent bar ────────────────────────────────────────────────────
        if (en) {
            ctx.fill(x, y, x + 3, y + h, 0xFF8B21F7);
        }

        // ── Badge pill ─────────────────────────────────────────────────────────
        int bW = this.textRenderer.getWidth(badge) + 10;
        int bX = x + 8, bY = y + 8;
        ctx.fill(bX, bY, bX + bW, bY + 14, (bClr & 0x00FFFFFF) | 0x33000000);
        ctx.drawBorder(bX, bY, bW, 14, bClr);
        ctx.drawCenteredTextWithShadow(this.textRenderer, badge, bX + bW / 2, bY + 3, bClr);

        // ── Module name ────────────────────────────────────────────────────────
        ctx.drawText(this.textRenderer, name, x + 8, y + 28, 0xFFE8E0FF, true);

        // ── ON/OFF label ──────────────────────────────────────────────────────
        String status = en ? "ON" : "OFF";
        int statusClr = en ? 0xFF86EFAC : 0xFF664477;
        ctx.drawText(this.textRenderer, status, x + 8, y + 42, statusClr, false);

        // ── Toggle switch (bottom right) ──────────────────────────────────────
        int swW = 34, swH = 14;
        int swX = x + w - swW - 8;
        int swY = y + h - swH - 10;
        drawToggle(ctx, swX, swY, swW, swH, en);
    }

    /** Pill-shaped toggle switch, clearly visible */
    private void drawToggle(DrawContext ctx, int x, int y, int w, int h, boolean on) {
        // Track
        int trackBg = on ? 0xFF6D28D9 : 0xFF1E1A34;
        int trackBorder = on ? 0xFFA855F7 : 0xFF3D2A6A;
        ctx.fill(x, y, x + w, y + h, trackBg);
        ctx.drawBorder(x, y, w, h, trackBorder);

        // Knob (white circle-ish square)
        int kSz = h - 4;
        int kx = on ? (x + w - kSz - 2) : (x + 2);
        ctx.fill(kx, y + 2, kx + kSz, y + 2 + kSz, 0xFFFFFFFF);
        // Knob inner highlight
        ctx.fill(kx + 1, y + 3, kx + kSz - 1, y + 3 + kSz - 2,
                on ? 0xFFD8B4FE : 0xFFAA99BB);
    }

    private boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INPUT
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int px = (this.width - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // Close
        int cx2 = px + PANEL_W - 20, cy2 = py + 10;
        if (inBox(mx, my, cx2, cy2, 14, 16)) { this.close(); return true; }

        // Category pills
        int pillX = px + SIDE_W + 72;
        for (int i = 0; i < CATS.length; i++) {
            if (inBox(mx, my, pillX, py + 9, 44, 18)) { selCat = i; return true; }
            pillX += 50;
        }

        // Sidebar
        if (inBox(mx, my, px, py + 70, SIDE_W, 52)) {
            if (client != null) client.setScreen(new CosmeticsLockerScreen()); return true;
        }
        if (inBox(mx, my, px, py + 130, SIDE_W, 52)) {
            if (client != null) client.setScreen(new HudEditorScreen()); return true;
        }

        // Module cards
        int hx = px + SIDE_W;
        int areaX = hx, areaY = py + HDR_H;
        int areaW = PANEL_W - SIDE_W;
        int cols = 3, padX = 10, padY = 10, gapX = 8, gapY = 8;
        int cellW = (areaW - padX * 2 - gapX * (cols - 1)) / cols;
        int cellH = 80;

        int col = 0, row = 0;
        for (int i = 0; i < MODS.length; i++) {
            String cat  = (String) MODS[i][1];
            String name = (String) MODS[i][0];
            if (selCat != 0 && !cat.equals(CATS[selCat])) continue;
            if (!search.isEmpty() && !name.toLowerCase().contains(search)) continue;

            int cx3 = areaX + padX + col * (cellW + gapX);
            int cy3 = areaY + padY + row * (cellH + gapY);

            if (inBox(mx, my, cx3, cy3, cellW, cellH)) {
                handleModClick(i); return true;
            }

            col++;
            if (col >= cols) { col = 0; row++; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            search += Character.toLowerCase(chr);
            selCat = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259 && !search.isEmpty()) { // Backspace
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private boolean isEnabled(int i) {
        return switch (i) {
            case 0  -> ModConfig.showFps;
            case 1  -> ModConfig.showKeystrokes;
            case 2  -> ModConfig.showPing;
            case 3  -> ModConfig.showCps;
            case 4  -> ModConfig.showArmorStatus;
            case 5  -> ModConfig.showCoordinates;
            case 6  -> ModConfig.showDayCounter;
            case 7  -> ModConfig.showBlockInfo;
            case 8  -> ModConfig.showToggleSprint;
            case 9  -> ModConfig.showToggleSneak;
            case 10 -> ModConfig.showZoom;
            case 11 -> ModConfig.showFreeLook;
            case 12 -> ModConfig.showSnapLook;
            case 13 -> ModConfig.showFullbright;
            case 14 -> ModConfig.showNoHurtCam;
            case 15 -> ModConfig.showMinimap;
            case 16 -> ModConfig.enableCape;
            default -> false;
        };
    }

    private void handleModClick(int i) {
        switch (i) {
            case 0  -> ModConfig.showFps           = !ModConfig.showFps;
            case 1  -> ModConfig.showKeystrokes    = !ModConfig.showKeystrokes;
            case 2  -> ModConfig.showPing          = !ModConfig.showPing;
            case 3  -> ModConfig.showCps           = !ModConfig.showCps;
            case 4  -> ModConfig.showArmorStatus   = !ModConfig.showArmorStatus;
            case 5  -> ModConfig.showCoordinates   = !ModConfig.showCoordinates;
            case 6  -> ModConfig.showDayCounter    = !ModConfig.showDayCounter;
            case 7  -> ModConfig.showBlockInfo     = !ModConfig.showBlockInfo;
            case 8  -> ModConfig.showToggleSprint  = !ModConfig.showToggleSprint;
            case 9  -> ModConfig.showToggleSneak   = !ModConfig.showToggleSneak;
            case 10 -> ModConfig.showZoom          = !ModConfig.showZoom;
            case 11 -> ModConfig.showFreeLook      = !ModConfig.showFreeLook;
            case 12 -> ModConfig.showSnapLook      = !ModConfig.showSnapLook;
            case 13 -> ModConfig.showFullbright    = !ModConfig.showFullbright;
            case 14 -> ModConfig.showNoHurtCam     = !ModConfig.showNoHurtCam;
            case 15 -> ModConfig.showMinimap       = !ModConfig.showMinimap;
            case 16 -> { if (client != null) client.setScreen(new CosmeticsLockerScreen()); return; }
        }
        ModConfig.saveConfig();
    }

    @Override
    public boolean shouldPause() { return false; }
}
