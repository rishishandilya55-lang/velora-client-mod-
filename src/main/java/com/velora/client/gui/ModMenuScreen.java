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

    private static final int PANEL_W = 540;
    private static final int PANEL_H = 310;
    private static final int HDR_H = 30;
    private static final int SIDEBAR_W = 48;
    private static final int CONTENT_W = PANEL_W - SIDEBAR_W; // 492px
    private static final int COLS = 2;

    private static final String[] TOP_TABS = {"MODS", "SETTINGS", "WAYPOINTS"};
    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};
    private static final String[] WAYPOINT_DIM_TABS = {"ALL", "OVERWORLD", "NETHER", "END"};

    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "FPS",  0xFF818CF8, "Display frames per second"},
        {"WASD Keys",     "HUD",      "KEYS", 0xFF818CF8, "Movement key presses and CPS"},
        {"Ping Display",  "HUD",      "MS",   0xFF34D399, "Real-time server latency"},
        {"CPS Counter",   "HUD",      "CPS",  0xFFF472B6, "Left and right clicks per second"},
        {"Armor Status",  "HUD",      "ARM",  0xFF34D399, "Armor durability and items on screen"},
        {"Coordinates",   "HUD",      "XYZ",  0xFF38BDF8, "Player position and biome info"},
        {"Day Counter",   "HUD",      "DAY",  0xFFFB923C, "World day and time indicator"},
        {"Block Info",    "HUD",      "BLK",  0xFF67E8F9, "Targeted block name and state"},
        {"Toggle Sprint", "Movement", "SPR",  0xFF60A5FA, "Automatic sprinting toggle"},
        {"Toggle Sneak",  "Movement", "SNK",  0xFF818CF8, "Automatic sneaking toggle"},
        {"Zoom Mod",      "Movement", "ZOOM", 0xFFC084FC, "Smooth camera zoom (C)"},
        {"Free Look",     "Movement", "LOOK", 0xFF34D399, "Freelook camera view (V)"},
        {"Snap Look",     "Movement", "SNAP", 0xFF2DD4BF, "Quick look backwards (B)"},
        {"Fullbright",    "Visual",   "BRT",  0xFFFBBF24, "Maximum world brightness (F6)"},
        {"No Hurt Cam",   "Visual",   "CAM",  0xFFF87171, "Disable damage camera shake"},
        {"Minimap",       "Visual",   "MAP",  0xFF818CF8, "Mini radar map overlay"},
        {"Waypoints",     "Visual",   "WAY",  0xFF38BDF8, "In-world beacon markers"},
        {"Chat Colors",   "HUD",      "CHAT", 0xFF34D399, "Player rank and chat highlights"},
        {"Item Tooltips", "HUD",      "TIPS", 0xFFC084FC, "Item stats and durability tooltips"},
        {"Hit Color",     "Visual",   "HIT",  0xFFF87171, "Custom damage flash overlay"},
        {"Item Physics",  "Visual",   "PHY",  0xFF38BDF8, "Realistic 3D dropped item physics"},
        {"Potion Status", "HUD",      "POT",  0xFFC084FC, "Active potion effects and timers"},
        {"Crosshair",     "Visual",   "+",    0xFF34D399, "Custom crosshair studio"},
        {"Item Model",    "Visual",   "MDL",  0xFF38BDF8, "Custom 3D held item transforms"},
        {"Nametag",       "Visual",   "TAG",  0xFF818CF8, "Custom player nametag and rank (P)"},
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
            VeloraRenderUtil.drawSolidPanel(ctx, x, y, w, h, VeloraColors.SURF2, VeloraColors.BORDER_S);
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
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF3);
            ctx.fill(x, y + h - 1, x + w, y + h, VeloraColors.DIVIDER);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.horizontalAlignment(HorizontalAlignment.LEFT);
        header.padding(Insets.of(0, 10, 0, 10));
        header.gap(6);

        FlowLayout brand = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        brand.verticalAlignment(VerticalAlignment.CENTER);
        brand.gap(4);
        brand.child(Components.label(Text.literal("VELORA")).color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true));
        header.child(brand);

        header.child(Containers.horizontalFlow(Sizing.fixed(12), Sizing.fixed(1)));

        FlowLayout topNav = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        topNav.verticalAlignment(VerticalAlignment.CENTER);
        topNav.gap(4);
        for (int i = 0; i < TOP_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent tabBtn = Components.button(Text.literal(TOP_TABS[tabIdx]), btn -> {
                activeTopTab = tabIdx;
                root.clearChildren();
                build(root);
            });
            tabBtn.sizing(Sizing.content(), Sizing.fixed(18));
            if (i == activeTopTab) {
                tabBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.VIOLET_D, VeloraColors.VIOLET_S, VeloraColors.VIOLET_D));
            } else {
                tabBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF2, VeloraColors.SURF4, VeloraColors.SURF2));
            }
            topNav.child(tabBtn);
        }
        header.child(topNav);

        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        if (activeTopTab == 0) {
            FlowLayout searchBox = Containers.horizontalFlow(Sizing.fixed(110), Sizing.fixed(18));
            searchBox.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, VeloraColors.SURF);
                ctx.drawBorder(x, y, w, h, VeloraColors.BORDER);
            });
            searchBox.padding(Insets.of(0, 4, 0, 4));
            searchBox.verticalAlignment(VerticalAlignment.CENTER);
            searchBox.gap(3);
            searchBox.child(Components.label(Text.literal("⌕")).color(Color.ofArgb(VeloraColors.TEXT_F)));
            TextBoxComponent searchInput = Components.textBox(Sizing.fill(100), searchQuery);
            searchInput.sizing(Sizing.fill(100), Sizing.fixed(14));
            searchInput.onChanged().subscribe(val -> { searchQuery = val.toLowerCase().trim(); rebuildGrid(); });
            searchBox.child(searchInput);
            header.child(searchBox);
        }

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED_F, VeloraColors.SURF3));
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
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF3);
            ctx.fill(x + w - 1, y, x + w, y + h, VeloraColors.DIVIDER);
        });
        sidebar.padding(Insets.of(6, 2, 6, 2));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.verticalAlignment(VerticalAlignment.TOP);
        sidebar.gap(3);

        sidebar.child(makeSidebarIcon("MODS", activeTopTab == 0, () -> { activeTopTab = 0; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarIcon("WAY", activeTopTab == 2, () -> { activeTopTab = 2; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarIcon("SKIN", false, () -> { if (client != null) client.setScreen(new CosmeticsLockerScreen()); }));
        sidebar.child(makeSidebarIcon("HUD", false, () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
        sidebar.child(makeSidebarIcon("CFG", activeTopTab == 1, () -> { activeTopTab = 1; root.clearChildren(); build(root); }));

        sidebar.child(Containers.verticalFlow(Sizing.fixed(1), Sizing.expand(1)));

        FlowLayout enabledCount = Containers.verticalFlow(Sizing.fixed(44), Sizing.content());
        enabledCount.horizontalAlignment(HorizontalAlignment.CENTER);
        int enabled = getEnabledCount();
        enabledCount.child(Components.label(Text.literal(String.valueOf(enabled))).color(Color.ofArgb(VeloraColors.GREEN)));
        enabledCount.child(Components.label(Text.literal("active")).color(Color.ofArgb(VeloraColors.TEXT_F)));
        sidebar.child(enabledCount);

        body.child(sidebar);

        FlowLayout contentWrapper = Containers.verticalFlow(Sizing.fixed(CONTENT_W), Sizing.fill(100));
        contentWrapper.surface(Surface.flat(VeloraColors.SURF2));
        contentWrapper.padding(Insets.of(6, 8, 6, 8));
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
        container.gap(4);

        FlowLayout catBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
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
        moduleGridContainer.padding(Insets.of(0, 0, 24, 0));
        moduleGridContainer.gap(4);
        rebuildGrid();

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 34), moduleGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
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
        list.padding(Insets.of(0, 0, 30, 0));
        list.gap(3);

        list.child(makeSectionHeader("Performance"));
        list.child(makeSettingToggle("Fast Math", "Optimized trigonometric calculations for increased frame rates", ModConfig.optiFastMath, () -> { ModConfig.optiFastMath = !ModConfig.optiFastMath; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Entity Culling", "Skip rendering hidden entities behind walls", ModConfig.optiEntityCulling, () -> { ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Low Memory Mode", "Reduce memory allocation and garbage collection overhead", ModConfig.optiLowMemoryMode, () -> { ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Disable Fog", "Remove terrain distance fog for clear visibility", ModConfig.optiDisableFog, () -> { ModConfig.optiDisableFog = !ModConfig.optiDisableFog; ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Limit Particles", "Cap maximum simultaneous particles on screen", ModConfig.optiLimitParticles, () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); }));

        list.child(makeSectionHeader("Visual"));
        list.child(makeSettingToggle("Fullbright (F6)", "Maximum gamma and illumination everywhere", ModConfig.showFullbright, () -> { ModConfig.showFullbright = !ModConfig.showFullbright; ModConfig.saveConfig(); }));

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 20), list);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        container.child(scroll);
        return container;
    }

    private FlowLayout buildWaypointsView(FlowLayout root) {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        container.horizontalAlignment(HorizontalAlignment.LEFT);
        container.gap(4);

        FlowLayout subHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        subHeader.verticalAlignment(VerticalAlignment.CENTER);
        subHeader.horizontalAlignment(HorizontalAlignment.LEFT);
        subHeader.gap(4);

        FlowLayout dimTabs = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(18));
        dimTabs.verticalAlignment(VerticalAlignment.CENTER);
        dimTabs.gap(3);
        for (int i = 0; i < WAYPOINT_DIM_TABS.length; i++) {
            final int tabIdx = i;
            ButtonComponent dimBtn = Components.button(Text.literal(WAYPOINT_DIM_TABS[tabIdx]), btn -> {
                waypointDimFilter = tabIdx;
                rebuildWaypointGrid(root);
            });
            dimBtn.sizing(Sizing.content(), Sizing.fixed(16));
            if (tabIdx == waypointDimFilter) {
                dimBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.CYAN_D, VeloraColors.CYAN, VeloraColors.CYAN_D));
            } else {
                dimBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.SURF4, VeloraColors.SURF3));
            }
            dimTabs.child(dimBtn);
        }
        subHeader.child(dimTabs);

        subHeader.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        ButtonComponent worldToggleBtn = Components.button(
            Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "CURRENT WORLD"),
            btn -> {
                waypointShowAllWorlds = !waypointShowAllWorlds;
                btn.setMessage(Text.literal(waypointShowAllWorlds ? "ALL WORLDS" : "CURRENT WORLD"));
                rebuildWaypointGrid(root);
            }
        );
        worldToggleBtn.sizing(Sizing.content(), Sizing.fixed(16));
        worldToggleBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.BORDER_S, VeloraColors.SURF3));
        subHeader.child(worldToggleBtn);

        ButtonComponent addBtn = Components.button(Text.literal("+ Add Waypoint"), btn -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, null));
        });
        addBtn.sizing(Sizing.content(), Sizing.fixed(16));
        addBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.CYAN_D, VeloraColors.CYAN, VeloraColors.CYAN_D));
        subHeader.child(addBtn);

        container.child(subHeader);

        waypointGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        waypointGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        waypointGridContainer.padding(Insets.of(0, 0, 30, 0));
        waypointGridContainer.gap(4);
        rebuildWaypointGrid(root);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(PANEL_H - HDR_H - 30), waypointGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
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
            FlowLayout emptyCard = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(80));
            emptyCard.horizontalAlignment(HorizontalAlignment.CENTER);
            emptyCard.verticalAlignment(VerticalAlignment.CENTER);
            emptyCard.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, VeloraColors.SURF);
            });
            emptyCard.gap(3);
            emptyCard.child(Components.label(Text.literal("No waypoints found")).color(Color.ofArgb(VeloraColors.TEXT_M)));
            emptyCard.child(Components.label(Text.literal("Click '+ Add Waypoint' to save coordinates.")).color(Color.ofArgb(VeloraColors.TEXT_F)));
            waypointGridContainer.child(emptyCard);
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
            if (currentRow != null) {
                currentRow.child(buildWaypointCard(list.get(i), root));
            }
        }
    }

    private FlowLayout buildWaypointCard(Waypoint wp, FlowLayout root) {
        FlowLayout card = Containers.horizontalFlow(Sizing.fixed(232), Sizing.fixed(34));
        card.verticalAlignment(VerticalAlignment.CENTER);
        card.horizontalAlignment(HorizontalAlignment.LEFT);
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = wp.enabled ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG;
            ctx.fill(x, y, x + w, y + h, bg);
            if (wp.enabled) {
                ctx.fill(x, y, x + 2, y + h, wp.color | 0xFF000000);
            }
        });
        card.padding(Insets.of(0, 6, 0, 6));
        card.gap(4);

        FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(10), Sizing.fixed(10));
        swatch.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, wp.color | 0xFF000000);
        });
        card.child(swatch);

        FlowLayout info = Containers.verticalFlow(Sizing.fixed(105), Sizing.content());
        info.horizontalAlignment(HorizontalAlignment.LEFT);
        info.gap(1);
        LabelComponent nameLbl = Components.label(Text.literal(wp.name));
        nameLbl.color(Color.ofArgb(wp.enabled ? VeloraColors.TEXT : VeloraColors.TEXT_F));
        nameLbl.shadow(true);
        info.child(nameLbl);
        String coordStr = String.format("%.0f, %.0f, %.0f", wp.x, wp.y, wp.z);
        info.child(Components.label(Text.literal(coordStr)).color(Color.ofArgb(VeloraColors.TEXT_F)));
        card.child(info);

        card.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        ButtonComponent onOffBtn = Components.button(Text.literal(wp.enabled ? "ON" : "OFF"), b -> {
            WaypointManager.toggleWaypoint(wp.id);
            b.setMessage(Text.literal(wp.enabled ? "ON" : "OFF"));
            b.renderer(ButtonComponent.Renderer.flat(
                wp.enabled ? VeloraColors.GREEN_D : VeloraColors.SURF3,
                wp.enabled ? VeloraColors.GREEN_S : VeloraColors.TEXT_F,
                wp.enabled ? VeloraColors.GREEN_D : VeloraColors.SURF3
            ));
            nameLbl.color(Color.ofArgb(wp.enabled ? VeloraColors.TEXT : VeloraColors.TEXT_F));
        });
        onOffBtn.sizing(Sizing.fixed(28), Sizing.fixed(16));
        onOffBtn.renderer(ButtonComponent.Renderer.flat(
            wp.enabled ? VeloraColors.GREEN_D : VeloraColors.SURF3,
            wp.enabled ? VeloraColors.GREEN_S : VeloraColors.TEXT_F,
            wp.enabled ? VeloraColors.GREEN_D : VeloraColors.SURF3
        ));
        card.child(onOffBtn);

        ButtonComponent editBtn = Components.button(Text.literal("Edit"), b -> {
            if (client != null) client.setScreen(new WaypointCreateScreen(this, wp));
        });
        editBtn.sizing(Sizing.fixed(28), Sizing.fixed(16));
        editBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.CYAN, VeloraColors.SURF3));
        card.child(editBtn);

        ButtonComponent delBtn = Components.button(Text.literal("✕"), b -> {
            WaypointManager.removeWaypoint(wp.id);
            rebuildWaypointGrid(root);
        });
        delBtn.sizing(Sizing.fixed(18), Sizing.fixed(16));
        delBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED, VeloraColors.SURF3));
        card.child(delBtn);

        return card;
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(4, 2, 2, 2));
        header.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(VeloraColors.VIOLET)));
        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));
        return header;
    }

    private FlowLayout makeSettingToggle(String label, String desc, boolean enabled, Runnable action) {
        final boolean[] state = new boolean[]{enabled};

        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, state[0] ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG);
            if (state[0]) {
                ctx.fill(x, y, x + 2, y + h, VeloraColors.VIOLET);
            }
        });
        row.padding(Insets.of(0, 8, 0, 8));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(6);

        FlowLayout textCol = Containers.verticalFlow(Sizing.content(), Sizing.content());
        textCol.gap(1);
        textCol.child(Components.label(Text.literal(label)).color(Color.ofArgb(VeloraColors.TEXT)));
        textCol.child(Components.label(Text.literal(desc)).color(Color.ofArgb(VeloraColors.TEXT_F)));
        row.child(textCol);

        row.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(14));
        Runnable toggleAction = () -> {
            state[0] = !state[0];
            action.run();
        };

        toggle.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            VeloraRenderUtil.drawToggleSwitch(ctx, x, y, w, h, state[0]);
        });
        row.child(toggle);
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { toggleAction.run(); return true; } return false; });
        return row;
    }

    private FlowLayout makeSidebarIcon(String label, boolean active, Runnable action) {
        FlowLayout icon = Containers.verticalFlow(Sizing.fixed(44), Sizing.fixed(34));
        icon.verticalAlignment(VerticalAlignment.CENTER);
        icon.horizontalAlignment(HorizontalAlignment.CENTER);
        icon.gap(2);

        icon.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (active) {
                ctx.fill(x, y, x + w, y + h, VeloraColors.SURF2);
                ctx.fill(x, y, x + 2, y + h, VeloraColors.VIOLET);
            }
        });

        icon.child(Components.label(Text.literal(label))
            .color(Color.ofArgb(active ? VeloraColors.VIOLET : VeloraColors.TEXT_F)));

        icon.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });
        return icon;
    }

    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) {
            pill.renderer(ButtonComponent.Renderer.flat(VeloraColors.VIOLET_D, VeloraColors.VIOLET_S, VeloraColors.VIOLET_D));
        } else {
            pill.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.SURF4, VeloraColors.SURF3));
        }
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
                currentRow.gap(6);
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

        int cardW = (CONTENT_W - 22) / COLS; // ~235px
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(cardW), Sizing.fixed(48));
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = curEn[0] ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG;
            ctx.fill(x, y, x + w, y + h, bg);
            if (curEn[0]) {
                ctx.fill(x, y, x + 2, y + h, col);
            }
        });
        card.padding(Insets.of(5, 7, 5, 7));
        card.gap(2);

        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15));
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(4);

        FlowLayout badge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(12));
        badge.surface(Surface.flat(col & 0x22FFFFFF));
        badge.padding(Insets.of(0, 3, 0, 3));
        badge.verticalAlignment(VerticalAlignment.CENTER);
        badge.child(Components.label(Text.literal(tag)).color(Color.ofArgb(col)));
        topRow.child(badge);

        LabelComponent titleLbl = Components.label(Text.literal(name));
        titleLbl.color(Color.ofArgb(VeloraColors.TEXT));
        topRow.child(titleLbl);

        topRow.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(26), Sizing.fixed(13));
        toggle.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            VeloraRenderUtil.drawToggleSwitch(ctx, x, y, w, h, curEn[0]);
        });
        topRow.child(toggle);
        card.child(topRow);

        LabelComponent descLbl = Components.label(Text.literal(desc));
        descLbl.color(Color.ofArgb(VeloraColors.TEXT_F));
        card.child(descLbl);

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(11));
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);
        LabelComponent statusLbl = Components.label(Text.literal(curEn[0] ? "Enabled" : "Disabled"));
        statusLbl.color(Color.ofArgb(curEn[0] ? VeloraColors.GREEN : VeloraColors.TEXT_DIM));
        bottomRow.child(statusLbl);

        bottomRow.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        LabelComponent settingsHint = Components.label(Text.literal("Right-click ⚙"));
        settingsHint.color(Color.ofArgb(VeloraColors.TEXT_DIM));
        bottomRow.child(settingsHint);
        card.child(bottomRow);

        card.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { // Left click: Toggle in-place
                curEn[0] = !curEn[0];
                toggleMod(modIndex);
                statusLbl.text(Text.literal(curEn[0] ? "Enabled" : "Disabled"));
                statusLbl.color(Color.ofArgb(curEn[0] ? VeloraColors.GREEN : VeloraColors.TEXT_DIM));
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
            case 24 -> ModConfig.showNametag;
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
            case 24 -> ModConfig.showNametag = !ModConfig.showNametag;
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
            context.fillGradient(0, 0, this.width, this.height, 0xD0090A0F, 0xF0090A0F);
        } else {
            context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xAA000000);
        }
    }
}
