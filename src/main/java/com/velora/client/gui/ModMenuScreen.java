package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import com.velora.client.waypoints.Waypoint;
import com.velora.client.waypoints.WaypointManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
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
import java.util.stream.Collectors;

public class ModMenuScreen extends BaseOwoScreen<FlowLayout> {

    // ── Layout constants ──────────────────────────────────────────
    private static final int PANEL_W    = 560;
    private static final int PANEL_H    = 340;
    private static final int HDR_H      = 32;
    private static final int SIDEBAR_W  = 40;  // narrower — single-icon sidebar
    private static final int CONTENT_W  = PANEL_W - SIDEBAR_W;
    private static final int COLS       = 3;
    private static final int TILE_SIZE  = 72;
    private static final int TILE_GAP   = 5;

    // ── Nav / category data ───────────────────────────────────────
    private static final String[] TOP_TABS = {"MODS", "SETTINGS", "WAYPOINTS"};
    private static final String[] CATS     = {"All", "HUD", "Movement", "Visual"};
    private static final String[] WAYPOINT_DIM_TABS = {"ALL", "OVERWORLD", "NETHER", "END"};

    // ── Module definitions: {name, category, icon, accentColor, desc} ──
    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "FPS",  0xFF818CF8, "Frames per second overlay"},
        {"WASD Keys",     "HUD",      "KEYS", 0xFF818CF8, "Movement key presses and CPS"},
        {"Ping Display",  "HUD",      "MS",   0xFF34D399, "Real-time server latency"},
        {"CPS Counter",   "HUD",      "CPS",  0xFFF472B6, "Clicks per second (L/R)"},
        {"Armor Status",  "HUD",      "ARM",  0xFF34D399, "Armor durability on screen"},
        {"Coordinates",   "HUD",      "XYZ",  0xFF38BDF8, "Player position and biome"},
        {"Day Counter",   "HUD",      "DAY",  0xFFFB923C, "World day and time"},
        {"Block Info",    "HUD",      "BLK",  0xFF67E8F9, "Targeted block info"},
        {"Toggle Sprint", "Movement", "SPR",  0xFF60A5FA, "Auto-sprint toggle"},
        {"Toggle Sneak",  "Movement", "SNK",  0xFF818CF8, "Auto-sneak toggle"},
        {"Zoom Mod",      "Movement", "ZM",   0xFFC084FC, "Smooth camera zoom (C)"},
        {"Free Look",     "Movement", "FL",   0xFF34D399, "Freelook camera (V)"},
        {"Snap Look",     "Movement", "SL",   0xFF2DD4BF, "Quick look back (B)"},
        {"Fullbright",    "Visual",   "BRT",  0xFFFBBF24, "Max world brightness (F6)"},
        {"No Hurt Cam",   "Visual",   "CAM",  0xFFF87171, "Disable damage shake"},
        {"Minimap",       "Visual",   "MAP",  0xFF818CF8, "Mini radar map overlay"},
        {"Waypoints",     "Visual",   "WAY",  0xFF38BDF8, "In-world markers"},
        {"Chat Colors",   "HUD",      "CHAT", 0xFF34D399, "Rank and chat highlights"},
        {"Item Tooltips", "HUD",      "TIPS", 0xFFC084FC, "Item stats tooltips"},
        {"Hit Color",     "Visual",   "HIT",  0xFFF87171, "Custom damage flash"},
        {"Item Physics",  "Visual",   "PHY",  0xFF38BDF8, "3D dropped item physics"},
        {"Potion Status", "HUD",      "POT",  0xFFC084FC, "Potion effects and timers"},
        {"Crosshair",     "Visual",   "+",    0xFF34D399, "Custom crosshair studio"},
        {"Item Model",    "Visual",   "MDL",  0xFF38BDF8, "3D held item transforms"},
        {"Nametag",       "Visual",   "TAG",  0xFF818CF8, "Custom player nametag (P)"},
    };

    // ── State ─────────────────────────────────────────────────────
    private int     activeTopTab          = 0;
    private int     selCat                = 0;
    private String  searchQuery           = "";
    private int     waypointDimFilter     = 0;
    private boolean waypointShowAllWorlds = false;

    private FlowLayout moduleGridContainer;
    private FlowLayout waypointGridContainer;
    private final List<ButtonComponent> catPills = new ArrayList<>();

    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
        new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"))
    );

    public ModMenuScreen()               { this(0); }
    public ModMenuScreen(int initialTab) {
        super(Text.literal("Velora Client"));
        this.activeTopTab = initialTab;
    }

    @Override protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(PANEL_W), Sizing.fixed(PANEL_H));
        panel.horizontalAlignment(HorizontalAlignment.LEFT);
        panel.verticalAlignment(VerticalAlignment.TOP);
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            // Frosted glass base — slightly transparent dark navy
            ctx.fill(x, y, x + w, y + h, 0xE2101220);
            // Subtle inner gradient: lighter at top
            ctx.fillGradient(x, y, x + w, y + 28, 0x18FFFFFF, 0x00000000);
            // Violet-tinted border instead of plain white
            ctx.drawBorder(x, y, w, h, 0x55818CF8);
            // 2px violet accent top line
            ctx.fill(x, y, x + w, y + 2, VeloraColors.VIOLET_D);
        });
        panel.padding(Insets.none());
        panel.child(buildHeader(root));
        panel.child(buildBody(root));
        root.child(panel);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Header
    // ═══════════════════════════════════════════════════════════════
    private FlowLayout buildHeader(FlowLayout root) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(HDR_H));
        header.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            // Semi-transparent header — glass layer
            ctx.fill(x, y, x + w, y + h, 0xCC0E0F1C);
            // Subtle shimmer at the very top of header
            ctx.fill(x, y + 2, x + w, y + 3, 0x14FFFFFF);
            // Divider
            ctx.fill(x, y + h - 1, x + w, y + h, 0x33818CF8);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.horizontalAlignment(HorizontalAlignment.LEFT);
        header.padding(Insets.of(0, 10, 0, 10));
        header.gap(8);

        header.child(Components.label(Text.literal("VELORA"))
            .color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true));

        FlowLayout topNav = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(22));
        topNav.verticalAlignment(VerticalAlignment.CENTER);
        topNav.gap(2);
        for (int i = 0; i < TOP_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent tabBtn = Components.button(Text.literal(TOP_TABS[tabIdx]), btn -> {
                activeTopTab = tabIdx;
                root.clearChildren();
                build(root);
            });
            tabBtn.sizing(Sizing.content(), Sizing.fixed(20));
            styleHeaderTab(tabBtn, i == activeTopTab);
            topNav.child(tabBtn);
        }
        header.child(topNav);
        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        if (activeTopTab == 0) {
            FlowLayout searchBox = Containers.horizontalFlow(Sizing.fixed(88), Sizing.fixed(18));
            searchBox.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, 0x220E0F1C);
                ctx.drawBorder(x, y, w, h, 0x33818CF8);
            });
            searchBox.padding(Insets.of(0, 4, 0, 4));
            searchBox.verticalAlignment(VerticalAlignment.CENTER);
            searchBox.gap(3);
            searchBox.child(Components.label(Text.literal("⌕")).color(Color.ofArgb(VeloraColors.TEXT_DIM)));
            TextBoxComponent searchInput = Components.textBox(Sizing.fill(100), searchQuery);
            searchInput.sizing(Sizing.fill(100), Sizing.fixed(12));
            searchInput.onChanged().subscribe(val -> { searchQuery = val.toLowerCase().trim(); rebuildGrid(); });
            searchBox.child(searchInput);
            header.child(searchBox);
        }

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF2, VeloraColors.RED, VeloraColors.SURF2));
        header.child(closeBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Body
    // ═══════════════════════════════════════════════════════════════
    private FlowLayout buildBody(FlowLayout root) {
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H));
        body.horizontalAlignment(HorizontalAlignment.LEFT);
        body.verticalAlignment(VerticalAlignment.TOP);
        body.child(buildSidebar(root));

        FlowLayout contentWrapper = Containers.verticalFlow(Sizing.fixed(CONTENT_W), Sizing.fill(100));
        contentWrapper.surface(Surface.flat(0xC010111A));
        contentWrapper.padding(Insets.of(8, 8, 8, 8));
        contentWrapper.horizontalAlignment(HorizontalAlignment.LEFT);
        contentWrapper.verticalAlignment(VerticalAlignment.TOP);

        if      (activeTopTab == 0) contentWrapper.child(buildModsView());
        else if (activeTopTab == 1) contentWrapper.child(buildSettingsView());
        else if (activeTopTab == 2) contentWrapper.child(buildWaypointsView(root));

        body.child(contentWrapper);
        return body;
    }

    // ─────────────────────────────────────────────────────────────
    //  Sidebar
    // ─────────────────────────────────────────────────────────────
    private FlowLayout buildSidebar(FlowLayout root) {
        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(SIDEBAR_W), Sizing.fill(100));
        sidebar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            // Slightly darker glass for sidebar
            ctx.fill(x, y, x + w, y + h, 0xD20B0C18);
            // Violet-tinted right divider
            ctx.fill(x + w - 1, y, x + w, y + h, 0x33818CF8);
        });
        sidebar.padding(Insets.of(8, 0, 8, 0));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.verticalAlignment(VerticalAlignment.TOP);
        sidebar.gap(4);

        sidebar.child(makeSidebarNav("MODS", "Modules",    activeTopTab == 0, () -> { activeTopTab = 0; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarNav("WAY",  "Waypoints",  activeTopTab == 2, () -> { activeTopTab = 2; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarNav("SKIN", "Cosmetics",  false, () -> { if (client != null) client.setScreen(new CosmeticsLockerScreen()); }));
        sidebar.child(makeSidebarNav("HUD",  "HUD Edit",   false, () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
        sidebar.child(makeSidebarNav("CFG",  "Settings",   activeTopTab == 1, () -> { activeTopTab = 1; root.clearChildren(); build(root); }));

        sidebar.child(Containers.verticalFlow(Sizing.fixed(1), Sizing.expand(1)));

        int enabled = getEnabledCount();
        FlowLayout badge = Containers.verticalFlow(Sizing.fixed(SIDEBAR_W), Sizing.content());
        badge.horizontalAlignment(HorizontalAlignment.CENTER);
        badge.gap(1);
        badge.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x + 8, y, x + w - 8, y + h + 2, VeloraColors.SURF3);
        });
        badge.padding(Insets.of(4, 0, 4, 0));
        badge.child(Components.label(Text.literal(String.valueOf(enabled))).color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true));
        badge.child(Components.label(Text.literal("on")).color(Color.ofArgb(VeloraColors.TEXT_DIM)));
        sidebar.child(badge);
        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────
    //  Mods view
    // ─────────────────────────────────────────────────────────────
    private FlowLayout buildModsView() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.verticalAlignment(VerticalAlignment.TOP);
        container.gap(6);

        FlowLayout catBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        catBar.verticalAlignment(VerticalAlignment.CENTER);
        catBar.horizontalAlignment(HorizontalAlignment.LEFT);
        catBar.gap(4);
        catPills.clear();
        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(CATS[idx]), btn -> { selCat = idx; rebuildGrid(); refreshPills(); });
            pill.sizing(Sizing.content(), Sizing.fixed(18));
            stylePill(pill, i == selCat);
            catPills.add(pill);
            catBar.child(pill);
        }
        container.child(catBar);

        moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        moduleGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        moduleGridContainer.verticalAlignment(VerticalAlignment.TOP);
        moduleGridContainer.padding(Insets.of(0, 0, 20, 0));
        moduleGridContainer.gap(TILE_GAP);
        rebuildGrid();

        int scrollH = PANEL_H - HDR_H - 22 - 6 - 12;
        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(scrollH), moduleGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        container.child(scroll);
        return container;
    }

    // ─────────────────────────────────────────────────────────────
    //  Settings view
    // ─────────────────────────────────────────────────────────────
    private FlowLayout buildSettingsView() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.padding(Insets.of(2, 4, 2, 4));
        container.gap(4);

        FlowLayout list = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        list.horizontalAlignment(HorizontalAlignment.LEFT);
        list.padding(Insets.of(0, 0, 30, 0));
        list.gap(3);

        list.child(makeSectionHeader("Performance"));
        list.child(makeSettingToggle("Fast Math",       "Optimised trig calculations for better frame rates",   ModConfig.optiFastMath,       () -> { ModConfig.optiFastMath       = !ModConfig.optiFastMath;       ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Entity Culling",  "Skip rendering hidden entities behind walls",          ModConfig.optiEntityCulling,  () -> { ModConfig.optiEntityCulling  = !ModConfig.optiEntityCulling;  ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Low Memory Mode", "Reduce allocation and garbage-collection overhead",    ModConfig.optiLowMemoryMode,  () -> { ModConfig.optiLowMemoryMode  = !ModConfig.optiLowMemoryMode;  ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Disable Fog",     "Remove terrain distance fog for clear sightlines",    ModConfig.optiDisableFog,     () -> { ModConfig.optiDisableFog     = !ModConfig.optiDisableFog;     ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Limit Particles", "Cap maximum simultaneous particles on screen",        ModConfig.optiLimitParticles, () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); }));

        list.child(makeSectionHeader("Visual"));
        list.child(makeSettingToggle("Fullbright (F6)", "Maximum gamma and illumination everywhere",           ModConfig.showFullbright,     () -> { ModConfig.showFullbright     = !ModConfig.showFullbright;     ModConfig.saveConfig(); }));

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 20), list);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        container.child(scroll);
        return container;
    }

    // ─────────────────────────────────────────────────────────────
    //  Waypoints view
    // ─────────────────────────────────────────────────────────────
    private FlowLayout buildWaypointsView(FlowLayout root) {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.gap(4);

        FlowLayout subHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        subHeader.verticalAlignment(VerticalAlignment.CENTER);
        subHeader.horizontalAlignment(HorizontalAlignment.LEFT);
        subHeader.gap(4);

        FlowLayout dimTabs = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        dimTabs.verticalAlignment(VerticalAlignment.CENTER);
        dimTabs.gap(3);
        for (int i = 0; i < WAYPOINT_DIM_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent dimBtn = Components.button(Text.literal(WAYPOINT_DIM_TABS[tabIdx]), btn -> {
                waypointDimFilter = tabIdx;
                rebuildWaypointGrid(root);
            });
            dimBtn.sizing(Sizing.content(), Sizing.fixed(18));
            if (tabIdx == waypointDimFilter) {
                dimBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.VIOLET_D, VeloraColors.VIOLET, VeloraColors.VIOLET_D));
            } else {
                dimBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.SURF4, VeloraColors.SURF3));
            }
            dimTabs.child(dimBtn);
        }
        subHeader.child(dimTabs);
        subHeader.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        ButtonComponent worldToggleBtn = Components.button(
            Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "THIS WORLD"),
            btn -> {
                waypointShowAllWorlds = !waypointShowAllWorlds;
                btn.setMessage(Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "THIS WORLD"));
                rebuildWaypointGrid(root);
            }
        );
        worldToggleBtn.sizing(Sizing.content(), Sizing.fixed(18));
        worldToggleBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.BORDER_S, VeloraColors.SURF3));
        subHeader.child(worldToggleBtn);

        ButtonComponent addBtn = Components.button(Text.literal("+ Add"), btn -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, null));
        });
        addBtn.sizing(Sizing.content(), Sizing.fixed(18));
        addBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.VIOLET_D, VeloraColors.VIOLET, VeloraColors.VIOLET_D));
        subHeader.child(addBtn);
        container.child(subHeader);

        waypointGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        waypointGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        waypointGridContainer.padding(Insets.of(0, 0, 30, 0));
        waypointGridContainer.gap(4);
        rebuildWaypointGrid(root);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 34), waypointGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        container.child(scroll);
        return container;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Grid rebuild — 3-column square tiles
    // ═══════════════════════════════════════════════════════════════
    private void rebuildGrid() {
        if (moduleGridContainer == null) return;
        moduleGridContainer.clearChildren();

        List<Integer> visible = new ArrayList<>();
        String targetCat = CATS[selCat];
        for (int i = 0; i < MODS.length; i++) {
            String name = (String) MODS[i][0];
            String cat  = (String) MODS[i][1];
            String desc = (String) MODS[i][4];
            if (!"All".equals(targetCat) && !cat.equals(targetCat)) continue;
            if (!searchQuery.isEmpty()) {
                if (!name.toLowerCase().contains(searchQuery) &&
                    !desc.toLowerCase().contains(searchQuery) &&
                    !cat.toLowerCase().contains(searchQuery)) continue;
            }
            visible.add(i);
        }

        int availW = CONTENT_W - 16 - 2;
        int tileW  = (availW - (COLS - 1) * TILE_GAP) / COLS;

        FlowLayout currentRow = null;
        for (int k = 0; k < visible.size(); k++) {
            if (k % COLS == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.horizontalAlignment(HorizontalAlignment.LEFT);
                currentRow.gap(TILE_GAP);
                moduleGridContainer.child(currentRow);
            }
            if (currentRow != null) currentRow.child(buildModTile(visible.get(k), tileW));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Square module tile
    // ─────────────────────────────────────────────────────────────
    private FlowLayout buildModTile(int modIndex, int tileW) {
        Object[] def  = MODS[modIndex];
        String name   = (String)  def[0];
        String icon   = (String)  def[2];
        int    accent = (Integer) def[3];
        final boolean[] en = { isEnabled(modIndex) };

        FlowLayout tile = Containers.verticalFlow(Sizing.fixed(tileW), Sizing.fixed(TILE_SIZE));
        tile.verticalAlignment(VerticalAlignment.TOP);
        tile.horizontalAlignment(HorizontalAlignment.LEFT);
        tile.padding(Insets.of(6, 6, 6, 6));
        tile.gap(0);

        tile.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (en[0]) {
                // Enabled: translucent accent-tinted card
                ctx.fill(x, y, x + w, y + h, 0xBB1A1C2E);
                // 3px accent top stripe
                ctx.fill(x, y, x + w, y + 3, accent);
                // faint accent tint overlay
                ctx.fill(x, y + 3, x + w, y + h, accent & 0x0CFFFFFF);
                // subtle bottom shimmer
                ctx.fill(x, y + h - 1, x + w, y + h, accent & 0x22FFFFFF);
            } else {
                // Disabled: darker transparent card
                ctx.fill(x, y, x + w, y + h, 0xA00B0C18);
                ctx.fill(x, y, x + w, y + 1, 0x1AFFFFFF);
                ctx.fill(x, y, x + 1, y + h, 0x0AFFFFFF);
            }
        });

        // Top row: icon badge + toggle
        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(4);

        FlowLayout iconBadge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(16));
        iconBadge.verticalAlignment(VerticalAlignment.CENTER);
        iconBadge.padding(Insets.of(0, 4, 0, 4));
        iconBadge.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, accent & 0x2AFFFFFF);
        });
        iconBadge.child(Components.label(Text.literal(icon)).color(Color.ofArgb(accent)).shadow(true));
        topRow.child(iconBadge);
        topRow.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(24), Sizing.fixed(12));
        toggle.surface((ctx, comp) -> drawTileToggle(ctx, comp.x(), comp.y(), comp.width(), comp.height(), en[0], accent));
        topRow.child(toggle);
        tile.child(topRow);

        // Name
        FlowLayout nameRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.padding(Insets.of(4, 0, 0, 0));
        LabelComponent nameLbl = Components.label(Text.literal(name));
        nameLbl.color(Color.ofArgb(en[0] ? VeloraColors.TEXT : VeloraColors.TEXT_M));
        nameLbl.shadow(en[0]);
        nameRow.child(nameLbl);
        tile.child(nameRow);

        // Status dot
        FlowLayout statusRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        statusRow.verticalAlignment(VerticalAlignment.CENTER);
        statusRow.padding(Insets.of(3, 0, 0, 0));
        statusRow.gap(3);
        FlowLayout dot = Containers.horizontalFlow(Sizing.fixed(5), Sizing.fixed(5));
        dot.surface((ctx, comp) -> ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), en[0] ? accent : VeloraColors.TEXT_DIM));
        statusRow.child(dot);
        LabelComponent statusLbl = Components.label(Text.literal(en[0] ? "On" : "Off"));
        statusLbl.color(Color.ofArgb(en[0] ? accent : VeloraColors.TEXT_DIM));
        statusRow.child(statusLbl);
        tile.child(statusRow);

        tile.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                en[0] = !en[0];
                toggleMod(modIndex);
                nameLbl.color(Color.ofArgb(en[0] ? VeloraColors.TEXT : VeloraColors.TEXT_M));
                nameLbl.shadow(en[0]);
                statusLbl.text(Text.literal(en[0] ? "On" : "Off"));
                statusLbl.color(Color.ofArgb(en[0] ? accent : VeloraColors.TEXT_DIM));
                return true;
            } else if (btn == 1) {
                openModSettings(name);
                return true;
            }
            return false;
        });
        return tile;
    }

    private void drawTileToggle(DrawContext ctx, int x, int y, int w, int h, boolean on, int accent) {
        ctx.fill(x, y, x + w, y + h, VeloraColors.SURF4);
        if (on) ctx.fill(x, y, x + w, y + h, (accent & 0x55FFFFFF) | 0x55000000);
        ctx.drawBorder(x, y, w, h, on ? (accent | 0xFF000000) : VeloraColors.BORDER_S);
        int kw = h - 4;
        int kx = on ? (x + w - kw - 2) : (x + 2);
        ctx.fill(kx, y + 2, kx + kw, y + h - 2, on ? 0xFFFFFFFF : VeloraColors.TEXT_DIM);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Waypoint helpers
    // ═══════════════════════════════════════════════════════════════
    private void rebuildWaypointGrid(FlowLayout root) {
        if (waypointGridContainer == null) return;
        waypointGridContainer.clearChildren();

        List<Waypoint> list = waypointShowAllWorlds
            ? WaypointManager.getAllWaypoints()
            : WaypointManager.getWaypointsForCurrentWorld();

        if (waypointDimFilter == 1) list = list.stream().filter(w -> "minecraft:overworld".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        else if (waypointDimFilter == 2) list = list.stream().filter(w -> "minecraft:the_nether".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        else if (waypointDimFilter == 3) list = list.stream().filter(w -> "minecraft:the_end".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());

        if (list.isEmpty()) {
            FlowLayout empty = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(80));
            empty.horizontalAlignment(HorizontalAlignment.CENTER);
            empty.verticalAlignment(VerticalAlignment.CENTER);
            empty.surface((ctx, comp) -> ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF));
            empty.gap(3);
            empty.child(Components.label(Text.literal("No waypoints found")).color(Color.ofArgb(VeloraColors.TEXT_M)));
            empty.child(Components.label(Text.literal("Click '+ Add' to save coordinates.")).color(Color.ofArgb(VeloraColors.TEXT_DIM)));
            waypointGridContainer.child(empty);
            return;
        }

        FlowLayout currentRow = null;
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.horizontalAlignment(HorizontalAlignment.LEFT);
                currentRow.gap(4);
                waypointGridContainer.child(currentRow);
            }
            if (currentRow != null) currentRow.child(buildWaypointCard(list.get(i), root));
        }
    }

    private FlowLayout buildWaypointCard(Waypoint wp, FlowLayout root) {
        FlowLayout card = Containers.horizontalFlow(Sizing.fixed(240), Sizing.fixed(36));
        card.verticalAlignment(VerticalAlignment.CENTER);
        card.horizontalAlignment(HorizontalAlignment.LEFT);
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, wp.enabled ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG);
            if (wp.enabled) ctx.fill(x, y, x + 2, y + h, wp.color | 0xFF000000);
        });
        card.padding(Insets.of(0, 6, 0, 6));
        card.gap(4);

        FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(8), Sizing.fixed(8));
        swatch.surface((ctx, comp) -> ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), wp.color | 0xFF000000));
        card.child(swatch);

        FlowLayout info = Containers.verticalFlow(Sizing.fixed(110), Sizing.content());
        info.gap(1);
        LabelComponent nameLbl = Components.label(Text.literal(wp.name));
        nameLbl.color(Color.ofArgb(wp.enabled ? VeloraColors.TEXT : VeloraColors.TEXT_M));
        nameLbl.shadow(true);
        info.child(nameLbl);
        info.child(Components.label(Text.literal(String.format("%.0f, %.0f, %.0f", wp.x, wp.y, wp.z)))
            .color(Color.ofArgb(VeloraColors.TEXT_DIM)));
        card.child(info);
        card.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        ButtonComponent onOffBtn = Components.button(Text.literal(wp.enabled ? "ON" : "OFF"), b -> {
            WaypointManager.toggleWaypoint(wp.id);
            b.setMessage(Text.literal(wp.enabled ? "ON" : "OFF"));
            b.renderer(ButtonComponent.Renderer.flat(
                wp.enabled ? VeloraColors.VIOLET_D : VeloraColors.SURF3,
                wp.enabled ? VeloraColors.VIOLET   : VeloraColors.TEXT_M,
                wp.enabled ? VeloraColors.VIOLET_D : VeloraColors.SURF3));
            nameLbl.color(Color.ofArgb(wp.enabled ? VeloraColors.TEXT : VeloraColors.TEXT_M));
        });
        onOffBtn.sizing(Sizing.fixed(28), Sizing.fixed(16));
        onOffBtn.renderer(ButtonComponent.Renderer.flat(
            wp.enabled ? VeloraColors.VIOLET_D : VeloraColors.SURF3,
            wp.enabled ? VeloraColors.VIOLET   : VeloraColors.TEXT_M,
            wp.enabled ? VeloraColors.VIOLET_D : VeloraColors.SURF3));
        card.child(onOffBtn);

        ButtonComponent editBtn = Components.button(Text.literal("Edit"), b -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, wp));
        });
        editBtn.sizing(Sizing.fixed(28), Sizing.fixed(16));
        editBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.VIOLET_S, VeloraColors.SURF3));
        card.child(editBtn);

        ButtonComponent delBtn = Components.button(Text.literal("✕"), b -> {
            WaypointManager.removeWaypoint(wp.id);
            rebuildWaypointGrid(root);
        });
        delBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        delBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED, VeloraColors.SURF3));
        card.child(delBtn);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Shared helpers
    // ═══════════════════════════════════════════════════════════════
    private FlowLayout makeSidebarNav(String icon, String label, boolean active, Runnable action) {
        FlowLayout btn = Containers.verticalFlow(Sizing.fixed(SIDEBAR_W), Sizing.fixed(38));
        btn.verticalAlignment(VerticalAlignment.CENTER);
        btn.horizontalAlignment(HorizontalAlignment.CENTER);
        btn.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (active) {
                ctx.fill(x, y, x + w, y + h, 0x1A818CF8);
                // Left violet bar indicator
                ctx.fill(x, y + 5, x + 2, y + h - 5, VeloraColors.VIOLET);
            }
        });
        // Single icon label only — no cluttered second line
        btn.child(Components.label(Text.literal(icon))
            .color(Color.ofArgb(active ? VeloraColors.VIOLET : VeloraColors.TEXT_DIM))
            .shadow(active));
        btn.mouseDown().subscribe((mx, my, b) -> { if (b == 0) { action.run(); return true; } return false; });
        return btn;
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(4, 2, 2, 2));
        header.gap(6);
        header.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(VeloraColors.VIOLET)));
        FlowLayout line = Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1));
        line.surface(Surface.flat(VeloraColors.DIVIDER));
        header.child(line);
        return header;
    }

    private FlowLayout makeSettingToggle(String label, String desc, boolean enabled, Runnable action) {
        final boolean[] state = { enabled };
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(34));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, state[0] ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG);
            if (state[0]) ctx.fill(x, y, x + 2, y + h, VeloraColors.VIOLET);
        });
        row.padding(Insets.of(0, 8, 0, 8));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(6);

        FlowLayout textCol = Containers.verticalFlow(Sizing.content(), Sizing.content());
        textCol.gap(1);
        textCol.child(Components.label(Text.literal(label)).color(Color.ofArgb(VeloraColors.TEXT)));
        textCol.child(Components.label(Text.literal(desc)).color(Color.ofArgb(VeloraColors.TEXT_DIM)));
        row.child(textCol);
        row.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(14));
        toggle.surface((ctx, comp) -> VeloraRenderUtil.drawToggleSwitch(ctx, comp.x(), comp.y(), comp.width(), comp.height(), state[0]));
        row.child(toggle);
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { state[0] = !state[0]; action.run(); return true; } return false; });
        return row;
    }

    private void styleHeaderTab(ButtonComponent btn, boolean active) {
        if (active) {
            btn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF, VeloraColors.VIOLET, VeloraColors.SURF));
        } else {
            btn.renderer(ButtonComponent.Renderer.flat(0x00000000, VeloraColors.TEXT_M, 0x00000000));
        }
    }

    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) {
            pill.renderer(ButtonComponent.Renderer.flat(VeloraColors.VIOLET_D, VeloraColors.VIOLET_S, VeloraColors.VIOLET_D));
        } else {
            pill.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.SURF4, VeloraColors.SURF3));
        }
    }

    private void refreshPills() {
        for (int i = 0; i < catPills.size(); i++) stylePill(catPills.get(i), i == selCat);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Mod routing / state
    // ═══════════════════════════════════════════════════════════════
    private void openModSettings(String modName) {
        if (client == null) return;
        if ("Crosshair".equalsIgnoreCase(modName) || "Custom Crosshair".equalsIgnoreCase(modName)) {
            client.setScreen(new CrosshairEditorScreen(this));
        } else if ("Item Model".equalsIgnoreCase(modName) || "View Model".equalsIgnoreCase(modName)) {
            client.setScreen(new ItemModelSettingsScreen(this));
        } else {
            client.setScreen(new ModuleSettingsScreen(modName, this));
        }
    }

    private boolean isEnabled(int index) {
        return switch (index) {
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
            case 16 -> ModConfig.showWaypoints;
            case 17 -> ModConfig.showChatColors;
            case 18 -> ModConfig.showItemTooltips;
            case 19 -> ModConfig.showHitColor;
            case 20 -> ModConfig.showItemPhysics;
            case 21 -> ModConfig.showPotionHud;
            case 22 -> ModConfig.enableCustomCrosshair;
            case 23 -> ModConfig.showViewModel;
            case 24 -> ModConfig.showNametag;
            default -> false;
        };
    }

    private void toggleMod(int index) {
        switch (index) {
            case 0  -> ModConfig.showFps             = !ModConfig.showFps;
            case 1  -> ModConfig.showKeystrokes      = !ModConfig.showKeystrokes;
            case 2  -> ModConfig.showPing            = !ModConfig.showPing;
            case 3  -> ModConfig.showCps             = !ModConfig.showCps;
            case 4  -> ModConfig.showArmorStatus     = !ModConfig.showArmorStatus;
            case 5  -> ModConfig.showCoordinates     = !ModConfig.showCoordinates;
            case 6  -> ModConfig.showDayCounter      = !ModConfig.showDayCounter;
            case 7  -> ModConfig.showBlockInfo       = !ModConfig.showBlockInfo;
            case 8  -> ModConfig.showToggleSprint    = !ModConfig.showToggleSprint;
            case 9  -> ModConfig.showToggleSneak     = !ModConfig.showToggleSneak;
            case 10 -> ModConfig.showZoom            = !ModConfig.showZoom;
            case 11 -> ModConfig.showFreeLook        = !ModConfig.showFreeLook;
            case 12 -> ModConfig.showSnapLook        = !ModConfig.showSnapLook;
            case 13 -> ModConfig.showFullbright      = !ModConfig.showFullbright;
            case 14 -> ModConfig.showNoHurtCam       = !ModConfig.showNoHurtCam;
            case 15 -> ModConfig.showMinimap         = !ModConfig.showMinimap;
            case 16 -> ModConfig.showWaypoints       = !ModConfig.showWaypoints;
            case 17 -> ModConfig.showChatColors      = !ModConfig.showChatColors;
            case 18 -> ModConfig.showItemTooltips    = !ModConfig.showItemTooltips;
            case 19 -> ModConfig.showHitColor        = !ModConfig.showHitColor;
            case 20 -> ModConfig.showItemPhysics     = !ModConfig.showItemPhysics;
            case 21 -> ModConfig.showPotionHud       = !ModConfig.showPotionHud;
            case 22 -> ModConfig.enableCustomCrosshair = !ModConfig.enableCustomCrosshair;
            case 23 -> ModConfig.showViewModel       = !ModConfig.showViewModel;
            case 24 -> ModConfig.showNametag         = !ModConfig.showNametag;
        }
        ModConfig.saveConfig();
    }

    private int getEnabledCount() {
        int c = 0;
        for (int i = 0; i < MODS.length; i++) if (isEnabled(i)) c++;
        return c;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world == null) {
            this.panoramaRenderer.render(context, this.width, this.height, 1.0F, delta);
            // Deep darken for main menu
            context.fillGradient(0, 0, this.width, this.height, 0xD8090A0F, 0xF2090A0F);
        } else {
            // In-game: stronger blur + dark vignette so glass panel reads well
            context.fillGradient(0, 0, this.width, this.height, 0x99000000, 0xBB000000);
            // Subtle radial-ish violet edge tint (top-bottom gradient)
            context.fillGradient(0, 0, this.width, this.height / 3, 0x22150C2E, 0x00000000);
            context.fillGradient(0, this.height * 2 / 3, this.width, this.height, 0x00000000, 0x1A150C2E);
        }
    }
}
