package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
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

public class ModMenuScreen extends BaseOwoScreen<FlowLayout> {

    private static final int PANEL_W = 560;
    private static final int PANEL_H = 320;
    private static final int HDR_H = 32;
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
    private static final int VIOLET_F = 0x1FA78BFA;
    private static final int GREEN    = 0xFF34D399;
    private static final int GREEN_D  = 0xFF166534;

    private static final String[] CATS = {"All", "HUD", "Movement", "Visual"};

    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "FPS",  0xFFA78BFA, "Shows current frames per second"},
        {"WASD Keys",     "HUD",      "KEYS", 0xFF818CF8, "Displays movement key presses"},
        {"Ping Display",  "HUD",      "MS",   0xFF34D399, "Shows network latency"},
        {"CPS Counter",   "HUD",      "CPS",  0xFFF472B6, "Counts clicks per second"},
        {"Armor Status",  "HUD",      "ARM",  0xFF34D399, "Shows equipped armor and durability"},
        {"Coordinates",   "HUD",      "XYZ",  0xFF38BDF8, "Displays current position"},
        {"Day Counter",   "HUD",      "DAY",  0xFFFB923C, "Shows in-game day count"},
        {"Block Info",    "HUD",      "BLK",  0xFF67E8F9, "Info about looked-at block"},
        {"Toggle Sprint", "Movement", "SPR",  0xFF60A5FA, "Toggle sprint without holding key"},
        {"Toggle Sneak",  "Movement", "SNK",  0xFFA78BFA, "Toggle sneak without holding key"},
        {"Zoom Mod",      "Movement", "ZOOM", 0xFFC084FC, "Hold C to zoom, scroll to adjust"},
        {"Free Look",     "Movement", "LOOK", 0xFF34D399, "Hold V for free camera look"},
        {"Snap Look",     "Movement", "SNAP", 0xFF2DD4BF, "Hold B for quick rear view"},
        {"Fullbright",    "Visual",   "BRT",  0xFFFBBF24, "Full brightness toggle (F6)"},
        {"No Hurt Cam",   "Visual",   "CAM",  0xFFF87171, "Disables hurt camera wobble"},
        {"Minimap",       "Visual",   "MAP",  0xFFA78BFA, "Radar minimap with entity tracking"},
        {"Nametag",       "Visual",   "TAG",  0xFFA78BFA, "Custom nametag with rank badges"},
        {"Chat Colors",   "HUD",      "CHAT", 0xFF34D399, "Rank-based chat message coloring"},
        {"Item Tooltips", "HUD",      "TIPS", 0xFFC084FC, "Enhanced item tooltips with details"},
    };

    private FlowLayout moduleGridContainer;
    private final List<ButtonComponent> catPills = new ArrayList<>();
    private TextBoxComponent searchInput;
    private String searchQuery = "";
    private int selCat = 0;
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
        new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"))
    );

    public ModMenuScreen() {
        super(Text.literal("Velora Client"));
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
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        panel.padding(Insets.none());

        panel.child(buildHeader());
        panel.child(buildBody());
        root.child(panel);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private FlowLayout buildHeader() {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(HDR_H));
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
        brand.child(Components.label(Text.literal("Mods")).color(Color.ofArgb(TEXT)).shadow(true));
        header.child(brand);

        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        FlowLayout tabs = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        tabs.verticalAlignment(VerticalAlignment.CENTER);
        tabs.gap(4);
        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            ButtonComponent pill = Components.button(Text.literal(CATS[idx]), btn -> {
                selCat = idx;
                rebuildGrid();
                refreshPills();
            });
            pill.sizing(Sizing.fixed(42), Sizing.fixed(18));
            stylePill(pill, i == selCat);
            catPills.add(pill);
            tabs.child(pill);
        }
        header.child(tabs);

        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        FlowLayout searchBox = Containers.horizontalFlow(Sizing.fixed(100), Sizing.fixed(18));
        searchBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF);
            ctx.drawBorder(x, y, w, h, BORDER);
        });
        searchBox.padding(Insets.of(0, 4, 0, 4));
        searchBox.verticalAlignment(VerticalAlignment.CENTER);
        searchBox.gap(4);
        searchBox.child(Components.label(Text.literal(">")).color(Color.ofArgb(TEXT_F)));
        searchInput = Components.textBox(Sizing.fill(100), "Search...");
        searchInput.sizing(Sizing.fill(100), Sizing.fixed(14));
        searchInput.onChanged().subscribe(val -> {
            searchQuery = val.toLowerCase().trim();
            rebuildGrid();
        });
        searchBox.child(searchInput);
        header.child(searchBox);

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        header.child(closeBtn);

        return header;
    }

    // ── Body (sidebar + grid) ─────────────────────────────────────────────────

    private FlowLayout buildBody() {
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));

        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(60), Sizing.fill(100));
        sidebar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x + w - 1, y, x + w, y + h, BORDER);
        });
        sidebar.padding(Insets.of(10, 4, 10, 4));
        sidebar.horizontalAlignment(HorizontalAlignment.CENTER);
        sidebar.gap(8);

        sidebar.child(makeSidebarIcon("MODS", true, () -> {}));
        sidebar.child(makeSidebarIcon("SKIN", false, () -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));
        sidebar.child(makeSidebarIcon("HUD", false, () -> {
            if (client != null) client.setScreen(new HudEditorScreen());
        }));
        sidebar.child(makeSidebarIcon("CFG", false, () -> {
            if (client != null) client.setScreen(new ClientSettingsScreen());
        }));

        sidebar.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        FlowLayout enabledCount = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        enabledCount.horizontalAlignment(HorizontalAlignment.CENTER);
        int enabled = getEnabledCount();
        enabledCount.child(Components.label(Text.literal(String.valueOf(enabled)))
            .color(Color.ofArgb(VIOLET)));
        enabledCount.child(Components.label(Text.literal("on"))
            .color(Color.ofArgb(TEXT_F)));
        sidebar.child(enabledCount);

        body.child(sidebar);

        FlowLayout gridWrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        gridWrapper.surface(Surface.flat(SURF2));
        gridWrapper.padding(Insets.of(6));

        moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        moduleGridContainer.gap(4);
        rebuildGrid();

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), moduleGridContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        gridWrapper.child(scroll);

        body.child(gridWrapper);
        return body;
    }

    private int getEnabledCount() {
        int count = 0;
        for (Object[] mod : MODS) {
            int idx = java.util.List.of(MODS).indexOf(mod);
            if (isEnabled(idx)) count++;
        }
        return count;
    }

    private FlowLayout makeSidebarIcon(String label, boolean active, Runnable action) {
        FlowLayout icon = Containers.verticalFlow(Sizing.fixed(48), Sizing.fixed(40));
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

    // ── Module grid ───────────────────────────────────────────────────────────

    private void rebuildGrid() {
        moduleGridContainer.clearChildren();
        FlowLayout row = null;
        int col = 0;

        for (int i = 0; i < MODS.length; i++) {
            String name = (String) MODS[i][0];
            String cat = (String) MODS[i][1];
            String badge = (String) MODS[i][2];
            int bClr = (int) MODS[i][3];
            String desc = (String) MODS[i][4];
            boolean en = isEnabled(i);

            if (selCat != 0 && !cat.equals(CATS[selCat])) continue;
            if (!searchQuery.isEmpty() && !name.toLowerCase().contains(searchQuery) && !desc.toLowerCase().contains(searchQuery)) continue;

            if (col == 0) {
                row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                row.gap(4);
                moduleGridContainer.child(row);
            }

            FlowLayout card = buildCard(name, badge, bClr, desc, en, i);
            if (row != null) row.child(card);

            col++;
            if (col >= COLS) col = 0;
        }

        if (moduleGridContainer.children().isEmpty()) {
            FlowLayout empty = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(40));
            empty.verticalAlignment(VerticalAlignment.CENTER);
            empty.horizontalAlignment(HorizontalAlignment.CENTER);
            empty.child(Components.label(Text.literal("No modules found")).color(Color.ofArgb(TEXT_F)));
            moduleGridContainer.child(empty);
        }
    }

    private FlowLayout buildCard(String name, String badge, int bClr, String desc, boolean en, int modIndex) {
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(154), Sizing.fixed(72));

        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = en ? SURF3 : SURF;
            int border = en ? VIOLET : BORDER;
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, border);
            if (en) ctx.fill(x, y, x + 3, y + h, VIOLET);
        });
        card.padding(Insets.of(8, 10, 8, 10));
        card.gap(2);

        FlowLayout topRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        topRow.verticalAlignment(VerticalAlignment.CENTER);
        topRow.gap(4);

        FlowLayout badgeBox = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(12));
        badgeBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, (bClr & 0x00FFFFFF) | 0x44000000);
        });
        badgeBox.padding(Insets.of(0, 4, 0, 4));
        badgeBox.child(Components.label(Text.literal(badge)).color(Color.ofArgb(bClr)));
        topRow.child(badgeBox);

        topRow.child(Components.label(Text.literal(name))
            .color(Color.ofArgb(TEXT))
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));
        card.child(topRow);

        FlowLayout descRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        descRow.child(Components.label(Text.literal(desc))
            .color(Color.ofArgb(TEXT_F))
            .sizing(Sizing.fill(100), Sizing.content()));
        card.child(descRow);

        FlowLayout divider = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
        divider.surface(Surface.flat(BORDER));
        card.child(divider);

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout statusDot = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        statusDot.gap(2);
        statusDot.child(Components.label(Text.literal("+"))
            .color(Color.ofArgb(en ? GREEN : TEXT_F)));
        statusDot.child(Components.label(Text.literal(en ? "ON" : "OFF"))
            .color(Color.ofArgb(en ? GREEN : TEXT_F)));
        bottomRow.child(statusDot);

        bottomRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        final boolean[] state = {en};
        ButtonComponent toggleBtn = Components.button(Text.literal(""), btn -> {
            handleModClick(modIndex);
            rebuildGrid();
        });
        toggleBtn.sizing(Sizing.fixed(32), Sizing.fixed(14));
        toggleBtn.renderer((ctx, comp, delta) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            boolean curEn = isEnabled(modIndex);
            int trackBg = curEn ? GREEN_D : SURF3;
            ctx.fill(x, y, x + w, y + h, trackBg);
            ctx.drawBorder(x, y, w, h, curEn ? GREEN : BORDER_S);
            int knobX = curEn ? x + w - 10 : x + 2;
            ctx.fill(knobX, y + 2, knobX + 8, y + h - 2, curEn ? GREEN : TEXT_F);
        });
        bottomRow.child(toggleBtn);

        card.child(bottomRow);

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

    private void stylePill(ButtonComponent pill, boolean selected) {
        if (selected) {
            pill.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET_S, VIOLET_D));
        } else {
            pill.renderer(ButtonComponent.Renderer.flat(SURF, SURF3, SURF));
        }
    }

    private void refreshPills() {
        for (int i = 0; i < catPills.size(); i++) {
            stylePill(catPills.get(i), i == selCat);
        }
    }

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
            case 16 -> ModConfig.showNametag;
            case 17 -> ModConfig.showChatColors;
            case 18 -> ModConfig.showItemTooltips;
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
            case 16 -> ModConfig.showNametag       = !ModConfig.showNametag;
            case 17 -> ModConfig.showChatColors    = !ModConfig.showChatColors;
            case 18 -> ModConfig.showItemTooltips  = !ModConfig.showItemTooltips;
        }
        ModConfig.saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.world == null) {
            this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
            context.fill(0, 0, this.width, this.height, 0xCC08080A);
        } else {
            context.fill(0, 0, this.width, this.height, 0xCC08080A);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
