package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Velora Client – Mod Menu Screen
 * Layout: sidebar icon tabs + top category pills + search bar + 3-column module card grid.
 * Theme: deep black / rich purple (matches Velora brand).
 */
public class ModMenuScreen extends Screen {

    // ─── palette ───────────────────────────────────────────────────────────────
    // Blacks / panels
    private static final int C_BG         = 0xFF080810;  // Screen backdrop
    private static final int C_PANEL      = 0xFF0E0E1A;  // Outer window
    private static final int C_SIDE       = 0xFF0A0A14;  // Sidebar
    private static final int C_TITLE      = 0xFF0C0C18;  // Title bar
    private static final int C_CONTENT    = 0xFF101020;  // Content area bg
    private static final int C_CARD       = 0xFF141428;  // Module card bg
    private static final int C_CARD_HOV   = 0xFF1E1A38;  // Module card hovered
    private static final int C_CARD_ON    = 0xFF1A1436;  // Module card enabled
    private static final int C_CARD_ON_H  = 0xFF241B44;  // Module card enabled+hovered

    // Purple accents
    private static final int C_PURPLE     = 0xFFA855F7;  // Primary purple
    private static final int C_PURPLE_L   = 0xFFD8B4FE;  // Light purple / text
    private static final int C_PURPLE_D   = 0xFF7E22CE;  // Deep purple
    private static final int C_PURPLE_G   = 0xFF6D28D9;  // Gradient deep

    // Text
    private static final int C_TXT_W     = 0xFFFFFFFF;
    private static final int C_TXT_M     = 0xFFE2E2F0;  // Medium text
    private static final int C_TXT_DIM   = 0xFF8888AA;  // Dimmed

    // Misc
    private static final int C_SWITCH_ON  = 0xFFA855F7;
    private static final int C_SWITCH_OFF = 0xFF3A3A5A;
    private static final int C_SEP        = 0xFF1E1E38;  // Separator lines

    // ─── layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W  = 590;
    private static final int PANEL_H  = 390;
    private static final int SIDE_W   = 54;
    private static final int TITLE_H  = 42;

    // Sidebar icon slots
    private static final int SLOT_H   = 46;

    // Category tabs
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private int selectedCat = 0;

    // Search
    private String searchText = "";

    // Modules: [name, category, badgeLabel, badge-color(hex)]
    private static final Object[][] MODULES = {
        // name                category    badge   color
        {"FPS Display",        "HUD",      "FPS",  0xFF22C55E},
        {"WASD Keys",          "HUD",      "KEYS", 0xFF06B6D4},
        {"Ping Display",       "HUD",      "MS",   0xFFEAB308},
        {"CPS Counter",        "HUD",      "CPS",  0xFFEC4899},
        {"Armor Status",       "HUD",      "ARM",  0xFF6366F1},
        {"Coordinates",        "HUD",      "XYZ",  0xFF84CC16},
        {"Day Counter",        "HUD",      "DAY",  0xFFF97316},
        {"Block Info",         "HUD",      "BLK",  0xFF38BDF8},
        {"Toggle Sprint",      "Movement", "SPR",  0xFF3B82F6},
        {"Toggle Sneak",       "Movement", "SNK",  0xFF8B5CF6},
        {"Zoom Mod",           "Movement", "ZOOM", 0xFFA855F7},
        {"Free Look",          "Movement", "LOOK", 0xFF10B981},
        {"Snap Look",          "Movement", "SNAP", 0xFF14B8A6},
        {"Fullbright",         "Visual",   "BRT",  0xFFF59E0B},
        {"No Hurt Cam",        "Visual",   "CAM",  0xFFEF4444},
        {"Minimap",            "Visual",   "MAP",  0xFF3B82F6},
        {"Capes Locker",       "Visual",   "CAPE", 0xFFA855F7},
    };

