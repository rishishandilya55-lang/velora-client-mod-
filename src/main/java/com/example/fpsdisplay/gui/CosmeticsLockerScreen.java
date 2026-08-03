package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Velora Client — Cosmetics Locker GUI Screen (OwoLib remake)
 * Clean layout, typography fix, texture card thumbnails & live 3D player cosmetic preview with 360° mouse rotation.
 */
public class CosmeticsLockerScreen extends BaseOwoScreen<FlowLayout> {

    private static final Identifier BG_TEX = Identifier.of("fpsdisplay", "textures/gui/background.png");

    private int activeCategory = 0;
    private int activeEnv = 0;
    private boolean showOptions = false;
    private static int selectedItem = 0;
    private String searchQuery = "";

    // 3D preview rotation state
    private static boolean isLockerOpen = false;
    private float yaw = 0f;
    private float pitch = 0f;
    private boolean autoSpin = false;

    // Cosmetic Item Definitions
    private static final String[] COS_NAMES  = {"Velora Cape", "Classic Cape", "Wave Cape"};
    private static final String[] COS_TYPES  = {"HD Animated", "Vintage",      "Particle"};
    private static final int[]    COS_COLORS = {0xFF8B21F7,    0xFF2563EB,     0xFF059669};
    private static final Identifier[] COS_THUMBS = {
        Identifier.of("fpsdisplay", "textures/gui/logo.png"),
        Identifier.of("fpsdisplay", "textures/gui/armor_status.png"),
        Identifier.of("fpsdisplay", "textures/gui/fullbright.png")
    };

