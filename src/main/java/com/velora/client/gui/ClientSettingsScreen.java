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

public class ClientSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private FlowLayout settingsContainer;
    private final net.minecraft.client.gui.RotatingCubeMapRenderer panoramaRenderer =
        new net.minecraft.client.gui.RotatingCubeMapRenderer(
            new net.minecraft.client.gui.CubeMapRenderer(net.minecraft.util.Identifier.ofVanilla("textures/gui/title/background/panorama"))
        );

    public ClientSettingsScreen() {
        super(Text.literal("Client Settings"));
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

        // ── Outer panel (480x320) ─────────────────────────────────────────────
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(480), Sizing.fixed(320));
        panel.surface(Surface.flat(0xCC1A1A2E));

        // ── Header ────────────────────────────────────────────────────────────
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        header.surface(Surface.flat(0xEE222240));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 14, 0, 14));

        header.child(Components.label(Text.literal("Velora Client - Performance & Client Settings"))
            .color(Color.WHITE)
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x55FF4444, 0x00000000));
        header.child(closeBtn);

        panel.child(header);

        // ── Settings rows ─────────────────────────────────────────────────────
        settingsContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        settingsContainer.padding(Insets.of(10, 20, 10, 20));
        settingsContainer.gap(6);
        buildRows();

        panel.child(settingsContainer);
        root.child(panel);
    }

    private void buildRows() {
        settingsContainer.clearChildren();

        settingsContainer.child(makeRow("Fast Math & Lightweight Render", ModConfig.optiFastMath,
            () -> { ModConfig.optiFastMath = !ModConfig.optiFastMath; ModConfig.saveConfig(); buildRows(); }));

        settingsContainer.child(makeRow("Low Memory Garbage Mode", ModConfig.optiLowMemoryMode,
            () -> { ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode; ModConfig.saveConfig(); buildRows(); }));

        settingsContainer.child(makeRow("Particle Limiter (FPS Boost)", ModConfig.optiLimitParticles,
            () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); buildRows(); }));

        settingsContainer.child(makeRow("Disable Terrain Fog", ModConfig.optiDisableFog,
            () -> { ModConfig.optiDisableFog = !ModConfig.optiDisableFog; ModConfig.saveConfig(); buildRows(); }));

        settingsContainer.child(makeRow("Entity Culling (Occlusion & FPS Boost)", ModConfig.optiEntityCulling,
            () -> { ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling; ModConfig.saveConfig(); buildRows(); }));
    }

    // ── Setting row: label + toggle switch ────────────────────────────────────
    private FlowLayout makeRow(String label, boolean enabled, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
        row.surface(Surface.flat(enabled ? 0x44FFFFFF : 0x22FFFFFF));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(0, 14, 0, 14));

        row.child(Components.label(Text.literal(label))
            .color(Color.WHITE)
            .sizing(Sizing.fill(100), Sizing.content()));

        // Toggle switch — colored rectangle with knob illusion via text
        int swBg = enabled ? 0xFF22C55E : 0xFF71717A;
        ButtonComponent sw = Components.button(Text.literal(enabled ? "ON " : "OFF"), btn -> action.run());
        sw.sizing(Sizing.fixed(32), Sizing.fixed(16));
        sw.renderer(ButtonComponent.Renderer.flat(swBg, swBg, swBg));
        row.child(sw);

        row.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });

        return row;
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.world == null) {
            this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
            context.fill(0, 0, this.width, this.height, 0x88060A12);
        } else {
            context.fill(0, 0, this.width, this.height, 0xAA000000);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
