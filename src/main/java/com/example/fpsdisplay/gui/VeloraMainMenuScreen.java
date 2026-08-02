package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.client.ModKeybindings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * VeloraMainMenuScreen — fully standalone Screen (not TitleScreen).
 *
 * Zero vanilla TitleScreen rendering involved, so there are NO panorama bleed-through issues.
 * Styled after the Leaf Client reference: dark cinematic background, centered logo + title,
 * dark pill buttons with icons, top bar with user info, bottom icon bar.
 */
public class VeloraMainMenuScreen extends Screen {

    private static final Identifier LOGO = Identifier.of("fpsdisplay", "textures/gui/logo.png");

    // Button layout
    private static final int BTN_W = 230;
    private static final int BTN_H = 28;
    private static final int BTN_GAP = 6;

    // Track hovered button index (-1 = none)
    private int hoveredBtn = -1;

    // Animated bokeh circles (simulated)
    private float animTick = 0f;

    public VeloraMainMenuScreen() {
        super(Text.literal("Velora Client"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;  // Can't close the main menu with ESC
    }

    // ──────────────────────────────────────────────────────────────
    // Completely blocks the vanilla panorama — solid opaque fill, NO super call
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF060A12);
    }

    // ──────────────────────────────────────────────────────────────
    // Main render
    // ──────────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animTick += delta;

