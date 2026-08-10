package com.velora.client.gui;

import com.velora.client.waypoints.Waypoint;
import com.velora.client.waypoints.WaypointManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class WaypointManagerScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private boolean showAllWorlds = false;
    private int selectedDimFilter = 0; // 0 = ALL, 1 = OVERWORLD, 2 = NETHER, 3 = END
    private FlowLayout waypointList;

    private static final int BG_COLOR = 0xCC08080A;
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
    private static final int CYAN     = 0xFF38BDF8;
    private static final int RED      = 0xFFEF4444;

    private static final String[] DIM_TABS = {"ALL", "WORLD", "NETHER", "END"};

    public WaypointManagerScreen(Screen parent) {
        super(Text.literal("Waypoint Manager"));
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.flat(BG_COLOR));
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(560), Sizing.fixed(330));
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        panel.padding(Insets.none());

        // 1. Header Bar
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        header.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x, y + h - 1, x + w, y + h, BORDER_S);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 10, 0, 10));
        header.gap(8);

        FlowLayout brand = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        brand.verticalAlignment(VerticalAlignment.CENTER);
        brand.gap(4);
        brand.child(Components.label(Text.literal("VELORA")).color(Color.ofArgb(VIOLET)).shadow(true));
        brand.child(Components.label(Text.literal("Waypoints")).color(Color.ofArgb(TEXT)).shadow(true));
        header.child(brand);

        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        // Dimension Tabs: [ ALL ] [ WORLD ] [ NETHER ] [ END ]
        FlowLayout dimTabs = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        dimTabs.verticalAlignment(VerticalAlignment.CENTER);
        dimTabs.gap(4);
        for (int i = 0; i < DIM_TABS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(DIM_TABS[idx]), btn -> {
                selectedDimFilter = idx;
                rebuildList();
                root.clearChildren();
                build(root);
            });
            pill.sizing(Sizing.content(), Sizing.fixed(18));
            if (i == selectedDimFilter) {
                pill.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET_S, VIOLET_D));
            } else {
                pill.renderer(ButtonComponent.Renderer.flat(SURF3, BORDER_S, SURF3));
            }
            dimTabs.child(pill);
        }
        header.child(dimTabs);

        header.child(Containers.horizontalFlow(Sizing.fixed(30), Sizing.fixed(1)));

        // Filter Scope Button: Current World vs All
        ButtonComponent filterBtn = Components.button(
            Text.literal(showAllWorlds ? "All Worlds" : "Current World"),
            b -> {
                showAllWorlds = !showAllWorlds;
                rebuildList();
                root.clearChildren();
                build(root);
            }
        );
        filterBtn.sizing(Sizing.content(), Sizing.fixed(18));
        filterBtn.renderer(ButtonComponent.Renderer.flat(SURF3, TEXT_M, SURF3));
        header.child(filterBtn);

        // + Create Waypoint Button
        ButtonComponent newBtn = Components.button(Text.literal("+ Add Waypoint"), b -> {
            if (this.client != null) {
                this.client.setScreen(new WaypointCreateScreen(this, null));
            }
        });
        newBtn.sizing(Sizing.content(), Sizing.fixed(18));
        newBtn.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET, VIOLET_D));
        header.child(newBtn);

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        header.child(closeBtn);

        panel.child(header);

        // Scope Sub-Bar
        FlowLayout subBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        subBar.surface(Surface.flat(SURF));
        subBar.verticalAlignment(VerticalAlignment.CENTER);
        subBar.padding(Insets.of(0, 10, 0, 10));
        String curScope = WaypointManager.getCurrentWorldOrServerKey();
        String curDim = WaypointManager.getCurrentDimensionKey().replace("minecraft:", "");
        subBar.child(Components.label(Text.literal("Scope: " + curScope + "  |  Dimension: " + curDim)).color(Color.ofArgb(TEXT_F)));
        panel.child(subBar);

        // 2. Waypoint Grid List
        waypointList = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        waypointList.gap(4);
        waypointList.padding(Insets.of(6, 8, 6, 8));

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), waypointList);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        panel.child(scroll);

        root.child(panel);

        rebuildList();
    }

    private void rebuildList() {
        waypointList.clearChildren();

        List<Waypoint> list = showAllWorlds ? WaypointManager.getAllWaypoints() : WaypointManager.getWaypointsForCurrentWorld();

        // Apply Dimension Filter
        if (selectedDimFilter == 1) { // WORLD / OVERWORLD
            list = list.stream().filter(w -> "minecraft:overworld".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        } else if (selectedDimFilter == 2) { // NETHER
            list = list.stream().filter(w -> "minecraft:the_nether".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        } else if (selectedDimFilter == 3) { // END
            list = list.stream().filter(w -> "minecraft:the_end".equalsIgnoreCase(w.dimension)).collect(Collectors.toList());
        }

        if (list.isEmpty()) {
            FlowLayout emptyCard = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(120));
            emptyCard.horizontalAlignment(HorizontalAlignment.CENTER);
            emptyCard.verticalAlignment(VerticalAlignment.CENTER);
            emptyCard.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, SURF3);
                ctx.drawBorder(x, y, w, h, BORDER);
            });
            emptyCard.gap(4);
            emptyCard.child(Components.label(Text.literal("No waypoints found")).color(Color.ofArgb(TEXT_M)));
            emptyCard.child(Components.label(Text.literal("Click '+ Add Waypoint' to save your location in this world!")).color(Color.ofArgb(TEXT_F)));
            waypointList.child(emptyCard);
            return;
        }

        // 2-Column Grid (Image 1 layout)
        FlowLayout currentRow = null;
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.gap(6);
                waypointList.child(currentRow);
            }
            if (currentRow != null) {
                currentRow.child(buildWaypointCard(list.get(i)));
            }
        }
    }

    private FlowLayout buildWaypointCard(Waypoint wp) {
        FlowLayout card = Containers.horizontalFlow(Sizing.fill(50), Sizing.fixed(32));
        card.verticalAlignment(VerticalAlignment.CENTER);
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF);
            ctx.drawBorder(x, y, w, h, wp.enabled ? BORDER_S : BORDER);
        });
        card.padding(Insets.of(0, 6, 0, 6));
        card.gap(6);

        // Color Swatch Box
        FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(14), Sizing.fixed(14));
        swatch.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, wp.color | 0xFF000000);
            ctx.drawBorder(x, y, w, h, 0x55FFFFFF);
        });
        card.child(swatch);

        // Waypoint Name & Coords
        FlowLayout info = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        info.gap(1);
        info.child(Components.label(Text.literal(wp.name))
            .color(Color.ofArgb(wp.enabled ? TEXT : TEXT_F))
            .sizing(Sizing.fill(100), Sizing.content()));
        String coordStr = String.format("%.0f, %.0f, %.0f", wp.x, wp.y, wp.z);
        info.child(Components.label(Text.literal(coordStr)).color(Color.ofArgb(TEXT_F)));
        card.child(info);

        // Action 1: Edit Pencil Button (✏)
        ButtonComponent editBtn = Components.button(Text.literal("✏"), b -> {
            if (client != null) {
                client.setScreen(new WaypointCreateScreen(this, wp));
            }
        });
        editBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        editBtn.renderer(ButtonComponent.Renderer.flat(SURF2, CYAN, SURF2));
        card.child(editBtn);

        // Action 2: Visibility Eye Button (👁)
        ButtonComponent eyeBtn = Components.button(Text.literal("👁"), b -> {
            WaypointManager.toggleWaypoint(wp.id);
            rebuildList();
        });
        eyeBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        eyeBtn.renderer(ButtonComponent.Renderer.flat(SURF2, wp.enabled ? CYAN : TEXT_F, SURF2));
        card.child(eyeBtn);

        // Action 3: Delete Cross Button (✕)
        ButtonComponent delBtn = Components.button(Text.literal("✕"), b -> {
            WaypointManager.removeWaypoint(wp.id);
            rebuildList();
        });
        delBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        delBtn.renderer(ButtonComponent.Renderer.flat(SURF2, RED, SURF2));
        card.child(delBtn);

        return card;
    }

    @Override
    public void close() {
        if (this.client != null && this.parent != null) {
            this.client.setScreen(this.parent);
        } else {
            super.close();
        }
    }
}
