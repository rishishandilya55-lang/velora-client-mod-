package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

/**
 * Velora Cosmetics Locker.
 * Features 360° 3D player model preview starting at 180° (showing back capes & wings clearly),
 * favorite toggling, and instant equip/unequip for all capes.
 */
public class CosmeticsLockerScreen extends Screen {

    // State
    private int activeCategory  = 0; // 0=All, 1=Favorites, 2=Capes, 3=Hats, 4=Face, 5=Wings, 6=Aura
    private int activeEnv       = 0;
    private boolean showOptions = false;
    private static int selectedItem = 0;
    private String searchQuery  = "";
    private TextFieldWidget searchBox;

    // Preview rotation (yaw = 0 => 180° turned around so BACK of player faces viewer)
    private static boolean isLockerOpen = false;
    private float yaw   = 0f;
    private float pitch = 0f;
    private boolean dragging = false;
    private boolean autoSpin = false;
    private float animTick = 0f;

    // Cosmetics data
    private static final String[] COS_NAMES  = {"Velora Cape", "Classic Cape", "Wave Cape"};
    private static final String[] COS_TYPES  = {"HD Animated", "Vintage",      "Particle"};
    private static final int[]    COS_COLORS = {0xFF8B21F7,    0xFF2563EB,     0xFF059669};

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Locker"));
    }

    public static boolean isPreviewingCape() { return isLockerOpen; }

    public static int getPreviewingCapeIndex() {
        return isLockerOpen ? selectedItem : -1;
    }

    @Override
    protected void init() {
        super.init();
        isLockerOpen = true;
        int[] layout = getLayout();
        int centerX = layout[0] + layout[2] + 6;
        int centerW = layout[4];
        int panelY  = layout[1];
        searchBox = new TextFieldWidget(this.textRenderer,
                centerX + 8, panelY + 44, centerW - 16, 18, Text.literal("Search"));
        searchBox.setPlaceholder(Text.literal("Search cosmetics..."));
        searchBox.setMaxLength(32);
        searchBox.setChangedListener(t -> searchQuery = t != null ? t.toLowerCase().trim() : "");
        this.addSelectableChild(searchBox);
    }

    @Override
    public void close() { isLockerOpen = false; super.close(); }

    private int[] getLayout() {
        int panelW   = Math.min(760, this.width  - 20);
        int panelH   = Math.min(440, this.height - 20);
        int panelX   = (this.width  - panelW) / 2;
        int panelY   = (this.height - panelH) / 2;
        int sideW    = 130;
        int previewW = 210;
        int centerW  = panelW - sideW - previewW - 12;
        return new int[]{panelX, panelY, sideW, previewW, centerW, panelW, panelH};
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xBB040410);
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        animTick += delta;
        if (autoSpin) yaw = (yaw + delta * 1.8f) % 360f;

        int[] L   = getLayout();
        int panelX = L[0], panelY = L[1], sideW = L[2], previewW = L[3],
            centerW = L[4], panelW = L[5], panelH = L[6];
        int centerX = panelX + sideW + 6;
        int rightX  = centerX + centerW + 6;

        // Outer panel
        ctx.fill(panelX + 3, panelY + 3, panelX + panelW + 3, panelY + panelH + 3, 0x55000000);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF0D0D1E);
        ctx.drawBorder(panelX, panelY, panelW, panelH, 0xFF8B21F7);
        ctx.drawBorder(panelX + 1, panelY + 1, panelW - 2, panelH - 2, 0x334C1D95);

        // Title bar
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 36, 0xFF08081A);
        ctx.fill(panelX, panelY + 35, panelX + panelW, panelY + 36, 0xFF7C3AED);
        ctx.drawText(this.textRenderer, "✦  VELORA COSMETICS LOCKER", panelX + 14, panelY + 13, 0xFFFFFFFF, true);

        // X close
        int xbX = panelX + panelW - 22, xbY = panelY + 10;
        boolean xHov = mx >= xbX && mx <= xbX + 14 && my >= xbY && my <= xbY + 16;
        ctx.drawCenteredTextWithShadow(this.textRenderer, "✕", xbX + 7, xbY + 2,
                xHov ? 0xFFFF4444 : 0xFF553355);

        drawSidebar(ctx, mx, my, panelX, panelY, sideW, panelW, panelH);
        drawCenterGrid(ctx, mx, my, panelX, panelY, panelH, centerX, centerW);
        drawPreviewPanel(ctx, mx, my, panelX, panelY, panelH, rightX, previewW, delta);

        if (showOptions) drawOptionsModal(ctx, mx, my);

        super.render(ctx, mx, my, delta);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private void drawSidebar(DrawContext ctx, int mx, int my,
                             int px, int py, int sideW, int panelW, int panelH) {
        ctx.fill(px, py + 36, px + sideW, py + panelH, 0xFF09091A);
        ctx.fill(px + sideW - 1, py + 36, px + sideW, py + panelH, 0xFF5B21B6);

        String[] cats = {"All", "Favorites", "Capes", "Hats", "Face", "Wings", "Aura"};
        for (int i = 0; i < cats.length; i++) {
            int ry = py + 44 + i * 26;
            boolean sel = (activeCategory == i);
            boolean hov = mx >= px + 4 && mx <= px + sideW - 4 && my >= ry && my <= ry + 22;
            ctx.fill(px + 4, ry, px + sideW - 4, ry + 22,
                    sel ? 0xFF21104A : (hov ? 0xFF160E30 : 0xFF0D0D1A));
            ctx.drawBorder(px + 4, ry, sideW - 8, 22,
                    sel ? 0xFF8B21F7 : (hov ? 0xFF3D1D6A : 0xFF1A1830));
            if (sel) ctx.fill(px + 4, ry, px + 7, ry + 22, 0xFF8B21F7);
            ctx.drawText(this.textRenderer, cats[i], px + 12, ry + 7,
                    sel ? 0xFFFFFFFF : (hov ? 0xFFD8B4FE : 0xFF7766AA), sel);
        }

        // Options button
        int opY = py + panelH - 30;
        boolean opHov = mx >= px + 6 && mx <= px + sideW - 6 && my >= opY && my <= opY + 22;
        ctx.fill(px + 6, opY, px + sideW - 6, opY + 22,
                opHov ? 0xFF21104A : 0xFF120A28);
        ctx.drawBorder(px + 6, opY, sideW - 12, 22,
                opHov ? 0xFF8B21F7 : 0xFF3D1D6A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "⚙  Options",
                px + sideW / 2, opY + 7,
                opHov ? 0xFFD8B4FE : 0xFF8855BB);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CENTER GRID
    // ══════════════════════════════════════════════════════════════════════════
    private void drawCenterGrid(DrawContext ctx, int mx, int my,
                                int panelX, int panelY, int panelH,
                                int cx, int cw) {
        ctx.fill(cx, panelY + 36, cx + cw, panelY + panelH, 0xFF0C0C1E);

        if (searchBox != null) searchBox.render(ctx, mx, my, 0f);

        int gridY = panelY + 70;
        int cardW = 88, cardH = 118, gap = 8;
        int cardIndex = 0;

        for (int i = 0; i < COS_NAMES.length; i++) {
            if (activeCategory == 1 && (ModConfig.favoriteCosmetics == null || !ModConfig.favoriteCosmetics[i])) continue;
            if (activeCategory >= 3) continue;
            if (!searchQuery.isEmpty() && !COS_NAMES[i].toLowerCase().contains(searchQuery)) continue;

            int col = cardIndex % 3, row = cardIndex / 3;
            int cardX = cx + 8 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);

            boolean sel = (selectedItem == i);
            boolean hov = mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH;

            // Card body
            ctx.fill(cardX, cardY, cardX + cardW, cardY + cardH,
                    sel ? 0xFF1E1040 : (hov ? 0xFF16102C : 0xFF100E20));
            ctx.drawBorder(cardX, cardY, cardW, cardH,
                    sel ? 0xFF8B21F7 : (hov ? 0xFF4C2880 : 0xFF221A40));
            if (sel) ctx.fill(cardX, cardY, cardX + cardW, cardY + 2, 0xFF4ADE80);

            // Favorite star
            boolean isFav = (ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics[i]);
            boolean starHov = mx >= cardX + 2 && mx <= cardX + 22 && my >= cardY + 2 && my <= cardY + 22;
            int starColor = isFav ? 0xFFFBBF24 : (starHov ? 0xFFD8B4FE : 0x44AAAAAA);
            ctx.drawText(this.textRenderer, "★", cardX + 5, cardY + 5, starColor, false);

            // Cape thumbnail
            drawCapeThumbnail(ctx, cardX, cardY + 16, cardW, COS_COLORS[i], i);

            // Name & type
            ctx.drawCenteredTextWithShadow(this.textRenderer, COS_NAMES[i],
                    cardX + cardW / 2, cardY + 94, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, COS_TYPES[i],
                    cardX + cardW / 2, cardY + 106, 0xFF7755AA);

            cardIndex++;
        }
    }

    private void drawCapeThumbnail(DrawContext ctx, int cx, int cy, int cw, int color, int idx) {
        int capeW = 52, capeH = 64;
        int x = cx + (cw - capeW) / 2;
        int y = cy;

        for (int row = 0; row < capeH; row++) {
            float t = (float) row / capeH;
            int alpha = (int) (255 * (1f - t * 0.45f));
            int c = (alpha << 24) | (color & 0x00FFFFFF);
            ctx.fill(x, y + row, x + capeW, y + row + 1, c);
        }

        ctx.drawBorder(x, y, capeW, capeH, color);

        for (int r = 1; r < 4; r++) {
            int ly = y + r * (capeH / 4);
            ctx.fill(x + 2, ly, x + capeW - 2, ly + 1, 0x33FFFFFF);
        }

        ctx.fill(x, y, x + capeW, y + 5, 0x44000000);

        int clipW = 12;
        ctx.fill(x + (capeW - clipW) / 2, y - 4, x + (capeW + clipW) / 2, y + 1, 0xFF888888);
        ctx.drawBorder(x + (capeW - clipW) / 2, y - 4, clipW, 5, 0xFFCCCCCC);

        switch (idx) {
            case 0 -> {
                int w = 0x88FFFFFF, mx2 = x + capeW / 2, my2 = y + 22;
                for (int i = 0; i < 8; i++) {
                    ctx.fill(mx2 - 8 + i, my2 + i, mx2 - 8 + i + 2, my2 + i + 2, w);
                    ctx.fill(mx2 + 8 - i - 1, my2 + i, mx2 + 8 - i + 1, my2 + i + 2, w);
                }
            }
            case 1 -> {
                int w = 0x66FFFFFF, mx2 = x + capeW / 2, my2 = y + capeH / 2;
                ctx.fill(mx2 - 10, my2 - 1, mx2 + 10, my2 + 1, w);
                ctx.fill(mx2 - 1, my2 - 10, mx2 + 1, my2 + 10, w);
            }
            case 2 -> {
                for (int row = 0; row < 3; row++) {
                    int by = y + 14 + row * 16;
                    for (int px = 2; px < capeW - 2; px += 3) {
                        int wy = by + (int)(Math.sin(px * 0.5f + row * 2) * 2f);
                        ctx.fill(x + px, wy, x + px + 2, wy + 2, 0x77FFFFFF);
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RIGHT PREVIEW PANEL (Player Model showing BACKSIDE with capes & wings)
    // ══════════════════════════════════════════════════════════════════════════
    private void drawPreviewPanel(DrawContext ctx, int mx, int my,
                                  int panelX, int panelY, int panelH,
                                  int rightX, int previewW, float delta) {
        ctx.fill(rightX, panelY + 36, rightX + previewW, panelY + panelH, 0xFF0C0C1A);
        ctx.fill(rightX, panelY + 36, rightX + 1, panelY + panelH, 0xFF3D1D6A);

        // Env tabs
        String[] envs = {"Sun", "World", "Hell", "End"};
        int envW = previewW / 4;
        for (int i = 0; i < envs.length; i++) {
            int ex = rightX + i * envW;
            boolean sel = (activeEnv == i);
            boolean hov = mx >= ex && mx <= ex + envW && my >= panelY + 36 && my <= panelY + 52;
            ctx.fill(ex, panelY + 36, ex + envW, panelY + 52,
                    sel ? 0xFF1A0E3A : (hov ? 0xFF130A28 : 0xFF0C0C1A));
            if (sel) ctx.fill(ex, panelY + 50, ex + envW, panelY + 52, 0xFF8B21F7);
            ctx.drawCenteredTextWithShadow(this.textRenderer, envs[i],
                    ex + envW / 2, panelY + 40,
                    sel ? 0xFFD8B4FE : (hov ? 0xFF8855BB : 0xFF443355));
        }

        // 3D Preview viewport
        int pW = previewW - 16, pH = 220;
        int pX = rightX + 8, pY = panelY + 56;

        int[] envBg = {0xFF0C0C1E, 0xFF081A0C, 0xFF1A0808, 0xFF0A0A0A};
        ctx.fill(pX, pY, pX + pW, pY + pH, envBg[activeEnv]);

        for (int gx = 0; gx <= pW; gx += 18)
            ctx.fill(pX + gx, pY, pX + gx + 1, pY + pH, 0x0AFFFFFF);
        for (int gy = 0; gy <= pH; gy += 18)
            ctx.fill(pX, pY + gy, pX + pW, pY + gy + 1, 0x0AFFFFFF);

        ctx.drawBorder(pX, pY, pW, pH, 0xFF5B21B6);
        ctx.drawBorder(pX - 1, pY - 1, pW + 2, pH + 2, 0x338B21F7);

        LivingEntity player = (client != null) ? client.player : null;
        if (player != null) {
            // Real 3D Player rendering using InventoryScreen.drawEntity
            // At yaw = 0, player.bodyYaw = 180° turns the player around so their BACKSIDE (cape/wings) is facing the screen!
            float origBodyYaw   = player.bodyYaw;
            float origYaw       = player.getYaw();
            float origPitch     = player.getPitch();
            float origPrevBY    = player.prevBodyYaw;
            float origPrevYaw   = player.prevYaw;
            float origPrevPitch = player.prevPitch;
            float origHeadYaw   = player.headYaw;
            float origPrevHY    = player.prevHeadYaw;

            float previewAngle = 180f + yaw;
            player.bodyYaw      = previewAngle;
            player.setYaw(previewAngle);
            player.setPitch(pitch);
            player.prevBodyYaw  = previewAngle;
            player.prevYaw      = previewAngle;
            player.prevPitch    = pitch;
            player.headYaw      = previewAngle;
            player.prevHeadYaw  = previewAngle;

            float centerEntityX = (pX + 4 + pX + pW - 4) / 2.0F;
            float centerEntityY = (pY + 4 + pY + pH - 4) / 2.0F - 30.0F;

            InventoryScreen.drawEntity(ctx,
                    pX + 4, pY + 4, pX + pW - 4, pY + pH - 4,
                    58, 0.0625f, centerEntityX, centerEntityY, player);

            player.bodyYaw      = origBodyYaw;
            player.setYaw(origYaw);
            player.setPitch(origPitch);
            player.prevBodyYaw  = origPrevBY;
            player.prevYaw      = origPrevYaw;
            player.prevPitch    = origPrevPitch;
            player.headYaw      = origHeadYaw;
            player.prevHeadYaw  = origPrevHY;

        } else {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "✦", pX + pW / 2, pY + pH / 2 - 18, 0xFF6D28D9);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Preview available", pX + pW / 2, pY + pH / 2, 0xFF7755AA);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "in-game only", pX + pW / 2, pY + pH / 2 + 14, 0xFF443355);
        }

        // Drag instruction hint
        boolean pBoxHov = mx >= pX && mx <= pX + pW && my >= pY && my <= pY + pH;
        if (pBoxHov && player != null) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "drag to rotate 360°",
                    pX + pW / 2, pY + pH - 14, 0x88D8B4FE);
        }

        // Controls
        int ctrlY = panelY + panelH - 76;
        // Reset
        boolean rstHov = mx >= rightX + 8 && mx <= rightX + 80 && my >= ctrlY && my <= ctrlY + 18;
        ctx.fill(rightX + 8, ctrlY, rightX + 80, ctrlY + 18, rstHov ? 0xFF21104A : 0xFF110822);
        ctx.drawBorder(rightX + 8, ctrlY, 72, 18, rstHov ? 0xFF8B21F7 : 0xFF3D1D6A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "⟲ Reset", rightX + 44, ctrlY + 5, rstHov ? 0xFFD8B4FE : 0xFF8855BB);

        // Auto spin
        boolean spinHov = mx >= rightX + 86 && mx <= rightX + previewW - 8 && my >= ctrlY && my <= ctrlY + 18;
        ctx.fill(rightX + 86, ctrlY, rightX + previewW - 8, ctrlY + 18,
                autoSpin ? (spinHov ? 0xFF3A1F04 : 0xFF2A1402) : (spinHov ? 0xFF1E104A : 0xFF130828));
        ctx.drawBorder(rightX + 86, ctrlY, previewW - 94, 18,
                autoSpin ? 0xFFF59E0B : (spinHov ? 0xFF8B21F7 : 0xFF3D1D6A));
        ctx.drawCenteredTextWithShadow(this.textRenderer, autoSpin ? "⏸ Stop" : "↻ Spin",
                rightX + 86 + (previewW - 94) / 2, ctrlY + 5, autoSpin ? 0xFFFBBF24 : (spinHov ? 0xFFD8B4FE : 0xFF8855BB));

        // Equip / Unequip button for ANY selected cosmetic
        int eqY = panelY + panelH - 50;
        boolean isCurrentItemEquipped = ModConfig.enableCape && (ModConfig.selectedCape == selectedItem);
        boolean eqHov = mx >= rightX + 8 && mx <= rightX + previewW - 8 && my >= eqY && my <= eqY + 22;

        ctx.fill(rightX + 8, eqY, rightX + previewW - 8, eqY + 22,
                isCurrentItemEquipped ? (eqHov ? 0xFF3A0808 : 0xFF220606) : (eqHov ? 0xFF0A3A14 : 0xFF06200C));
        ctx.drawBorder(rightX + 8, eqY, previewW - 16, 22,
                isCurrentItemEquipped ? 0xFFEF4444 : 0xFF22C55E);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                isCurrentItemEquipped ? "✕  Unequip" : "✓  Equip Cape",
                rightX + previewW / 2, eqY + 7,
                isCurrentItemEquipped ? 0xFFFCA5A5 : 0xFF86EFAC);

        // Selected cosmetic title
        ctx.drawCenteredTextWithShadow(this.textRenderer, COS_NAMES[selectedItem],
                rightX + previewW / 2, panelY + panelH - 18, 0xFF7744AA);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OPTIONS MODAL
    // ══════════════════════════════════════════════════════════════════════════
    private void drawOptionsModal(DrawContext ctx, int mx, int my) {
        int mW = 360, mH = 200;
        int mX = (this.width - mW) / 2, mY = (this.height - mH) / 2;

        ctx.fill(mX - 4, mY - 4, mX + mW + 4, mY + mH + 4, 0x88000000);
        ctx.fill(mX, mY, mX + mW, mY + mH, 0xFF0D0D1E);
        ctx.drawBorder(mX, mY, mW, mH, 0xFF8B21F7);
        ctx.drawText(this.textRenderer, "⚙  Cape & Physics Options", mX + 14, mY + 12, 0xFFFFFFFF, true);

        drawModalRow(ctx, mx, my, mX + 14, mY + 38,  mW - 28, "Cloth Physics (Wavey Capes)", ModConfig.enableCapePhysics);
        drawModalRow(ctx, mx, my, mX + 14, mY + 74,  mW - 28, "Override Vanilla Capes",       ModConfig.overrideDefaultCape);
        drawModalRow(ctx, mx, my, mX + 14, mY + 110, mW - 28, "Local Player Only",            ModConfig.capeOnlyLocal);

        int doneX = mX + mW - 80, doneY = mY + mH - 32;
        boolean dHov = mx >= doneX && mx <= doneX + 68 && my >= doneY && my <= doneY + 22;
        ctx.fill(doneX, doneY, doneX + 68, doneY + 22, dHov ? 0xFF21104A : 0xFF130828);
        ctx.drawBorder(doneX, doneY, 68, 22, dHov ? 0xFF8B21F7 : 0xFF3D1D6A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Done", doneX + 34, doneY + 7, dHov ? 0xFFD8B4FE : 0xFFAA88DD);
    }

    private void drawModalRow(DrawContext ctx, int mx, int my,
                              int x, int y, int w, String label, boolean on) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + 28;
        ctx.fill(x, y, x + w, y + 28, hov ? 0xFF16103A : 0xFF0D0D20);
        ctx.drawBorder(x, y, w, 28, hov ? 0xFF3D1D6A : 0xFF1A1830);
        ctx.drawText(this.textRenderer, label, x + 10, y + 10, 0xFFE8E0FF, false);

        int swX = x + w - 36, swY = y + 7;
        ctx.fill(swX, swY, swX + 30, swY + 14, on ? 0xFF6D28D9 : 0xFF1E1A34);
        ctx.drawBorder(swX, swY, 30, 14, on ? 0xFFA855F7 : 0xFF3D2A6A);
        int kx = on ? swX + 16 : swX + 2;
        ctx.fill(kx, swY + 2, kx + 12, swY + 12, 0xFFFFFFFF);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INPUT
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int[] L = getLayout();
        int panelX = L[0], panelY = L[1], sideW = L[2], previewW = L[3],
            centerW = L[4], panelW = L[5], panelH = L[6];
        int centerX = panelX + sideW + 6;
        int rightX  = centerX + centerW + 6;
        int mx = (int) mouseX, my = (int) mouseY;

        // Options modal clicks
        if (showOptions) {
            int mW = 360, mH = 200, mX = (this.width - mW) / 2, mY = (this.height - mH) / 2;
            int mRowH = 28;
            if (mx >= mX + 14 && mx <= mX + mW - 14) {
                if (my >= mY + 38  && my <= mY + 38  + mRowH) { ModConfig.enableCapePhysics    = !ModConfig.enableCapePhysics;    ModConfig.saveConfig(); return true; }
                if (my >= mY + 74  && my <= mY + 74  + mRowH) { ModConfig.overrideDefaultCape  = !ModConfig.overrideDefaultCape;  ModConfig.saveConfig(); return true; }
                if (my >= mY + 110 && my <= mY + 110 + mRowH) { ModConfig.capeOnlyLocal        = !ModConfig.capeOnlyLocal;        ModConfig.saveConfig(); return true; }
            }
            if (mx >= mX + mW - 80 && mx <= mX + mW - 12 && my >= mY + mH - 32 && my <= mY + mH - 10) {
                showOptions = false; return true;
            }
            return true;
        }

        // Close button
        if (mx >= panelX + panelW - 22 && mx <= panelX + panelW - 8 && my >= panelY + 8 && my <= panelY + 26) {
            this.close(); return true;
        }

        // Sidebar categories
        for (int i = 0; i < 7; i++) {
            int ry = panelY + 44 + i * 26;
            if (mx >= panelX + 4 && mx <= panelX + sideW - 4 && my >= ry && my <= ry + 22) {
                activeCategory = i; return true;
            }
        }
        // Options btn
        int opY = panelY + panelH - 30;
        if (mx >= panelX + 6 && mx <= panelX + sideW - 6 && my >= opY && my <= opY + 22) {
            showOptions = true; return true;
        }

        // Cosmetic cards & Star Favorite clicks
        int gridY = panelY + 70;
        int cardW = 88, cardH = 118, gap = 8;
        int cardIndex = 0;

        for (int i = 0; i < COS_NAMES.length; i++) {
            if (activeCategory == 1 && (ModConfig.favoriteCosmetics == null || !ModConfig.favoriteCosmetics[i])) continue;
            if (activeCategory >= 3) continue;
            if (!searchQuery.isEmpty() && !COS_NAMES[i].toLowerCase().contains(searchQuery)) continue;

            int col = cardIndex % 3, row = cardIndex / 3;
            int cardX = centerX + 8 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);

            // Favorite Star click
            if (mx >= cardX + 2 && mx <= cardX + 22 && my >= cardY + 2 && my <= cardY + 22) {
                if (ModConfig.favoriteCosmetics == null) {
                    ModConfig.favoriteCosmetics = new boolean[]{true, false, false};
                }
                ModConfig.favoriteCosmetics[i] = !ModConfig.favoriteCosmetics[i];
                ModConfig.saveConfig();
                return true;
            }

            // Card click -> Select cosmetic
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH) {
                selectedItem = i;
                return true;
            }

            cardIndex++;
        }

        // Env tabs
        int envW = previewW / 4;
        for (int i = 0; i < 4; i++) {
            int ex = rightX + i * envW;
            if (mx >= ex && mx <= ex + envW && my >= panelY + 36 && my <= panelY + 52) {
                activeEnv = i; return true;
            }
        }

        // Preview Box Drag
        int pW = previewW - 16, pH = 220;
        int pX = rightX + 8, pY = panelY + 56;
        if (mx >= pX && mx <= pX + pW && my >= pY && my <= pY + pH) {
            dragging = true; return true;
        }

        // Reset button
        int ctrlY = panelY + panelH - 76;
        if (mx >= rightX + 8 && mx <= rightX + 80 && my >= ctrlY && my <= ctrlY + 18) {
            yaw = 0f; pitch = 0f; autoSpin = false; return true;
        }
        // Spin toggle
        if (mx >= rightX + 86 && mx <= rightX + previewW - 8 && my >= ctrlY && my <= ctrlY + 18) {
            autoSpin = !autoSpin; return true;
        }

        // Equip / Unequip Button for ANY selected cosmetic
        int eqY = panelY + panelH - 50;
        if (mx >= rightX + 8 && mx <= rightX + previewW - 8 && my >= eqY && my <= eqY + 22) {
            if (ModConfig.enableCape && ModConfig.selectedCape == selectedItem) {
                ModConfig.enableCape = false;
            } else {
                ModConfig.enableCape = true;
                ModConfig.selectedCape = selectedItem;
            }
            ModConfig.saveConfig();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button == 0) {
            yaw   = (yaw + (float) dx * 2.2f) % 360f;
            pitch = Math.max(-60f, Math.min(60f, pitch + (float) dy * 1.5f));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean shouldPause() { return false; }
}
