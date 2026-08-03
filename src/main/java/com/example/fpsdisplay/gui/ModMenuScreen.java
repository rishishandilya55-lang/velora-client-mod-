package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Velora Client – Mod Menu Screen (OwoLib remake)
 * Sleek, ultra-dark obsidian & violet Feather-style layout with aligned header, sidebar & dividers.
 */
public class ModMenuScreen extends BaseOwoScreen<FlowLayout> {

    // Layout constants
    private static final int PANEL_W = 500;
    private static final int PANEL_H = 310;
    private static final int SIDE_W  = 55;
    private static final int HDR_H   = 32;

    // Categories
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private int selCat = 0;

    // Module definitions: {name, category, badge, color}
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
    };

    // Holds references so we can refresh the grid after category/toggle changes
    private FlowLayout moduleGridContainer;
    private final List<ButtonComponent> catPills = new ArrayList<>();
    private final net.minecraft.client.gui.RotatingCubeMapRenderer panoramaRenderer =
        new net.minecraft.client.gui.RotatingCubeMapRenderer(
            new net.minecraft.client.gui.CubeMapRenderer(net.minecraft.util.Identifier.ofVanilla("textures/gui/title/background/panorama"))
        );

    public ModMenuScreen() {
        super(Text.literal("Velora Client – Mods"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));   // handled in render() based on world state
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Outer panel wrapper (Sleek Dark Obsidian) ────────────────────────
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(PANEL_W), Sizing.fixed(PANEL_H));
        panel.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA090A12);
            context.drawBorder(x, y, w, h, 0xFF7C3AED);
            context.drawBorder(x + 1, y + 1, w - 2, h - 2, 0x223B0764);
        });
        panel.padding(Insets.none());

        // ── Top Header Row (Spans full panel width above sidebar & content) ──
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(HDR_H));
        header.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA070810);
            context.fill(x, y + h - 1, x + w, y + h, 0xFF5B21B6); // horizontal header divider line
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 10, 0, 10));
        header.gap(8);

        // Sidebar logo / title space (left 55px)
        FlowLayout logoBox = Containers.horizontalFlow(Sizing.fixed(SIDE_W - 10), Sizing.content());
        logoBox.child(Components.label(Text.literal("VELORA"))
            .color(Color.ofArgb(0xFFA855F7))
            .shadow(true)
            .sizing(Sizing.content(), Sizing.content()));
        header.child(logoBox);

        // Title
        header.child(Components.label(Text.literal("Mod Menu"))
            .color(Color.ofArgb(0xFFE2D9FF))
            .shadow(true)
            .sizing(Sizing.fixed(60), Sizing.content()));

        // Category pills
        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(CATS[i]), btn -> {
                selCat = idx;
                rebuildGrid();
                refreshPills();
            });
            pill.sizing(Sizing.fixed(42), Sizing.fixed(16));
            stylePill(pill, i == selCat);
            catPills.add(pill);
            header.child(pill);
        }

        // Spacer + close button
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content())); // spacer
        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x55FF4444, 0x00000000));
        header.child(closeBtn);

        panel.child(header);

        // ── Main Content Container (Sidebar + Grid side-by-side) ─────────────
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        // ── Sidebar (55px wide, Deep Dark) ────────────────────────────────────
        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(SIDE_W), Sizing.fill(100));
        sidebar.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFA05050C);
            context.fill(x + w - 1, y, x + w, y + h, 0xFF5B21B6); // vertical sidebar divider line
        });
        sidebar.padding(Insets.of(6, 0, 6, 0));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.gap(4);

        // Sidebar items
        sidebar.child(makeSidebarItem("MODS", true, () -> {}));
        sidebar.child(makeSidebarItem("LOOKS", false, () -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));
        sidebar.child(makeSidebarItem("HUD", false, () -> {
            if (client != null) client.setScreen(new HudEditorScreen());
        }));
        // Bottom spacer + CFG
        sidebar.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))); // spacer
        sidebar.child(makeSidebarItem("CFG", false, () -> {}));

        body.child(sidebar);

        // ── Module grid (scrollable, dark obsidian background) ──────────────
        FlowLayout gridWrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        gridWrapper.surface(Surface.flat(0xF7070810));
        gridWrapper.padding(Insets.of(8));

        moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        moduleGridContainer.gap(8); // Vertical gap between card rows
        buildGrid();

        var scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), moduleGridContainer);
        gridWrapper.child(scrollContainer);

        body.child(gridWrapper);
        panel.child(body);

        root.child(panel);
    }

    // ── Sidebar item button ───────────────────────────────────────────────────
    private ButtonComponent makeSidebarItem(String label, boolean active, Runnable action) {
        ButtonComponent btn = Components.button(Text.literal(label), b -> action.run());
        btn.sizing(Sizing.fixed(SIDE_W - 4), Sizing.fixed(36));

        int bg = active ? 0xFF18103A : 0x00000000;
        int hoverBg = active ? 0xFF1C1438 : 0xFF120B28;
        btn.renderer((context, button, delta) -> {
            int x = button.x(), y = button.y(), w = button.width(), h = button.height();
            boolean hovered = button.isHovered();
            int currentBg = hovered ? hoverBg : bg;

            // Fill background
            if (currentBg != 0) {
                context.fill(x, y, x + w, y + h, currentBg);
            }

            // Draw left accent bar when active
            if (active) {
                context.fill(x, y, x + 3, y + h, 0xFF8B21F7);
            }

            // Text label centered
            int textClr = active ? 0xFFE2D9FF : (hovered ? 0xFFBBAFDD : 0xFF665577);
            int textX = x + (w - textRenderer.getWidth(label)) / 2;
            int textY = y + (h - 8) / 2;
            context.drawText(textRenderer, label, textX, textY, textClr, true);
        });

        return btn;
    }

    // ── Category pill style ───────────────────────────────────────────────────
    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) {
            pill.renderer(ButtonComponent.Renderer.flat(0xFF6D28D9, 0xFF7E39E8, 0xFF6D28D9));
        } else {
            pill.renderer(ButtonComponent.Renderer.flat(0xFF0F0E20, 0xFF1B1934, 0xFF0F0E20));
        }
    }

    private void refreshPills() {
        for (int i = 0; i < catPills.size(); i++) {
            stylePill(catPills.get(i), i == selCat);
        }
    }

    // ── Module grid builder ───────────────────────────────────────────────────
    private void buildGrid() {
        moduleGridContainer.clearChildren();
        FlowLayout row = null;
        int col = 0;
        final int COLS = 3;

        for (int i = 0; i < MODS.length; i++) {
            String name  = (String) MODS[i][0];
            String cat   = (String) MODS[i][1];
            String badge = (String) MODS[i][2];
            int    bClr  = (int)    MODS[i][3];
            boolean en   = isEnabled(i);

            if (selCat != 0 && !cat.equals(CATS[selCat])) continue;

            if (col == 0) {
                row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                row.gap(8); // Horizontal gap between module cards
                moduleGridContainer.child(row);
            }

            final int modIndex = i;
            FlowLayout card = buildCard(name, badge, bClr, en, modIndex);
            if (row != null) row.child(card);

            col++;
            if (col >= COLS) col = 0;
        }
    }

    private void rebuildGrid() {
        buildGrid();
    }

    private FlowLayout buildCard(String name, String badge, int bClr, boolean en, int modIndex) {
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(136), Sizing.fixed(58));

        // Dark obsidian card colors
        int bg = en ? 0xFF111222 : 0xFF0A0B14;
        int border = en ? 0xFF6D28D9 : 0xFF19182E;

        card.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            // Solid dark card fill
            context.fill(x, y, x + w, y + h, bg);
            // Crisp border around card
            context.drawBorder(x, y, w, h, border);
            // Left purple accent bar when enabled
            if (en) {
                context.fill(x, y, x + 3, y + h, 0xFF8B21F7);
            }
        });
        card.padding(Insets.of(4, 5, 4, 5));
        card.gap(2);

        // Top row: badge + name
        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(4);

        // Badge label
        FlowLayout badgeBox = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(12));
        badgeBox.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, (bClr & 0x00FFFFFF) | 0x33000000);
            context.drawBorder(x, y, w, h, bClr);
        });
        badgeBox.padding(Insets.horizontal(3));
        badgeBox.child(Components.label(Text.literal(badge))
            .color(Color.ofArgb(bClr))
            .sizing(Sizing.content(), Sizing.content()));
        topRow.child(badgeBox);

        // Module name
        topRow.child(Components.label(Text.literal(name))
            .color(Color.ofArgb(0xFFF1F5F9))
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));
        card.child(topRow);

        // ── Horizontal divider line inside card ────────────────────────────────
        FlowLayout cardDivider = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
        cardDivider.surface(Surface.flat(en ? 0x338B21F7 : 0x15201C38));
        card.child(cardDivider);

        // Bottom row: status + toggle
        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);

        String status = en ? "ON" : "OFF";
        int statusClr = en ? 0xFF86EFAC : 0xFF525270;
        bottomRow.child(Components.label(Text.literal(status))
            .color(Color.ofArgb(statusClr))
            .sizing(Sizing.fill(100), Sizing.content()));

        // Toggle switch button
        String toggleLabel = en ? "ON" : "OFF";
        int toggleBg = en ? 0xFF6D28D9 : 0xFF1A182E;
        ButtonComponent toggleBtn = Components.button(Text.literal(toggleLabel), btn -> {
            handleModClick(modIndex);
            rebuildGrid();
        });
        toggleBtn.sizing(Sizing.fixed(28), Sizing.fixed(14));
        toggleBtn.renderer(ButtonComponent.Renderer.flat(toggleBg, en ? 0xFF7E39E8 : 0xFF282544, toggleBg));
        bottomRow.child(toggleBtn);

        card.child(bottomRow);

        // Click anywhere on card to toggle
        card.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                handleModClick(modIndex);
                rebuildGrid();
                return true;
            }
            return false;
        });

        return card;
    }

    // ── Enable checks ─────────────────────────────────────────────────────────
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
        }
        ModConfig.saveConfig();
    }

    private static final net.minecraft.util.Identifier BG_TEX = net.minecraft.util.Identifier.of("fpsdisplay", "textures/gui/background.png");

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.world == null) {
            // Render custom 360° rotating panorama background + dark vignette overlay when in main menu
            this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
            context.fill(0, 0, this.width, this.height, 0x88060A12);
        } else {
            // Opened in-game: render world dimmed behind the panel
            context.fill(0, 0, this.width, this.height, 0xAA000000);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
