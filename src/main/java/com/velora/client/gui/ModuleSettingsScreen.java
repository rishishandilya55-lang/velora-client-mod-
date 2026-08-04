package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ModuleSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private final String moduleName;
    private boolean listeningForKey = false;
    private FlowLayout settingsPanel;

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
        super(Text.literal(moduleName + " Settings"));
        this.moduleName = moduleName;
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

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(420), Sizing.fixed(300));
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

        settingsPanel = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        settingsPanel.padding(Insets.of(8, 14, 8, 14));
        settingsPanel.gap(3);
        buildSettings();

        panel.child(settingsPanel);
        root.child(panel);
    }

    private void buildSettings() {
        settingsPanel.clearChildren();

        switch (moduleName) {
            case "Armor Status" -> buildArmorSettings();
            case "NoHurtCam" -> buildHurtCamSettings();
            case "Zoom Mod" -> buildZoomSettings();
            case "Free Look" -> buildFreeLookSettings();
            case "Snap Look" -> buildSnapLookSettings();
            case "CPS Counter" -> buildCpsSettings();
            case "WASD Keys" -> buildKeystrokesSettings();
            case "Minimap" -> buildMinimapSettings();
            case "Capes & Physics" -> buildCapeSettings();
            case "Nametag" -> buildNametagSettings();
            default -> buildGenericSettings();
        }
    }

    private void buildArmorSettings() {
        settingsPanel.child(makeSectionHeader("Display"));
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
    }

    private void buildHurtCamSettings() {
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

    private void buildZoomSettings() {
        settingsPanel.child(makeSectionHeader("Zoom"));
        settingsPanel.child(makeToggleRow("Smooth Animation", "Smoothly transition zoom in/out",
            ModConfig.zoomSmooth,
            () -> { ModConfig.zoomSmooth = !ModConfig.zoomSmooth; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeToggleRow("Scale Sensitivity", "Reduce mouse sensitivity while zoomed",
            ModConfig.zoomScaleSensitivity,
            () -> { ModConfig.zoomScaleSensitivity = !ModConfig.zoomScaleSensitivity; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Hold C to zoom. Scroll to adjust zoom level."));
    }

    private void buildFreeLookSettings() {
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

    private void buildSnapLookSettings() {
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

    private void buildCpsSettings() {
        settingsPanel.child(makeToggleRow("Right-Click CPS", "Also count right-clicks per second",
            ModConfig.showRightCps,
            () -> { ModConfig.showRightCps = !ModConfig.showRightCps; ModConfig.saveConfig(); buildSettings(); }));
    }

    private void buildKeystrokesSettings() {
        settingsPanel.child(makeToggleRow("Mouse Buttons", "Show LMB and RMB in keystrokes display",
            ModConfig.showMouseStrokes,
            () -> { ModConfig.showMouseStrokes = !ModConfig.showMouseStrokes; ModConfig.saveConfig(); buildSettings(); }));
    }

    private void buildMinimapSettings() {
        settingsPanel.child(makeSectionHeader("Display"));
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

    private void buildNametagSettings() {
        settingsPanel.child(makeToggleRow("Show Nametag", "Display custom nametag with rank badge above your head",
            ModConfig.showNametag,
            () -> { ModConfig.showNametag = !ModConfig.showNametag; ModConfig.saveConfig(); buildSettings(); }));
        settingsPanel.child(makeHintRow("Your rank badge will appear to the left of your name."));
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

    private FlowLayout makeToggleRow(String label, String desc, boolean enabled, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            if (enabled) {
                ctx.fill(x, y, x + w, y + h, 0x0AFFFFFF);
                ctx.fill(x, y, x + 2, y + h, VIOLET);
            }
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);

        FlowLayout info = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        info.gap(1);
        info.child(Components.label(Text.literal(label)).color(Color.ofArgb(enabled ? TEXT : TEXT_M)));
        info.child(Components.label(Text.literal(desc)).color(Color.ofArgb(TEXT_F)).sizing(Sizing.fill(100), Sizing.content()));
        row.child(info);

        ButtonComponent toggle = Components.button(Text.literal(""), btn -> action.run());
        toggle.sizing(Sizing.fixed(28), Sizing.fixed(14));
        toggle.renderer((ctx, comp, delta) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, enabled ? GREEN_D : SURF3);
            ctx.drawBorder(x, y, w, h, enabled ? GREEN : BORDER_S);
            int knobX = enabled ? x + w - 10 : x + 2;
            ctx.fill(knobX, y + 2, knobX + 8, y + h - 2, enabled ? GREEN : TEXT_F);
        });
        row.child(toggle);
        row.mouseDown().subscribe((mx, my, btn) -> { if (btn == 0) { action.run(); return true; } return false; });
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
        super.close();
    }
}