        // 1. Background
        renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context);

        // 2. Top bar
        drawTopBar(context, mouseX, mouseY);

        // 3. Center content (logo + buttons)
        int cx = this.width / 2;
        int cy = this.height / 2;
        drawCenterContent(context, cx, cy, mouseX, mouseY);

        // 4. Bottom bar
        drawBottomBar(context, mouseX, mouseY);
        // Do NOT call super.render() — it calls Screen.renderBackground() again
        // which would re-draw the panorama on top of everything.
    }

    // ──────────────────────────────────────────────────────────────
    // Cinematic background — gradient + subtle ambient glows
    // ──────────────────────────────────────────────────────────────
    private void drawBackground(DrawContext context) {
        // Dark blue-black radial gradient simulation with layered fills
        VeloraRenderUtil.drawGradient(context, 0, 0, this.width, this.height / 2, 0xFF090D18, 0xFF0C1020);
        VeloraRenderUtil.drawGradient(context, 0, this.height / 2, this.width, this.height, 0xFF0C1020, 0xFF070B14);

        // Subtle purple ambient glow from center
        int gx = this.width / 2 - 180;
        int gy = this.height / 2 - 160;
        VeloraRenderUtil.drawGlowBorder(context, gx, gy, 360, 320, 0x18A855F7, 40);
        VeloraRenderUtil.drawGlowBorder(context, gx + 60, gy + 60, 240, 200, 0x10C084FC, 20);

        // Subtle blue ambient from bottom-left (scene lighting)
        VeloraRenderUtil.drawGlowBorder(context, -30, this.height - 120, 200, 180, 0x183B82F6, 30);

        // Faint golden tint top (simulates bokeh lanterns from reference)
        VeloraRenderUtil.drawGradient(context, this.width / 4, 0, (this.width / 4) * 3, this.height / 3,
                0x0AEAB308, 0x00EAB308);

        // Simulated bokeh dots (small, very faint)
        drawBokeh(context);
    }

    private void drawBokeh(DrawContext context) {
        // Static scattered glowing circles to simulate the lantern bokeh in the reference
        int[][] dots = {
                {(int)(this.width * 0.35f), (int)(this.height * 0.15f), 3, 0x1AEAB308},
                {(int)(this.width * 0.55f), (int)(this.height * 0.10f), 5, 0x15EAB308},
                {(int)(this.width * 0.45f), (int)(this.height * 0.20f), 2, 0x20EAB308},
                {(int)(this.width * 0.60f), (int)(this.height * 0.25f), 4, 0x12EAB308},
                {(int)(this.width * 0.30f), (int)(this.height * 0.25f), 3, 0x18EAB308},
                {(int)(this.width * 0.70f), (int)(this.height * 0.18f), 2, 0x0FEAB308},
                {(int)(this.width * 0.25f), (int)(this.height * 0.12f), 4, 0x10EAB308},
                {(int)(this.width * 0.80f), (int)(this.height * 0.22f), 3, 0x14EAB308},
        };
        for (int[] d : dots) {
            int bx = d[0], by = d[1], bs = d[2], bc = d[3];
            VeloraRenderUtil.drawGlowBorder(context, bx - bs, by - bs, bs * 2, bs * 2, bc, bs + 3);
            context.fill(bx - 1, by - 1, bx + 2, by + 2, bc | 0x33000000);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Top bar — Leaf-style: server name left, icons right
    // ──────────────────────────────────────────────────────────────
    private void drawTopBar(DrawContext context, int mouseX, int mouseY) {
        // Dark frosted glass strip
        context.fill(0, 0, this.width, 22, 0xAA060A12);
        context.fill(0, 21, this.width, 22, 0x33A855F7);

        // Username (left side)
        String username = (this.client != null && this.client.getSession() != null)
                ? this.client.getSession().getUsername() : "Player";
        context.fill(8, 4, 14, 18, 0xFF22C55E); // green status dot
        context.drawText(this.textRenderer, username, 18, 7, 0xFFDDDDDD, true);

        // Right icons: settings dot, close dot (decorative)
        int iconRight = this.width - 10;
        // X close area
        context.fill(iconRight - 10, 5, iconRight, 17, 0x33FF4444);
        context.drawBorder(iconRight - 10, 5, 10, 12, 0x44FF6666);
        context.drawCenteredTextWithShadow(this.textRenderer, "×", iconRight - 5, 6, 0xFFCC4444);
        // gear icon
        context.fill(iconRight - 24, 5, iconRight - 12, 17, 0x22FFFFFF);
        context.drawBorder(iconRight - 24, 5, 12, 12, 0x33FFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "⚙", iconRight - 18, 6, 0xFF888888);
    }

    // ──────────────────────────────────────────────────────────────
    // Center content — Logo + "VELORA CLIENT" + buttons
    // ──────────────────────────────────────────────────────────────
    private void drawCenterContent(DrawContext context, int cx, int cy, int mouseX, int mouseY) {
        // Logo icon
        int logoSize = 48;
        int logoX = cx - logoSize / 2;
        int logoY = cy - 130;

        // Subtle glow behind logo
        VeloraRenderUtil.drawGlowBorder(context, logoX - 4, logoY - 4, logoSize + 8, logoSize + 8, 0x44A855F7, 6);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(RenderLayer::getGuiTextured, LOGO, logoX, logoY, 0f, 0f, logoSize, logoSize, logoSize, logoSize);

        // "VELORA CLIENT" bold title (drawn large via multiple offsets = faux bold)
        int titleY = logoY + logoSize + 8;
        context.drawCenteredTextWithShadow(this.textRenderer, "VELORA CLIENT", cx, titleY, 0xFFFFFFFF);
        // Subtitle
        context.drawCenteredTextWithShadow(this.textRenderer, "The Velora Experience", cx, titleY + 12, 0xFF8888AA);

        // Thin separator line
        int sepY = titleY + 24;
        context.fill(cx - 70, sepY, cx + 70, sepY + 1, 0x44A855F7);

        // Track hovered button
        hoveredBtn = -1;
        int btnStartY = sepY + 14;

        // Button definitions: {icon, label, key for detection}
        String[][] buttons = {
                {"▶", "Singleplayer"},
                {"⚔", "Multiplayer"},
                {"★", "Velora Settings"},
                {"⚙", "Options"},
        };

        for (int i = 0; i < buttons.length; i++) {
            int bx = cx - BTN_W / 2;
            int by = btnStartY + i * (BTN_H + BTN_GAP);
            boolean hov = mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H;
            if (hov) hoveredBtn = i;
            drawLeafButton(context, bx, by, BTN_W, BTN_H, buttons[i][0], buttons[i][1], hov, i == 2);
        }

        // Quit row — small, below the main buttons
        int quitY = btnStartY + 4 * (BTN_H + BTN_GAP) + 4;
        boolean qHov = mouseX >= cx - 55 && mouseX <= cx + 55 && mouseY >= quitY && mouseY <= quitY + 16;
        context.drawCenteredTextWithShadow(this.textRenderer, qHov ? "✕  Quit Game" : "Quit Game",
                cx, quitY + 2, qHov ? 0xFFFF6666 : 0xFF666688);
    }

    /**
     * Draws a Leaf Client-style button: dark semi-transparent fill, subtle border,
     * left icon, center text. Featured buttons get a green accent.
     */
    private void drawLeafButton(DrawContext context, int x, int y, int w, int h, String icon, String label, boolean hovered, boolean featured) {
        // Background
        int bg = featured
                ? (hovered ? 0xDD1A3322 : 0xCC111D17)
                : (hovered ? 0xCC1A1D28 : 0xBB0E111C);
        context.fill(x, y, x + w, y + h, bg);

        // Border
        int border = featured
                ? (hovered ? 0x8822CC55 : 0x5518AA40)
                : (hovered ? 0x55A855F7 : 0x33FFFFFF);
        context.drawBorder(x, y, w, h, border);

        // Left accent bar (1px, featured = green, others = purple when hovered)
        if (hovered || featured) {
            int accentColor = featured ? 0xFF22C55E : 0xFF9F7AEA;
            context.fill(x, y, x + 2, y + h, accentColor);
        }

        // Icon (left aligned)
        int iconColor = featured ? 0xFF22C55E : (hovered ? 0xFFBFA7FF : 0xFF9CA3AF);
        context.drawText(this.textRenderer, icon, x + 14, y + (h - 8) / 2, iconColor, true);

        // Label (centered)
        int textColor = featured ? 0xFF86EFAC : (hovered ? 0xFFFFFFFF : 0xFFCCCCCC);
        context.drawCenteredTextWithShadow(this.textRenderer, label, x + w / 2 + 8, y + (h - 8) / 2, textColor);
    }

    // ──────────────────────────────────────────────────────────────
    // Bottom bar — Leaf-style icon row
    // ──────────────────────────────────────────────────────────────
    private void drawBottomBar(DrawContext context, int mouseX, int mouseY) {
        int barY = this.height - 28;
        context.fill(0, barY, this.width, this.height, 0xAA060A12);
        context.fill(0, barY, this.width, barY + 1, 0x33FFFFFF);

        // Cosmetics button (center bottom)
        int cosmW = 140;
        int cosmX = this.width / 2 - cosmW / 2;
        int cosmY = barY + 4;
        boolean cosmHov = mouseX >= cosmX && mouseX <= cosmX + cosmW && mouseY >= cosmY && mouseY <= cosmY + 20;
        context.fill(cosmX, cosmY, cosmX + cosmW, cosmY + 20,
                cosmHov ? 0xCC2A1545 : 0xBB180D2E);
        context.drawBorder(cosmX, cosmY, cosmW, 20,
                cosmHov ? 0x99A855F7 : 0x55A855F7);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "✦  Cosmetics Locker", cosmX + cosmW / 2, cosmY + 6,
                cosmHov ? 0xFFD8B4FE : 0xFF9F7AEA);

        // Version text (left)
        context.drawText(this.textRenderer, "Velora Client  1.21.4", 10, barY + 9, 0xFF445566, true);
        // Keybind hint (right)
        context.drawText(this.textRenderer, "Mods: V", this.width - 55, barY + 9, 0xFF445566, true);
    }

    // ──────────────────────────────────────────────────────────────
    // Mouse click dispatch
    // ──────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        // Top bar X close
        if (mx >= this.width - 20 && mx <= this.width - 10 && my >= 5 && my <= 17) {
            if (this.client != null) this.client.scheduleStop();
            return true;
        }

        // Top bar gear = options
        if (mx >= this.width - 34 && mx <= this.width - 22 && my >= 5 && my <= 17) {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
            return true;
        }

        // Center buttons
        int sepY = cy - 130 + 48 + 8 + 12 + 25; // matches drawCenterContent layout
        int btnStartY = sepY + 14;
        int bx = cx - BTN_W / 2;

        for (int i = 0; i < 4; i++) {
            int by = btnStartY + i * (BTN_H + BTN_GAP);
            if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H) {
                handleButtonClick(i);
                return true;
            }
        }

        // Quit text row
        int quitY = btnStartY + 4 * (BTN_H + BTN_GAP) + 4;
        if (mx >= cx - 55 && mx <= cx + 55 && my >= quitY && my <= quitY + 16) {
            if (this.client != null) this.client.scheduleStop();
            return true;
        }

        // Bottom cosmetics button
        int barY = this.height - 28;
        int cosmW = 140, cosmX = cx - cosmW / 2, cosmY = barY + 4;
        if (mx >= cosmX && mx <= cosmX + cosmW && my >= cosmY && my <= cosmY + 20) {
            if (this.client != null) this.client.setScreen(new CosmeticsLockerScreen());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleButtonClick(int index) {
        if (this.client == null) return;
        switch (index) {
            case 0 -> this.client.setScreen(new SelectWorldScreen(this));
            case 1 -> this.client.setScreen(new MultiplayerScreen(this));
            case 2 -> this.client.setScreen(new ModMenuScreen());
            case 3 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
        }
    }

    @Override
    public void tick() {
        animTick += 0.05f;
    }
}
