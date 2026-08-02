package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

/**
 * CosmeticsLockerScreen — Velora Cosmetics Locker.
 *
 * Player preview works both in-game (live player model) and from the Main Menu
 * (beautiful pixel-art silhouette with animated cape). Cape card thumbnails show
 * realistic cape shapes instead of plain rectangles with text.
 */
public class CosmeticsLockerScreen extends Screen {

    private int activeCategory = 0;
    private int activeEnv = 0;
    private boolean showSettingsModal = false;
    private TextFieldWidget searchBox;
    private String searchQuery = "";
    private int selectedCosmetic = 0;

    // 3D preview drag state
    private static boolean isLockerOpen = false;
    private float previewYaw = 200.0f;
    private float previewPitch = -5.0f;
    private boolean isDraggingPreview = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean autoRotate = false;

    // Subtle idle sway animation for the silhouette
    private float animTick = 0f;

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Locker"));
    }

    public static boolean isPreviewingCape() { return isLockerOpen; }

    @Override
    protected void init() {
        super.init();
        isLockerOpen = true;
        int cx = this.width / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2;
        int panelY = this.height / 2 - panelH / 2;
        int sideW = 140;
        int previewW = 200;
        int centerW = panelW - sideW - previewW - 12;
        int centerX = panelX + sideW + 6;
        searchBox = new TextFieldWidget(this.textRenderer, centerX + 10, panelY + 44, centerW - 20, 20,
                Text.literal("Search..."));
        searchBox.setPlaceholder(Text.literal("Search cosmetics..."));
        searchBox.setMaxLength(32);
        searchBox.setChangedListener(t -> searchQuery = t != null ? t.toLowerCase().trim() : "");
        this.addSelectableChild(searchBox);
    }

    @Override
    public void close() { isLockerOpen = false; super.close(); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xCC080A12);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RENDER
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        animTick += delta;
        if (autoRotate) previewYaw = (previewYaw + delta * 1.4f) % 360f;

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        // ── Outer window ──────────────────────────────────────────────────────
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF0E0E1A);
        VeloraRenderUtil.drawGlowBorder(ctx, panelX, panelY, panelW, panelH, 0x66A855F7, 4);
        ctx.drawBorder(panelX, panelY, panelW, panelH, 0xFFA855F7);

        // ── Title bar ─────────────────────────────────────────────────────────
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 36, 0xFF0A0A16);
        ctx.fill(panelX, panelY + 35, panelX + panelW, panelY + 36, 0xFF7E22CE);
        ctx.drawText(this.textRenderer, "✦  VELORA COSMETICS LOCKER", panelX + 16, panelY + 13, 0xFFFFFFFF, true);

        // X close
        int xBtnX = panelX + panelW - 24, xBtnY = panelY + 9;
        boolean xHov = mx >= xBtnX && mx <= xBtnX + 16 && my >= xBtnY && my <= xBtnY + 18;
        ctx.drawCenteredTextWithShadow(this.textRenderer, "✕", xBtnX + 8, xBtnY + 2, xHov ? 0xFFEF4444 : 0xFF664466);

        int sideW   = 140;
        int previewW = 200;
        int centerW  = panelW - sideW - previewW - 12;
        int centerX  = panelX + sideW + 6;
        int rightX   = centerX + centerW + 6;

        drawSidebar(ctx, mx, my, panelX, panelY, sideW, panelW, panelH);
        drawCenterGrid(ctx, mx, my, panelX, panelY, panelH, centerX, centerW);
        drawPreviewPanel(ctx, mx, my, panelX, panelY, panelW, panelH, rightX, previewW, delta);

        // Settings modal
        if (showSettingsModal) drawSettingsModal(ctx, mx, my, cx, cy);

        super.render(ctx, mx, my, delta);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private void drawSidebar(DrawContext ctx, int mx, int my,
                             int panelX, int panelY, int sideW, int panelW, int panelH) {
        ctx.fill(panelX, panelY + 36, panelX + sideW, panelY + panelH, 0xFF0A0A14);
        ctx.fill(panelX + sideW - 1, panelY + 36, panelX + sideW, panelY + panelH, 0xFF7E22CE);

        String[] cats = {"All (3)", "Favorites (1)", "Capes (2)", "Hats (0)", "Face (0)", "Wings (0)", "Aura (0)"};
        int sideY = panelY + 44;
        for (int i = 0; i < cats.length; i++) {
            int ry = sideY + i * 28;
            boolean sel = (activeCategory == i);
            boolean hov = mx >= panelX + 6 && mx <= panelX + sideW - 6 && my >= ry && my <= ry + 24;
            int bg = sel ? 0xFF2A0E54 : (hov ? 0xFF1A1A30 : 0xFF0E0E1A);
            ctx.fill(panelX + 6, ry, panelX + sideW - 6, ry + 24, bg);
            ctx.drawBorder(panelX + 6, ry, sideW - 12, 24, sel ? 0xFFA855F7 : 0xFF1E1E38);
            if (sel) ctx.fill(panelX + 6, ry, panelX + 9, ry + 24, 0xFFA855F7);
            ctx.drawText(this.textRenderer, cats[i], panelX + 16, ry + 8, sel ? 0xFFFFFFFF : 0xFFAAAAAA, true);
        }

        // Options button
        int settBtnY = panelY + panelH - 34;
        boolean settHov = mx >= panelX + 8 && mx <= panelX + sideW - 8 && my >= settBtnY && my <= settBtnY + 24;
        VeloraRenderUtil.drawPillButton(ctx, panelX + 8, settBtnY, sideW - 16, 24, "⚙ Options", settHov, 0xFF7E22CE, this.textRenderer);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CENTER GRID — proper cape thumbnails
    // ══════════════════════════════════════════════════════════════════════════
    private void drawCenterGrid(DrawContext ctx, int mx, int my,
                                int panelX, int panelY, int panelH,
                                int centerX, int centerW) {
        ctx.fill(centerX, panelY + 36, centerX + centerW, panelY + panelH, 0xFF101020);
        ctx.fill(centerX, panelY + 36, centerX + 1, panelY + panelH, 0xFF1E1E38);

        if (searchBox != null) searchBox.render(ctx, mx, my, 0f);

        int gridY = panelY + 74;
        int cardW = 86, cardH = 114, gap = 8;
        String[] names = {"Velora Cape", "Vanilla Cape", "Velora Waves"};
        String[] types = {"HD Animated",  "Mojang Cape",  "Particle Aura"};
        int[] capeColors = {0xFF8B3CE8, 0xFF2563EB, 0xFF059669};

        for (int i = 0; i < names.length; i++) {
            if (!searchQuery.isEmpty() && !names[i].toLowerCase().contains(searchQuery)) continue;
            int col = i % 3, row = i / 3;
            int cardX = centerX + 10 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);

            boolean sel = (selectedCosmetic == i);
            boolean hov = mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH;

            ctx.fill(cardX, cardY, cardX + cardW, cardY + cardH, sel ? 0xFF1E0A3A : (hov ? 0xFF1A1A2E : 0xFF12121E));
            ctx.drawBorder(cardX, cardY, cardW, cardH, sel ? 0xFFA855F7 : (hov ? 0xFF55336A : 0xFF1E1E38));
            if (sel) ctx.fill(cardX, cardY, cardX + cardW, cardY + 2, 0xFF22C55E);

            // Favorite star
            ctx.drawText(this.textRenderer, "★", cardX + 5, cardY + 5, i == 0 ? 0xFFEAB308 : 0x44FFFFFF, false);

            // ── Cape thumbnail ── (looks like an actual cape, not a rectangle)
            drawCapeThumbnail(ctx, cardX, cardY, cardW, capeColors[i], i);

            // Name + type label
            ctx.drawCenteredTextWithShadow(this.textRenderer, names[i], cardX + cardW / 2, cardY + 84, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, types[i],  cardX + cardW / 2, cardY + 96, 0xFF8866AA);
        }
    }

    /**
     * Draws a stylised cape shape inside a card — looks like a real flowing cape preview
     * rather than a plain rectangle with text.
     */
    private void drawCapeThumbnail(DrawContext ctx, int cardX, int cardY, int cardW, int mainColor, int idx) {
        int capeW = 44, capeH = 58;
        int cx = cardX + (cardW - capeW) / 2;
        int cy = cardY + 14;

        // Cape body gradient (darker at bottom = flowing look)
        int topColor = mainColor;
        int botColor = (mainColor & 0x00FFFFFF) | 0x88000000;
        VeloraRenderUtil.drawGradient(ctx, cx, cy, cx + capeW, cy + capeH, topColor, botColor);

        // Cape border
        ctx.drawBorder(cx, cy, capeW, capeH, mainColor);

        // Subtle inside stitching lines (horizontal)
        for (int row = 1; row <= 4; row++) {
            int ly = cy + row * (capeH / 5);
            ctx.fill(cx + 2, ly, cx + capeW - 2, ly + 1, (mainColor & 0x00FFFFFF) | 0x44000000);
        }

        // Cape top edge fold shadow (darker strip at top)
        ctx.fill(cx, cy, cx + capeW, cy + 4, 0x44000000);

        // Cape clasp/clip center-top
        int clipW = 10;
        ctx.fill(cx + (capeW - clipW) / 2, cy - 3, cx + (capeW + clipW) / 2, cy + 1, 0xFF888888);
        ctx.drawBorder(cx + (capeW - clipW) / 2, cy - 3, clipW, 4, 0xFFCCCCCC);

        // Pattern overlay depending on cape
        switch (idx) {
            case 0 -> drawVeloraPattern(ctx, cx, cy, capeW, capeH);   // Velora: V symbol
            case 1 -> drawMojangPattern(ctx, cx, cy, capeW, capeH);   // Vanilla: simple cross
            case 2 -> drawWavesPattern(ctx, cx, cy, capeW, capeH);    // Waves: wavy lines
        }

        // Small glow under cape
        VeloraRenderUtil.drawGlowBorder(ctx, cx, cy, capeW, capeH, (mainColor & 0x00FFFFFF) | 0x33000000, 2);
    }

    /** Velora Cape: draws a small V in the center */
    private void drawVeloraPattern(DrawContext ctx, int x, int y, int w, int h) {
        int white = 0x88FFFFFF;
        int midX = x + w / 2;
        int midY = y + h / 2 - 4;
        // Left arm of V
        for (int i = 0; i < 7; i++) {
            ctx.fill(midX - 7 + i, midY + i, midX - 7 + i + 2, midY + i + 2, white);
        }
        // Right arm of V
        for (int i = 0; i < 7; i++) {
            ctx.fill(midX + 7 - i - 1, midY + i, midX + 7 - i + 1, midY + i + 2, white);
        }
    }

    /** Vanilla Cape: draws a simple thin cross */
    private void drawMojangPattern(DrawContext ctx, int x, int y, int w, int h) {
        int white = 0x66FFFFFF;
        int midX = x + w / 2, midY = y + h / 2;
        ctx.fill(midX - 8, midY - 1, midX + 8, midY + 1, white);
        ctx.fill(midX - 1, midY - 8, midX + 1, midY + 8, white);
    }

    /** Waves Cape: draws three horizontal sine-like wavy lines */
    private void drawWavesPattern(DrawContext ctx, int x, int y, int w, int h) {
        int white = 0x77FFFFFF;
        for (int row = 0; row < 3; row++) {
            int baseY = y + 12 + row * 14;
            for (int px = 2; px < w - 2; px += 4) {
                int waveY = baseY + (int)(Math.sin((px * 0.4f) + row * 2f) * 2f);
                ctx.fill(x + px, waveY, x + px + 3, waveY + 2, white);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RIGHT PREVIEW PANEL
    // ══════════════════════════════════════════════════════════════════════════
    private void drawPreviewPanel(DrawContext ctx, int mx, int my,
                                  int panelX, int panelY, int panelW, int panelH,
                                  int rightX, int previewW, float delta) {
        ctx.fill(rightX, panelY + 36, rightX + previewW, panelY + panelH, 0xFF0C0C1A);
        ctx.fill(rightX, panelY + 36, rightX + 1, panelY + panelH, 0xFF1E1E38);

        // Env tabs
        String[] envs = {"Default", "World", "Nether", "End"};
        int envW = previewW / 4;
        for (int i = 0; i < envs.length; i++) {
            int ex = rightX + i * envW;
            boolean sel = (activeEnv == i);
            boolean hov = mx >= ex && mx <= ex + envW && my >= panelY + 36 && my <= panelY + 54;
            ctx.fill(ex, panelY + 36, ex + envW, panelY + 54,
                    sel ? 0xFF2A0E54 : (hov ? 0xFF141428 : 0xFF0C0C1A));
            if (sel) ctx.fill(ex, panelY + 52, ex + envW, panelY + 54, 0xFFA855F7);
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    envs[i].substring(0, Math.min(3, envs[i].length())),
                    ex + envW / 2, panelY + 41,
                    sel ? 0xFFD8B4FE : (hov ? 0xFF8888AA : 0xFF554466));
        }

        // Preview box
        int pBoxW = 176, pBoxH = 240;
        int pBoxX = rightX + (previewW - pBoxW) / 2;
        int pBoxY = panelY + 58;

        // Gradient floor in preview
        VeloraRenderUtil.drawGradient(ctx, pBoxX, pBoxY, pBoxX + pBoxW, pBoxY + pBoxH, 0xFF0E0E20, 0xFF16123A);
        // Grid lines on floor (gives depth)
        for (int gx = 0; gx < pBoxW; gx += 20)
            ctx.fill(pBoxX + gx, pBoxY, pBoxX + gx + 1, pBoxY + pBoxH, 0x0AFFFFFF);
        for (int gy = 0; gy < pBoxH; gy += 20)
            ctx.fill(pBoxX, pBoxY + gy, pBoxX + pBoxW, pBoxY + gy + 1, 0x0AFFFFFF);

        boolean pBoxHov = mx >= pBoxX && mx <= pBoxX + pBoxW && my >= pBoxY && my <= pBoxY + pBoxH;
        ctx.drawBorder(pBoxX, pBoxY, pBoxW, pBoxH, pBoxHov ? 0xFFA855F7 : 0xFF3A2A6A);

        // ── Try live player render first (works in-game) ──────────────────────
        LivingEntity entity = (this.client != null) ? this.client.player : null;
        if (entity != null) {
            try {
                float origBodyYaw  = entity.bodyYaw;
                float origYaw      = entity.getYaw();
                float origPitch    = entity.getPitch();
                float origPrevBY   = entity.prevBodyYaw;
                float origPrevYaw  = entity.prevYaw;
                float origPrevPit  = entity.prevPitch;
                float origHeadYaw  = entity.headYaw;
                float origPrevHY   = entity.prevHeadYaw;

                entity.bodyYaw      = previewYaw;
                entity.setYaw(previewYaw);
                entity.setPitch(previewPitch);
                entity.prevBodyYaw  = previewYaw;
                entity.prevYaw      = previewYaw;
                entity.prevPitch    = previewPitch;
                entity.headYaw      = previewYaw;
                entity.prevHeadYaw  = previewYaw;

                InventoryScreen.drawEntity(ctx,
                        pBoxX + 10, pBoxY + 10, pBoxX + pBoxW - 10, pBoxY + pBoxH - 10,
                        56, 0.0625f, 0.0f, 0.0f, entity);

                entity.bodyYaw     = origBodyYaw;
                entity.setYaw(origYaw);
                entity.setPitch(origPitch);
                entity.prevBodyYaw = origPrevBY;
                entity.prevYaw     = origPrevYaw;
                entity.prevPitch   = origPrevPit;
                entity.headYaw     = origHeadYaw;
                entity.prevHeadYaw = origPrevHY;
            } catch (Exception ex) {
                drawSilhouette(ctx, pBoxX, pBoxY, pBoxW, pBoxH);
            }
        } else {
            // Main menu: no player entity — draw animated silhouette
            drawSilhouette(ctx, pBoxX, pBoxY, pBoxW, pBoxH);
        }

        // Drag hint
        if (pBoxHov) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "✦ Drag to rotate ✦",
                    pBoxX + pBoxW / 2, pBoxY + pBoxH - 16, 0x99D8B4FE);
        } else {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Drag to rotate",
                    pBoxX + pBoxW / 2, pBoxY + pBoxH - 16, 0x44A855F7);
        }

        // Controls
        int ctrlY = panelY + panelH - 82;
        boolean rstHov = mx >= rightX + 12 && mx <= rightX + 88 && my >= ctrlY && my <= ctrlY + 20;
        VeloraRenderUtil.drawPillButton(ctx, rightX + 12, ctrlY, 76, 20, "⟲ Reset", rstHov, 0xFF3A3A5A, this.textRenderer);

        boolean spinHov = mx >= rightX + 100 && mx <= rightX + 188 && my >= ctrlY && my <= ctrlY + 20;
        VeloraRenderUtil.drawPillButton(ctx, rightX + 100, ctrlY, 88, 20,
                autoRotate ? "⏸ Pause" : "↻ Spin", spinHov,
                autoRotate ? 0xFFD97706 : 0xFF7C3AED, this.textRenderer);

        // Equip button
        int eqBtnW = 160, eqBtnH = 26;
        int eqBtnX = rightX + (previewW - eqBtnW) / 2;
        int eqBtnY = panelY + panelH - 54;
        boolean isEquipped = (selectedCosmetic == 0 && ModConfig.enableCape);
        boolean eqHov = mx >= eqBtnX && mx <= eqBtnX + eqBtnW && my >= eqBtnY && my <= eqBtnY + eqBtnH;
        VeloraRenderUtil.drawPillButton(ctx, eqBtnX, eqBtnY, eqBtnW, eqBtnH,
                isEquipped ? "✕ Unequip" : "✓ Equip Cosmetic",
                eqHov, isEquipped ? 0xFFDC2626 : 0xFF16A34A, this.textRenderer);

        String[] cosNames = {"Velora Cape", "Vanilla Cape", "Velora Waves"};
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                "Selected: " + cosNames[selectedCosmetic],
                rightX + previewW / 2, panelY + panelH - 22, 0xFF8844AA);
    }

    /**
     * Pixel-art player silhouette with animated cape — shown when no world is loaded
     * (i.e. from the main menu). The character sways slightly and the cape waves.
     *
     * Layout (all coords relative to pBoxX, pBoxY):
     *   Head     : 10×10 px at center-top
     *   Torso    : 10×14 px below head
     *   Arms     : 4×12 px left/right of torso
     *   Legs     : 5×14 px below torso
     *   Cape     : drawn behind the torso with wave offset
     */
    private void drawSilhouette(DrawContext ctx, int pBoxX, int pBoxY, int pBoxW, int pBoxH) {
        // Figure center
        int figCX = pBoxX + pBoxW / 2;
        int figTopY = pBoxY + 28;

        // Sway: subtle left-right movement
        float sway = (float) Math.sin(animTick * 0.04f) * 2f;
        int sw = (int) sway;

        // Color palette (same as default Steve/Alex skin tones for realism)
        int skinColor  = 0xFFF5C998; // skin
        int darkSkin   = 0xFFD4A76A; // shadow skin
        int hairColor  = 0xFF3A2010; // hair/head top
        int shirtColor = 0xFF4A6EA8; // shirt / torso
        int pantColor  = 0xFF334466; // pants
        int shoeColor  = 0xFF222233; // shoes

        // ── HEAD ──────────────────────────────────────────────────────────────
        int hx = figCX - 6 + sw, hy = figTopY;
        ctx.fill(hx, hy, hx + 12, hy + 12, skinColor);         // face
        ctx.fill(hx, hy, hx + 12, hy + 4,  hairColor);          // hair top
        ctx.fill(hx, hy + 4, hx + 2, hy + 9, hairColor);        // hair left
        ctx.fill(hx + 10, hy + 4, hx + 12, hy + 9, hairColor);  // hair right
        ctx.fill(hx + 3, hy + 7, hx + 5, hy + 9, 0xFF3A3A3A);  // left eye
        ctx.fill(hx + 7, hy + 7, hx + 9, hy + 9, 0xFF3A3A3A);  // right eye
        ctx.fill(hx + 4, hy + 10, hx + 8, hy + 11, 0xFFAA6644); // mouth
        ctx.drawBorder(hx, hy, 12, 12, 0x44000000);

        // ── NECK ──────────────────────────────────────────────────────────────
        ctx.fill(figCX - 2 + sw, figTopY + 12, figCX + 2 + sw, figTopY + 14, skinColor);

        // ── CAPE (behind torso, drawn first so it appears behind) ─────────────
        int torsoY = figTopY + 14;
        drawSilhouetteCape(ctx, figCX + sw, torsoY, 14);

        // ── TORSO ─────────────────────────────────────────────────────────────
        ctx.fill(figCX - 6 + sw, torsoY, figCX + 6 + sw, torsoY + 14, shirtColor);
        // Shirt buttons (faux detail)
        ctx.fill(figCX + sw, torsoY + 3, figCX + sw + 1, torsoY + 12, 0x44FFFFFF);
        ctx.drawBorder(figCX - 6 + sw, torsoY, 12, 14, 0x33000000);

        // ── LEFT ARM ──────────────────────────────────────────────────────────
        float armSwingL = (float) Math.sin(animTick * 0.04f) * 3f;
        int lax = figCX - 10 + sw, lay = torsoY;
        ctx.fill(lax, lay, lax + 4, lay + 12, shirtColor);
        ctx.fill(lax, lay + 12, lax + 4, lay + 14, skinColor);   // wrist
        ctx.drawBorder(lax, lay, 4, 14, 0x33000000);

        // ── RIGHT ARM ─────────────────────────────────────────────────────────
        int rax = figCX + 6 + sw, ray = torsoY;
        ctx.fill(rax, ray, rax + 4, ray + 12, shirtColor);
        ctx.fill(rax, ray + 12, rax + 4, ray + 14, skinColor);
        ctx.drawBorder(rax, ray, 4, 14, 0x33000000);

        // ── LEGS ──────────────────────────────────────────────────────────────
        int legY = torsoY + 14;
        float legSwingL = (float) Math.sin(animTick * 0.04f) * 2f;
        float legSwingR = -legSwingL;

        // Left leg
        int llx = figCX - 6 + sw, lly = legY;
        ctx.fill(llx, lly, llx + 5, lly + 12, pantColor);
        ctx.fill(llx, lly + 12, llx + 5, lly + 14, shoeColor);
        ctx.drawBorder(llx, lly, 5, 14, 0x33000000);

        // Right leg
        int rlx = figCX + 1 + sw, rly = legY;
        ctx.fill(rlx, rly, rlx + 5, rly + 12, pantColor);
        ctx.fill(rlx, rly + 12, rlx + 5, rly + 14, shoeColor);
        ctx.drawBorder(rlx, rly, 5, 14, 0x33000000);

        // ── SHADOW ────────────────────────────────────────────────────────────
        int shadowY = legY + 14 + 2;
        for (int i = -10; i <= 10; i++) {
            int alpha = (int) (50 * (1f - Math.abs(i) / 11f));
            ctx.fill(figCX + i + sw, shadowY, figCX + i + sw + 1, shadowY + 3,
                    (alpha << 24) | 0x00A855F7);
        }
    }

    /**
     * Draws a waving cape behind the player silhouette.
     * The cape waves sinusoidally — simulates fabric in wind.
     */
    private void drawSilhouetteCape(DrawContext ctx, int figCX, int torsoY, int torsoH) {
        int[] capeColors = {0xFF8B3CE8, 0xFF2563EB, 0xFF059669};
        int capeColor = capeColors[selectedCosmetic];
        int capeW = 10, capeH = torsoH + 10;

        // Cape hangs from shoulders, 1px behind torso center
        int capeX = figCX - capeW / 2 - 1;
        int capeY = torsoY;

        // Wave columns — each column offset by sin to make it flow
        for (int col = 0; col < capeW; col++) {
            float wave = (float) Math.sin(animTick * 0.06f + col * 0.4f) * 2f;
            int colX = capeX + col;
            for (int row = 0; row < capeH; row++) {
                // Taper bottom of cape (gets narrower)
                int taper = (row * 3) / (capeH * 2);
                if (col < taper || col >= capeW - taper) continue;

                float t = (float) row / capeH;
                int alpha = (int) (200 * (1f - t * 0.3f));
                int finalColor = (alpha << 24) | (capeColor & 0x00FFFFFF);
                ctx.fill(colX + (int) wave, capeY + row, colX + (int) wave + 1, capeY + row + 1, finalColor);
            }
        }

        // Cape border outline
        ctx.drawBorder(capeX, capeY, capeW, capeH - 2, (capeColor & 0x00FFFFFF) | 0x99000000);

        // Velora pattern on cape
        if (selectedCosmetic == 0) {
            // Small V in purple lighter
            int pv = 0x77FFFFFF;
            int midX = figCX - 1, midY = torsoY + 6;
            for (int i = 0; i < 4; i++) {
                ctx.fill(midX - 3 + i, midY + i, midX - 3 + i + 1, midY + i + 1, pv);
                ctx.fill(midX + 3 - i, midY + i, midX + 3 - i + 1, midY + i + 1, pv);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SETTINGS MODAL
    // ══════════════════════════════════════════════════════════════════════════
    private void drawSettingsModal(DrawContext ctx, int mx, int my, int cx, int cy) {
        int modalW = 360, modalH = 220;
        int modalX = cx - modalW / 2, modalY = cy - modalH / 2;
        ctx.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xFF0E0E1E);
        VeloraRenderUtil.drawGlowBorder(ctx, modalX, modalY, modalW, modalH, 0x88A855F7, 4);
        ctx.drawBorder(modalX, modalY, modalW, modalH, 0xFFA855F7);
        ctx.drawText(this.textRenderer, "⚙  CAPE & PHYSICS OPTIONS", modalX + 16, modalY + 14, 0xFFFFFFFF, true);

        int mRowY = modalY + 42, mRowH = 32;
        renderModalToggle(ctx, mx, my, modalX + 16, mRowY,      modalW - 32, mRowH, "Cloth Cape Physics (Wavey Capes)", ModConfig.enableCapePhysics);
        renderModalToggle(ctx, mx, my, modalX + 16, mRowY + 38, modalW - 32, mRowH, "Override Account / Vanilla Capes", ModConfig.overrideDefaultCape);
        renderModalToggle(ctx, mx, my, modalX + 16, mRowY + 76, modalW - 32, mRowH, "Apply Cape to Local Player Only",  ModConfig.capeOnlyLocal);

        int closeM = modalX + modalW - 80, closeMY = modalY + modalH - 34;
        boolean mHov = mx >= closeM && mx <= closeM + 68 && my >= closeMY && my <= closeMY + 22;
        VeloraRenderUtil.drawPillButton(ctx, closeM, closeMY, 68, 22, "Done", mHov, 0xFF7E22CE, this.textRenderer);
    }

    private void renderModalToggle(DrawContext ctx, int mx, int my,
                                   int x, int y, int w, int h, String title, boolean enabled) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
        ctx.fill(x, y, x + w, y + h, hov ? 0xFF1A1A30 : 0xFF121220);
        ctx.drawBorder(x, y, w, h, 0xFF2A2A44);
        ctx.drawText(this.textRenderer, title, x + 10, y + (h - 8) / 2, 0xFFFFFFFF, true);
        VeloraRenderUtil.drawSwitch(ctx, x + w - 38, y + (h - 14) / 2, 28, 14, enabled, hov);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INPUT
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2, cy = this.height / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2, panelY = cy - panelH / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // ── Modal ─────────────────────────────────────────────────────────────
        if (showSettingsModal) {
            int modalW = 360, modalH = 220;
            int modalX = cx - modalW / 2, modalY = cy - modalH / 2;
            int mRowY = modalY + 42, mRowH = 32;
            if (mx >= modalX + 16 && mx <= modalX + modalW - 16) {
                if (my >= mRowY && my <= mRowY + mRowH)            { ModConfig.enableCapePhysics = !ModConfig.enableCapePhysics; ModConfig.saveConfig(); return true; }
                if (my >= mRowY + 38 && my <= mRowY + 38 + mRowH) { ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape; ModConfig.saveConfig(); return true; }
                if (my >= mRowY + 76 && my <= mRowY + 76 + mRowH) { ModConfig.capeOnlyLocal = !ModConfig.capeOnlyLocal; ModConfig.saveConfig(); return true; }
            }
            int closeM = modalX + modalW - 80, closeMY = modalY + modalH - 34;
            if (mx >= closeM && mx <= closeM + 68 && my >= closeMY && my <= closeMY + 22) { showSettingsModal = false; return true; }
            return true;
        }

        // ── X close ───────────────────────────────────────────────────────────
        if (mx >= panelX + panelW - 24 && mx <= panelX + panelW - 8 && my >= panelY + 6 && my <= panelY + 24) { this.close(); return true; }

        int sideW = 140, sideY = panelY + 44;
        // ── Categories ────────────────────────────────────────────────────────
        for (int i = 0; i < 7; i++) {
            int ry = sideY + i * 28;
            if (mx >= panelX + 6 && mx <= panelX + sideW - 6 && my >= ry && my <= ry + 24) { activeCategory = i; return true; }
        }
        // ── Settings btn ──────────────────────────────────────────────────────
        int settBtnY = panelY + panelH - 34;
        if (mx >= panelX + 8 && mx <= panelX + sideW - 8 && my >= settBtnY && my <= settBtnY + 24) { showSettingsModal = true; return true; }

        // ── Cosmetic cards ────────────────────────────────────────────────────
        int previewW = 200, centerW = panelW - sideW - previewW - 12;
        int centerX = panelX + sideW + 6;
        int gridY = panelY + 74, cardW = 86, cardH = 114, gap = 8;
        for (int i = 0; i < 3; i++) {
            int col = i % 3, row = i / 3;
            int cardX = centerX + 10 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH) { selectedCosmetic = i; return true; }
        }

        // ── Preview box drag ─────────────────────────────────────────────────
        int rightX  = centerX + centerW + 6;
        int pBoxW = 176, pBoxH = 240;
        int pBoxX = rightX + (previewW - pBoxW) / 2, pBoxY = panelY + 58;
        if (mx >= pBoxX && mx <= pBoxX + pBoxW && my >= pBoxY && my <= pBoxY + pBoxH) {
            isDraggingPreview = true; lastMouseX = mouseX; lastMouseY = mouseY; return true;
        }

        // ── Env tabs ──────────────────────────────────────────────────────────
        int envW = previewW / 4;
        for (int i = 0; i < 4; i++) {
            int ex = rightX + i * envW;
            if (mx >= ex && mx <= ex + envW && my >= panelY + 36 && my <= panelY + 54) { activeEnv = i; return true; }
        }

        // ── Controls ─────────────────────────────────────────────────────────
        int ctrlY = panelY + panelH - 82;
        if (mx >= rightX + 12 && mx <= rightX + 88 && my >= ctrlY && my <= ctrlY + 20) { previewYaw = 200f; previewPitch = -5f; autoRotate = false; return true; }
        if (mx >= rightX + 100 && mx <= rightX + 188 && my >= ctrlY && my <= ctrlY + 20) { autoRotate = !autoRotate; return true; }

        // ── Equip ─────────────────────────────────────────────────────────────
        int eqBtnW = 160, eqBtnH = 26;
        int eqBtnX = rightX + (previewW - eqBtnW) / 2, eqBtnY = panelY + panelH - 54;
        if (mx >= eqBtnX && mx <= eqBtnX + eqBtnW && my >= eqBtnY && my <= eqBtnY + eqBtnH) {
            if (selectedCosmetic == 0) { ModConfig.enableCape = !ModConfig.enableCape; ModConfig.saveConfig(); }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) isDraggingPreview = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (isDraggingPreview && button == 0) {
            previewYaw   = (previewYaw + (float) dx * 1.8f) % 360f;
            previewPitch = Math.max(-50f, Math.min(50f, previewPitch + (float) dy * 1.5f));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean shouldPause() { return false; }
}
