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
import org.lwjgl.glfw.GLFW;

public class ModuleSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private final String moduleName;
    private boolean listeningForKey = false;

    // Root ref for key-rebind row refresh
    private FlowLayout settingsPanel;

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

        // ── Outer panel (460x280) ─────────────────────────────────────────────
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(460), Sizing.fixed(280));
        panel.surface(Surface.flat(0xCC1A1A2E));
        panel.padding(Insets.none());

        // ── Header ────────────────────────────────────────────────────────────
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        header.surface(Surface.flat(0xEE222240));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 14, 0, 14));

        header.child(Components.label(Text.literal(moduleName + " Settings"))
            .color(Color.WHITE)
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));

        // Close button
        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x55FF4444, 0x00000000));
        header.child(closeBtn);

        panel.child(header);

        // ── Settings content area ─────────────────────────────────────────────
        settingsPanel = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        settingsPanel.padding(Insets.of(12, 20, 12, 20));
        settingsPanel.gap(6);
        buildSettings(settingsPanel);

        panel.child(settingsPanel);
        root.child(panel);
    }

    private void buildSettings(FlowLayout container) {
        container.clearChildren();

        if ("Armor Status".equals(moduleName)) {
            container.child(makeCycleRow(
                "HUD Orientation: " + ModConfig.armorOrientation,
                "[ Click to Toggle ]", 0xFFA855F7,
                () -> {
                    ModConfig.armorOrientation = "VERTICAL".equalsIgnoreCase(ModConfig.armorOrientation) ? "HORIZONTAL" : "VERTICAL";
                    ModConfig.saveConfig();
                    rebuildSettings();
                }));

            container.child(makeCycleRow(
                "Background Style: " + ModConfig.armorBackgroundStyle,
                "[ Click to Cycle ]", 0xFFC084FC,
                () -> {
                    if ("MODERN".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "TRANSPARENT";
                    else if ("TRANSPARENT".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "COMPACT";
                    else ModConfig.armorBackgroundStyle = "MODERN";
                    ModConfig.saveConfig();
                    rebuildSettings();
                }));

            container.child(makeCycleRow(
                "Durability Display: " + ModConfig.armorDurabilityMode,
                "[ Click to Cycle ]", 0xFF38BDF8,
                () -> {
                    if ("MAX_VALUE".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "PERCENT";
                    else if ("PERCENT".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "VALUE";
                    else ModConfig.armorDurabilityMode = "MAX_VALUE";
                    ModConfig.saveConfig();
                    rebuildSettings();
                }));

            container.child(makeToggleRow("Show Offhand Item", ModConfig.armorShowOffhand,
                () -> { ModConfig.armorShowOffhand = !ModConfig.armorShowOffhand; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Count Inventory Items (Blocks/Items)", ModConfig.armorShowCount,
                () -> { ModConfig.armorShowCount = !ModConfig.armorShowCount; ModConfig.saveConfig(); rebuildSettings(); }));

        } else if ("NoHurtCam".equals(moduleName)) {
            container.child(makeToggleRow("Enable NoHurtCam (Disable Camera Wobble)", ModConfig.showNoHurtCam,
                () -> { ModConfig.showNoHurtCam = !ModConfig.showNoHurtCam; ModConfig.saveConfig(); rebuildSettings(); }));

            String intensityText = ModConfig.hurtCamIntensity <= 0.0f ? "0% (No Wobble)" : (int)(ModConfig.hurtCamIntensity * 100) + "%";
            container.child(makeCycleRow(
                "Hurt Camera Intensity: " + intensityText,
                "[ Click to Cycle ]", 0xFF22C55E,
                () -> {
                    if (ModConfig.hurtCamIntensity <= 0.0f) ModConfig.hurtCamIntensity = 0.5f;
                    else if (ModConfig.hurtCamIntensity <= 0.5f) ModConfig.hurtCamIntensity = 1.0f;
                    else ModConfig.hurtCamIntensity = 0.0f;
                    ModConfig.saveConfig();
                    rebuildSettings();
                }));

        } else if ("Zoom Mod".equals(moduleName)) {
            container.child(makeToggleRow("Smooth Zoom Animation", ModConfig.zoomSmooth,
                () -> { ModConfig.zoomSmooth = !ModConfig.zoomSmooth; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Scale Mouse Sensitivity While Zoomed", ModConfig.zoomScaleSensitivity,
                () -> { ModConfig.zoomScaleSensitivity = !ModConfig.zoomScaleSensitivity; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(Components.label(Text.literal("Hold key 'C' to Zoom. Scroll Mouse Wheel to adjust Zoom level!"))
                .color(Color.ofArgb(0xFFA1A1AA))
                .sizing(Sizing.fill(100), Sizing.content()));

        } else if ("Free Look".equals(moduleName)) {
            container.child(makeToggleRow("Enable 360 Camera Free Look", ModConfig.showFreeLook,
                () -> { ModConfig.showFreeLook = !ModConfig.showFreeLook; ModConfig.saveConfig(); rebuildSettings(); }));

            String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.freeLookKey, 0);
            if (keyName == null) keyName = "KEY " + ModConfig.freeLookKey;
            container.child(makeCycleRow(
                "Free Look Keybind",
                "[ " + keyName.toUpperCase() + " ]",
                listeningForKey ? 0xFF00FFCC : 0xFFA855F7,
                () -> { listeningForKey = true; rebuildSettings(); }));

            container.child(Components.label(Text.literal("Hold this key to rotate camera freely around player!"))
                .color(Color.ofArgb(0xFFA1A1AA))
                .sizing(Sizing.fill(100), Sizing.content()));

        } else if ("Snap Look".equals(moduleName)) {
            container.child(makeToggleRow("Enable Quick Rear View Snap", ModConfig.showSnapLook,
                () -> { ModConfig.showSnapLook = !ModConfig.showSnapLook; ModConfig.saveConfig(); rebuildSettings(); }));

            String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.snapLookKey, 0);
            if (keyName == null) keyName = "KEY " + ModConfig.snapLookKey;
            container.child(makeCycleRow(
                "Snap Look Keybind",
                "[ " + keyName.toUpperCase() + " ]",
                listeningForKey ? 0xFF00FFCC : 0xFFA855F7,
                () -> { listeningForKey = true; rebuildSettings(); }));

            container.child(Components.label(Text.literal("Hold this key to instantly look behind your player!"))
                .color(Color.ofArgb(0xFFA1A1AA))
                .sizing(Sizing.fill(100), Sizing.content()));

        } else if ("CPS Counter".equals(moduleName)) {
            container.child(makeToggleRow("Show Right-Click CPS", ModConfig.showRightCps,
                () -> { ModConfig.showRightCps = !ModConfig.showRightCps; ModConfig.saveConfig(); rebuildSettings(); }));

        } else if ("WASD Keys".equals(moduleName)) {
            container.child(makeToggleRow("Show Mouse Buttons (LMB / RMB)", ModConfig.showMouseStrokes,
                () -> { ModConfig.showMouseStrokes = !ModConfig.showMouseStrokes; ModConfig.saveConfig(); rebuildSettings(); }));

        } else if ("Minimap".equals(moduleName)) {
            container.child(makeCycleRow(
                "Minimap Shape: " + ModConfig.minimapShape,
                "[ Click to Cycle ]", 0xFFA855F7,
                () -> {
                    ModConfig.minimapShape = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape) ? "SQUARE" : "CIRCLE";
                    ModConfig.saveConfig();
                    rebuildSettings();
                }));

            container.child(makeToggleRow("Show Entity Radar (Mobs, Players, Items)", ModConfig.minimapShowEntities,
                () -> { ModConfig.minimapShowEntities = !ModConfig.minimapShowEntities; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Rotate Compass Bezel with Player Yaw", ModConfig.minimapRotateMap,
                () -> { ModConfig.minimapRotateMap = !ModConfig.minimapRotateMap; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Show Position Coordinates Footer", ModConfig.minimapShowCoordinates,
                () -> { ModConfig.minimapShowCoordinates = !ModConfig.minimapShowCoordinates; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Show Biome Name & Heading Header", ModConfig.minimapShowBiome,
                () -> { ModConfig.minimapShowBiome = !ModConfig.minimapShowBiome; ModConfig.saveConfig(); rebuildSettings(); }));

        } else if ("Capes & Physics".equals(moduleName)) {
            container.child(makeToggleRow("Enable Local Velora Cape", ModConfig.enableCape,
                () -> { ModConfig.enableCape = !ModConfig.enableCape; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Enable Cape Physics Simulation", ModConfig.enableCapePhysics,
                () -> { ModConfig.enableCapePhysics = !ModConfig.enableCapePhysics; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Apply Cape to Local Player Only", ModConfig.capeOnlyLocal,
                () -> { ModConfig.capeOnlyLocal = !ModConfig.capeOnlyLocal; ModConfig.saveConfig(); rebuildSettings(); }));

            container.child(makeToggleRow("Override Vanilla / Default Capes", ModConfig.overrideDefaultCape,
                () -> { ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape; ModConfig.saveConfig(); rebuildSettings(); }));

        } else {
            container.child(Components.label(Text.literal("Module settings for " + moduleName))
                .color(Color.ofArgb(0xFFA855F7))
                .sizing(Sizing.fill(100), Sizing.content()));

            container.child(Components.label(Text.literal("Scroll mouse wheel in HUD Editor to resize this element!"))
                .color(Color.ofArgb(0xFFA1A1AA))
                .sizing(Sizing.fill(100), Sizing.content()));
        }
    }

    private void rebuildSettings() {
        buildSettings(settingsPanel);
    }

    // ── Row: toggle (label + switch button) ───────────────────────────────────
    private FlowLayout makeToggleRow(String label, boolean enabled, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        row.surface(Surface.flat(0x33FFFFFF));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(0, 14, 0, 14));

        row.child(Components.label(Text.literal(label))
            .color(Color.WHITE)
            .sizing(Sizing.fill(100), Sizing.content()));

        // Toggle switch button
        int sw = enabled ? 0xFF22C55E : 0xFF71717A;
        ButtonComponent swBtn = Components.button(Text.literal(enabled ? "  ON" : " OFF"), btn -> action.run());
        swBtn.sizing(Sizing.fixed(36), Sizing.fixed(16));
        swBtn.renderer(ButtonComponent.Renderer.flat(sw, sw, sw));
        row.child(swBtn);

        row.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });

        return row;
    }

    // ── Row: cycle option (label + right-aligned action hint) ────────────────
    private FlowLayout makeCycleRow(String label, String action, int actionColor, Runnable onClick) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        row.surface(Surface.flat(0x33FFFFFF));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(0, 14, 0, 14));

        row.child(Components.label(Text.literal(label))
            .color(Color.WHITE)
            .sizing(Sizing.fill(100), Sizing.content()));

        row.child(Components.label(Text.literal(action))
            .color(Color.ofArgb(actionColor))
            .sizing(Sizing.content(), Sizing.content()));

        row.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { onClick.run(); return true; }
            return false;
        });

        return row;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if ("Free Look".equals(moduleName)) {
                    ModConfig.freeLookKey = keyCode;
                } else if ("Snap Look".equals(moduleName)) {
                    ModConfig.snapLookKey = keyCode;
                }
                ModConfig.saveConfig();
            }
            listeningForKey = false;
            rebuildSettings();
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
