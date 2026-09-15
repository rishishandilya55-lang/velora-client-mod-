package com.velora.client.gui;

import com.velora.client.config.ModConfig;
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

public class ModMenuScreen extends AnimatedOwoScreen {

    // ── Ultra-Compact Layout constants ───────────────────────────────
    private static final int SIDEBAR_W  = 100;
    private static final int PANEL_W    = 450;
    private static final int PANEL_H    = 310;
    private static final int TILE_W     = 76;
    private static final int TILE_H     = 68;
    private static final int TILE_GAP   = 6;

    // ── Module definitions: {name, category, desc} ──────────────────
    private static final Object[][] MODS = {
        {"FPS Display",   "HUD",      "Frames per second overlay"},
        {"WASD Keys",     "HUD",      "Movement key presses and CPS"},
        {"Ping Display",  "HUD",      "Real-time server latency"},
        {"CPS Counter",   "HUD",      "Clicks per second (L/R)"},
        {"Armor Status",  "HUD",      "Armor durability on screen"},
        {"Coordinates",   "HUD",      "Player position and biome"},
        {"Day Counter",   "HUD",      "World day and time"},
        {"Block Info",    "HUD",      "Targeted block info"},
        {"Toggle Sprint", "Movement", "Auto-sprint toggle"},
        {"Toggle Sneak",  "Movement", "Auto-sneak toggle"},
        {"Zoom Mod",      "Movement", "Smooth camera zoom (C)"},
        {"Free Look",     "Movement", "Freelook camera (V)"},
        {"Snap Look",     "Movement", "Quick look back (B)"},
        {"Fullbright",    "Visual",   "Max world brightness (F6)"},
        {"No Hurt Cam",   "Visual",   "Disable damage shake"},
        {"Minimap",       "Visual",   "Mini radar map overlay"},
        {"Waypoints",     "Visual",   "In-world markers"},
        {"Chat Colors",   "HUD",      "Rank and chat highlights"},
        {"Item Tooltips", "HUD",      "Item stats tooltips"},
        {"Hit Color",     "Visual",   "Custom damage flash"},
        {"Item Physics",  "Visual",   "3D dropped item physics"},
        {"Potion Status", "HUD",      "Potion effects and timers"},
        {"Crosshair",     "Visual",   "Custom crosshair studio"},
        {"Item Model",    "Visual",   "3D held item transforms"},
    };

    // ── State ─────────────────────────────────────────────────────
    private int     activeTopTab          = 0;
    private String  searchQuery           = "";

    private FlowLayout moduleGridContainer;

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

        FlowLayout panel = Containers.horizontalFlow(Sizing.fixed(PANEL_W), Sizing.fixed(PANEL_H));
        panel.surface((ctx, comp) -> {
            VeloraRenderUtil.drawSmoothWindow(ctx, comp.x(), comp.y(), comp.width(), comp.height());
        });
        panel.padding(Insets.none());
        
        panel.child(buildSidebar(root, PANEL_H));
        panel.child(buildMainContent(root, PANEL_W - SIDEBAR_W, PANEL_H));
        