    // Components & Layout refs
    private FlowLayout cardGrid;
    private ButtonComponent[] catButtons;
    private EntityComponent<LivingEntity> entityComponent;

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Client – Cosmetics Locker"));
    }

    public static boolean isPreviewingCape() { return isLockerOpen; }
    public static int getPreviewingCapeIndex() { return isLockerOpen ? selectedItem : -1; }

    @Override
    public void close() {
        isLockerOpen = false;
        super.close();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        isLockerOpen = true;

        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000)); // transparent root — background drawn in render()
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Outer Panel (780x460) ─────────────────────────────────────────────
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(780), Sizing.fixed(460));
        panel.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA090A12);
            context.drawBorder(x, y, w, h, 0xFF7C3AED);
            context.drawBorder(x + 1, y + 1, w - 2, h - 2, 0x223B0764);
        });
        panel.padding(Insets.none());

        // ── 1. Top Header Bar (Clean title & close button, no overlapping) ────
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        header.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA070810);
            context.fill(x, y + h - 1, x + w, y + h, 0xFF5B21B6);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 14, 0, 14));

        header.child(Components.label(Text.literal("✦  CLIENT COSMETICS LOCKER"))
            .color(Color.ofArgb(0xFFF1F5F9))
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x55FF4444, 0x00000000));
        header.child(closeBtn);

        panel.child(header);

        // ── 2. Main Content Body (Sidebar | Card Grid | 3D Player Preview) ─────
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        // ── Left Sidebar (130px wide, clean typography & hover effects) ───────
        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(130), Sizing.fill(100));
        sidebar.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA05050C);
            context.fill(x + w - 1, y, x + w, y + h, 0xFF5B21B6);
        });
        sidebar.padding(Insets.of(12, 6, 12, 6));
        sidebar.gap(5);

        String[] cats = {"All", "Favorites", "Capes", "Hats", "Face", "Wings", "Aura"};
        catButtons = new ButtonComponent[cats.length];
        for (int i = 0; i < cats.length; i++) {
            final int idx = i;
            ButtonComponent catBtn = Components.button(Text.literal(cats[i]), btn -> {
                activeCategory = idx;
                refreshCatButtons();
                rebuildGrid();
            });
            catBtn.sizing(Sizing.fill(100), Sizing.fixed(24));
            styleCatBtn(catBtn, i == activeCategory);
            catButtons[i] = catBtn;
            sidebar.child(catBtn);
        }

        // Spacer
        sidebar.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // Options button
        ButtonComponent optBtn = Components.button(Text.literal("⚙  Physics Options"), btn -> showOptions = !showOptions);
        optBtn.sizing(Sizing.fill(100), Sizing.fixed(22));
        optBtn.renderer(ButtonComponent.Renderer.flat(0xFF120A28, 0xFF21104A, 0xFF120A28));
        sidebar.child(optBtn);

        body.child(sidebar);

        // ── Center Card Grid Section ──────────────────────────────────────────
        FlowLayout centerSection = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        centerSection.surface(Surface.flat(0xF7070810));
        centerSection.padding(Insets.of(8, 8, 8, 8));
        centerSection.gap(8);

        // Search bar
        FlowLayout searchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        searchRow.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF111128);
            context.drawBorder(x, y, w, h, 0xFF2D1F54);
        });
        searchRow.padding(Insets.of(0, 8, 0, 8));
        searchRow.verticalAlignment(VerticalAlignment.CENTER);
        searchRow.child(Components.label(Text.literal("Search Cosmetics..."))
            .color(Color.ofArgb(0xFF7755AA))
            .sizing(Sizing.fill(100), Sizing.content()));
        centerSection.child(searchRow);

        // Card grid
        cardGrid = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        cardGrid.gap(8);
        buildGrid();

        var scrollBox = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), cardGrid);
        centerSection.child(scrollBox);

        body.child(centerSection);

        // ── Right 3D Player Preview Panel (230px wide, live EntityComponent) ──
        FlowLayout previewSection = Containers.verticalFlow(Sizing.fixed(230), Sizing.fill(100));
        previewSection.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA060710);
            context.fill(x, y, x + 1, y + h, 0xFF5B21B6);
        });
        previewSection.padding(Insets.of(10, 10, 10, 10));
        previewSection.gap(6);

        // Environment tabs
        FlowLayout envRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        envRow.gap(2);
        String[] envs = {"Sun", "World", "Hell", "End"};
        for (int i = 0; i < envs.length; i++) {
            final int idx = i;
            ButtonComponent envBtn = Components.button(Text.literal(envs[i]), btn -> { activeEnv = idx; });
            envBtn.sizing(Sizing.fill(25), Sizing.fixed(18));
            envBtn.renderer(ButtonComponent.Renderer.flat(0xFF0C0C1A, 0xFF1A0E3A, 0xFF0C0C1A));
            envRow.child(envBtn);
        }
        previewSection.child(envRow);

        // 3D Player EntityComponent viewport container
        FlowLayout entityViewport = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(240));
        entityViewport.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            int[] envBg = {0xFF0C0C1E, 0xFF081A0C, 0xFF1A0808, 0xFF0A0A0A};
            context.fill(x, y, x + w, y + h, envBg[Math.min(activeEnv, envBg.length - 1)]);
            context.drawBorder(x, y, w, h, 0xFF6D28D9);
        });
        entityViewport.verticalAlignment(VerticalAlignment.CENTER);
        entityViewport.horizontalAlignment(HorizontalAlignment.CENTER);

        // Live player entity setup
        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity targetPlayer = (mc != null && mc.player != null) ? mc.player : null;
        if (targetPlayer != null) {
            entityComponent = Components.entity(Sizing.fixed(220), targetPlayer);
            entityComponent.sizing(Sizing.fill(100), Sizing.fixed(220));
            entityComponent.allowMouseRotation(true);
            entityComponent.showNametag(false);
            entityViewport.child(entityComponent);
        } else {
            entityViewport.child(Components.label(Text.literal("Player preview available\nin-game"))
                .color(Color.ofArgb(0xFF7755AA))
                .sizing(Sizing.content(), Sizing.content()));
        }

        previewSection.child(entityViewport);

        // Reset & Spin control row
        FlowLayout ctrlRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        ctrlRow.gap(6);
        ButtonComponent resetBtn = Components.button(Text.literal("⟲ Reset"), btn -> {
            yaw = 0f; pitch = 0f; autoSpin = false;
        });
        resetBtn.sizing(Sizing.fixed(72), Sizing.fixed(20));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF110822, 0xFF21104A, 0xFF110822));

        ButtonComponent spinBtn = Components.button(
            Text.literal(autoSpin ? "⏸ Stop" : "↻ Spin"),
            btn -> {
                autoSpin = !autoSpin;
                btn.setMessage(Text.literal(autoSpin ? "⏸ Stop" : "↻ Spin"));
            });
        spinBtn.sizing(Sizing.fill(100), Sizing.fixed(20));
        spinBtn.renderer(ButtonComponent.Renderer.flat(0xFF130828, 0xFF1E104A, 0xFF130828));
        ctrlRow.child(resetBtn);
        ctrlRow.child(spinBtn);
        previewSection.child(ctrlRow);

        // Equip / Unequip button
        boolean equipped = ModConfig.enableCape && ModConfig.selectedCape == selectedItem;
        ButtonComponent eqBtn = Components.button(
            Text.literal(equipped ? "✕  Unequip Cape" : "✓  Equip Cape"),
            btn -> {
                if (ModConfig.enableCape && ModConfig.selectedCape == selectedItem) {
                    ModConfig.enableCape = false;
                } else {
                    ModConfig.enableCape = true;
                    ModConfig.selectedCape = selectedItem;
                }
                ModConfig.saveConfig();
                rebuildGrid();
            });
        eqBtn.sizing(Sizing.fill(100), Sizing.fixed(24));
        eqBtn.renderer(ButtonComponent.Renderer.flat(
            equipped ? 0xFF3D0606 : 0xFF0B3814,
            equipped ? 0xFF5B0A0A : 0xFF135721,
            equipped ? 0xFF3D0606 : 0xFF0B3814));
        previewSection.child(eqBtn);

        body.child(previewSection);
        panel.child(body);

        root.child(panel);
    }

    // ── Category Sidebar Button Styling ───────────────────────────────────────
    private void styleCatBtn(ButtonComponent btn, boolean active) {
        int bg = active ? 0xFF18103A : 0x00000000;
        int hoverBg = active ? 0xFF1C1438 : 0xFF120B28;
        String text = btn.getMessage().getString();

        btn.renderer((context, button, delta) -> {
            int x = button.x(), y = button.y(), w = button.width(), h = button.height();
            boolean hovered = button.isHovered();
            int currentBg = hovered ? hoverBg : bg;

            if (currentBg != 0) {
                context.fill(x, y, x + w, y + h, currentBg);
            }

            if (active) {
                context.fill(x, y, x + 3, y + h, 0xFF8B21F7);
            }

            int textClr = active ? 0xFFE2D9FF : (hovered ? 0xFFBBAFDD : 0xFF665577);
            int textX = x + (w - textRenderer.getWidth(text)) / 2;
            int textY = y + (h - 8) / 2;
            context.drawText(textRenderer, text, textX, textY, textClr, true);
        });
    }

    private void refreshCatButtons() {
        for (int i = 0; i < catButtons.length; i++) {
            styleCatBtn(catButtons[i], i == activeCategory);
        }
    }

    // ── Cards Grid Builder ───────────────────────────────────────────────────
    private void buildGrid() {
        cardGrid.clearChildren();
        FlowLayout row = null;
        int col = 0;
        final int COLS = 3;

        for (int i = 0; i < COS_NAMES.length; i++) {
            if (activeCategory == 1 && (ModConfig.favoriteCosmetics == null || !ModConfig.favoriteCosmetics[i])) continue;
            if (activeCategory >= 3) continue;
            if (!searchQuery.isEmpty() && !COS_NAMES[i].toLowerCase().contains(searchQuery)) continue;

            if (col == 0) {
                row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                row.gap(8);
                cardGrid.child(row);
            }

            final int idx = i;
            FlowLayout card = buildCard(i);
            if (row != null) row.child(card);

            col++;
            if (col >= COLS) col = 0;
        }
    }

    private FlowLayout buildCard(int i) {
        boolean sel = (selectedItem == i);
        boolean isFav = (ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics[i]);

        FlowLayout card = Containers.verticalFlow(Sizing.fixed(122), Sizing.fixed(140));

        int bg = sel ? 0xFF16112C : 0xFF0D0B1A;
        int border = sel ? 0xFF8B21F7 : 0xFF1D1B36;

        card.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, bg);
            context.drawBorder(x, y, w, h, border);
            if (sel) {
                context.fill(x, y, x + 3, y + h, 0xFF8B21F7);
            }
        });
        card.padding(Insets.of(6, 6, 6, 6));
        card.gap(4);

        // Top row: favorite star + title
        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        topRow.verticalAlignment(VerticalAlignment.CENTER);

        ButtonComponent starBtn = Components.button(Text.literal(isFav ? "★" : "☆"), btn -> {
            if (ModConfig.favoriteCosmetics == null) ModConfig.favoriteCosmetics = new boolean[]{false, false, false};
            ModConfig.favoriteCosmetics[i] = !ModConfig.favoriteCosmetics[i];
            ModConfig.saveConfig();
            rebuildGrid();
        });
        starBtn.sizing(Sizing.fixed(16), Sizing.fixed(12));
        starBtn.renderer((context, button, delta) -> {
            int textClr = isFav ? 0xFFEAB308 : 0xFF525270;
            context.drawText(textRenderer, button.getMessage(), button.x(), button.y(), textClr, true);
        });
        topRow.child(starBtn);

        topRow.child(Components.label(Text.literal(COS_NAMES[i]))
            .color(Color.ofArgb(0xFFF1F5F9))
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));
        card.child(topRow);

        // Texture Thumbnail Block (High-resolution texture with fallback sprite rendering)
        FlowLayout thumbBox = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(70));
        final int capeColor = COS_COLORS[i];
        final Identifier thumbTex = COS_THUMBS[i];

        thumbBox.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            // Background fill
            context.fill(x, y, x + w, y + h, 0xFF080712);
            // Draw thumbnail texture sprite
            context.drawTexture(RenderLayer::getGuiTextured, thumbTex, x + (w - 32) / 2, y + (h - 32) / 2, 0f, 0f, 32, 32, 32, 32);
            // Outer crisp accent border
            context.drawBorder(x, y, w, h, (capeColor & 0x00FFFFFF) | 0x66000000);
        });
        card.child(thumbBox);

        // Subtitle badge
        card.child(Components.label(Text.literal(COS_TYPES[i]))
            .color(Color.ofArgb(0xFF7755AA))
            .sizing(Sizing.fill(100), Sizing.content()));

        // Click to select item
        card.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { selectedItem = i; rebuildGrid(); return true; }
            return false;
        });

        return card;
    }

    private void rebuildGrid() { buildGrid(); }

    // ── Raw Background & Options Overlay ─────────────────────────────────────
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Render ultra HD background image
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(RenderLayer::getGuiTextured, BG_TEX, 0, 0, 0f, 0f, this.width, this.height, this.width, this.height);

        // 2. Dark vignette overlay
        context.fill(0, 0, this.width, this.height, 0x88060A12);

        // 3. OwoLib UI components (Panels, Grid, EntityComponent)
        super.render(context, mouseX, mouseY, delta);

        // 4. Options modal overlay if opened
        if (showOptions) drawOptionsModal(context, mouseX, mouseY);
    }

    private void drawOptionsModal(DrawContext ctx, int mx, int my) {
        int mW = 360, mH = 200;
        int mX = (this.width - mW) / 2, mY = (this.height - mH) / 2;
        ctx.fill(mX, mY, mX + mW, mY + mH, 0xFF0D0D1E);
        ctx.drawBorder(mX, mY, mW, mH, 0xFF8B21F7);
        ctx.drawText(this.textRenderer, "⚙  Cape & Physics Options", mX + 14, mY + 12, 0xFFFFFFFF, true);

        drawModalRow(ctx, mx, my, mX + 14, mY + 38,  mW - 28, "Cloth Physics (Wavey Capes)", ModConfig.enableCapePhysics);
        drawModalRow(ctx, mx, my, mX + 14, mY + 74,  mW - 28, "Override Vanilla Capes",       ModConfig.overrideDefaultCape);
        drawModalRow(ctx, mx, my, mX + 14, mY + 110, mW - 28, "Local Player Only",            ModConfig.capeOnlyLocal);

        int doneX = mX + mW - 80, doneY = mY + mH - 32;
        ctx.fill(doneX, doneY, doneX + 68, doneY + 22, 0xFF130828);
        ctx.drawBorder(doneX, doneY, 68, 22, 0xFF3D1D6A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Done", doneX + 34, doneY + 7, 0xFFAA88DD);
    }

    private void drawModalRow(DrawContext ctx, int mx, int my, int x, int y, int w, String label, boolean on) {
        ctx.fill(x, y, x + w, y + 28, 0xFF0D0D20);
        ctx.drawBorder(x, y, w, 28, 0xFF1A1830);
        ctx.drawText(this.textRenderer, label, x + 10, y + 10, 0xFFE8E0FF, false);
        int swX = x + w - 36, swY = y + 7;
        ctx.fill(swX, swY, swX + 30, swY + 14, on ? 0xFF6D28D9 : 0xFF1E1A34);
        ctx.drawBorder(swX, swY, 30, 14, on ? 0xFFA855F7 : 0xFF3D2A6A);
        int kx = on ? swX + 16 : swX + 2;
        ctx.fill(kx, swY + 2, kx + 12, swY + 12, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && showOptions) {
            int mW = 360, mH = 200, mX = (this.width - mW) / 2, mY = (this.height - mH) / 2;
            int mx = (int) mouseX, my = (int) mouseY;
            if (mx >= mX + 14 && mx <= mX + mW - 14) {
                if (my >= mY + 38  && my <= mY + 66)  { ModConfig.enableCapePhysics   = !ModConfig.enableCapePhysics;   ModConfig.saveConfig(); return true; }
                if (my >= mY + 74  && my <= mY + 102) { ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape; ModConfig.saveConfig(); return true; }
                if (my >= mY + 110 && my <= mY + 138) { ModConfig.capeOnlyLocal       = !ModConfig.capeOnlyLocal;       ModConfig.saveConfig(); return true; }
            }
            if (mx >= mX + mW - 80 && mx <= mX + mW - 12 && my >= mY + mH - 32 && my <= mY + mH - 10) {
                showOptions = false; return true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }
}