    // ─── constructor ───────────────────────────────────────────────────────────
    public ModMenuScreen() {
        super(Text.literal("Velora Client – Mods"));
    }

    // ─── background ────────────────────────────────────────────────────────────
    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, C_BG);
    }

    // ─── main render ───────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        int px = (this.width  - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;

        drawWindow(ctx, px, py, mx, my);
        drawSidebar(ctx, px, py, mx, my);
        drawTitleBar(ctx, px, py, mx, my);
        drawContent(ctx, px, py, mx, my);

        super.render(ctx, mx, my, delta);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WINDOW FRAME
    // ══════════════════════════════════════════════════════════════════════════
    private void drawWindow(DrawContext ctx, int px, int py, int mx, int my) {
        // Outer glow
        VeloraRenderUtil.drawGlowBorder(ctx, px, py, PANEL_W, PANEL_H, 0x55A855F7, 4);

        // Panel body
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, C_PANEL);

        // Crisp purple border
        ctx.drawBorder(px, py, PANEL_W, PANEL_H, C_PURPLE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private void drawSidebar(DrawContext ctx, int px, int py, int mx, int my) {
        int sx = px;
        int sy = py;

        // Sidebar bg
        ctx.fill(sx, sy, sx + SIDE_W, sy + PANEL_H, C_SIDE);

        // Right separator line
        ctx.fill(sx + SIDE_W - 1, sy, sx + SIDE_W, sy + PANEL_H, C_PURPLE_D);

        // Velora 'V' logo at top
        int logoY = sy + 8;
        drawVLogo(ctx, sx + SIDE_W / 2, logoY);

        // Divider under logo
        ctx.fill(sx + 6, logoY + 22, sx + SIDE_W - 6, logoY + 23, C_SEP);

        // Slot 0 – Mods (active)
        drawSideSlot(ctx, mx, my, sx, sy + 36, "MODS", "▦", true);

        // Slot 1 – Cosmetics
        boolean cosHov = isSideSlotHov(mx, my, sx, sy + 36 + SLOT_H);
        drawSideSlot(ctx, mx, my, sx, sy + 36 + SLOT_H, "LOOKS", "✦", false);

        // Slot 2 – HUD
        drawSideSlot(ctx, mx, my, sx, sy + 36 + SLOT_H * 2, "HUD", "⊞", false);

        // Divider
        ctx.fill(sx + 6, sy + PANEL_H - 56, sx + SIDE_W - 6, sy + PANEL_H - 55, C_SEP);

        // Slot bottom – Settings
        drawSideSlot(ctx, mx, my, sx, sy + PANEL_H - 52, "CFG", "⚙", false);
    }

    private boolean isSideSlotHov(int mx, int my, int slotX, int slotY) {
        return mx >= slotX && mx <= slotX + SIDE_W && my >= slotY && my <= slotY + SLOT_H;
    }

    private void drawSideSlot(DrawContext ctx, int mx, int my, int slotX, int slotY,
                               String label, String icon, boolean active) {
        boolean hov = isSideSlotHov(mx, my, slotX, slotY);

        if (active) {
            // Active: purple left bar + tinted bg
            ctx.fill(slotX, slotY, slotX + SIDE_W, slotY + SLOT_H, 0x33A855F7);
            ctx.fill(slotX, slotY, slotX + 3, slotY + SLOT_H, C_PURPLE);
        } else if (hov) {
            ctx.fill(slotX, slotY, slotX + SIDE_W, slotY + SLOT_H, 0x18A855F7);
        }

        int iconColor = active ? C_PURPLE : (hov ? C_PURPLE_L : C_TXT_DIM);
        int txtColor  = active ? C_TXT_W   : (hov ? C_TXT_M    : C_TXT_DIM);

        ctx.drawCenteredTextWithShadow(this.textRenderer, icon,  slotX + SIDE_W / 2, slotY + 8,  iconColor);
        ctx.drawCenteredTextWithShadow(this.textRenderer, label, slotX + SIDE_W / 2, slotY + 22, txtColor);
    }

    /** Draw a stylised "V" (Velora logo) using pixel fills */
    private void drawVLogo(DrawContext ctx, int cx, int y) {
        int p = C_PURPLE;
        int w = 12; // half-width of V
        // Left arm of V
        for (int i = 0; i < 10; i++) {
            ctx.fill(cx - w + i, y + i / 2, cx - w + i + 2, y + i / 2 + 2, p);
        }
        // Right arm of V
        for (int i = 0; i < 10; i++) {
            ctx.fill(cx + w - i - 2, y + i / 2, cx + w - i, y + i / 2 + 2, p);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TITLE BAR
    // ══════════════════════════════════════════════════════════════════════════
    private void drawTitleBar(DrawContext ctx, int px, int py, int mx, int my) {
        int bx = px + SIDE_W;
        int by = py;
        int bw = PANEL_W - SIDE_W;

        // Title bar bg
        ctx.fill(bx, by, bx + bw, by + TITLE_H, C_TITLE);

        // Bottom separator line (purple)
        ctx.fill(bx, by + TITLE_H - 1, bx + bw, by + TITLE_H, C_PURPLE_D);

        // "Mod Menu" text
        ctx.drawText(this.textRenderer, "Mod Menu", bx + 14, by + 10, C_PURPLE, true);

        // ── Category pills ──────────────────────────────────────────────
        int pillX = bx + 88;
        int pillY = by + 10;
        for (int i = 0; i < CATS.length; i++) {
            boolean sel = (i == selectedCat);
            boolean hov = mx >= pillX && mx <= pillX + 40 && my >= pillY && my <= pillY + 18;
            int pillBg    = sel ? C_PURPLE    : (hov ? 0xFF1E1E3A : 0xFF16162A);
            int pillBdr   = sel ? C_PURPLE    : (hov ? 0xFF5533AA : C_SEP);
            int pillTxt   = sel ? C_TXT_W     : (hov ? C_PURPLE_L : C_TXT_DIM);

            ctx.fill(pillX, pillY, pillX + 40, pillY + 18, pillBg);
            ctx.drawBorder(pillX, pillY, 40, 18, pillBdr);
            ctx.drawCenteredTextWithShadow(this.textRenderer, CATS[i], pillX + 20, pillY + 5, pillTxt);
            pillX += 46;
        }

        // ── Search box ──────────────────────────────────────────────────
        int searchX = bx + bw - 136;
        int searchY = by + 11;
        int searchW = 110;
        int searchH = 18;
        ctx.fill(searchX, searchY, searchX + searchW, searchY + searchH, 0xFF0E0E1C);
        ctx.drawBorder(searchX, searchY, searchW, searchH, searchText.isEmpty() ? 0xFF2A2A44 : C_PURPLE_D);
        String searchDisplay = searchText.isEmpty() ? "Search Mods" : searchText;
        int searchColor = searchText.isEmpty() ? C_TXT_DIM : C_TXT_M;
        ctx.drawText(this.textRenderer, searchDisplay, searchX + 8, searchY + 5, searchColor, false);
        // Search icon (magnifier ≈ pixel art)
        ctx.drawText(this.textRenderer, "⌕", searchX + searchW - 12, searchY + 5, C_TXT_DIM, false);

        // ── Close button ────────────────────────────────────────────────
        int closX = bx + bw - 18;
        int closY = by + 12;
        boolean closHov = mx >= closX && mx <= closX + 14 && my >= closY && my <= closY + 16;
        int closColor = closHov ? 0xFFEF4444 : C_TXT_DIM;
        ctx.drawCenteredTextWithShadow(this.textRenderer, "✕", closX + 7, closY + 2, closColor);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONTENT – module grid
    // ══════════════════════════════════════════════════════════════════════════
    private void drawContent(DrawContext ctx, int px, int py, int mx, int my) {
        int cx = px + SIDE_W;
        int cy = py + TITLE_H;
        int cw = PANEL_W - SIDE_W;
        int ch = PANEL_H - TITLE_H;

        // Content bg
        ctx.fill(cx, cy, cx + cw, cy + ch, C_CONTENT);

        // ── Module grid ──────────────────────────────────────────────────────
        int cols    = 3;
        int cellW   = (cw - 16 - (cols - 1) * 8) / cols;
        int cellH   = 74;
        int gapX    = 8;
        int gapY    = 8;
        int startX  = cx + 8;
        int startY  = cy + 10;

        int col = 0, row = 0;
        for (int i = 0; i < MODULES.length; i++) {
            String modName = (String) MODULES[i][0];
            String cat     = (String) MODULES[i][1];
            String badge   = (String) MODULES[i][2];
            int    bColor  = (int)    MODULES[i][3];
            boolean enabled = isEnabled(i);

            // Category filter
            if (selectedCat != 0) {
                if (!cat.equals(CATS[selectedCat])) continue;
            }
            // Search filter
            if (!searchText.isEmpty() && !modName.toLowerCase().contains(searchText.toLowerCase())) {
                continue;
            }

            int cardX = startX + col * (cellW + gapX);
            int cardY = startY + row * (cellH + gapY);

            // Overflow guard
            if (cardY + cellH > cy + ch - 4) break;

            drawModCard(ctx, mx, my, cardX, cardY, cellW, cellH, modName, badge, bColor, enabled, i);

            col++;
            if (col >= cols) { col = 0; row++; }
        }
    }

    // ─── single module card ────────────────────────────────────────────────
    private void drawModCard(DrawContext ctx, int mx, int my,
                             int x, int y, int w, int h,
                             String name, String badge, int bColor,
                             boolean enabled, int idx) {

        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;

        // Card background
        int bg = enabled
                ? (hov ? C_CARD_ON_H : C_CARD_ON)
                : (hov ? C_CARD_HOV  : C_CARD);
        ctx.fill(x, y, x + w, y + h, bg);

        // Border
        int border = enabled ? C_PURPLE : (hov ? 0xFF3A2A6A : 0xFF1E1E38);
        ctx.drawBorder(x, y, w, h, border);

        // Left accent bar (enabled = purple, disabled = dark)
        int accent = enabled ? C_PURPLE : 0xFF2A2A44;
        ctx.fill(x, y, x + 3, y + h, accent);

        // ── Badge (top-left icon area) ──────────────────────────────────
        int badgeBg = (bColor & 0x00FFFFFF) | 0x28000000;
        ctx.fill(x + 8, y + 8, x + 42, y + 22, badgeBg);
        ctx.drawBorder(x + 8, y + 8, 34, 14, bColor);
        ctx.drawCenteredTextWithShadow(this.textRenderer, badge, x + 25, y + 11, bColor);

        // ── Module name ────────────────────────────────────────────────
        int nameColor = enabled ? C_TXT_W : C_TXT_M;
        ctx.drawText(this.textRenderer, name, x + 8, y + 28, nameColor, true);

        // ── Status label ───────────────────────────────────────────────
        String statusTxt = enabled ? "ACTIVE" : "INACTIVE";
        int statusColor  = enabled ? 0xFF88AAFF : 0xFF554466;
        ctx.drawText(this.textRenderer, statusTxt, x + 8, y + 40, statusColor, false);

        // ── Toggle switch (bottom-right) ───────────────────────────────
        int swW = 28, swH = 12;
        int swX = x + w - swW - 8;
        int swY = y + h - swH - 8;
        drawSwitch(ctx, swX, swY, swW, swH, enabled, hov);

        // ── Settings gear icon (bottom-left, if hovered) ───────────────
        if (hov) {
            ctx.drawText(this.textRenderer, "⚙", x + 8, y + h - 14, 0x66D8B4FE, false);
        }
    }

    /** Custom purple toggle switch */
    private void drawSwitch(DrawContext ctx, int x, int y, int w, int h, boolean on, boolean hov) {
        // Track
        int trackBg  = on ? (hov ? 0xFF9333EA : C_SWITCH_ON) : (hov ? 0xFF4A3A6A : C_SWITCH_OFF);
        ctx.fill(x, y, x + w, y + h, trackBg);
        int trackBorder = on ? 0xFFD8B4FE : 0xFF5A4A7A;
        ctx.drawBorder(x, y, w, h, trackBorder);

        // Knob
        int knobSz = h - 4;
        int knobX  = on ? x + w - knobSz - 2 : x + 2;
        int knobY  = y + 2;
        ctx.fill(knobX, knobY, knobX + knobSz, knobY + knobSz, 0xFFFFFFFF);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INPUT
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int px = (this.width  - PANEL_W) / 2;
        int py = (this.height - PANEL_H) / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // ── Close ────────────────────────────────────────────────────────────
        int closX = px + PANEL_W - 18;
        int closY = py + 12;
        if (mx >= closX && mx <= closX + 14 && my >= closY && my <= closY + 16) {
            this.close(); return true;
        }

        // ── Category pills ────────────────────────────────────────────────────
        int pillX = px + SIDE_W + 88;
        int pillY = py + 10;
        for (int i = 0; i < CATS.length; i++) {
            if (mx >= pillX && mx <= pillX + 40 && my >= pillY && my <= pillY + 18) {
                selectedCat = i; return true;
            }
            pillX += 46;
        }

        // ── Sidebar: Cosmetics ────────────────────────────────────────────────
        int sbCosY = py + 36 + SLOT_H;
        if (mx >= px && mx <= px + SIDE_W && my >= sbCosY && my <= sbCosY + SLOT_H) {
            if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen());
            return true;
        }

        // ── Sidebar: HUD editor ───────────────────────────────────────────────
        int sbHudY = py + 36 + SLOT_H * 2;
        if (mx >= px && mx <= px + SIDE_W && my >= sbHudY && my <= sbHudY + SLOT_H) {
            if (this.client != null) this.client.setScreen(new HudEditorScreen());
            return true;
        }

        // ── Module cards ──────────────────────────────────────────────────────
        int cx = px + SIDE_W;
        int cy = px + TITLE_H;  // Unused but kept for clarity

        int cols   = 3;
        int cw     = PANEL_W - SIDE_W;
        int cellW  = (cw - 16 - (cols - 1) * 8) / cols;
        int cellH  = 74;
        int startX = px + SIDE_W + 8;
        int startY = py + TITLE_H + 10;

        int col = 0, row = 0;
        for (int i = 0; i < MODULES.length; i++) {
            String cat = (String) MODULES[i][1];
            if (selectedCat != 0 && !cat.equals(CATS[selectedCat])) continue;
            String modName = (String) MODULES[i][0];
            if (!searchText.isEmpty() && !modName.toLowerCase().contains(searchText.toLowerCase())) continue;

            int cardX = startX + col * (cellW + 8);
            int cardY = startY + row * (cellH + 8);

            if (mx >= cardX && mx <= cardX + cellW && my >= cardY && my <= cardY + cellH) {
                toggleModule(i);
                return true;
            }

            col++;
            if (col >= cols) { col = 0; row++; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            searchText += chr;
            selectedCat = 0; // reset to All when searching
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Backspace clears search
        if (keyCode == 259 && !searchText.isEmpty()) {
            searchText = searchText.substring(0, searchText.length() - 1);
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
            case 16 -> true; // Capes Locker always reachable
            default -> false;
        };
    }

    private void toggleModule(int i) {
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
            case 16 -> { if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen()); }
        }
        ModConfig.saveConfig();
    }

    @Override
    public boolean shouldPause() { return false; }
}
