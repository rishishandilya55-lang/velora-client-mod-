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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public class ModuleSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private final String moduleName;
    private final @Nullable Screen parent;
    private boolean listeningForKey = false;
    private FlowLayout settingsPanel;
    private String itemSearchQuery = "";

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
    private static final int RED      = 0xFFEF4444;

    public ModuleSettingsScreen(String moduleName) {
        this(moduleName, null);
    }

    public ModuleSettingsScreen(String moduleName, @Nullable Screen parent) {
        super(Text.literal(moduleName + " Settings"));
        this.moduleName = moduleName;
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0xAA000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(420), Sizing.fixed(320));
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        panel.padding(Insets.none());

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        header.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x, y + h - 1, x + w, y + h, BORDER_S);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 12, 0, 12));
        header.gap(6);

        header.child(Components.label(Text.literal(moduleName))
            .color(Color.ofArgb(VIOLET))
            .shadow(true));
        header.child(Components.label(Text.literal("Settings"))
            .color(Color.ofArgb(TEXT))
            .shadow(true));
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        header.child(closeBtn);

        panel.child(header);

        settingsPanel = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        settingsPanel.padding(Insets.of(8, 14, 60, 14));
        settingsPanel.gap(3);
        buildSettings();

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), settingsPanel);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        panel.child(scroll);

        root.child(panel);
    }

    private void buildSettings() {
        settingsPanel.clearChildren();

        switch (moduleName) {
            case "FPS Display" -> buildFpsSettings();
            case "WASD Keys" -> buildKeystrokesSettings();
            case "Ping Display" -> buildPingSettings();
            case "CPS Counter" -> buildCpsSettings();
            case "Armor Status" -> buildArmorSettings();
            case "Coordinates" -> buildCoordinatesSettings();
            case "Day Counter" -> buildDayCounterSettings();
            case "Block Info" -> buildBlockInfoSettings();
            case "Toggle Sprint" -> buildToggleSprintSettings();
            case "Toggle Sneak" -> buildToggleSneakSettings();
            case "Zoom Mod" -> buildZoomSettings();
            case "Free Look" -> buildFreeLookSettings();
            case "Snap Look" -> buildSnapLookSettings();
            case "Fullbright" -> buildFullbrightSettings();
            case "No Hurt Cam", "NoHurtCam" -> buildHurtCamSettings();
            case "Minimap" -> buildMinimapSettings();
            case "Capes & Physics" -> buildCapeSettings();
            case "Waypoints", "Waypoint", "WaypointsMod" -> buildWaypointsSettings();
            case "Chat Colors" -> buildChatColorSettings();
            case "Item Tooltips" -> buildItemTooltipSettings();
            case "Item Physics", "ItemPhysics" -> buildItemPhysicsSettings();
            case "Hit Color", "HitColor" -> buildHitColorSettings();
            case "Potion Status", "Potion HUD", "PotionHud" -> buildPotionSettings();
            case "Crosshair", "Custom Crosshair", "Crosshair Mod" -> buildCrosshairSettings();
            case "Item Model", "ItemModel", "View Model", "ViewModel", "View Model Mod", "Item Scale" -> buildViewModelSettings();
            default -> buildGenericSettings();
        }
    }

    private void buildFpsSettings() {
        settingsPanel.child(makeSectionHeader("FPS Display"));
        settingsPanel.child(makeToggleRow("Enable FPS", "Show current frames per second on screen",
            ModConfig.showFps,
            () -> { ModConfig.showFps = !ModConfig.showFps; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Prefix", "Display 'FPS: 144' instead of '144 FPS'",
            ModConfig.fpsShowPrefix,
            () -> { ModConfig.fpsShowPrefix = !ModConfig.fpsShowPrefix; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.fpsBackground,
            () -> { ModConfig.fpsBackground = !ModConfig.fpsBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("Text Color", ModConfig.fpsTextColor, ModConfig.fpsTextRainbow,
            c -> ModConfig.fpsTextColor = c,
            () -> ModConfig.fpsTextRainbow = !ModConfig.fpsTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildKeystrokesSettings() {
        settingsPanel.child(makeSectionHeader("Keystrokes"));
        settingsPanel.child(makeToggleRow("Enable Keystrokes", "Display WASD key presses on HUD",
            ModConfig.showKeystrokes,
            () -> { ModConfig.showKeystrokes = !ModConfig.showKeystrokes; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Mouse Buttons", "Show LMB and RMB in keystrokes display",
            ModConfig.showMouseStrokes,
            () -> { ModConfig.showMouseStrokes = !ModConfig.showMouseStrokes; ModConfig.saveConfig(); buildSettings(); }));
        int opacityPercent = (int) Math.round((ModConfig.keystrokesOpacity / 255.0) * 100);
        settingsPanel.child(makeCycleRow("Key Opacity", opacityPercent + "%",
            () -> {
                if (ModConfig.keystrokesOpacity <= 0x40) ModConfig.keystrokesOpacity = 0x80;
                else if (ModConfig.keystrokesOpacity <= 0x80) ModConfig.keystrokesOpacity = 0xC0;
                else if (ModConfig.keystrokesOpacity <= 0xC0) ModConfig.keystrokesOpacity = 0xFF;
                else ModConfig.keystrokesOpacity = 0x40;
                ModConfig.saveConfig(); buildSettings();
            }));
        settingsPanel.child(makeColorPickerRow("Key Text Color", ModConfig.keystrokesTextColor, ModConfig.keystrokesRainbow,
            c -> ModConfig.keystrokesTextColor = c,
            () -> ModConfig.keystrokesRainbow = !ModConfig.keystrokesRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildPingSettings() {
        settingsPanel.child(makeSectionHeader("Ping Display"));
        settingsPanel.child(makeToggleRow("Enable Ping", "Show network latency (ms) on screen",
            ModConfig.showPing,
            () -> { ModConfig.showPing = !ModConfig.showPing; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Custom Text Color", "Use custom text color instead of ping quality colors",
            ModConfig.pingCustomColor,
            () -> { ModConfig.pingCustomColor = !ModConfig.pingCustomColor; ModConfig.saveConfig(); buildSettings(); }));
        if (ModConfig.pingCustomColor) {
            settingsPanel.child(makeColorPickerRow("Ping Color", ModConfig.pingTextColor, ModConfig.pingTextRainbow,
                c -> ModConfig.pingTextColor = c,
                () -> ModConfig.pingTextRainbow = !ModConfig.pingTextRainbow));
        }
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.pingBackground,
            () -> { ModConfig.pingBackground = !ModConfig.pingBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildCpsSettings() {
        settingsPanel.child(makeSectionHeader("CPS Counter"));
        settingsPanel.child(makeToggleRow("Enable CPS Counter", "Show clicks per second on HUD",
            ModConfig.showCps,
            () -> { ModConfig.showCps = !ModConfig.showCps; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Right-Click CPS", "Also count right-clicks per second",
            ModConfig.showRightCps,
            () -> { ModConfig.showRightCps = !ModConfig.showRightCps; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.cpsBackground,
            () -> { ModConfig.cpsBackground = !ModConfig.cpsBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("CPS Text Color", ModConfig.cpsTextColor, ModConfig.cpsTextRainbow,
            c -> ModConfig.cpsTextColor = c,
            () -> ModConfig.cpsTextRainbow = !ModConfig.cpsTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildArmorSettings() {
        settingsPanel.child(makeSectionHeader("Display"));
        settingsPanel.child(makeToggleRow("Enable Armor Status", "Show armor pieces and durability on HUD",
            ModConfig.showArmorStatus,
            () -> { ModConfig.showArmorStatus = !ModConfig.showArmorStatus; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeCycleRow("Orientation", ModConfig.armorOrientation,
            () -> {
                ModConfig.armorOrientation = "VERTICAL".equalsIgnoreCase(ModConfig.armorOrientation) ? "HORIZONTAL" : "VERTICAL";
                ModConfig.saveConfig(); buildSettings();
            }));
        settingsPanel.child(makeCycleRow("Background", ModConfig.armorBackgroundStyle,
            () -> {
                if ("MODERN".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "TRANSPARENT";
                else if ("TRANSPARENT".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "COMPACT";
                else ModConfig.armorBackgroundStyle = "MODERN";
                ModConfig.saveConfig(); buildSettings();
            }));
        settingsPanel.child(makeCycleRow("Durability", ModConfig.armorDurabilityMode,
            () -> {
                if ("MAX_VALUE".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "PERCENT";
                else if ("PERCENT".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "VALUE";
                else ModConfig.armorDurabilityMode = "MAX_VALUE";
                ModConfig.saveConfig(); buildSettings();
            }));

        settingsPanel.child(makeSectionHeader("Items"));
        settingsPanel.child(makeToggleRow("Show Offhand", "Display offhand item in armor HUD",
            ModConfig.armorShowOffhand,
            () -> { ModConfig.armorShowOffhand = !ModConfig.armorShowOffhand; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Item Count", "Show block/item counts in inventory",
            ModConfig.armorShowCount,
            () -> { ModConfig.armorShowCount = !ModConfig.armorShowCount; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildCoordinatesSettings() {
        settingsPanel.child(makeSectionHeader("Coordinates"));
        settingsPanel.child(makeToggleRow("Enable Coordinates", "Show current X, Y, Z position on screen",
            ModConfig.showCoordinates,
            () -> { ModConfig.showCoordinates = !ModConfig.showCoordinates; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Direction Facing", "Display compass heading (N/S/E/W)",
            ModConfig.coordsShowDirection,
            () -> { ModConfig.coordsShowDirection = !ModConfig.coordsShowDirection; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Nether Coordinates", "Show converted Nether X, Z position",
            ModConfig.coordsShowNether,
            () -> { ModConfig.coordsShowNether = !ModConfig.coordsShowNether; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Biome Name", "Show current biome on HUD",
            ModConfig.coordsShowBiome,
            () -> { ModConfig.coordsShowBiome = !ModConfig.coordsShowBiome; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.coordsBackground,
            () -> { ModConfig.coordsBackground = !ModConfig.coordsBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("Coords Color", ModConfig.coordsTextColor, ModConfig.coordsTextRainbow,
            c -> ModConfig.coordsTextColor = c,
            () -> ModConfig.coordsTextRainbow = !ModConfig.coordsTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildDayCounterSettings() {
        settingsPanel.child(makeSectionHeader("Day Counter"));
        settingsPanel.child(makeToggleRow("Enable Day Counter", "Display in-game world day count",
            ModConfig.showDayCounter,
            () -> { ModConfig.showDayCounter = !ModConfig.showDayCounter; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show 24h Clock", "Display current in-game time (HH:mm)",
            ModConfig.dayShowTime,
            () -> { ModConfig.dayShowTime = !ModConfig.dayShowTime; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.dayBackground,
            () -> { ModConfig.dayBackground = !ModConfig.dayBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("Day Text Color", ModConfig.dayTextColor, ModConfig.dayTextRainbow,
            c -> ModConfig.dayTextColor = c,
            () -> ModConfig.dayTextRainbow = !ModConfig.dayTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildBlockInfoSettings() {
        settingsPanel.child(makeSectionHeader("Block Info"));
        settingsPanel.child(makeToggleRow("Enable Block Info", "Show name and details of looked-at block",
            ModConfig.showBlockInfo,
            () -> { ModConfig.showBlockInfo = !ModConfig.showBlockInfo; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Harvest Tool", "Display recommended tool (Pickaxe/Axe/Shovel)",
            ModConfig.blockInfoShowTool,
            () -> { ModConfig.blockInfoShowTool = !ModConfig.blockInfoShowTool; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.blockInfoBackground,
            () -> { ModConfig.blockInfoBackground = !ModConfig.blockInfoBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("Block Info Color", ModConfig.blockInfoTextColor, ModConfig.blockInfoTextRainbow,
            c -> ModConfig.blockInfoTextColor = c,
            () -> ModConfig.blockInfoTextRainbow = !ModConfig.blockInfoTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildToggleSprintSettings() {
        settingsPanel.child(makeSectionHeader("Toggle Sprint"));
        settingsPanel.child(makeToggleRow("Enable Toggle Sprint", "Automatically keep sprinting without holding key",
            ModConfig.showToggleSprint,
            () -> { ModConfig.showToggleSprint = !ModConfig.showToggleSprint; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
        settingsPanel.child(makeHintRow("Shows [Sprinting (Toggled)] on screen."));
    }

    private void buildToggleSneakSettings() {
        settingsPanel.child(makeSectionHeader("Toggle Sneak"));
        settingsPanel.child(makeToggleRow("Enable Toggle Sneak", "Toggle sneak ON/OFF without holding shift key",
            ModConfig.showToggleSneak,
            () -> { ModConfig.showToggleSneak = !ModConfig.showToggleSneak; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Press sneak once to stay crouching; press again to stand."));
    }

    private void buildZoomSettings() {
        settingsPanel.child(makeSectionHeader("Zoom"));
        settingsPanel.child(makeToggleRow("Smooth Animation", "Smoothly transition zoom in/out",
            ModConfig.zoomSmooth,
            () -> { ModConfig.zoomSmooth = !ModConfig.zoomSmooth; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Scale Sensitivity", "Reduce mouse sensitivity while zoomed",
            ModConfig.zoomScaleSensitivity,
            () -> { ModConfig.zoomScaleSensitivity = !ModConfig.zoomScaleSensitivity; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Hold C to zoom. Scroll mouse wheel to adjust zoom level."));
    }

    private void buildFreeLookSettings() {
        settingsPanel.child(makeSectionHeader("Free Look"));
        settingsPanel.child(makeToggleRow("Enable Free Look", "Hold key for 360 camera rotation",
            ModConfig.showFreeLook,
            () -> { ModConfig.showFreeLook = !ModConfig.showFreeLook; ModConfig.saveConfig(); buildSettings(); }));

        String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.freeLookKey, 0);
        if (keyName == null) keyName = "Key " + ModConfig.freeLookKey;
        settingsPanel.child(makeKeybindRow("Keybind", keyName.toUpperCase(),
            listeningForKey ? GREEN : VIOLET,
            () -> { listeningForKey = true; buildSettings(); }));

        settingsPanel.child(makeHintRow("Hold this key to rotate camera freely."));
    }

    private void buildPotionSettings() {
        settingsPanel.child(makeSectionHeader("Potion Status HUD"));
        settingsPanel.child(makeToggleRow("Enable Potion HUD", "Display active status effects on screen",
            ModConfig.showPotionHud,
            () -> { ModConfig.showPotionHud = !ModConfig.showPotionHud; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Effect Icons", "Display potion icon next to effect name",
            ModConfig.potionHudShowIcon,
            () -> { ModConfig.potionHudShowIcon = !ModConfig.potionHudShowIcon; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Background Box", "Show semi-transparent background plate",
            ModConfig.potionHudBackground,
            () -> { ModConfig.potionHudBackground = !ModConfig.potionHudBackground; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeColorPickerRow("Text Color", ModConfig.potionHudTextColor, ModConfig.potionHudTextRainbow,
            c -> ModConfig.potionHudTextColor = c,
            () -> ModConfig.potionHudTextRainbow = !ModConfig.potionHudTextRainbow));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildSnapLookSettings() {
        settingsPanel.child(makeSectionHeader("Snap Look"));
        settingsPanel.child(makeToggleRow("Enable Snap Look", "Hold key for quick rear view",
            ModConfig.showSnapLook,
            () -> { ModConfig.showSnapLook = !ModConfig.showSnapLook; ModConfig.saveConfig(); buildSettings(); }));

        String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.snapLookKey, 0);
        if (keyName == null) keyName = "Key " + ModConfig.snapLookKey;
        settingsPanel.child(makeKeybindRow("Keybind", keyName.toUpperCase(),
            listeningForKey ? GREEN : VIOLET,
            () -> { listeningForKey = true; buildSettings(); }));

        settingsPanel.child(makeHintRow("Hold this key to instantly look behind."));
    }

    private void buildFullbrightSettings() {
        settingsPanel.child(makeSectionHeader("Fullbright"));
        settingsPanel.child(makeToggleRow("Enable Fullbright", "Set night and cave brightness to maximum",
            ModConfig.showFullbright,
            () -> { ModConfig.showFullbright = !ModConfig.showFullbright; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Shortcut: Press F6 in-game to quickly toggle fullbright."));
    }

    private void buildHurtCamSettings() {
        settingsPanel.child(makeSectionHeader("No Hurt Cam"));
        settingsPanel.child(makeToggleRow("Enable NoHurtCam", "Disable camera wobble when taking damage",
            ModConfig.showNoHurtCam,
            () -> { ModConfig.showNoHurtCam = !ModConfig.showNoHurtCam; ModConfig.saveConfig(); buildSettings(); }));

        String intensityText = ModConfig.hurtCamIntensity <= 0.0f ? "Off" :
            (int)(ModConfig.hurtCamIntensity * 100) + "%";
        settingsPanel.child(makeCycleRow("Wobble Intensity", intensityText,
            () -> {
                if (ModConfig.hurtCamIntensity <= 0.0f) ModConfig.hurtCamIntensity = 0.5f;
                else if (ModConfig.hurtCamIntensity <= 0.5f) ModConfig.hurtCamIntensity = 1.0f;
                else ModConfig.hurtCamIntensity = 0.0f;
                ModConfig.saveConfig(); buildSettings();
            }));
    }

    private void buildMinimapSettings() {
        settingsPanel.child(makeSectionHeader("Display"));
        settingsPanel.child(makeToggleRow("Enable Minimap", "Show radar minimap on HUD",
            ModConfig.showMinimap,
            () -> { ModConfig.showMinimap = !ModConfig.showMinimap; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeCycleRow("Shape", ModConfig.minimapShape,
            () -> {
                ModConfig.minimapShape = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape) ? "SQUARE" : "CIRCLE";
                ModConfig.saveConfig(); buildSettings();
            }));
        settingsPanel.child(makeToggleRow("Entity Radar", "Show mobs, players, and items on minimap",
            ModConfig.minimapShowEntities,
            () -> { ModConfig.minimapShowEntities = !ModConfig.minimapShowEntities; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Entity Names", "Show player names above blips",
            ModConfig.minimapShowEntityNames,
            () -> { ModConfig.minimapShowEntityNames = !ModConfig.minimapShowEntityNames; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeSectionHeader("Navigation"));
        settingsPanel.child(makeToggleRow("Rotate Map", "Rotate compass with player yaw",
            ModConfig.minimapRotateMap,
            () -> { ModConfig.minimapRotateMap = !ModConfig.minimapRotateMap; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Coordinates", "Display X/Y/Z below minimap",
            ModConfig.minimapShowCoordinates,
            () -> { ModConfig.minimapShowCoordinates = !ModConfig.minimapShowCoordinates; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Biome", "Display biome name and heading",
            ModConfig.minimapShowBiome,
            () -> { ModConfig.minimapShowBiome = !ModConfig.minimapShowBiome; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Waypoints", "Display waypoint markers and names on radar map",
            ModConfig.minimapShowWaypoints,
            () -> { ModConfig.minimapShowWaypoints = !ModConfig.minimapShowWaypoints; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeButtonRow("HUD Position & Scale", "Open HUD Editor",
            () -> { if (client != null) client.setScreen(new HudEditorScreen()); }));
    }

    private void buildCapeSettings() {
        settingsPanel.child(makeSectionHeader("Cape"));
        settingsPanel.child(makeToggleRow("Enable Cape", "Show custom Velora cape on your player",
            ModConfig.enableCape,
            () -> { ModConfig.enableCape = !ModConfig.enableCape; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Cape Physics", "Enable flowing cape animation",
            ModConfig.enableCapePhysics,
            () -> { ModConfig.enableCapePhysics = !ModConfig.enableCapePhysics; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeSectionHeader("Visibility"));
        settingsPanel.child(makeToggleRow("Local Only", "Only show cape to yourself",
            ModConfig.capeOnlyLocal,
            () -> { ModConfig.capeOnlyLocal = !ModConfig.capeOnlyLocal; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Override Default", "Replace Mojang/Microsoft capes",
            ModConfig.overrideDefaultCape,
            () -> { ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape; ModConfig.saveConfig(); buildSettings(); }));
    }

    private void buildWaypointsSettings() {
        settingsPanel.child(makeSectionHeader("Waypoints"));
        settingsPanel.child(makeToggleRow("Enable Waypoints", "Show in-world 3D waypoint markers",
            ModConfig.showWaypoints,
            () -> { ModConfig.showWaypoints = !ModConfig.showWaypoints; ModConfig.saveConfig(); }));
        settingsPanel.child(makeButtonRow("Waypoint Studio", "Open Waypoint Manager", () -> {
            if (this.client != null) this.client.setScreen(new WaypointManagerScreen(this));
        }));
        settingsPanel.child(makeButtonRow("Quick Mark", "+ New Waypoint Here", () -> {
            if (this.client != null) this.client.setScreen(new WaypointCreateScreen(this, null));
        }));

        settingsPanel.child(makeSectionHeader("Display Options"));
        settingsPanel.child(makeToggleRow("Vertical Beacon Beams", "Shoot bright vertical beacon beams into the sky at waypoint locations",
            ModConfig.waypointsBeaconBeams,
            () -> { ModConfig.waypointsBeaconBeams = !ModConfig.waypointsBeaconBeams; ModConfig.saveConfig(); }));
        settingsPanel.child(makeToggleRow("Show Distance", "Display distance in meters (e.g. [125m]) on waypoint tag",
            ModConfig.waypointsShowDistance,
            () -> { ModConfig.waypointsShowDistance = !ModConfig.waypointsShowDistance; ModConfig.saveConfig(); }));
        settingsPanel.child(makeToggleRow("Show on Minimap", "Display waypoint blip markers on radar minimap",
            ModConfig.minimapShowWaypoints,
            () -> { ModConfig.minimapShowWaypoints = !ModConfig.minimapShowWaypoints; ModConfig.saveConfig(); }));

        settingsPanel.child(makeSectionHeader("Keybind"));
        settingsPanel.child(makeButtonRow("Open Manager Shortcut", "Press 'U' Key In-Game", () -> {
            if (this.client != null) this.client.setScreen(new WaypointManagerScreen(this));
        }));

        settingsPanel.child(makeHintRow("Waypoints are isolated per server / singleplayer world and dimension automatically."));
    }

    private void buildChatColorSettings() {
        settingsPanel.child(makeSectionHeader("Chat Formatting"));
        settingsPanel.child(makeToggleRow("Chat Colors", "Enable rank-colored chat formatting",
            ModConfig.showChatColors,
            () -> { ModConfig.showChatColors = !ModConfig.showChatColors; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Chat Rank Prefix", "Show [Rank] prefix before player names in chat",
            ModConfig.showChatPrefix,
            () -> { ModConfig.showChatPrefix = !ModConfig.showChatPrefix; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Tab List Prefix", "Show [Rank] prefix in the tab player list",
            ModConfig.showTabListPrefix,
            () -> { ModConfig.showTabListPrefix = !ModConfig.showTabListPrefix; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Show Timestamps", "Display message send time [HH:mm]",
            ModConfig.chatShowTimestamp,
            () -> { ModConfig.chatShowTimestamp = !ModConfig.chatShowTimestamp; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeSectionHeader("Mention Highlights"));
        settingsPanel.child(makeToggleRow("Highlight Mentions", "Highlight your username when mentioned in chat",
            ModConfig.chatHighlightMentions,
            () -> { ModConfig.chatHighlightMentions = !ModConfig.chatHighlightMentions; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Mention Ping Sound", "Play notification chime on mention",
            ModConfig.chatMentionSound,
            () -> { ModConfig.chatMentionSound = !ModConfig.chatMentionSound; ModConfig.saveConfig(); buildSettings(); }));

        if (ModConfig.chatHighlightMentions) {
            String colorName = getMentionColorName();
            settingsPanel.child(makeCycleRow("Mention Preset", colorName, () -> {
                cycleMentionColor();
                ModConfig.saveConfig();
                buildSettings();
            }));
            settingsPanel.child(makeMentionColorWheelPicker());
        }

        settingsPanel.child(makeSectionHeader("Chat Background"));
        settingsPanel.child(makeToggleRow("Custom Background", "Customize chat window background color and opacity",
            ModConfig.customChatBackground,
            () -> { ModConfig.customChatBackground = !ModConfig.customChatBackground; ModConfig.saveConfig(); buildSettings(); }));

        if (ModConfig.customChatBackground) {
            int bgPercent = (int) Math.round((ModConfig.chatBackgroundOpacity / 255.0) * 100);
            settingsPanel.child(makeCycleRow("BG Opacity", bgPercent + "%", () -> {
                if (ModConfig.chatBackgroundOpacity <= 50) ModConfig.chatBackgroundOpacity = 100;
                else if (ModConfig.chatBackgroundOpacity <= 100) ModConfig.chatBackgroundOpacity = 160;
                else if (ModConfig.chatBackgroundOpacity <= 160) ModConfig.chatBackgroundOpacity = 220;
                else if (ModConfig.chatBackgroundOpacity <= 220) ModConfig.chatBackgroundOpacity = 255;
                else ModConfig.chatBackgroundOpacity = 0;
                ModConfig.saveConfig();
                buildSettings();
            }));
            settingsPanel.child(makeChatBgOpacitySlider());
            settingsPanel.child(makeChatBgColorPicker());
        }
    }

    private FlowLayout makeChatBgOpacitySlider() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(2);
        container.padding(Insets.of(0, 4, 0, 4));

        FlowLayout sliderBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        sliderBar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
            int baseRgb = ModConfig.chatBackgroundColor & 0x00FFFFFF;
            for (int col = 0; col < w; col++) {
                int a = (int) ((col / (float) Math.max(1, w)) * 255);
                ctx.fill(x + col, y, x + col + 1, y + h, (a << 24) | baseRgb);
            }
            int markerX = x + (int) ((ModConfig.chatBackgroundOpacity / 255.0f) * w);
            ctx.fill(markerX - 2, y - 2, markerX + 2, y + h + 2, 0xFFFFFFFF);
            ctx.fill(markerX - 1, y - 1, markerX + 1, y + h + 1, 0xFF000000);
        });

        sliderBar.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                int w = sliderBar.width();
                if (w > 0) {
                    float pct = Math.max(0.0f, Math.min(1f, (float) mx / (float) w));
                    ModConfig.chatBackgroundOpacity = Math.round(pct * 255f);
                    ModConfig.saveConfig();
                    buildSettings();
                }
                return true;
            }
            return false;
        });

        container.child(sliderBar);
        return container;
    }

    private FlowLayout makeChatBgColorPicker() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(2, 4, 2, 4));

        FlowLayout swatchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        swatchRow.verticalAlignment(VerticalAlignment.CENTER);
        swatchRow.gap(8);

        FlowLayout previewBox = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(16));
        previewBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int currentArgb = 0xFF000000 | (ModConfig.chatBackgroundColor & 0x00FFFFFF);
            ctx.fill(x, y, x + w, y + h, currentArgb);
            ctx.drawBorder(x, y, w, h, 0xFFFFFFFF);
        });
        swatchRow.child(previewBox);

        String hexCode = String.format("#%06X", ModConfig.chatBackgroundColor & 0x00FFFFFF);
        swatchRow.child(Components.label(Text.literal("BG Color: " + hexCode + " (Click / drag below)"))
            .color(Color.ofArgb(TEXT_M)));
        container.child(swatchRow);

        FlowLayout spectrumBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        spectrumBar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
            for (int col = 0; col < w; col++) {
                float hue = col / (float) Math.max(1, w);
                int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                ctx.fill(x + col, y, x + col + 1, y + h, 0xFF000000 | rgb);
            }
        });

        spectrumBar.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                int w = spectrumBar.width();
                if (w > 0) {
                    float hue = Math.max(0f, Math.min(1f, (float) mx / (float) w));
                    int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                    ModConfig.chatBackgroundColor = 0xFF000000 | (rgb & 0x00FFFFFF);
                    ModConfig.saveConfig();
                    buildSettings();
                }
                return true;
            }
            return false;
        });

        container.child(spectrumBar);
        return container;
    }

    private FlowLayout makeColorPickerRow(String label, int currentColor, boolean rainbow, Consumer<Integer> onColorChange, Runnable onRainbowToggle) {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(2, 4, 2, 4));

        String colorName = rainbow ? "Rainbow / Chroma" : com.velora.client.util.HudColorHelper.getColorName(currentColor);
        container.child(makeCycleRow(label, colorName, () -> {
            if (rainbow) {
                onRainbowToggle.run();
                onColorChange.accept(0xFFFFFFFF);
            } else {
                int next = com.velora.client.util.HudColorHelper.cycleColor(currentColor);
                if (next == 0xFFFFFFFF && (currentColor & 0x00FFFFFF) == 0x00AAAA) {
                    onRainbowToggle.run();
                } else {
                    onColorChange.accept(next);
                }
            }
            ModConfig.saveConfig();
            buildSettings();
        }));

        if (!rainbow) {
            FlowLayout swatchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            swatchRow.verticalAlignment(VerticalAlignment.CENTER);
            swatchRow.gap(8);

            FlowLayout previewBox = Containers.horizontalFlow(Sizing.fixed(24), Sizing.fixed(12));
            previewBox.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                int currentArgb = 0xFF000000 | (currentColor & 0x00FFFFFF);
                ctx.fill(x, y, x + w, y + h, currentArgb);
                ctx.drawBorder(x, y, w, h, 0xFFFFFFFF);
            });
            swatchRow.child(previewBox);

            String hexCode = String.format("#%06X", currentColor & 0x00FFFFFF);
            swatchRow.child(Components.label(Text.literal("Color: " + hexCode + " (Click spectrum below)"))
                .color(Color.ofArgb(TEXT_M)));
            container.child(swatchRow);

            FlowLayout spectrumBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            spectrumBar.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
                for (int col = 0; col < w; col++) {
                    float hue = col / (float) Math.max(1, w);
                    int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                    ctx.fill(x + col, y, x + col + 1, y + h, 0xFF000000 | rgb);
                }
            });

            spectrumBar.mouseDown().subscribe((mx, my, btn) -> {
                if (btn == 0) {
                    int w = spectrumBar.width();
                    if (w > 0) {
                        float hue = Math.max(0f, Math.min(1f, (float) mx / (float) w));
                        int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                        onColorChange.accept(0xFF000000 | (rgb & 0x00FFFFFF));
                        ModConfig.saveConfig();
                        buildSettings();
                    }
                    return true;
                }
                return false;
            });
            container.child(spectrumBar);
        }

        return container;
    }

    private FlowLayout makeMentionColorWheelPicker() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(2, 4, 2, 4));

        FlowLayout swatchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        swatchRow.verticalAlignment(VerticalAlignment.CENTER);
        swatchRow.gap(8);

        FlowLayout previewBox = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(16));
        previewBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int currentArgb = 0xFF000000 | (ModConfig.chatMentionColor & 0x00FFFFFF);
            ctx.fill(x, y, x + w, y + h, currentArgb);
            ctx.drawBorder(x, y, w, h, 0xFFFFFFFF);
        });
        swatchRow.child(previewBox);

        String hexCode = String.format("#%06X", ModConfig.chatMentionColor & 0x00FFFFFF);
        swatchRow.child(Components.label(Text.literal("Selected Color: " + hexCode + " (Click / drag below)"))
            .color(Color.ofArgb(TEXT_M)));
        container.child(swatchRow);

        FlowLayout spectrumBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        spectrumBar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
            for (int col = 0; col < w; col++) {
                float hue = col / (float) Math.max(1, w);
                int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                ctx.fill(x + col, y, x + col + 1, y + h, 0xFF000000 | rgb);
            }
        });

        spectrumBar.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                int w = spectrumBar.width();
                if (w > 0) {
                    float hue = Math.max(0f, Math.min(1f, (float) mx / (float) w));
                    int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                    ModConfig.chatMentionColor = 0xFF000000 | (rgb & 0x00FFFFFF);
                    ModConfig.saveConfig();
                    buildSettings();
                }
                return true;
            }
            return false;
        });

        container.child(spectrumBar);
        return container;
    }

    private String getMentionColorName() {
        int c = ModConfig.chatMentionColor & 0x00FFFFFF;
        if (c == 0xFFD700) return "Gold";
        if (c == 0xFF5555) return "Red";
        if (c == 0x55FF55) return "Lime";
        if (c == 0x55FFFF) return "Aqua";
        if (c == 0xFF55FF) return "Pink";
        if (c == 0xA78BFA) return "Purple";
        if (c == 0xFFAA00) return "Orange";
        if (c == 0xFFFFFF) return "White";
        return String.format("#%06X", c);
    }

    private void cycleMentionColor() {
        int c = ModConfig.chatMentionColor & 0x00FFFFFF;
        if (c == 0xFFD700) ModConfig.chatMentionColor = 0xFFFF5555;      // Red
        else if (c == 0xFF5555) ModConfig.chatMentionColor = 0xFF55FF55; // Lime
        else if (c == 0x55FF55) ModConfig.chatMentionColor = 0xFF55FFFF; // Aqua
        else if (c == 0x55FFFF) ModConfig.chatMentionColor = 0xFFFF55FF; // Pink
        else if (c == 0xFF55FF) ModConfig.chatMentionColor = 0xFFA78BFA; // Purple
        else if (c == 0xA78BFA) ModConfig.chatMentionColor = 0xFFFFAA00; // Orange
        else if (c == 0xFFAA00) ModConfig.chatMentionColor = 0xFFFFFFFF; // White
        else ModConfig.chatMentionColor = 0xFFFFD700;                    // Gold
    }

    private void buildItemTooltipSettings() {
        settingsPanel.child(makeSectionHeader("Tooltips"));
        settingsPanel.child(makeToggleRow("Enhanced Tooltips", "Show detailed info on item tooltips",
            ModConfig.showItemTooltips,
            () -> { ModConfig.showItemTooltips = !ModConfig.showItemTooltips; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Exact Durability", "Display current and max durability with percentage",
            ModConfig.tooltipShowDurability,
            () -> { ModConfig.tooltipShowDurability = !ModConfig.tooltipShowDurability; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Food Nutrition", "Display hunger points and saturation values",
            ModConfig.tooltipShowFood,
            () -> { ModConfig.tooltipShowFood = !ModConfig.tooltipShowFood; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Item ID", "Display minecraft:item registry identifier",
            ModConfig.tooltipShowId,
            () -> { ModConfig.tooltipShowId = !ModConfig.tooltipShowId; ModConfig.saveConfig(); buildSettings(); }));
    }

    private void buildItemPhysicsSettings() {
        settingsPanel.child(makeSectionHeader("Item Physics"));
        settingsPanel.child(makeToggleRow("Enable Item Physics", "Dropped items lay realistically flat on the ground",
            ModConfig.showItemPhysics,
            () -> { ModConfig.showItemPhysics = !ModConfig.showItemPhysics; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Items lay flat on blocks naturally rather than hovering and spinning."));
    }

    private void buildHitColorSettings() {
        settingsPanel.child(makeSectionHeader("Hit Color"));
        settingsPanel.child(makeToggleRow("Enable Hit Color", "Custom flash color when entities take damage",
            ModConfig.showHitColor,
            () -> {
                ModConfig.showHitColor = !ModConfig.showHitColor;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

        settingsPanel.child(makeToggleRow("Show on Armor", "Also flash custom color on equipped armor pieces",
            ModConfig.hitColorShowOnArmor,
            () -> {
                ModConfig.hitColorShowOnArmor = !ModConfig.hitColorShowOnArmor;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

        settingsPanel.child(makeToggleRow("Rainbow Mode", "Cycle dynamic RGB colors smoothly over time",
            ModConfig.hitColorRainbow,
            () -> {
                ModConfig.hitColorRainbow = !ModConfig.hitColorRainbow;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

        if (!ModConfig.hitColorRainbow) {
            settingsPanel.child(makeSectionHeader("Interactive Color Wheel"));
            settingsPanel.child(makeColorWheelPicker());

            settingsPanel.child(makeSectionHeader("Color Presets"));
            String currentPreset = getHitColorPresetName();
            settingsPanel.child(makeCycleRow("Preset", currentPreset, () -> {
                cycleHitColorPreset();
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

            settingsPanel.child(makeSectionHeader("Custom Channels"));
            settingsPanel.child(makeCycleRow("Red (R)", String.valueOf(ModConfig.hitColorRed), () -> {
                ModConfig.hitColorRed = (ModConfig.hitColorRed + 50) % 300;
                if (ModConfig.hitColorRed > 255) ModConfig.hitColorRed = 0;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

            settingsPanel.child(makeCycleRow("Green (G)", String.valueOf(ModConfig.hitColorGreen), () -> {
                ModConfig.hitColorGreen = (ModConfig.hitColorGreen + 50) % 300;
                if (ModConfig.hitColorGreen > 255) ModConfig.hitColorGreen = 0;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));

            settingsPanel.child(makeCycleRow("Blue (B)", String.valueOf(ModConfig.hitColorBlue), () -> {
                ModConfig.hitColorBlue = (ModConfig.hitColorBlue + 50) % 300;
                if (ModConfig.hitColorBlue > 255) ModConfig.hitColorBlue = 0;
                ModConfig.saveConfig();
                com.velora.client.client.HitColorMod.markDirty();
                buildSettings();
            }));
        }

        settingsPanel.child(makeSectionHeader("Opacity"));
        int alphaPercent = (int) Math.round((ModConfig.hitColorAlpha / 255.0) * 100);
        settingsPanel.child(makeCycleRow("Flash Opacity", alphaPercent + "%", () -> {
            if (ModConfig.hitColorAlpha <= 64) ModConfig.hitColorAlpha = 128;
            else if (ModConfig.hitColorAlpha <= 128) ModConfig.hitColorAlpha = 190;
            else if (ModConfig.hitColorAlpha <= 190) ModConfig.hitColorAlpha = 230;
            else if (ModConfig.hitColorAlpha <= 230) ModConfig.hitColorAlpha = 255;
            else ModConfig.hitColorAlpha = 64;
            ModConfig.saveConfig();
            com.velora.client.client.HitColorMod.markDirty();
            buildSettings();
        }));
        settingsPanel.child(makeOpacitySlider());

        settingsPanel.child(makeHintRow("Damage color is rendered on mobs, players, and armor."));
    }

    private FlowLayout makeOpacitySlider() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(2);
        container.padding(Insets.of(0, 4, 0, 4));

        FlowLayout sliderBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        sliderBar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
            int baseRgb = (ModConfig.hitColorRed << 16) | (ModConfig.hitColorGreen << 8) | ModConfig.hitColorBlue;
            for (int col = 0; col < w; col++) {
                int a = (int) ((col / (float) Math.max(1, w)) * 255);
                ctx.fill(x + col, y, x + col + 1, y + h, (a << 24) | baseRgb);
            }
            int markerX = x + (int) ((ModConfig.hitColorAlpha / 255.0f) * w);
            ctx.fill(markerX - 2, y - 2, markerX + 2, y + h + 2, 0xFFFFFFFF);
            ctx.fill(markerX - 1, y - 1, markerX + 1, y + h + 1, 0xFF000000);
        });

        sliderBar.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                int w = sliderBar.width();
                if (w > 0) {
                    float pct = Math.max(0.05f, Math.min(1f, (float) mx / (float) w));
                    ModConfig.hitColorAlpha = Math.round(pct * 255f);
                    ModConfig.saveConfig();
                    com.velora.client.client.HitColorMod.markDirty();
                    buildSettings();
                }
                return true;
            }
            return false;
        });

        container.child(sliderBar);
        return container;
    }

    private FlowLayout makeColorWheelPicker() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(2, 4, 2, 4));

        FlowLayout swatchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        swatchRow.verticalAlignment(VerticalAlignment.CENTER);
        swatchRow.gap(8);

        FlowLayout previewBox = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(16));
        previewBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int currentArgb = (ModConfig.hitColorAlpha << 24) | (ModConfig.hitColorRed << 16) | (ModConfig.hitColorGreen << 8) | ModConfig.hitColorBlue;
            ctx.fill(x, y, x + w, y + h, currentArgb);
            ctx.drawBorder(x, y, w, h, 0xFFFFFFFF);
        });
        swatchRow.child(previewBox);

        String hexCode = String.format("#%02X%02X%02X", ModConfig.hitColorRed, ModConfig.hitColorGreen, ModConfig.hitColorBlue);
        swatchRow.child(Components.label(Text.literal("Selected: " + hexCode + "  (Click / Drag on spectrum below)"))
            .color(Color.ofArgb(TEXT_M)));
        container.child(swatchRow);

        FlowLayout spectrumBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        spectrumBar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, BORDER_S);
            for (int col = 0; col < w; col++) {
                float hue = col / (float) Math.max(1, w);
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                ctx.fill(x + col, y, x + col + 1, y + h, 0xFF000000 | rgb);
            }
            float[] hsb = java.awt.Color.RGBtoHSB(ModConfig.hitColorRed, ModConfig.hitColorGreen, ModConfig.hitColorBlue, null);
            int markerX = x + (int) (hsb[0] * w);
            ctx.fill(markerX - 2, y - 2, markerX + 2, y + h + 2, 0xFFFFFFFF);
            ctx.fill(markerX - 1, y - 1, markerX + 1, y + h + 1, 0xFF000000);
        });

        spectrumBar.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                int w = spectrumBar.width();
                if (w > 0) {
                    float hue = Math.max(0f, Math.min(1f, (float) mx / (float) w));
                    int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    ModConfig.hitColorRed = (rgb >> 16) & 0xFF;
                    ModConfig.hitColorGreen = (rgb >> 8) & 0xFF;
                    ModConfig.hitColorBlue = rgb & 0xFF;
                    ModConfig.saveConfig();
                    com.velora.client.client.HitColorMod.markDirty();
                    buildSettings();
                }
                return true;
            }
            return false;
        });

        container.child(spectrumBar);
        return container;
    }

    private String getHitColorPresetName() {
        int r = ModConfig.hitColorRed;
        int g = ModConfig.hitColorGreen;
        int b = ModConfig.hitColorBlue;

        if (r == 255 && g == 0 && b == 0) return "Red";
        if (r == 255 && g == 100 && b == 0) return "Orange";
        if (r == 255 && g == 255 && b == 0) return "Yellow";
        if (r == 0 && g == 255 && b == 0) return "Green";
        if (r == 0 && g == 255 && b == 255) return "Cyan";
        if (r == 0 && g == 100 && b == 255) return "Blue";
        if (r == 160 && g == 32 && b == 240) return "Purple";
        if (r == 255 && g == 0 && b == 255) return "Magenta";
        if (r == 255 && g == 255 && b == 255) return "White";
        return "Custom (" + r + "," + g + "," + b + ")";
    }

    private void cycleHitColorPreset() {
        String current = getHitColorPresetName();
        switch (current) {
            case "Red" -> { ModConfig.hitColorRed = 255; ModConfig.hitColorGreen = 100; ModConfig.hitColorBlue = 0; } // Orange
            case "Orange" -> { ModConfig.hitColorRed = 255; ModConfig.hitColorGreen = 255; ModConfig.hitColorBlue = 0; } // Yellow
            case "Yellow" -> { ModConfig.hitColorRed = 0; ModConfig.hitColorGreen = 255; ModConfig.hitColorBlue = 0; } // Green
            case "Green" -> { ModConfig.hitColorRed = 0; ModConfig.hitColorGreen = 255; ModConfig.hitColorBlue = 255; } // Cyan
            case "Cyan" -> { ModConfig.hitColorRed = 0; ModConfig.hitColorGreen = 100; ModConfig.hitColorBlue = 255; } // Blue
            case "Blue" -> { ModConfig.hitColorRed = 160; ModConfig.hitColorGreen = 32; ModConfig.hitColorBlue = 240; } // Purple
            case "Purple" -> { ModConfig.hitColorRed = 255; ModConfig.hitColorGreen = 0; ModConfig.hitColorBlue = 255; } // Magenta
            case "Magenta" -> { ModConfig.hitColorRed = 255; ModConfig.hitColorGreen = 255; ModConfig.hitColorBlue = 255; } // White
            default -> { ModConfig.hitColorRed = 255; ModConfig.hitColorGreen = 0; ModConfig.hitColorBlue = 0; } // Red
        }
    }

    private static String expandedItemId = null;
    private static boolean showAdvancedTransforms = false;

    private void buildViewModelSettings() {
        // 1. Top Header Subtitle & Status
        FlowLayout descBox = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        descBox.gap(2);
        descBox.child(Components.label(Text.literal("Displays a 3D model of the item you're holding for better visualization"))
            .color(Color.ofArgb(TEXT_F)));
        settingsPanel.child(descBox);

        // Quick Master & Reset Bar
        FlowLayout masterBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        masterBar.verticalAlignment(VerticalAlignment.CENTER);
        masterBar.gap(6);

        ButtonComponent toggleBtn = Components.button(
            Text.literal(ModConfig.showViewModel ? "ENABLED (ON)" : "DISABLED (OFF)"),
            b -> {
                ModConfig.showViewModel = !ModConfig.showViewModel;
                ModConfig.saveConfig();
                buildSettings();
            }
        );
        toggleBtn.sizing(Sizing.content(), Sizing.fixed(20));
        toggleBtn.renderer(ButtonComponent.Renderer.flat(
            ModConfig.showViewModel ? GREEN_D : SURF3,
            ModConfig.showViewModel ? GREEN : TEXT_F,
            ModConfig.showViewModel ? GREEN_D : SURF3
        ));
        masterBar.child(toggleBtn);

        ButtonComponent resetAllBtn = Components.button(Text.literal("RESET ALL"), b -> {
            ModConfig.itemScales.clear();
            ModConfig.itemGroundScales.clear();
            ModConfig.itemGuiScales.clear();
            ModConfig.viewModelMainHandScale = 1.0f;
            ModConfig.viewModelOffHandScale = 1.0f;
            ModConfig.viewModelMainHandX = 0.0f;
            ModConfig.viewModelMainHandY = 0.0f;
            ModConfig.viewModelMainHandZ = 0.0f;
            ModConfig.viewModelOffHandX = 0.0f;
            ModConfig.viewModelOffHandY = 0.0f;
            ModConfig.viewModelOffHandZ = 0.0f;
            ModConfig.viewModelPitch = 0.0f;
            ModConfig.viewModelYaw = 0.0f;
            ModConfig.viewModelRoll = 0.0f;
            ModConfig.saveConfig();
            buildSettings();
        });
        resetAllBtn.sizing(Sizing.content(), Sizing.fixed(20));
        resetAllBtn.renderer(ButtonComponent.Renderer.flat(SURF3, RED, SURF3));
        masterBar.child(resetAllBtn);

        settingsPanel.child(masterBar);

        // 2. Search Bar
        FlowLayout searchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        searchRow.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        searchRow.verticalAlignment(VerticalAlignment.CENTER);
        searchRow.padding(Insets.of(2, 8, 2, 8));
        searchRow.gap(6);
        searchRow.child(Components.label(Text.literal("Search items...")).color(Color.ofArgb(TEXT_F)));

        TextBoxComponent searchBox = Components.textBox(Sizing.fill(100), itemSearchQuery);
        searchBox.sizing(Sizing.fill(100), Sizing.fixed(16));
        searchBox.setMaxLength(50);
        searchBox.onChanged().subscribe(val -> {
            itemSearchQuery = val.trim().toLowerCase();
            buildSettings();
        });
        searchRow.child(searchBox);
        settingsPanel.child(searchRow);

        // 3. Item List (with Accordion Expand/Collapse)
        List<ItemDisplayEntry> itemsToDisplay = getItemsForSearch(itemSearchQuery);
        if (itemsToDisplay.isEmpty()) {
            settingsPanel.child(makeHintRow("No items found matching \"" + itemSearchQuery + "\""));
        } else {
            for (ItemDisplayEntry entry : itemsToDisplay) {
                settingsPanel.child(buildAccordionItemCard(entry.displayName, entry.itemId));
            }
        }
    }

    private FlowLayout buildAccordionItemCard(String displayName, String itemId) {
        boolean isExpanded = itemId.equals(expandedItemId);
        float firstPersonScale = ModConfig.getItemScaleById(itemId);
        float groundScale = ModConfig.getItemGroundScaleById(itemId);
        float guiScale = ModConfig.getItemGuiScaleById(itemId);
        boolean hasCustom = (firstPersonScale != 1.0f || groundScale != 1.0f || guiScale != 1.0f);

        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = isExpanded ? SURF3 : (hasCustom ? 0x0AFFFFFF : SURF);
            int bdr = isExpanded ? VIOLET_S : (hasCustom ? GREEN_D : BORDER);
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, bdr);
        });
        card.padding(Insets.of(4, 8, 4, 8));
        card.gap(4);

        // Collapsed / Header Row
        FlowLayout headerRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        headerRow.verticalAlignment(VerticalAlignment.CENTER);
        headerRow.gap(6);

        int nameColor = hasCustom ? TEXT : TEXT_M;
        if ("minecraft:enchanted_golden_apple".equals(itemId)) {
            nameColor = 0xFFFCD34D;
        }

        headerRow.child(Components.label(Text.literal(displayName)).color(Color.ofArgb(nameColor)));
        headerRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        if (hasCustom && !isExpanded) {
            String badge = String.format("1P:%.1fx | G:%.1fx", firstPersonScale, groundScale);
            headerRow.child(Components.label(Text.literal(badge)).color(Color.ofArgb(GREEN)));
        }

        // Chevron
        String chevron = isExpanded ? "v" : ">";
        headerRow.child(Components.label(Text.literal(chevron)).color(Color.ofArgb(isExpanded ? VIOLET : TEXT_F)));

        headerRow.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                expandedItemId = isExpanded ? null : itemId;
                buildSettings();
                return true;
            }
            return false;
        });

        card.child(headerRow);

        // Expanded Content Panel (Matching Video Frame 00:06 - 00:11)
        if (isExpanded) {
            FlowLayout expandedPanel = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            expandedPanel.surface(Surface.flat(SURF2));
            expandedPanel.padding(Insets.of(6, 8, 6, 8));
            expandedPanel.gap(4);

            // Preview Title Row
            FlowLayout previewRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            previewRow.verticalAlignment(VerticalAlignment.CENTER);
            previewRow.child(Components.label(Text.literal("MODEL PREVIEW")).color(Color.ofArgb(TEXT_F)));
            previewRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
            previewRow.child(Components.label(Text.literal(itemId)).color(Color.ofArgb(0xFF52525B)));
            expandedPanel.child(previewRow);

            // 3 Scale Rows: GROUND, 1ST PERSON, GUI
            expandedPanel.child(makeModelScaleRow("GROUND", groundScale,
                () -> { ModConfig.setItemGroundScale(itemId, Math.max(0.1f, Math.round((groundScale - 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemGroundScale(itemId, Math.min(4.0f, Math.round((groundScale + 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemGroundScale(itemId, 1.0f); buildSettings(); }
            ));

            expandedPanel.child(makeModelScaleRow("1ST PERSON", firstPersonScale,
                () -> { ModConfig.setItemScale(itemId, Math.max(0.1f, Math.round((firstPersonScale - 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemScale(itemId, Math.min(4.0f, Math.round((firstPersonScale + 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemScale(itemId, 1.0f); buildSettings(); }
            ));

            expandedPanel.child(makeModelScaleRow("GUI", guiScale,
                () -> { ModConfig.setItemGuiScale(itemId, Math.max(0.1f, Math.round((guiScale - 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemGuiScale(itemId, Math.min(4.0f, Math.round((guiScale + 0.1f) * 10.0f) / 10.0f)); buildSettings(); },
                () -> { ModConfig.setItemGuiScale(itemId, 1.0f); buildSettings(); }
            ));

            // ADVANCED > Toggle
            FlowLayout advRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            advRow.verticalAlignment(VerticalAlignment.CENTER);
            ButtonComponent advBtn = Components.button(
                Text.literal(showAdvancedTransforms ? "ADVANCED (HIDE v)" : "ADVANCED >"),
                b -> {
                    showAdvancedTransforms = !showAdvancedTransforms;
                    buildSettings();
                }
            );
            advBtn.sizing(Sizing.content(), Sizing.fixed(18));
            advBtn.renderer(ButtonComponent.Renderer.flat(SURF3, TEXT_M, SURF3));
            advRow.child(advBtn);
            expandedPanel.child(advRow);

            if (showAdvancedTransforms) {
                expandedPanel.child(makeStepRow("Main Hand X", String.format("%+.2f", ModConfig.viewModelMainHandX),
                    () -> { ModConfig.viewModelMainHandX = Math.max(-2.0f, Math.round((ModConfig.viewModelMainHandX - 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandX = Math.min(2.0f, Math.round((ModConfig.viewModelMainHandX + 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandX = 0.0f; ModConfig.saveConfig(); buildSettings(); }
                ));
                expandedPanel.child(makeStepRow("Main Hand Y", String.format("%+.2f", ModConfig.viewModelMainHandY),
                    () -> { ModConfig.viewModelMainHandY = Math.max(-2.0f, Math.round((ModConfig.viewModelMainHandY - 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandY = Math.min(2.0f, Math.round((ModConfig.viewModelMainHandY + 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandY = 0.0f; ModConfig.saveConfig(); buildSettings(); }
                ));
                expandedPanel.child(makeStepRow("Main Hand Z", String.format("%+.2f", ModConfig.viewModelMainHandZ),
                    () -> { ModConfig.viewModelMainHandZ = Math.max(-2.0f, Math.round((ModConfig.viewModelMainHandZ - 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandZ = Math.min(2.0f, Math.round((ModConfig.viewModelMainHandZ + 0.05f) * 100.0f) / 100.0f); ModConfig.saveConfig(); buildSettings(); },
                    () -> { ModConfig.viewModelMainHandZ = 0.0f; ModConfig.saveConfig(); buildSettings(); }
                ));
            }

            card.child(expandedPanel);
        }

        return card;
    }

    private FlowLayout makeModelScaleRow(String label, float scaleVal, Runnable onMinus, Runnable onPlus, Runnable onReset) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x06FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(1, 6, 1, 6));
        row.gap(6);

        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_M)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        String valStr = String.format("%.2fx", scaleVal);
        int valColor = (scaleVal != 1.0f) ? ((scaleVal > 1.0f) ? GREEN : VIOLET) : TEXT_F;
        row.child(Components.label(Text.literal(valStr)).color(Color.ofArgb(valColor)));

        ButtonComponent minusBtn = Components.button(Text.literal("-"), b -> onMinus.run());
        minusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        minusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(minusBtn);

        ButtonComponent plusBtn = Components.button(Text.literal("+"), b -> onPlus.run());
        plusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        plusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(plusBtn);

        ButtonComponent resetBtn = Components.button(Text.literal("R"), b -> onReset.run());
        resetBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF3F3F46, RED, 0xFF3F3F46));
        row.child(resetBtn);

        return row;
    }

    private record ItemDisplayEntry(String displayName, String itemId) {}

    private List<ItemDisplayEntry> getItemsForSearch(String query) {
        List<ItemDisplayEntry> results = new ArrayList<>();
        Set<String> added = new HashSet<>();

        if (query == null || query.isEmpty()) {
            // Quick preset popular items
            String[][] popular = {
                {"Golden Apple", "minecraft:golden_apple"},
                {"Enchanted Golden Apple", "minecraft:enchanted_golden_apple"},
                {"Diamond Sword", "minecraft:diamond_sword"},
                {"Netherite Sword", "minecraft:netherite_sword"},
                {"Bow", "minecraft:bow"},
                {"Crossbow", "minecraft:crossbow"},
                {"Ender Pearl", "minecraft:ender_pearl"},
                {"Totem of Undying", "minecraft:totem_of_undying"},
                {"Shield", "minecraft:shield"},
                {"Wind Charge", "minecraft:wind_charge"},
                {"Mace", "minecraft:mace"},
                {"Potion", "minecraft:potion"},
                {"Splash Potion", "minecraft:splash_potion"},
                {"End Crystal", "minecraft:end_crystal"},
                {"Golden Carrot", "minecraft:golden_carrot"}
            };
            for (String[] p : popular) {
                results.add(new ItemDisplayEntry(p[0], p[1]));
                added.add(p[1]);
            }
            return results;
        }

        // Always check Golden Apple and Enchanted Golden Apple for query matches first
        if ("golden apple".contains(query) || "apple".contains(query) || "gold".contains(query)) {
            if (!added.contains("minecraft:golden_apple")) {
                results.add(new ItemDisplayEntry("Golden Apple", "minecraft:golden_apple"));
                added.add("minecraft:golden_apple");
            }
        }
        if ("enchanted golden apple".contains(query) || "notch".contains(query) || "god apple".contains(query) || "enchanted".contains(query) || "gapple".contains(query)) {
            if (!added.contains("minecraft:enchanted_golden_apple")) {
                results.add(new ItemDisplayEntry("Enchanted Golden Apple", "minecraft:enchanted_golden_apple"));
                added.add("minecraft:enchanted_golden_apple");
            }
        }

        // Search through Registries.ITEM
        for (Identifier id : Registries.ITEM.getIds()) {
            if (results.size() >= 20) break;
            String idStr = id.toString();
            String path = id.getPath();
            String formattedName = formatItemName(idStr);

            if (path.toLowerCase().contains(query) || formattedName.toLowerCase().contains(query)) {
                if (!added.contains(idStr)) {
                    results.add(new ItemDisplayEntry(formattedName, idStr));
                    added.add(idStr);
                }
            }
        }

        return results;
    }

    private static String formatItemName(String itemId) {
        if ("minecraft:enchanted_golden_apple".equals(itemId)) return "Enchanted Golden Apple";
        if ("minecraft:golden_apple".equals(itemId)) return "Golden Apple";

        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private FlowLayout makeStepRow(String label, String valueText, Runnable onMinus, Runnable onPlus, Runnable onReset) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x05FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);

        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_M)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        row.child(Components.label(Text.literal(valueText)).color(Color.ofArgb(VIOLET)));

        ButtonComponent minusBtn = Components.button(Text.literal("-"), b -> onMinus.run());
        minusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        minusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(minusBtn);

        ButtonComponent plusBtn = Components.button(Text.literal("+"), b -> onPlus.run());
        plusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        plusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(plusBtn);

        ButtonComponent resetBtn = Components.button(Text.literal("R"), b -> onReset.run());
        resetBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF3F3F46, TEXT_F, 0xFF3F3F46));
        row.child(resetBtn);

        return row;
    }

    private FlowLayout makeItemScaleRow(String displayName, String itemId) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            float curScale = ModConfig.getItemScaleById(itemId);
            if (curScale != 1.0f) {
                ctx.fill(x, y, x + w, y + h, 0x0CFFFFFF);
                ctx.fill(x, y, x + 2, y + h, (curScale > 1.0f) ? GREEN : VIOLET);
            } else {
                ctx.fill(x, y, x + w, y + h, 0x04FFFFFF);
            }
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);

        FlowLayout labelCol = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        labelCol.gap(1);

        float curScale = ModConfig.getItemScaleById(itemId);
        int nameColor = (curScale != 1.0f) ? TEXT : TEXT_M;
        if ("minecraft:enchanted_golden_apple".equals(itemId)) {
            nameColor = 0xFFFCD34D;
        }
        labelCol.child(Components.label(Text.literal(displayName)).color(Color.ofArgb(nameColor)));
        labelCol.child(Components.label(Text.literal(itemId)).color(Color.ofArgb(TEXT_F)));
        row.child(labelCol);

        String scaleBadge;
        int badgeColor;
        if (curScale > 1.0f) {
            scaleBadge = String.format("%.1fx (Big)", curScale);
            badgeColor = GREEN;
        } else if (curScale < 1.0f) {
            scaleBadge = String.format("%.1fx (Small)", curScale);
            badgeColor = VIOLET;
        } else {
            scaleBadge = "1.0x (Normal)";
            badgeColor = TEXT_F;
        }
        row.child(Components.label(Text.literal(scaleBadge)).color(Color.ofArgb(badgeColor)));

        ButtonComponent minusBtn = Components.button(Text.literal("-"), b -> {
            float s = Math.max(0.1f, Math.round((ModConfig.getItemScaleById(itemId) - 0.1f) * 10.0f) / 10.0f);
            ModConfig.setItemScale(itemId, s);
            buildSettings();
        });
        minusBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        minusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(minusBtn);

        ButtonComponent plusBtn = Components.button(Text.literal("+"), b -> {
            float s = Math.min(3.0f, Math.round((ModConfig.getItemScaleById(itemId) + 0.1f) * 10.0f) / 10.0f);
            ModConfig.setItemScale(itemId, s);
            buildSettings();
        });
        plusBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        plusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_S, SURF3));
        row.child(plusBtn);

        ButtonComponent resetBtn = Components.button(Text.literal("R"), b -> {
            ModConfig.resetItemScale(itemId);
            buildSettings();
        });
        resetBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF3F3F46, RED, 0xFF3F3F46));
        row.child(resetBtn);

        return row;
    }

    private void buildGenericSettings() {
        settingsPanel.child(Components.label(Text.literal("Settings for " + moduleName))
            .color(Color.ofArgb(VIOLET)));
        settingsPanel.child(makeHintRow("Scroll mouse wheel in HUD Editor to resize."));
    }

    // ── Row builders ──────────────────────────────────────────────────────────

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(4, 0, 2, 0));
        header.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(TEXT_F)));
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        return header;
    }

    private FlowLayout makeToggleRow(String label, String desc, boolean initialEnabled, Runnable action) {
        final boolean[] state = new boolean[]{initialEnabled};
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (state[0]) {
                ctx.fill(x, y, x + w, y + h, 0x0AFFFFFF);
                ctx.fill(x, y, x + 2, y + h, VIOLET);
            }
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);

        FlowLayout info = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        info.gap(1);
        LabelComponent titleLbl = Components.label(Text.literal(label));
        titleLbl.color(Color.ofArgb(state[0] ? TEXT : TEXT_M));
        info.child(titleLbl);
        info.child(Components.label(Text.literal(desc)).color(Color.ofArgb(TEXT_F)).sizing(Sizing.fill(100), Sizing.content()));
        row.child(info);

        Runnable toggleAction = () -> {
            state[0] = !state[0];
            titleLbl.color(Color.ofArgb(state[0] ? TEXT : TEXT_M));
            action.run();
        };

        ButtonComponent toggle = Components.button(Text.literal(""), btn -> toggleAction.run());
        toggle.sizing(Sizing.fixed(28), Sizing.fixed(14));
        toggle.renderer((ctx, comp, delta) -> {
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

    private FlowLayout makeCycleRow(String label, String value, Runnable onClick) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x05FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);
        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_M)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        row.child(Components.label(Text.literal("[ " + value + " ]")).color(Color.ofArgb(VIOLET)));
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { onClick.run(); return true; } return false; });
        return row;
    }

    private FlowLayout makeKeybindRow(String label, String key, int color, Runnable onClick) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x05FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);
        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_M)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        row.child(Components.label(Text.literal("[ " + key + " ]")).color(Color.ofArgb(color)));
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { onClick.run(); return true; } return false; });
        return row;
    }

    private FlowLayout makeButtonRow(String label, String btnLabel, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x05FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);
        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_M)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        ButtonComponent btn = Components.button(Text.literal(btnLabel), b -> action.run());
        btn.sizing(Sizing.content(), Sizing.fixed(16));
        btn.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET_S, VIOLET_D));
        row.child(btn);
        return row;
    }

    private int crosshairPreviewBgIndex = 0;
    private static final int[] CROSSHAIR_BG_COLORS = new int[]{0xFF111115, 0xFF38BDF8, 0xFF15803D, 0xFFE2E8F0, 0xFF7F1D1D};
    private static final String[] CROSSHAIR_BG_NAMES = new String[]{"Dark", "Sky", "Grass", "Snow", "Nether"};

    private void buildCrosshairSettings() {
        settingsPanel.child(makeSectionHeader("Custom Crosshair"));
        settingsPanel.child(makeToggleRow("Enable Custom Crosshair", "Replace default crosshair with customized style",
            ModConfig.enableCustomCrosshair,
            () -> { ModConfig.enableCustomCrosshair = !ModConfig.enableCustomCrosshair; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeButtonRow("Crosshair Studio", "Open Full Editor", () -> {
            if (this.client != null) this.client.setScreen(new CrosshairEditorScreen(this));
        }));

        // 1. Live Preview Card
        settingsPanel.child(makeCrosshairLivePreview());

        // 2. Preset Selector
        String[] presets = new String[]{"CLASSIC_CROSS", "DOT", "CIRCLE", "SQUARE", "CHEVRON", "DIAMOND", "T_SHAPE", "BOX_FEET", "CUSTOM_DRAWN"};
        String currentPresetName = ModConfig.crosshairPreset.replace('_', ' ');
        settingsPanel.child(makeCycleRow("Preset", currentPresetName, () -> {
            int curIdx = 0;
            for (int i = 0; i < presets.length; i++) {
                if (presets[i].equalsIgnoreCase(ModConfig.crosshairPreset)) { curIdx = i; break; }
            }
            ModConfig.crosshairPreset = presets[(curIdx + 1) % presets.length];
            ModConfig.saveConfig();
            buildSettings();
        }));

        // 3. Geometry (for non-drawable presets)
        if (!"CUSTOM_DRAWN".equalsIgnoreCase(ModConfig.crosshairPreset) && !"DRAWABLE".equalsIgnoreCase(ModConfig.crosshairPreset)) {
            settingsPanel.child(makeSectionHeader("Geometry"));
            settingsPanel.child(makeCycleRow("Size", ModConfig.crosshairSize + "px", () -> {
                ModConfig.crosshairSize = (ModConfig.crosshairSize >= 15) ? 1 : ModConfig.crosshairSize + 1;
                ModConfig.saveConfig(); buildSettings();
            }));
            settingsPanel.child(makeCycleRow("Gap", ModConfig.crosshairGap + "px", () -> {
                ModConfig.crosshairGap = (ModConfig.crosshairGap >= 12) ? 0 : ModConfig.crosshairGap + 1;
                ModConfig.saveConfig(); buildSettings();
            }));
            settingsPanel.child(makeCycleRow("Thickness", ModConfig.crosshairThickness + "px", () -> {
                ModConfig.crosshairThickness = (ModConfig.crosshairThickness >= 4) ? 1 : ModConfig.crosshairThickness + 1;
                ModConfig.saveConfig(); buildSettings();
            }));
            settingsPanel.child(makeToggleRow("Center Dot", "Display central dot in the crosshair",
                ModConfig.crosshairShowDot,
                () -> { ModConfig.crosshairShowDot = !ModConfig.crosshairShowDot; ModConfig.saveConfig(); buildSettings(); }));
            if (ModConfig.crosshairShowDot) {
                settingsPanel.child(makeCycleRow("Dot Size", ModConfig.crosshairDotSize + "px", () -> {
                    ModConfig.crosshairDotSize = (ModConfig.crosshairDotSize >= 4) ? 1 : ModConfig.crosshairDotSize + 1;
                    ModConfig.saveConfig(); buildSettings();
                }));
            }
        }

        // 4. Drawable Pixel Canvas
        if ("CUSTOM_DRAWN".equalsIgnoreCase(ModConfig.crosshairPreset) || "DRAWABLE".equalsIgnoreCase(ModConfig.crosshairPreset)) {
            settingsPanel.child(makeSectionHeader("Pixel Canvas (15x15 Grid)"));
            settingsPanel.child(makeHintRow("Click on any pixel cell to draw or erase custom crosshair shapes."));
            settingsPanel.child(makeCrosshairPixelGridEditor());
            settingsPanel.child(makeCrosshairTemplateRow());
        }

        // 5. Colors & Highlights
        settingsPanel.child(makeSectionHeader("Colors & Enemy Hitbox"));
        settingsPanel.child(makeColorPickerRow("Crosshair Color", ModConfig.crosshairColor, ModConfig.crosshairRainbow,
            c -> ModConfig.crosshairColor = c,
            () -> ModConfig.crosshairRainbow = !ModConfig.crosshairRainbow));

        settingsPanel.child(makeToggleRow("Outline / Shadow", "Draw high-contrast outline border",
            ModConfig.crosshairOutline,
            () -> { ModConfig.crosshairOutline = !ModConfig.crosshairOutline; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeToggleRow("Enemy Crosshair", "Change crosshair when aiming at player or mob hitbox",
            ModConfig.crosshairEnemyCrosshair,
            () -> { ModConfig.crosshairEnemyCrosshair = !ModConfig.crosshairEnemyCrosshair; ModConfig.saveConfig(); buildSettings(); }));

        if (ModConfig.crosshairEnemyCrosshair) {
            String[] enemyModes = new String[]{"COLOR_CHANGE", "TARGET_LOCK_BOX", "RED_DOT", "CROSS_EXPAND"};
            String enemyModeName = ModConfig.crosshairEnemyMode.replace('_', ' ');
            settingsPanel.child(makeCycleRow("Enemy Hitbox Mode", enemyModeName, () -> {
                int cur = 0;
                for (int i = 0; i < enemyModes.length; i++) {
                    if (enemyModes[i].equalsIgnoreCase(ModConfig.crosshairEnemyMode)) { cur = i; break; }
                }
                ModConfig.crosshairEnemyMode = enemyModes[(cur + 1) % enemyModes.length];
                ModConfig.saveConfig(); buildSettings();
            }));

            settingsPanel.child(makeColorPickerRow("Enemy Hitbox Color", ModConfig.crosshairEnemyColor, false,
                c -> ModConfig.crosshairEnemyColor = c,
                () -> {}));
        }

        // 6. Dynamics
        settingsPanel.child(makeSectionHeader("Behavior"));
        settingsPanel.child(makeToggleRow("Dynamic Spread", "Expand gap while walking, sprinting or jumping",
            ModConfig.crosshairDynamic,
            () -> { ModConfig.crosshairDynamic = !ModConfig.crosshairDynamic; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeToggleRow("Attack Indicator", "Show weapon cooldown progress under crosshair",
            ModConfig.crosshairAttackIndicator,
            () -> { ModConfig.crosshairAttackIndicator = !ModConfig.crosshairAttackIndicator; ModConfig.saveConfig(); buildSettings(); }));

        settingsPanel.child(makeToggleRow("3rd Person Visibility", "Keep crosshair visible in F5 / FreeLook mode",
            ModConfig.crosshairThirdPerson,
            () -> { ModConfig.crosshairThirdPerson = !ModConfig.crosshairThirdPerson; ModConfig.saveConfig(); buildSettings(); }));
    }

    private FlowLayout makeCrosshairLivePreview() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(2, 4, 4, 4));

        FlowLayout previewBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(56));
        previewBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bgColor = CROSSHAIR_BG_COLORS[crosshairPreviewBgIndex % CROSSHAIR_BG_COLORS.length];
            ctx.fill(x, y, x + w, y + h, bgColor);
            ctx.drawBorder(x, y, w, h, BORDER_S);

            int cx = x + w / 2;
            int cy = y + h / 2;
            com.velora.client.client.CustomCrosshairMod.renderCrosshair(ctx, cx, cy, 0.0f, 1.0f, false);
        });

        previewBox.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                crosshairPreviewBgIndex = (crosshairPreviewBgIndex + 1) % CROSSHAIR_BG_COLORS.length;
                buildSettings();
                return true;
            }
            return false;
        });

        String bgName = CROSSHAIR_BG_NAMES[crosshairPreviewBgIndex % CROSSHAIR_BG_NAMES.length];
        FlowLayout labelRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        labelRow.verticalAlignment(VerticalAlignment.CENTER);
        labelRow.padding(Insets.of(0, 4, 0, 4));
        labelRow.child(Components.label(Text.literal("Preview (Click box to test against: " + bgName + ")")).color(Color.ofArgb(TEXT_F)));

        container.child(previewBox);
        container.child(labelRow);
        return container;
    }

    private FlowLayout makeCrosshairPixelGridEditor() {
        FlowLayout gridContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        gridContainer.horizontalAlignment(HorizontalAlignment.CENTER);
        gridContainer.padding(Insets.of(4, 0, 4, 0));
        gridContainer.gap(1);

        boolean[] grid = ModConfig.crosshairGrid;
        if (grid == null || grid.length < 225) {
            grid = com.velora.client.client.CustomCrosshairMod.getDefaultGrid();
            ModConfig.crosshairGrid = grid;
        }

        int cellSize = 10;
        int gridSize = 15;
        for (int row = 0; row < gridSize; row++) {
            final int r = row;
            FlowLayout rowFlow = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(cellSize));
            rowFlow.gap(1);

            for (int col = 0; col < gridSize; col++) {
                final int c = col;
                final int idx = r * gridSize + c;

                FlowLayout cell = Containers.horizontalFlow(Sizing.fixed(cellSize), Sizing.fixed(cellSize));
                cell.surface((ctx, comp) -> {
                    int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                    boolean active = (ModConfig.crosshairGrid != null && idx < ModConfig.crosshairGrid.length && ModConfig.crosshairGrid[idx]);
                    if (active) {
                        ctx.fill(x, y, x + w, y + h, ModConfig.crosshairColor);
                    } else {
                        ctx.fill(x, y, x + w, y + h, 0x1AFFFFFF);
                    }
                    ctx.drawBorder(x, y, w, h, 0x33000000);
                });

                cell.mouseDown().subscribe((mx, my, btn) -> {
                    if (btn == 0 && ModConfig.crosshairGrid != null && idx < ModConfig.crosshairGrid.length) {
                        ModConfig.crosshairGrid[idx] = !ModConfig.crosshairGrid[idx];
                        ModConfig.saveConfig();
                        buildSettings();
                        return true;
                    }
                    return false;
                });

                rowFlow.child(cell);
            }
            gridContainer.child(rowFlow);
        }

        return gridContainer;
    }

    private FlowLayout makeCrosshairTemplateRow() {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.CENTER);
        row.gap(4);
        row.padding(Insets.of(2, 4, 2, 4));

        ButtonComponent boxBtn = Components.button(Text.literal("Box Feet"), b -> {
            ModConfig.crosshairGrid = com.velora.client.client.CustomCrosshairMod.getBoxFeetTemplate();
            ModConfig.saveConfig(); buildSettings();
        });
        boxBtn.sizing(Sizing.content(), Sizing.fixed(16));
        boxBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET, SURF3));

        ButtonComponent plusBtn = Components.button(Text.literal("Plus"), b -> {
            ModConfig.crosshairGrid = com.velora.client.client.CustomCrosshairMod.getDefaultGrid();
            ModConfig.saveConfig(); buildSettings();
        });
        plusBtn.sizing(Sizing.content(), Sizing.fixed(16));
        plusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET, SURF3));

        ButtonComponent circleBtn = Components.button(Text.literal("Circle"), b -> {
            ModConfig.crosshairGrid = com.velora.client.client.CustomCrosshairMod.getCircleTemplate();
            ModConfig.saveConfig(); buildSettings();
        });
        circleBtn.sizing(Sizing.content(), Sizing.fixed(16));
        circleBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET, SURF3));

        ButtonComponent diamondBtn = Components.button(Text.literal("Diamond"), b -> {
            ModConfig.crosshairGrid = com.velora.client.client.CustomCrosshairMod.getDiamondTemplate();
            ModConfig.saveConfig(); buildSettings();
        });
        diamondBtn.sizing(Sizing.content(), Sizing.fixed(16));
        diamondBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET, SURF3));

        ButtonComponent clearBtn = Components.button(Text.literal("Clear"), b -> {
            ModConfig.crosshairGrid = new boolean[225];
            ModConfig.saveConfig(); buildSettings();
        });
        clearBtn.sizing(Sizing.content(), Sizing.fixed(16));
        clearBtn.renderer(ButtonComponent.Renderer.flat(0xFF7F1D1D, RED, 0xFF7F1D1D));

        row.child(boxBtn);
        row.child(plusBtn);
        row.child(circleBtn);
        row.child(diamondBtn);
        row.child(clearBtn);

        return row;
    }

    private FlowLayout makeHintRow(String text) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        row.padding(Insets.of(4, 8, 0, 8));
        row.child(Components.label(Text.literal(text)).color(Color.ofArgb(TEXT_F)));
        return row;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if ("Free Look".equals(moduleName)) ModConfig.freeLookKey = keyCode;
                else if ("Snap Look".equals(moduleName)) ModConfig.snapLookKey = keyCode;
                ModConfig.saveConfig();
            }
            listeningForKey = false;
            buildSettings();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        if (this.client != null && this.parent != null) {
            this.client.setScreen(this.parent);
        } else {
            super.close();
        }
    }
}
