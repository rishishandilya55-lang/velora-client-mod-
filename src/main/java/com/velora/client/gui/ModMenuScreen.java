package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Velora Client — Modern Feather/Lunar-Style Mod Menu Screen (Fabric 1.21.4 / owo-lib).
 *
 * Design Refactor Specifications:
 * 1. Palette: Soft charcoal/slate base (#111318), muted slate card backgrounds (#161920 / #1E2430),
 *    and thin muted gray borders (#2D313E). Harsh neon purple lines completely eliminated.
 * 2. Card Typography & Padding: Explicit internal padding (Insets.of(10, 12, 10, 12)) inside every module card.
 *    Filled desaturated rounded badge tags, bold white title text (#FFFFFF), and live status dots (● ON / ● OFF).
 * 3. Top & Left Navigation: Horizontal top filter tabs with rounded active pill highlights (0xFF3B82F6).
 *    Dedicated left sidebar wrapper with unified vertical spacing (gap(16)) and clear active focus indicators.
 */
public class ModMenuScreen extends BaseOwoScreen<FlowLayout> {

    // Main Layout Dimensions
    private static final int PANEL_W = 580;
    private static final int PANEL_H = 340;
    private static final int SIDE_W  = 75;
    private static final int HDR_H   = 36;

    // Categories
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private int selCat = 0;

    // Module Definitions: {name, category, badge, accentColor}
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

    // Components & Containers
    private FlowLayout moduleGridContainer;
    private final List<ButtonComponent> catPills = new ArrayList<>();
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
        new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"))
    );

    public ModMenuScreen() {
        super(Text.literal("Velora Client — Mods"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Outer Panel Canvas (#111318 Charcoal Slate, Soft Border #2D313E) ───
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(PANEL_W), Sizing.fixed(PANEL_H));
        panel.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            // Dark charcoal base (#111318)
            context.fill(x, y, x + w, y + h, 0xFF111318);
            // Thin soft gray border outline (#2D313E) replacing harsh neon lines
            context.drawBorder(x, y, w, h, 0xFF2D313E);
            context.drawBorder(x + 1, y + 1, w - 2, h - 2, 0xFF161922);
        });
        panel.padding(Insets.none());

        // ── 1. Top Header Navigation Bar (Clean Brand + Distinct Filter Tabs) ──
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(HDR_H));
        header.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF151821);
            context.fill(x, y + h - 1, x + w, y + h, 0xFF2D313E); // Header divider line
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 12, 0, 12));
        header.gap(10);

        // Brand Logo + Title Group
        FlowLayout brandBox = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        brandBox.verticalAlignment(VerticalAlignment.CENTER);
        brandBox.gap(6);
        brandBox.child(Components.label(Text.literal("VELORA"))
            .color(Color.ofArgb(0xFF3B82F6))
            .shadow(true)
            .sizing(Sizing.content(), Sizing.content()));
        brandBox.child(Components.label(Text.literal("Mod Menu"))
            .color(Color.ofArgb(0xFFFFFFFF))
            .shadow(true)
            .sizing(Sizing.content(), Sizing.content()));
        header.child(brandBox);

        // Header Spacer pushing filter tabs to right
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        // Top Filter Tabs (Distinct Horizontal Margins & Active Rounded Pill Highlights)
        FlowLayout tabsRow = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(22));
        tabsRow.verticalAlignment(VerticalAlignment.CENTER);
        tabsRow.gap(6);

        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(CATS[idx]), btn -> {
                selCat = idx;
                rebuildGrid();
                refreshPills();
            });
            pill.sizing(Sizing.fixed(48), Sizing.fixed(20));
            stylePill(pill, i == selCat);
            catPills.add(pill);
            tabsRow.child(pill);
        }
        header.child(tabsRow);

        // Close Button (✕)
        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0xFF1C1F2B, 0xFFDC2626, 0xFF1C1F2B));
        header.child(closeBtn);

        panel.child(header);

        // ── 2. Main Content Split (Left Navigation Sidebar + Module Grid) ──────
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        // ── Left Navigation Sidebar (Dedicated Wrapper, Unified Vertical Spacing)
        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(SIDE_W), Sizing.fill(100));
        sidebar.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF151821);
            context.fill(x + w - 1, y, x + w, y + h, 0xFF2D313E); // Sidebar vertical divider line
        });
        sidebar.padding(Insets.of(12, 6, 12, 6));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.gap(16); // Unified vertical spacing(16) between sidebar rows

        // Sidebar Items (MODS, LOOKS, HUD, CFG)
        sidebar.child(makeSidebarItem("MODS", true, () -> {}));
        sidebar.child(makeSidebarItem("LOOKS", false, () -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));
        sidebar.child(makeSidebarItem("HUD", false, () -> {
            if (client != null) client.setScreen(new HudEditorScreen());
        }));

        // Spacer pushing CFG to bottom
        sidebar.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));
        sidebar.child(makeSidebarItem("CFG", false, () -> {}));

        body.child(sidebar);

        // ── 3. Module Grid Container (Scrollable 3-Column Layout) ─────────────
        FlowLayout gridWrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        gridWrapper.surface(Surface.flat(0xFF111318));
        gridWrapper.padding(Insets.of(10));

        moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        moduleGridContainer.gap(8); // Vertical gap between card rows
        buildGrid();

        ScrollContainer<FlowLayout> scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), moduleGridContainer);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xFF2D313E)));
        gridWrapper.child(scrollContainer);

        body.child(gridWrapper);
        panel.child(body);

        root.child(panel);
    }

    // ── Left Sidebar Button Builder ──────────────────────────────────────────
    private ButtonComponent makeSidebarItem(String label, boolean active, Runnable action) {
        ButtonComponent btn = Components.button(Text.literal(label), b -> action.run());
        btn.sizing(Sizing.fixed(SIDE_W - 12), Sizing.fixed(32));

        int bg = active ? 0xFF1E2433 : 0x00000000;
        int hoverBg = active ? 0xFF242C3E : 0xFF1A1E2B;

        btn.renderer((context, button, delta) -> {
            int x = button.x(), y = button.y(), w = button.width(), h = button.height();
            boolean hovered = button.isHovered();
            int currentBg = hovered ? hoverBg : bg;

            if (currentBg != 0) {
                context.fill(x, y, x + w, y + h, currentBg);
            }

            // Left focus indicator bar when active
            if (active) {
                context.fill(x, y, x + 3, y + h, 0xFF3B82F6);
            }

            int textClr = active ? 0xFFFFFFFF : (hovered ? 0xFFE2E8F0 : 0xFF64748B);
            int textX = x + (w - textRenderer.getWidth(label)) / 2;
            int textY = y + (h - 8) / 2;
            context.drawText(textRenderer, label, textX, textY, textClr, true);
        });

        return btn;
    }

    // ── Top Category Tab Styling (Active Pill Highlight) ──────────────────────
    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) {
            pill.renderer(ButtonComponent.Renderer.flat(0xFF3B82F6, 0xFF2563EB, 0xFF3B82F6));
        } else {
            pill.renderer(ButtonComponent.Renderer.flat(0xFF161922, 0xFF202532, 0xFF161922));
        }
    }

    private void refreshPills() {
        for (int i = 0; i < catPills.size(); i++) {
            stylePill(catPills.get(i), i == selCat);
        }
    }

    // ── Module Grid Builder (3 Columns) ──────────────────────────────────────
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

    // ── Overhauled Grid Card Builder (Explicit Insets & Sleek Badges) ────────
    private FlowLayout buildCard(String name, String badge, int bClr, boolean en, int modIndex) {
        // Module card dimensions (154px width x 68px height)
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(154), Sizing.fixed(68));

        // Soft slate card colors (#161920 when disabled, #1E2430 when enabled)
        int bg = en ? 0xFF1E2430 : 0xFF161920;
        int border = en ? 0xFF3B82F6 : 0xFF2D313E;

        card.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            // Solid dark card fill
            context.fill(x, y, x + w, y + h, bg);
            // Thin soft border outline
            context.drawBorder(x, y, w, h, border);
            // Left blue accent bar when enabled
            if (en) {
                context.fill(x, y, x + 3, y + h, 0xFF3B82F6);
            }
        });

        // Explicit Internal Padding: Insets.of(10, 12, 10, 12) for clean breathing room
        card.padding(Insets.of(10, 12, 10, 12));
        card.gap(4);

        // Top Row: Sleek Filled Badge Tag + Bold White Mod Title
        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(6);

        // Sleek Filled Rounded Badge Tag (Desaturated dark backdrop + clean colored text)
        FlowLayout badgeBox = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(14));
        badgeBox.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            // Solid dark desaturated backdrop
            context.fill(x, y, x + w, y + h, 0xFF222838);
            context.drawBorder(x, y, w, h, (bClr & 0x00FFFFFF) | 0x88000000);
        });
        badgeBox.padding(Insets.of(1, 5, 1, 5));
        badgeBox.child(Components.label(Text.literal(badge))
            .color(Color.ofArgb(bClr))
            .sizing(Sizing.content(), Sizing.content()));
        topRow.child(badgeBox);

        // Bold White Mod Title (#FFFFFF)
        topRow.child(Components.label(Text.literal(name))
            .color(Color.ofArgb(0xFFFFFFFF))
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));
        card.child(topRow);

        // Thin Muted Separator Line
        FlowLayout cardDivider = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
        cardDivider.surface(Surface.flat(0x332D313E));
        card.child(cardDivider);

        // Bottom Row: Status Dot (● ON / ● OFF) + Sliding Toggle Switch Widget
        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);

        // Status Indicator Dot + Text Label
        FlowLayout statusBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        statusBox.verticalAlignment(VerticalAlignment.CENTER);
        statusBox.gap(4);

        int dotClr = en ? 0xFF22C55E : 0xFF64748B;
        statusBox.child(Components.label(Text.literal("●"))
            .color(Color.ofArgb(dotClr))
            .sizing(Sizing.content(), Sizing.content()));

        String statusText = en ? "ON" : "OFF";
        statusBox.child(Components.label(Text.literal(statusText))
            .color(Color.ofArgb(en ? 0xFF86EFAC : 0xFF64748B))
            .sizing(Sizing.content(), Sizing.content()));

        bottomRow.child(statusBox);

        // Sliding Toggle Switch Widget
        ButtonComponent toggleBtn = Components.button(Text.literal(en ? "ON" : "OFF"), btn -> {
            handleModClick(modIndex);
            rebuildGrid();
        });
        toggleBtn.sizing(Sizing.fixed(32), Sizing.fixed(16));
        toggleBtn.renderer((context, button, delta) -> {
            int x = button.x(), y = button.y(), w = button.width(), h = button.height();
            int trackClr = en ? 0xFF166534 : 0xFF1F2937;
            int knobClr  = en ? 0xFF22C55E : 0xFF9CA3AF;

            context.fill(x, y, x + w, y + h, trackClr);
            context.drawBorder(x, y, w, h, en ? 0xFF22C55E : 0xFF374151);

            int knobX = en ? x + w - 12 : x + 2;
            context.fill(knobX, y + 2, knobX + 10, y + h - 2, knobClr);
        });
        bottomRow.child(toggleBtn);

        card.child(bottomRow);

        // Click Card Frame to Toggle State
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

    // ── Enable Checks & Click Handlers ───────────────────────────────────────
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.world == null) {
            // Main menu context: render rotating panorama + dark vignette overlay
            this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
            context.fill(0, 0, this.width, this.height, 0x88060A12);
        } else {
            // In-game context: dim background world
            context.fill(0, 0, this.width, this.height, 0xAA000000);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
