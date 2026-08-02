package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

public class CosmeticsLockerScreen extends Screen {

    private int activeCategory = 0; // 0 = All, 1 = Favorites, 2 = Capes, 3 = Hats, 4 = Face, 5 = Wings, 6 = Aura
    private int activeEnv = 0; // 0 = Default, 1 = World, 2 = Nether, 3 = End
    private boolean showSettingsModal = false;
    private TextFieldWidget searchBox;
    private String searchQuery = "";
    private int selectedCosmetic = 0; // 0 = Velora HD Cape, 1 = Vanilla Cape, 2 = Velora Waves

    // 3D Model Interactive Controls
    private static boolean isLockerOpen = false;
    private float previewYaw = 180.0f; // Start facing backward so cape is directly visible
    private float previewPitch = 0.0f;
    private boolean isDraggingPreview = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean autoRotate = false;

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Locker"));
    }

    public static boolean isPreviewingCape() {
        return isLockerOpen;
    }

    @Override
    protected void init() {
        super.init();
        isLockerOpen = true;
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        int sideW = 140;
        int previewW = 200;
        int centerW = panelW - sideW - previewW - 12;
        int centerX = panelX + sideW + 6;

        searchBox = new TextFieldWidget(this.textRenderer, centerX + 10, panelY + 44, centerW - 20, 20, Text.literal("Search..."));
        searchBox.setPlaceholder(Text.literal("Search cosmetics..."));
        searchBox.setMaxLength(32);
        searchBox.setChangedListener(text -> searchQuery = text != null ? text.toLowerCase().trim() : "");
        this.addSelectableChild(searchBox);
    }

    @Override
    public void close() {
        isLockerOpen = false;
        super.close();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xCC080A12);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        if (autoRotate) {
            previewYaw = (previewYaw + delta * 1.2f) % 360.0f;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        // Outer Container Window with Glowing Purple Border
        VeloraRenderUtil.drawSolidPanel(context, panelX, panelY, panelW, panelH, 0xFF141726, 0xFFA855F7);
        VeloraRenderUtil.drawGlowBorder(context, panelX, panelY, panelW, panelH, 0x88A855F7, 3);

        // --- TOP HEADER ---
        context.fill(panelX, panelY, panelX + panelW, panelY + 36, 0xFF0F111D);
        context.fill(panelX, panelY + 35, panelX + panelW, panelY + 36, 0xFFA855F7);
        context.drawText(this.textRenderer, "✦ VELORA 3D COSMETICS LOCKER", panelX + 16, panelY + 13, 0xFFFFFFFF, true);

        // Top Right: X Close Button
        int xBtnX = panelX + panelW - 24;
        int xBtnY = panelY + 9;
        boolean xHov = mouseX >= xBtnX && mouseX <= xBtnX + 16 && mouseY >= xBtnY && mouseY <= xBtnY + 18;
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", xBtnX + 8, xBtnY + 2, xHov ? 0xFFEF4444 : 0xFF94A3B8);

        int sideW = 140;
        int previewW = 200;
        int centerW = panelW - sideW - previewW - 12;
        int centerX = panelX + sideW + 6;
        int rightX = centerX + centerW + 6;

        // --- LEFT SIDEBAR (#10121C) ---
        VeloraRenderUtil.drawSolidPanel(context, panelX, panelY + 36, sideW, panelH - 36, 0xFF10121C, 0xFF2D334A);
        int sideY = panelY + 44;
        String[] categories = {"All (3)", "Favorites (1)", "Capes (2)", "Hats (0)", "Face (0)", "Wings (0)", "Aura (0)"};

        for (int i = 0; i < categories.length; i++) {
            int ry = sideY + i * 28;
            boolean isSel = (activeCategory == i);
            boolean hov = mouseX >= panelX + 6 && mouseX <= panelX + sideW - 6 && mouseY >= ry && mouseY <= ry + 24;

            int catBg = isSel ? 0xFF4C1D95 : (hov ? 0xFF26293D : 0xFF161826);
            int catBorder = isSel ? 0xFFA855F7 : 0xFF2D334A;
            VeloraRenderUtil.drawSolidPanel(context, panelX + 6, ry, sideW - 12, 24, catBg, catBorder);

            if (isSel) {
                context.fill(panelX + 6, ry, panelX + 9, ry + 24, 0xFFA855F7);
            }

            int textColor = isSel ? 0xFFFFFFFF : 0xFFCBD5E1;
            context.drawText(this.textRenderer, categories[i], panelX + 16, ry + 8, textColor, true);
        }

        // Sidebar Bottom: Options Button
        int settBtnY = panelY + panelH - 34;
        boolean settHov = mouseX >= panelX + 8 && mouseX <= panelX + sideW - 8 && mouseY >= settBtnY && mouseY <= settBtnY + 24;
        VeloraRenderUtil.drawPillButton(context, panelX + 8, settBtnY, sideW - 16, 24, "⚙ Options", settHov, 0xFF8B5CF6, this.textRenderer);

        // --- CENTER AREA (#181B2C) ---
        VeloraRenderUtil.drawSolidPanel(context, centerX, panelY + 36, centerW, panelH - 36, 0xFF181B2C, 0xFF2D334A);

        // Search Box
        if (searchBox != null) {
            searchBox.render(context, mouseX, mouseY, delta);
        }

        // Cosmetics Cards Grid
        int gridY = panelY + 74;
        int cardW = 86;
        int cardH = 114;
        int gap = 8;

        String[] cosmeticNames = {"Velora Cape", "Vanilla Cape", "Velora Waves"};
        String[] cosmeticTypes = {"HD Animated", "Mojang Cape", "Particle Aura"};

        for (int i = 0; i < cosmeticNames.length; i++) {
            if (!searchQuery.isEmpty() && !cosmeticNames[i].toLowerCase().contains(searchQuery)) continue;

            int col = i % 3;
            int row = i / 3;
            int cardX = centerX + 10 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);

            boolean isSelected = (selectedCosmetic == i);
            boolean cardHov = mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;

            int cBg = isSelected ? 0xFF351A5A : (cardHov ? 0xFF282C42 : 0xFF1E2235);
            int cBorder = isSelected ? 0xFFA855F7 : (cardHov ? 0xFF64748B : 0xFF333A54);
            VeloraRenderUtil.drawSolidPanel(context, cardX, cardY, cardW, cardH, cBg, cBorder);

            if (isSelected) {
                context.fill(cardX, cardY, cardX + cardW, cardY + 3, 0xFF22C55E);
            }

            // Favorite star badge
            context.drawText(this.textRenderer, "★", cardX + 6, cardY + 6, i == 0 ? 0xFFEAB308 : 0xAA64748B, true);

            // Thumbnail visual banner
            int thumbW = 48;
            int thumbH = 58;
            int thumbX = cardX + (cardW - thumbW) / 2;
            int thumbY = cardY + 18;
            int bannerColor = (i == 0) ? 0xFF7C3AED : ((i == 1) ? 0xFF2563EB : 0xFF059669);
            context.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, (bannerColor & 0x00FFFFFF) | 0x44000000);
            context.drawBorder(thumbX, thumbY, thumbW, thumbH, bannerColor);
            context.drawCenteredTextWithShadow(this.textRenderer, "CAPE", thumbX + thumbW / 2, thumbY + 22, 0xFFFFFFFF);

            // Cosmetic Title & Type
            context.drawCenteredTextWithShadow(this.textRenderer, cosmeticNames[i], cardX + cardW / 2, cardY + 84, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, cosmeticTypes[i], cardX + cardW / 2, cardY + 96, 0xFF94A3B8);
        }

        // --- RIGHT 3D PREVIEW PANEL (#141624) ---
        VeloraRenderUtil.drawSolidPanel(context, rightX, panelY + 36, previewW, panelH - 36, 0xFF141624, 0xFF2D334A);

        // Environment tabs (Default, World, Nether, End)
        String[] envs = {"Default", "World", "Nether", "End"};
        int envW = previewW / 4;
        for (int i = 0; i < envs.length; i++) {
            int ex = rightX + i * envW;
            boolean envSel = (activeEnv == i);
            if (envSel) context.fill(ex, panelY + 36, ex + envW, panelY + 54, 0xFF6D28D9);
            context.drawCenteredTextWithShadow(this.textRenderer, envs[i].substring(0, Math.min(3, envs[i].length())), ex + envW / 2, panelY + 41, envSel ? 0xFFFFFFFF : 0xFF94A3B8);
        }

        // 3D Player & Cosmetic Preview Viewport Box
        int pBoxW = 176;
        int pBoxH = 240;
        int pBoxX = rightX + (previewW - pBoxW) / 2;
        int pBoxY = panelY + 60;

        boolean pBoxHov = mouseX >= pBoxX && mouseX <= pBoxX + pBoxW && mouseY >= pBoxY && mouseY <= pBoxY + pBoxH;
        int pBoxBorder = pBoxHov ? 0xFFA855F7 : 0xFF3D4363;
        VeloraRenderUtil.drawSolidPanel(context, pBoxX, pBoxY, pBoxW, pBoxH, 0xFF0E101A, pBoxBorder);
        VeloraRenderUtil.drawGlowBorder(context, pBoxX, pBoxY, pBoxW, pBoxH, pBoxHov ? 0x88A855F7 : 0x33A855F7, 2);

        // Render 3D Rotatable Model
        LivingEntity previewEntity = (this.client != null) ? this.client.player : null;
        if (previewEntity != null) {
            try {
                float origBodyYaw = previewEntity.bodyYaw;
                float origYaw = previewEntity.getYaw();
                float origPitch = previewEntity.getPitch();
                float origPrevBodyYaw = previewEntity.prevBodyYaw;
                float origPrevYaw = previewEntity.prevYaw;
                float origPrevPitch = previewEntity.prevPitch;
                float origHeadYaw = previewEntity.headYaw;
                float origPrevHeadYaw = previewEntity.prevHeadYaw;

                previewEntity.bodyYaw = previewYaw;
                previewEntity.setYaw(previewYaw);
                previewEntity.setPitch(previewPitch);
                previewEntity.prevBodyYaw = previewYaw;
                previewEntity.prevYaw = previewYaw;
                previewEntity.prevPitch = previewPitch;
                previewEntity.headYaw = previewYaw;
                previewEntity.prevHeadYaw = previewYaw;

                int renderX1 = pBoxX + 10;
                int renderY1 = pBoxY + 10;
                int renderX2 = pBoxX + pBoxW - 10;
                int renderY2 = pBoxY + pBoxH - 10;

                InventoryScreen.drawEntity(context, renderX1, renderY1, renderX2, renderY2, 52, 0.0625f, 0.0f, 0.0f, previewEntity);

                previewEntity.bodyYaw = origBodyYaw;
                previewEntity.setYaw(origYaw);
                previewEntity.setPitch(origPitch);
                previewEntity.prevBodyYaw = origPrevBodyYaw;
                previewEntity.prevYaw = origPrevYaw;
                previewEntity.prevPitch = origPrevPitch;
                previewEntity.headYaw = origHeadYaw;
                previewEntity.prevHeadYaw = origPrevHeadYaw;

            } catch (Exception e) {
                render2DFallbackCape(context, pBoxX, pBoxY, pBoxW, pBoxH);
            }
        } else {
            render2DFallbackCape(context, pBoxX, pBoxY, pBoxW, pBoxH);
        }

        // Drag instruction text
        context.drawCenteredTextWithShadow(this.textRenderer, "✦ Drag to rotate 3D model ✦", pBoxX + pBoxW / 2, pBoxY + pBoxH - 18, 0xCCA855F7);

        // Preview Controls: Reset & Auto Spin
        int ctrlY = panelY + panelH - 82;
        boolean rstHov = mouseX >= rightX + 12 && mouseX <= rightX + 88 && mouseY >= ctrlY && mouseY <= ctrlY + 20;
        VeloraRenderUtil.drawPillButton(context, rightX + 12, ctrlY, 76, 20, "⟲ Reset", rstHov, 0xFF475569, this.textRenderer);

        boolean spinHov = mouseX >= rightX + 100 && mouseX <= rightX + 188 && mouseY >= ctrlY && mouseY <= ctrlY + 20;
        VeloraRenderUtil.drawPillButton(context, rightX + 100, ctrlY, 88, 20, autoRotate ? "⏸ Pause" : "↻ Auto Spin", spinHov, autoRotate ? 0xFFD97706 : 0xFF7C3AED, this.textRenderer);

        // Action Equip Button
        int eqBtnW = 160;
        int eqBtnH = 26;
        int eqBtnX = rightX + (previewW - eqBtnW) / 2;
        int eqBtnY = panelY + panelH - 54;
        boolean eqHov = mouseX >= eqBtnX && mouseX <= eqBtnX + eqBtnW && mouseY >= eqBtnY && mouseY <= eqBtnY + eqBtnH;

        boolean isEquippedCape = (selectedCosmetic == 0 && ModConfig.enableCape);
        VeloraRenderUtil.drawPillButton(context, eqBtnX, eqBtnY, eqBtnW, eqBtnH, isEquippedCape ? "Unequip Cape" : "Equip Cosmetic", eqHov, isEquippedCape ? 0xFFDC2626 : 0xFF16A34A, this.textRenderer);

        context.drawCenteredTextWithShadow(this.textRenderer, "Cosmetic selected: " + cosmeticNames[selectedCosmetic], rightX + previewW / 2, panelY + panelH - 22, 0xFFC084FC);

        // --- SETTINGS OVERLAY MODAL ---
        if (showSettingsModal) {
            int modalW = 360;
            int modalH = 220;
            int modalX = cx - modalW / 2;
            int modalY = cy - modalH / 2;

            VeloraRenderUtil.drawSolidPanel(context, modalX, modalY, modalW, modalH, 0xFF121422, 0xFFA855F7);
            VeloraRenderUtil.drawGlowBorder(context, modalX, modalY, modalW, modalH, 0xAAA855F7, 4);

            context.drawText(this.textRenderer, "⚙ VELORA CAPE & PHYSICS OPTIONS", modalX + 16, modalY + 14, 0xFFFFFFFF, true);

            int mRowY = modalY + 42;
            int mRowH = 32;

            renderModalToggle(context, mouseX, mouseY, modalX + 16, mRowY, modalW - 32, mRowH, "Cloth Cape Physics (Wavey Capes)", ModConfig.enableCapePhysics);
            renderModalToggle(context, mouseX, mouseY, modalX + 16, mRowY + 38, modalW - 32, mRowH, "Override Account / Vanilla Capes", ModConfig.overrideDefaultCape);
            renderModalToggle(context, mouseX, mouseY, modalX + 16, mRowY + 76, modalW - 32, mRowH, "Apply Cape to Local Player Only", ModConfig.capeOnlyLocal);

            int closeM = modalX + modalW - 80;
            int closeMY = modalY + modalH - 34;
            boolean mHov = mouseX >= closeM && mouseX <= closeM + 68 && mouseY >= closeMY && mouseY <= closeMY + 22;
            VeloraRenderUtil.drawPillButton(context, closeM, closeMY, 68, 22, "Done", mHov, 0xFF8B5CF6, this.textRenderer);
        }
    }

    private void render2DFallbackCape(DrawContext context, int pBoxX, int pBoxY, int pBoxW, int pBoxH) {
        int capeW = 70;
        int capeH = 120;
        int capeX = pBoxX + (pBoxW - capeW) / 2;
        int capeY = pBoxY + (pBoxH - capeH) / 2 - 10;

        int capeColor = (selectedCosmetic == 0) ? 0xFF7C3AED : ((selectedCosmetic == 1) ? 0xFF2563EB : 0xFF059669);
        VeloraRenderUtil.drawGradient(context, capeX, capeY, capeX + capeW, capeY + capeH, capeColor, 0xFF1E1035);
        context.drawBorder(capeX, capeY, capeW, capeH, 0xFFA855F7);
        VeloraRenderUtil.drawGlowBorder(context, capeX, capeY, capeW, capeH, 0x88A855F7, 2);

        context.drawCenteredTextWithShadow(this.textRenderer, "VELORA", capeX + capeW / 2, capeY + 40, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "HD CAPE", capeX + capeW / 2, capeY + 56, 0xFFD8B4FE);
    }

    private void renderModalToggle(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String title, boolean enabled) {
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        VeloraRenderUtil.drawSolidPanel(context, x, y, w, h, hov ? 0xFF22263B : 0xFF181B2B, 0xFF3D4363);
        context.drawText(this.textRenderer, title, x + 10, y + (h - 8) / 2, 0xFFFFFFFF, true);

        boolean swHov = mouseX >= x + w - 38 && mouseX <= x + w - 10 && mouseY >= y + (h - 14) / 2 && mouseY <= y + (h + 14) / 2;
        VeloraRenderUtil.drawSwitch(context, x + w - 38, y + (h - 14) / 2, 28, 14, enabled, swHov);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = Math.min(720, this.width - 20);
        int panelH = Math.min(430, this.height - 20);
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // Modal clicks
        if (showSettingsModal) {
            int modalW = 360;
            int modalH = 220;
            int modalX = cx - modalW / 2;
            int modalY = cy - modalH / 2;

            int mRowY = modalY + 42;
            int mRowH = 32;

            if (mx >= modalX + 16 && mx <= modalX + modalW - 16) {
                if (my >= mRowY && my <= mRowY + mRowH) {
                    ModConfig.enableCapePhysics = !ModConfig.enableCapePhysics;
                    ModConfig.saveConfig();
                    return true;
                }
                if (my >= mRowY + 38 && my <= mRowY + 38 + mRowH) {
                    ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape;
                    ModConfig.saveConfig();
                    return true;
                }
                if (my >= mRowY + 76 && my <= mRowY + 76 + mRowH) {
                    ModConfig.capeOnlyLocal = !ModConfig.capeOnlyLocal;
                    ModConfig.saveConfig();
                    return true;
                }
            }

            int closeM = modalX + modalW - 80;
            int closeMY = modalY + modalH - 34;
            if (mx >= closeM && mx <= closeM + 68 && my >= closeMY && my <= closeMY + 22) {
                showSettingsModal = false;
                return true;
            }
            return true;
        }

        // X Close
        if (mx >= panelX + panelW - 24 && mx <= panelX + panelW - 6 && my >= panelY + 6 && my <= panelY + 24) {
            this.close();
            return true;
        }

        int sideW = 140;
        int sideY = panelY + 44;

        // Categories
        for (int i = 0; i < 7; i++) {
            int ry = sideY + i * 28;
            if (mx >= panelX + 6 && mx <= panelX + sideW - 6 && my >= ry && my <= ry + 24) {
                activeCategory = i;
                return true;
            }
        }

        // Sidebar Options button
        int settBtnY = panelY + panelH - 34;
        if (mx >= panelX + 8 && mx <= panelX + sideW - 8 && my >= settBtnY && my <= settBtnY + 24) {
            showSettingsModal = true;
            return true;
        }

        // Cosmetic Cards Selection
        int previewW = 200;
        int centerW = panelW - sideW - previewW - 12;
        int centerX = panelX + sideW + 6;
        int gridY = panelY + 74;
        int cardW = 86;
        int cardH = 114;
        int gap = 8;

        for (int i = 0; i < 3; i++) {
            int col = i % 3;
            int row = i / 3;
            int cardX = centerX + 10 + col * (cardW + gap);
            int cardY = gridY + row * (cardH + gap);

            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH) {
                selectedCosmetic = i;
                return true;
            }
        }

        // 3D Viewport Mouse Drag Activation & Control Buttons
        int rightX = centerX + centerW + 6;
        int pBoxW = 176;
        int pBoxH = 240;
        int pBoxX = rightX + (previewW - pBoxW) / 2;
        int pBoxY = panelY + 60;

        if (mx >= pBoxX && mx <= pBoxX + pBoxW && my >= pBoxY && my <= pBoxY + pBoxH) {
            isDraggingPreview = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        int ctrlY = panelY + panelH - 82;
        if (mx >= rightX + 12 && mx <= rightX + 88 && my >= ctrlY && my <= ctrlY + 20) {
            previewYaw = 180.0f;
            previewPitch = 0.0f;
            autoRotate = false;
            return true;
        }

        if (mx >= rightX + 100 && mx <= rightX + 188 && my >= ctrlY && my <= ctrlY + 20) {
            autoRotate = !autoRotate;
            return true;
        }

        // Right Equip Button
        int eqBtnW = 160;
        int eqBtnH = 26;
        int eqBtnX = rightX + (previewW - eqBtnW) / 2;
        int eqBtnY = panelY + panelH - 54;

        if (mx >= eqBtnX && mx <= eqBtnX + eqBtnW && my >= eqBtnY && my <= eqBtnY + eqBtnH) {
            if (selectedCosmetic == 0) {
                ModConfig.enableCape = !ModConfig.enableCape;
                ModConfig.saveConfig();
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingPreview = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingPreview && button == 0) {
            previewYaw = (previewYaw + (float) deltaX * 1.5f) % 360.0f;
            previewPitch = Math.max(-40.0f, Math.min(40.0f, previewPitch + (float) deltaY * 1.5f));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
