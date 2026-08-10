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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModMenuScreen extends BaseOwoScreen<FlowLayout> {

    private static final int PANEL_W = 570;
    private static final int PANEL_H = 330;
    private static final int HDR_H = 32;
    private static final int SIDEBAR_W = 52;
    private static final int CONTENT_W = PANEL_W - SIDEBAR_W; // 518px
    private static final int COLS = 3;

    private static final int BG       = 0xFF08080A;
    private static final int SURF     = 0xFF0F0F12;
    private static final int SURF2    = 0xFF16161A;
    private static final int SURF3    = 0xFF1D1D22;
    private static final int TEXT     = 0xFFF4F4F5;
    private static final int TEXT_M   = 0xFFA1A1AA;
    private static final int TEXT_F   = 0xFF71717A;
    private static final int BORDER   = 0x14FFFFFF;
    private static final int BORDER_S = 0x29FFFFFF;
    private static final int VIOLET   = 0xFFA78BFA;
    private static final int VIOLET_S = 0xFF8B5CF6;
    private static final int VIOLET_D = 0xFF6D28D9;
    private static final int GREEN    = 0xFF34D399;
    private static final int GREEN_D  = 0xFF166534;
    private static final int CYAN     = 0xFF38BDF8;
    private static final int CYAN_D   = 0xFF0284C7;
    private static final int RED      = 0xFFEF4444;

    private static final String[] TOP_TABS = {"MODS", "SETTINGS", "WAYPOINTS"};
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private static final String[] WAYPOINT_DIM_TABS = {"ALL", "WORLD", "NETHER", "END"};

    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "FPS",  0xFFA78BFA, "Current frames per second"},
        {"WASD Keys",     "HUD",      "KEYS", 0xFF818CF8, "Movement key presses"},
        {"Ping Display",  "HUD",      "MS",   0xFF34D399, "Network ping latency"},
        {"CPS Counter",   "HUD",      "CPS",  0xFFF472B6, "Clicks per second"},
        {"Armor Status",  "HUD",      "ARM",  0xFF34D399, "Armor & durability HUD"},
        {"Coordinates",   "HUD",      "XYZ",  0xFF38BDF8, "Current world position"},
        {"Day Counter",   "HUD",      "DAY",  0xFFFB923C, "In-game day counter"},
        {"Block Info",    "HUD",      "BLK",  0xFF67E8F9, "Target block details"},
        {"Toggle Sprint", "Movement", "SPR",  0xFF60A5FA, "Auto sprint toggle"},
        {"Toggle Sneak",  "Movement", "SNK",  0xFFA78BFA, "Auto sneak toggle"},
        {"Zoom Mod",      "Movement", "ZOOM", 0xFFC084FC, "Smooth camera zoom (C)"},
        {"Free Look",     "Movement", "LOOK", 0xFF34D399, "360 camera view (V)"},
        {"Snap Look",     "Movement", "SNAP", 0xFF2DD4BF, "Quick rear view (B)"},
        {"Fullbright",    "Visual",   "BRT",  0xFFFBBF24, "Full brightness (F6)"},
        {"No Hurt Cam",   "Visual",   "CAM",  0xFFF87171, "Disable damage wobble"},
        {"Minimap",       "Visual",   "MAP",  0xFFA78BFA, "Radar minimap HUD"},
        {"Waypoints",     "Visual",   "WAY",  0xFF38BDF8, "World waypoint markers"},
        {"Chat Colors",   "HUD",      "CHAT", 0xFF34D399, "Rank chat coloring"},
        {"Item Tooltips", "HUD",      "TIPS", 0xFFC084FC, "Enhanced item tooltips"},
        {"Hit Color",     "Visual",   "HIT",  0xFFF87171, "Damage flash color"},
        {"Item Physics",  "Visual",   "PHY",  0xFF38BDF8, "Flat dropped items"},
        {"Potion Status", "HUD",      "POT",  0xFFC084FC, "Active potion effects"},
        {"Crosshair",     "Visual",   "+",    0xFF34D399, "Custom crosshair studio"},
        {"Item Model",    "Visual",   "MDL",  0xFF38BDF8, "Custom 3D item scale"},
    };

    private int activeTopTab = 0;
    private int selCat = 0;
    private String searchQuery = "";
    private int waypointDimFilter = 0;
    private boolean waypointShowAllWorlds = false;

    private FlowLayout moduleGridContainer;
    private FlowLayout waypointGridContainer;
    private final List<ButtonComponent> catPills = new ArrayList<>();
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
        new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"))
    );

    public ModMenuScreen() { this(0); }
    public ModMenuScreen(int initialTab) {
        super(Text.literal("Velora Client"));
        this.activeTopTab = initialTab;
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

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(PANEL_W), Sizing.fixed(PANEL_H));
        panel.horizontalAlignment(HorizontalAlignment.LEFT);
        panel.verticalAlignment(VerticalAlignment.TOP);
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        panel.padding(Insets.none());

        panel.child(buildHeader(root));
        panel.child(buildBody(root));
        root.child(panel);
    }

    private FlowLayout buildHeader(FlowLayout root) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(HDR_H));
        header.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x, y + h - 1, x + w, y + h, BORDER_S);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.horizontalAlignment(HorizontalAlignment.LEFT);
        header.padding(Insets.of(0, 10, 0, 10));
        header.gap(6);

        FlowLayout brand = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        brand.verticalAlignment(VerticalAlignment.CENTER);
        brand.horizontalAlignment(HorizontalAlignment.LEFT);
        brand.gap(4);
        brand.child(Components.label(Text.literal("VELORA")).color(Color.ofArgb(VIOLET)).shadow(true));
        brand.child(Components.label(Text.literal(TOP_TABS[activeTopTab])).color(Color.ofArgb(TEXT)).shadow(true));
        header.child(brand);

        header.child(Containers.horizontalFlow(Sizing.fixed(30), Sizing.fixed(1)));

        FlowLayout topNav = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        topNav.verticalAlignment(VerticalAlignment.CENTER);
        topNav.horizontalAlignment(HorizontalAlignment.LEFT);
        topNav.gap(4);
        for (int i = 0; i < TOP_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent tabBtn = Components.button(Text.literal(TOP_TABS[tabIdx]), btn -> {
                activeTopTab = tabIdx;
                root.clearChildren();
                build(root);
            });
            tabBtn.sizing(Sizing.content(), Sizing.fixed(18));
            if (i == activeTopTab) tabBtn.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET_S, VIOLET_D));
            else tabBtn.renderer(ButtonComponent.Renderer.flat(SURF3, BORDER_S, SURF3));
            topNav.child(tabBtn);
        }
        header.child(topNav);

        header.child(Containers.horizontalFlow(Sizing.fixed(40), Sizing.fixed(1)));

        if (activeTopTab == 0) {
            FlowLayout searchBox = Containers.horizontalFlow(Sizing.fixed(110), Sizing.fixed(18));
            searchBox.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, SURF);
                ctx.drawBorder(x, y, w, h, BORDER);
            });
            searchBox.padding(Insets.of(0, 4, 0, 4));
            searchBox.verticalAlignment(VerticalAlignment.CENTER);
            searchBox.horizontalAlignment(HorizontalAlignment.LEFT);
            searchBox.gap(4);
            searchBox.child(Components.label(Text.literal(">")).color(Color.ofArgb(TEXT_F)));
            TextBoxComponent searchInput = Components.textBox(Sizing.fill(100), searchQuery);
            searchInput.sizing(Sizing.fill(100), Sizing.fixed(14));
            searchInput.onChanged().subscribe(val -> { searchQuery = val.toLowerCase().trim(); rebuildGrid(); });
            searchBox.child(searchInput);
            header.child(searchBox);
        } else {
            header.child(Containers.horizontalFlow(Sizing.fixed(110), Sizing.fixed(1)));
        }

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        header.child(closeBtn);

        return header;
    }

    private FlowLayout buildBody(FlowLayout root) {
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H));
        body.horizontalAlignment(HorizontalAlignment.LEFT);
        body.verticalAlignment(VerticalAlignment.TOP);

        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(SIDEBAR_W), Sizing.fill(100));
        sidebar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x + w - 1, y, x + w, y + h, BORDER);
        });
        sidebar.padding(Insets.of(6, 2, 6, 2));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.verticalAlignment(VerticalAlignment.TOP);
        sidebar.gap(4);

        sidebar.child(makeSidebarIcon("MODS", activeTopTab == 0, () -> { activeTopTab = 0; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarIcon("WAY", activeTopTab == 2, () -> { activeTopTab = 2; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarIcon("SKIN", false, () -> { if (client != null) client.setScreen(new CosmeticsLockerScreen()); }));
        sidebar.child(makeSidebarIcon("HUD", false, () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
        sidebar.child(makeSidebarIcon("CFG", activeTopTab == 1, () -> { activeTopTab = 1; root.clearChildren(); build(root); }));

        sidebar.child(Containers.verticalFlow(Sizing.fixed(46), Sizing.fixed(20)));

        FlowLayout enabledCount = Containers.verticalFlow(Sizing.fixed(46), Sizing.content());
        enabledCount.horizontalAlignment(HorizontalAlignment.CENTER);
        int enabled = getEnabledCount();
        enabledCount.child(Components.label(Text.literal(String.valueOf(enabled))).color(Color.ofArgb(VIOLET)));
        enabledCount.child(Components.label(Text.literal("on")).color(Color.ofArgb(TEXT_F)));
        sidebar.child(enabledCount);

        body.child(sidebar);

        FlowLayout contentWrapper = Containers.verticalFlow(Sizing.fixed(CONTENT_W), Sizing.fill(100));
        contentWrapper.surface(Surface.flat(SURF2));
        contentWrapper.padding(Insets.of(4, 6, 4, 6));
        contentWrapper.horizontalAlignment(HorizontalAlignment.LEFT);
        contentWrapper.verticalAlignment(VerticalAlignment.TOP);

        if (activeTopTab == 0) contentWrapper.child(buildModsView());
        else if (activeTopTab == 1) contentWrapper.child(buildSettingsView());
        else if (activeTopTab == 2) contentWrapper.child(buildWaypointsView(root));

        body.child(contentWrapper);
        return body;
    }

    private FlowLayout buildModsView() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.verticalAlignment(VerticalAlignment.TOP);
        container.gap(3);

        FlowLayout catBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        catBar.verticalAlignment(VerticalAlignment.CENTER);
        catBar.horizontalAlignment(HorizontalAlignment.LEFT);
        catBar.padding(Insets.none());
        catBar.gap(4);
        catPills.clear();
        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(CATS[idx]), btn -> { selCat = idx; rebuildGrid(); refreshPills(); });
            pill.sizing(Sizing.fixed(42), Sizing.fixed(18));
            stylePill(pill, i == selCat);
            catPills.add(pill);
            catBar.child(pill);
        }
        container.child(catBar);

        moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        moduleGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        moduleGridContainer.verticalAlignment(VerticalAlignment.TOP);
        moduleGridContainer.padding(Insets.of(2, 0, 40, 0));
        moduleGridContainer.gap(3);
        rebuildGrid();

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 24 - 8), moduleGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        container.child(scroll);
        return container;
    }

    private FlowLayout buildSettingsView() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.padding(Insets.of(2, 4, 2, 4));
        container.gap(4);

        FlowLayout list = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        list.horizontalAlignment(HorizontalAlignment.LEFT);
        list.padding(Insets.of(0, 0, 40, 0));
        list.gap(4);

        list.child(makeSectionHeader("Performance & Optimizations"));
        list.child(makeSettingToggle("Fast Math", "Optimized trigonometric functions for extra FPS", ModConfig.optiFastMath, () -> { ModConfig.optiFastMath = !ModConfig.optiFastMath; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Entity Culling", "Skip rendering hidden entities behind walls", ModConfig.optiEntityCulling, () -> { ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Low Memory Mode", "Optimize memory buffers and garbage collection", ModConfig.optiLowMemoryMode, () -> { ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Disable Fog", "Removes thick world distance fog for clearer vision", ModConfig.optiDisableFog, () -> { ModConfig.optiDisableFog = !ModConfig.optiDisableFog; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Limit Particles", "Cap maximum simultaneous particles", ModConfig.optiLimitParticles, () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); }));

        list.child(makeSectionHeader("Visual & Fullbright"));
        list.child(makeSettingToggle("Fullbright (F6)", "Maximum brightness everywhere", ModConfig.showFullbright, () -> { ModConfig.showFullbright = !ModConfig.showFullbright; ModConfig.saveConfig(); }));

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 16), list);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        container.child(scroll);
        return container;
    }

    private FlowLayout buildWaypointsView(FlowLayout root) {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.gap(4);
        container.padding(Insets.none());

        FlowLayout subHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        subHeader.verticalAlignment(VerticalAlignment.CENTER);
        subHeader.horizontalAlignment(HorizontalAlignment.LEFT);
        subHeader.gap(4);

        FlowLayout dimTabs = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        dimTabs.verticalAlignment(VerticalAlignment.CENTER);
        dimTabs.horizontalAlignment(HorizontalAlignment.LEFT);
        dimTabs.gap(3);
        for (int i = 0; i < WAYPOINT_DIM_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent dimBtn = Components.button(Text.literal(WAYPOINT_DIM_TABS[tabIdx]), btn -> {
                waypointDimFilter = tabIdx;
                rebuildWaypointGrid(root);
            });
            dimBtn.sizing(Sizing.content(), Sizing.fixed(16));
            if (tabIdx == waypointDimFilter) dimBtn.renderer(ButtonComponent.Renderer.flat(CYAN_D, CYAN, CYAN_D));
            else dimBtn.renderer(ButtonComponent.Renderer.flat(SURF3, BORDER, SURF3));
            dimTabs.child(dimBtn);
        }
        subHeader.child(dimTabs);

        subHeader.child(Containers.horizontalFlow(Sizing.fixed(174), Sizing.fixed(1)));

        ButtonComponent worldToggleBtn = Components.button(
            Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "THIS WORLD"),
            btn -> {
                waypointShowAllWorlds = !waypointShowAllWorlds;
                btn.setMessage(Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "THIS WORLD"));
                rebuildWaypointGrid(root);
            }
        );
        worldToggleBtn.sizing(Sizing.content(), Sizing.fixed(16));
        worldToggleBtn.renderer(ButtonComponent.Renderer.flat(SURF3, BORDER_S, SURF3));
        subHeader.child(worldToggleBtn);

        ButtonComponent addBtn = Components.button(Text.literal("+ Add Waypoint"), btn -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, null));
        });
        addBtn.sizing(Sizing.content(), Sizing.fixed(18));
        addBtn.renderer(ButtonComponent.Renderer.flat(CYAN_D, CYAN, CYAN_D));
        subHeader.child(addBtn);

        container.child(subHeader);

        waypointGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        waypointGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        waypointGridContainer.padding(Insets.of(0, 0, 40, 0));
        waypointGridContainer.gap(4);
        rebuildWaypointGrid(root);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 26 - 12), waypointGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        container.child(scroll);
        return container;
    }

    private void rebuildWaypointGrid(FlowLayout root) {
        if (waypointGridContainer == null) return;
        waypointGridContainer.clearChildren();

        List<Waypoint> list = waypointShowAllWorlds ? WaypointManager.getAllWaypoints() : WaypointManager.getWaypointsForCurrentWorld();

        if (waypointDimFilter == 1) {
            list = list.stream().filter(w -> "minecraft:overworld".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        } else if (waypointDimFilter == 2) {
            list = list.stream().filter(w -> "minecraft:the_nether".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        } else if (waypointDimFilter == 3) {
            list = list.stream().filter(w -> "minecraft:the_end".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        }

        if (list.isEmpty()) {
            FlowLayout emptyCard = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(100));
            emptyCard.horizontalAlignment(HorizontalAlignment.CENTER);
            emptyCard.verticalAlignment(VerticalAlignment.CENTER);
            emptyCard.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, SURF);
                ctx.drawBorder(x, y, w, h, BORDER);
            });
            emptyCard.gap(4);
            emptyCard.child(Components.label(Text.literal("No waypoints found")).color(Color.ofArgb(TEXT_M)));
            emptyCard.child(Components.label(Text.literal("Click '+ Add Waypoint' above to mark a location in this world!")).color(Color.ofArgb(TEXT_F)));
            waypointGridContainer.child(emptyCard);
            return;
        }

        FlowLayout currentRow = null;
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.horizontalAlignment(HorizontalAlignment.LEFT);
                currentRow.gap(6);
                waypointGridContainer.child(currentRow);
            }
            if (currentRow != null) {
                currentRow.child(buildWaypointCard(list.get(i), root));
            }
        }
    }

    private FlowLayout buildWaypointCard(Waypoint wp, FlowLayout root) {
        FlowLayout card = Containers.horizontalFlow(Sizing.fixed(248), Sizing.fixed(36));
        card.verticalAlignment(VerticalAlignment.CENTER);
        card.horizontalAlignment(HorizontalAlignment.LEFT);
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = wp.enabled ? 0x2216161A : SURF;
            int bdr = wp.enabled ? (wp.color | 0x88000000) : BORDER;
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, bdr);
        });
        card.padding(Insets.of(0, 6, 0, 6));
        card.gap(5);

        FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(12), Sizing.fixed(12));
        swatch.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, wp.color | 0xFF000000);
            ctx.drawBorder(x, y, w, h, 0x66FFFFFF);
        });
        card.child(swatch);

        FlowLayout info = Containers.verticalFlow(Sizing.fixed(110), Sizing.content());
        info.horizontalAlignment(HorizontalAlignment.LEFT);
        info.gap(1);
        LabelComponent nameLbl = Components.label(Text.literal(wp.name));
        nameLbl.color(Color.ofArgb(wp.enabled ? TEXT : TEXT_F));
        nameLbl.shadow(true);
        info.child(nameLbl);
        String coordStr = String.format("%.0f, %.0f, %.0f", wp.x, wp.y, wp.z);
        info.child(Components.label(Text.literal(coordStr)).color(Color.ofArgb(TEXT_F)));
        card.child(info);

        ButtonComponent onOffBtn = Components.button(Text.literal(wp.enabled ? "ON" : "OFF"), b -> {
            WaypointManager.toggleWaypoint(wp.id);
            b.setMessage(Text.literal(wp.enabled ? "ON" : "OFF"));
            b.renderer(ButtonComponent.Renderer.flat(
                wp.enabled ? 0x3310B981 : SURF3,
                wp.enabled ? 0xFF34D399 : TEXT_F,
                wp.enabled ? 0x3310B981 : SURF3
            ));
            nameLbl.color(Color.ofArgb(wp.enabled ? TEXT : TEXT_F));
        });
        onOffBtn.sizing(Sizing.fixed(28), Sizing.fixed(18));
        onOffBtn.renderer(ButtonComponent.Renderer.flat(
            wp.enabled ? 0x3310B981 : SURF3,
            wp.enabled ? 0xFF34D399 : TEXT_F,
            wp.enabled ? 0x3310B981 : SURF3
        ));
        card.child(onOffBtn);

        ButtonComponent editBtn = Components.button(Text.literal("EDIT"), b -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, wp));
        });
        editBtn.sizing(Sizing.fixed(32), Sizing.fixed(18));
        editBtn.renderer(ButtonComponent.Renderer.flat(SURF3, CYAN, SURF3));
        card.child(editBtn);

        ButtonComponent delBtn = Components.button(Text.literal("DEL"), b -> {
            WaypointManager.removeWaypoint(wp.id);
            rebuildWaypointGrid(root);
        });
        delBtn.sizing(Sizing.fixed(28), Sizing.fixed(18));
        delBtn.renderer(ButtonComponent.Renderer.flat(SURF3, RED, SURF3));
        card.child(delBtn);

        return card;
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(4, 0, 2, 0));
        header.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(TEXT_F)));
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        return header;
    }

    private FlowLayout makeSettingToggle(String label, String desc, boolean enabled, Runnable action) {
        final boolean[] state = new boolean[]{enabled};

        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(34));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, state[0] ? 0x1238BDF8 : SURF);
            ctx.drawBorder(x, y, w, h, state[0] ? 0x4438BDF8 : BORDER);
        });
        row.padding(Insets.of(0, 8, 0, 8));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(6);

        FlowLayout textCol = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        textCol.gap(1);
        textCol.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT)));
        textCol.child(Components.label(Text.literal(desc)).color(Color.ofArgb(TEXT_F)));
        row.child(textCol);

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(14));
        Runnable toggleAction = () -> {
            state[0] = !state[0];
            action.run();
        };

        toggle.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, state[0] ? GREEN_D : SURF3);
            ctx.drawBorder(x, y, w, h, state[0] ? GREEN : BORDER_S);
            int knobX = state[0] ? x + w - 10 : x + 2;
            ctx.fill(knobX, y + 2, knobX + 8, y + h - 2, state[0] ? GREEN : TEXT_F);
        });
        row.child(toggle);
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { toggleAction.run(); return true; } return false; });
        return row;
    }

    private FlowLayout makeSidebarIcon(String label, boolean active, Runnable action) {
        FlowLayout icon = Containers.verticalFlow(Sizing.fixed(48), Sizing.fixed(36));
        icon.verticalAlignment(VerticalAlignment.CENTER);
        icon.horizontalAlignment(HorizontalAlignment.CENTER);
        icon.gap(2);

        icon.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (active) {
                ctx.fill(x, y, x + w, y + h, SURF2);
                ctx.fill(x, y, x + 2, y + h, VIOLET);
            }
        });

        icon.child(Components.label(Text.literal(label))
            .color(Color.ofArgb(active ? VIOLET : TEXT_F)));

        icon.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });
        return icon;
    }

    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) pill.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET_S, VIOLET_D));
        else pill.renderer(ButtonComponent.Renderer.flat(SURF3, BORDER_S, SURF3));
    }

    private void refreshPills() {
        for (int i = 0; i < catPills.size(); i++) {
            stylePill(catPills.get(i), i == selCat);
        }
    }

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
                    !cat.toLowerCase().contains(searchQuery)) {
                    continue;
                }
            }
            visible.add(i);
        }

        FlowLayout currentRow = null;
        for (int k = 0; k < visible.size(); k++) {
            if (k % COLS == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.horizontalAlignment(HorizontalAlignment.LEFT);
                currentRow.gap(3);
                moduleGridContainer.child(currentRow);
            }
            if (currentRow != null) {
                currentRow.child(buildModCard(visible.get(k)));
            }
        }
    }

    private FlowLayout buildModCard(int modIndex) {
        Object[] def = MODS[modIndex];
        String name = (String) def[0];
        String tag  = (String) def[2];
        int col     = (Integer) def[3];
        String desc = (String) def[4];
        final boolean[] curEn = new boolean[]{isEnabled(modIndex)};

        FlowLayout card = Containers.verticalFlow(Sizing.fixed((PANEL_W - SIDEBAR_W - 18) / COLS), Sizing.fixed(58));
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = curEn[0] ? 0x18166534 : SURF;
            int bdr = curEn[0] ? GREEN_D : BORDER;
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, bdr);
        });
        card.padding(Insets.of(5, 6, 5, 6));
        card.gap(2);

        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(4);

        FlowLayout badge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(14));
        badge.surface(Surface.flat(col & 0x33FFFFFF));
        badge.padding(Insets.of(0, 3, 0, 3));
        badge.verticalAlignment(VerticalAlignment.CENTER);
        badge.child(Components.label(Text.literal(tag)).color(Color.ofArgb(col)));
        topRow.child(badge);

        LabelComponent titleLbl = Components.label(Text.literal(name));
        titleLbl.color(Color.ofArgb(TEXT));
        topRow.child(titleLbl);
        card.child(topRow);

        LabelComponent descLbl = Components.label(Text.literal(desc));
        descLbl.color(Color.ofArgb(TEXT_F));
        card.child(descLbl);

        LabelComponent statusLbl = Components.label(Text.literal(curEn[0] ? "+ ON" : "+ OFF"));
        statusLbl.color(Color.ofArgb(curEn[0] ? GREEN : TEXT_F));
        card.child(statusLbl);

        card.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { // Left click: Toggle in-place
                curEn[0] = !curEn[0];
                toggleMod(modIndex);
                statusLbl.text(Text.literal(curEn[0] ? "+ ON" : "+ OFF"));
                statusLbl.color(Color.ofArgb(curEn[0] ? GREEN : TEXT_F));
                return true;
            } else if (btn == 1) { // Right click: Open Settings Screen
                openModSettings(name);
                return true;
            }
            return false;
        });

        return card;
    }

    private void openModSettings(String modName) {
        if (client == null) return;
        if ("Crosshair".equalsIgnoreCase(modName) || "Custom Crosshair".equalsIgnoreCase(modName)) {
            client.setScreen(new CrosshairEditorScreen(this));
        } else if ("Item Model".equalsIgnoreCase(modName) || "View Model".equalsIgnoreCase(modName) || "ViewModel".equalsIgnoreCase(modName)) {
            client.setScreen(new ItemModelSettingsScreen(this));
        } else {
            client.setScreen(new ModuleSettingsScreen(modName, this));
        }
    }

    private boolean isEnabled(int index) {
        return switch (index) {
            case 0 -> ModConfig.showFps;
            case 1 -> ModConfig.showKeystrokes;
            case 2 -> ModConfig.showPing;
            case 3 -> ModConfig.showCps;
            case 4 -> ModConfig.showArmorStatus;
            case 5 -> ModConfig.showCoordinates;
            case 6 -> ModConfig.showDayCounter;
            case 7 -> ModConfig.showBlockInfo;
            case 8 -> ModConfig.showToggleSprint;
            case 9 -> ModConfig.showToggleSneak;
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
            default -> false;
        };
    }

    private void toggleMod(int index) {
        switch (index) {
            case 0 -> ModConfig.showFps = !ModConfig.showFps;
            case 1 -> ModConfig.showKeystrokes = !ModConfig.showKeystrokes;
            case 2 -> ModConfig.showPing = !ModConfig.showPing;
            case 3 -> ModConfig.showCps = !ModConfig.showCps;
            case 4 -> ModConfig.showArmorStatus = !ModConfig.showArmorStatus;
            case 5 -> ModConfig.showCoordinates = !ModConfig.showCoordinates;
            case 6 -> ModConfig.showDayCounter = !ModConfig.showDayCounter;
            case 7 -> ModConfig.showBlockInfo = !ModConfig.showBlockInfo;
            case 8 -> ModConfig.showToggleSprint = !ModConfig.showToggleSprint;
            case 9 -> ModConfig.showToggleSneak = !ModConfig.showToggleSneak;
            case 10 -> ModConfig.showZoom = !ModConfig.showZoom;
            case 11 -> ModConfig.showFreeLook = !ModConfig.showFreeLook;
            case 12 -> ModConfig.showSnapLook = !ModConfig.showSnapLook;
            case 13 -> ModConfig.showFullbright = !ModConfig.showFullbright;
            case 14 -> ModConfig.showNoHurtCam = !ModConfig.showNoHurtCam;
            case 15 -> ModConfig.showMinimap = !ModConfig.showMinimap;
            case 16 -> ModConfig.showWaypoints = !ModConfig.showWaypoints;
            case 17 -> ModConfig.showChatColors = !ModConfig.showChatColors;
            case 18 -> ModConfig.showItemTooltips = !ModConfig.showItemTooltips;
            case 19 -> ModConfig.showHitColor = !ModConfig.showHitColor;
            case 20 -> ModConfig.showItemPhysics = !ModConfig.showItemPhysics;
            case 21 -> ModConfig.showPotionHud = !ModConfig.showPotionHud;
            case 22 -> ModConfig.enableCustomCrosshair = !ModConfig.enableCustomCrosshair;
            case 23 -> ModConfig.showViewModel = !ModConfig.showViewModel;
        }
        ModConfig.saveConfig();
    }

    private int getEnabledCount() {
        int c = 0;
        for (int i = 0; i < MODS.length; i++) {
            if (isEnabled(i)) c++;
        }
        return c;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world == null) {
            this.panoramaRenderer.render(context, this.width, this.height, 1.0F, delta);
            context.fillGradient(0, 0, this.width, this.height, 0x90050508, 0xD008080C);
        } else {
            context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xAA000000);
        }
    }
}