        root.child(panel);
    }

    private FlowLayout buildSidebar(FlowLayout root, int panelH) {
        FlowLayout sidebar = Containers.verticalFlow(Sizing.fixed(SIDEBAR_W), Sizing.fixed(panelH));
        sidebar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x + w - 1, y, x + w, y + h, 0x1AFFFFFF);
        });
        sidebar.padding(Insets.of(12, 8, 12, 8));
        sidebar.horizontalAlignment(HorizontalAlignment.LEFT);
        sidebar.verticalAlignment(VerticalAlignment.TOP);
        sidebar.gap(5);

        // Sidebar Navigation items
        sidebar.child(makeSidebarNav("Mods",      activeTopTab == 0, () -> { activeTopTab = 0; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarNav("Settings",  activeTopTab == 1, () -> { activeTopTab = 1; root.clearChildren(); build(root); }));
        sidebar.child(makeSidebarNav("Configs",   activeTopTab == 2, () -> { activeTopTab = 2; root.clearChildren(); build(root); }));
        
        sidebar.child(Containers.verticalFlow(Sizing.fixed(1), Sizing.expand(1)));
        
        sidebar.child(makeSidebarNav("Edit HUD",  false, () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));

        return sidebar;
    }

    private ButtonComponent makeSidebarNav(String text, boolean active, Runnable onClick) {
        ButtonComponent btn = Components.button(Text.empty(), b -> onClick.run());
        btn.sizing(Sizing.fill(100), Sizing.fixed(22));
        btn.renderer((ctx, button, delta) -> {
            int x = button.x(), y = button.y(), w = button.width(), h = button.height();
            VeloraRenderUtil.drawSmoothButton(ctx, x, y, w, h, active);
            int textCol = active ? 0xFF000000 : 0x88FFFFFF;
            ctx.drawText(client.textRenderer, text, x + (w - client.textRenderer.getWidth(text)) / 2, y + (h - 8) / 2, textCol, false);
        });
        return btn;
    }

    private FlowLayout buildMainContent(FlowLayout root, int contentW, int contentH) {
        FlowLayout content = Containers.verticalFlow(Sizing.fixed(contentW), Sizing.fixed(contentH));
        content.horizontalAlignment(HorizontalAlignment.LEFT);
        content.verticalAlignment(VerticalAlignment.TOP);
        
        // Header
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 12, 0, 12));

        if (activeTopTab == 0) {
            header.child(Components.label(Text.literal("Mods")).color(Color.ofArgb(0xFFFFFFFF)).shadow(false));
            
            // Spacer pushes search box to the far right!
            header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));
            
            // Far-Right Custom Search Input Box
            FlowLayout searchBox = Containers.horizontalFlow(Sizing.fixed(130), Sizing.fixed(20));
            searchBox.verticalAlignment(VerticalAlignment.CENTER);
            searchBox.padding(Insets.of(0, 6, 0, 6));
            searchBox.surface((ctx, comp) -> {
                VeloraRenderUtil.drawSmoothSearch(ctx, comp.x(), comp.y(), comp.width(), comp.height());
            });
            TextBoxComponent searchInput = Components.textBox(Sizing.fill(100), searchQuery);
            searchInput.setDrawsBackground(false);
            searchInput.sizing(Sizing.fill(100), Sizing.fixed(12));
            searchInput.setMaxLength(32);
            searchInput.onChanged().subscribe(val -> { searchQuery = val.toLowerCase().trim(); rebuildGrid(); });
            searchBox.child(searchInput);
            header.child(searchBox);
        } else if (activeTopTab == 1) {
            header.child(Components.label(Text.literal("Settings")).color(Color.ofArgb(0xFFFFFFFF)).shadow(false));
        } else {
            header.child(Components.label(Text.literal("Configs")).color(Color.ofArgb(0xFFFFFFFF)).shadow(false));
        }
        
        content.child(header);

        // Body area
        FlowLayout bodyArea = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(contentH - 32));
        bodyArea.padding(Insets.of(2, 10, 10, 10));
        
        if (activeTopTab == 0) {
            moduleGridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            moduleGridContainer.gap(TILE_GAP);
            moduleGridContainer.horizontalAlignment(HorizontalAlignment.CENTER);
            rebuildGrid(); 
            ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), moduleGridContainer);
            scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0x44FFFFFF)));
            bodyArea.child(scroll);
        } else if (activeTopTab == 1) {
            bodyArea.child(buildSettingsView(contentH - 32 - 20));
        }
        
        content.child(bodyArea);
        return content;
    }

    private io.wispforest.owo.ui.core.Component buildSettingsView(int availH) {
        FlowLayout list = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        list.horizontalAlignment(HorizontalAlignment.LEFT);
        list.padding(Insets.of(0, 0, 16, 0));
        list.gap(5);

        list.child(makeSectionHeader("Performance"));
        list.child(makeSettingToggle("Fast Math",       "Optimised trig calculations for better frame rates",   ModConfig.optiFastMath,       () -> { ModConfig.optiFastMath       = !ModConfig.optiFastMath;       ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Entity Culling",  "Skip rendering hidden entities behind walls",          ModConfig.optiEntityCulling,  () -> { ModConfig.optiEntityCulling  = !ModConfig.optiEntityCulling;  ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Low Memory Mode", "Reduce allocation and garbage-collection overhead",    ModConfig.optiLowMemoryMode,  () -> { ModConfig.optiLowMemoryMode  = !ModConfig.optiLowMemoryMode;  ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Disable Fog",     "Remove terrain distance fog for clear sightlines",    ModConfig.optiDisableFog,     () -> { ModConfig.optiDisableFog     = !ModConfig.optiDisableFog;     ModConfig.saveConfig(); }));
        list.child(makeSettingToggle("Limit Particles", "Cap maximum simultaneous particles on screen",        ModConfig.optiLimitParticles, () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); }));

        list.child(makeSectionHeader("Visual"));
        list.child(makeSettingToggle("Fullbright (F6)", "Maximum gamma and illumination everywhere",           ModConfig.showFullbright,     () -> { ModConfig.showFullbright     = !ModConfig.showFullbright;     ModConfig.saveConfig(); }));

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fixed(availH), list);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        return scroll;
    }

    private void rebuildGrid() {
        if (moduleGridContainer == null) return;
        moduleGridContainer.clearChildren();

        List<Integer> visible = new ArrayList<>();
        for (int i = 0; i < MODS.length; i++) {
            String name = (String) MODS[i][0];
            String cat  = (String) MODS[i][1];
            String desc = (String) MODS[i][2];
            if (!searchQuery.isEmpty()) {
                if (!name.toLowerCase().contains(searchQuery) &&
                    !desc.toLowerCase().contains(searchQuery) &&
                    !cat.toLowerCase().contains(searchQuery)) continue;
            }
            visible.add(i);
        }

        int cols = 4;
        FlowLayout currentRow = null;
        for (int k = 0; k < visible.size(); k++) {
            if (k % cols == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.horizontalAlignment(HorizontalAlignment.CENTER);
                currentRow.gap(TILE_GAP);
                moduleGridContainer.child(currentRow);
            }
            if (currentRow != null) currentRow.child(buildModTile(visible.get(k)));
        }
    }

    private FlowLayout buildModTile(int modIndex) {
        Object[] def  = MODS[modIndex];
        String name   = (String) def[0];
        final boolean[] en = { isEnabled(modIndex) };
        final boolean[] hovered = { false };
        Identifier iconId = getModIconIdentifier(name);

        FlowLayout tile = Containers.verticalFlow(Sizing.fixed(TILE_W), Sizing.fixed(TILE_H));
        tile.verticalAlignment(VerticalAlignment.TOP);
        tile.horizontalAlignment(HorizontalAlignment.CENTER);
        tile.padding(Insets.of(5, 2, 8, 2));

        tile.surface((ctx, comp) -> {
            VeloraRenderUtil.drawSmoothCard(ctx, comp.x(), comp.y(), comp.width(), comp.height(), hovered[0]);
        });

        // 1. TOP: Module Title Text Label (Centered)
        LabelComponent nameLbl = Components.label(Text.literal(name));
        nameLbl.color(Color.ofArgb(0xFFFFFFFF));
        nameLbl.shadow(false);
        tile.child(nameLbl);

        // Gap 1: Exactly 7px fixed vertical gap
        tile.child(Containers.verticalFlow(Sizing.fixed(1), Sizing.fixed(7)));

        // 2. MIDDLE: Mod's PNG Logo Icon (20x20px, centered)
        FlowLayout iconBox = Containers.horizontalFlow(Sizing.fixed(20), Sizing.fixed(20));
        iconBox.surface((ctx, comp) -> {
            ctx.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, iconId, comp.x(), comp.y(), 0f, 0f, 20, 20, 20, 20);
        });
        tile.child(iconBox);

        // Gap 2: Exactly 7px fixed vertical gap
        tile.child(Containers.verticalFlow(Sizing.fixed(1), Sizing.fixed(7)));

        // 3. BOTTOM: Black PNG Pill Toggle Switch (22x11px, centered)
        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(22), Sizing.fixed(11));
        toggle.surface((ctx, comp) -> {
            VeloraRenderUtil.drawPillPng(ctx, comp.x(), comp.y(), comp.width(), comp.height(), en[0]);
        });
        tile.child(toggle);

        tile.mouseEnter().subscribe(() -> hovered[0] = true);
        tile.mouseLeave().subscribe(() -> hovered[0] = false);

        tile.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                en[0] = !en[0];
                toggleMod(modIndex);
                return true;
            } else if (btn == 1) {
                openModSettings(name);
                return true;
            }
            return false;
        });
        return tile;
    }

    private static Identifier getModIconIdentifier(String modName) {
        String filename;
        switch (modName) {
            case "FPS Display":   filename = "fps.png"; break;
            case "WASD Keys":     filename = "keystrokes.png"; break;
            case "Ping Display":  filename = "ping.png"; break;
            case "CPS Counter":   filename = "cps.png"; break;
            case "Armor Status":  filename = "armor_status.png"; break;
            case "Coordinates":   filename = "coordinates.png"; break;
            case "Day Counter":   filename = "day_counter.png"; break;
            case "Block Info":    filename = "blockinfo.png"; break;
            case "Toggle Sprint": filename = "togglesprint.png"; break;
            case "Toggle Sneak":  filename = "togglesneak.png"; break;
            case "Zoom Mod":      filename = "zoom.png"; break;
            case "Free Look":     filename = "freelook.png"; break;
            case "Snap Look":     filename = "snaplook.png"; break;
            case "Fullbright":    filename = "fullbright.png"; break;
            case "No Hurt Cam":   filename = "nohurtcam.png"; break;
            case "Minimap":       filename = "minimap.png"; break;
            case "Waypoints":     filename = "waypoints.png"; break;
            case "Chat Colors":   filename = "chat.png"; break;
            case "Item Tooltips": filename = "scrolltooltips.png"; break;
            case "Hit Color":     filename = "hit_color.png"; break;
            case "Item Physics":  filename = "itemphysics.png"; break;
            case "Potion Status": filename = "potion_status.png"; break;
            case "Crosshair":     filename = "crosshair.png"; break;
            case "Item Model":    filename = "itemmodel.png"; break;
            default:              filename = "logo.png"; break;
        }
        return Identifier.of("velora", "textures/gui/" + filename);
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(2, 2, 2, 2));
        header.gap(5);
        header.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(VeloraColors.VIOLET)));
        FlowLayout line = Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1));
        line.surface(Surface.flat(VeloraColors.DIVIDER));
        header.child(line);
        return header;
    }

    private FlowLayout makeSettingToggle(String label, String desc, boolean enabled, Runnable action) {
        final boolean[] state = { enabled };
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, state[0] ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG);
            if (state[0]) ctx.fill(x, y, x + 2, y + h, VeloraColors.VIOLET);
        });
        row.padding(Insets.of(0, 6, 0, 6));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(5);

        FlowLayout textCol = Containers.verticalFlow(Sizing.content(), Sizing.content());
        textCol.gap(1);
        textCol.child(Components.label(Text.literal(label)).color(Color.ofArgb(VeloraColors.TEXT)));
        textCol.child(Components.label(Text.literal(desc)).color(Color.ofArgb(VeloraColors.TEXT_DIM)));
        row.child(textCol);
        row.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(22), Sizing.fixed(10));
        toggle.surface((ctx, comp) -> VeloraRenderUtil.drawToggleSwitch(ctx, comp.x(), comp.y(), comp.width(), comp.height(), state[0]));
        row.child(toggle);
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { state[0] = !state[0]; action.run(); return true; } return false; });
        return row;
    }

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
        }
        ModConfig.saveConfig();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world == null) {
            this.panoramaRenderer.render(context, this.width, this.height, getAnimationProgress(), delta);
            context.fillGradient(0, 0, this.width, this.height, getFadeColor(0xD8090A0F), getFadeColor(0xF2090A0F));
        } else {
            context.fillGradient(0, 0, this.width, this.height, getFadeColor(0x88000000), getFadeColor(0xAA000000));
        }
    }
}
