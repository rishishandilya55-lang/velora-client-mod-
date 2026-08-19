package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ClientSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private FlowLayout settingsContainer;

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

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(460), Sizing.fixed(380));
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            VeloraRenderUtil.drawSolidPanel(ctx, x, y, w, h, VeloraColors.SURF2, VeloraColors.BORDER_S);
        });
        panel.padding(Insets.none());

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        header.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF3);
            ctx.fill(x, y + h - 1, x + w, y + h, VeloraColors.DIVIDER);
        });
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(0, 14, 0, 14));
        header.gap(8);

        header.child(Components.label(Text.literal("VELORA"))
            .color(Color.ofArgb(VeloraColors.VIOLET))
            .shadow(true));
        header.child(Components.label(Text.literal("Performance"))
            .color(Color.ofArgb(VeloraColors.TEXT))
            .shadow(true));

        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        int enabledCount = getEnabledCount();
        if (enabledCount > 0) {
            FlowLayout badge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(16));
            badge.surface(Surface.flat(VeloraColors.GREEN_F));
            badge.verticalAlignment(VerticalAlignment.CENTER);
            badge.padding(Insets.of(0, 6, 0, 6));
            badge.child(Components.label(Text.literal(enabledCount + " active")).color(Color.ofArgb(VeloraColors.GREEN)));
            header.child(badge);
        }

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED_F, VeloraColors.SURF3));
        header.child(closeBtn);

        panel.child(header);

        settingsContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        settingsContainer.padding(Insets.of(8, 14, 8, 14));
        settingsContainer.gap(4);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), settingsContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        buildSections();
        panel.child(scroll);

        root.child(panel);
    }

    private int getEnabledCount() {
        int c = 0;
        if (ModConfig.optiFastMath) c++;
        if (ModConfig.optiLowMemoryMode) c++;
        if (ModConfig.optiLimitParticles) c++;
        if (ModConfig.optiDisableFog) c++;
        if (ModConfig.optiEntityCulling) c++;
        return c;
    }

    private void buildSections() {
        settingsContainer.clearChildren();

        settingsContainer.child(makeSectionHeader("Rendering"));

        settingsContainer.child(makeToggleRow("Fast Math", "Faster math calculations with minimal precision tradeoff",
            ModConfig.optiFastMath,
            () -> { ModConfig.optiFastMath = !ModConfig.optiFastMath; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Disable Fog", "Remove distance fog for clear vision",
            ModConfig.optiDisableFog,
            () -> { ModConfig.optiDisableFog = !ModConfig.optiDisableFog; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Entity Culling", "Skip rendering entities occluded by opaque blocks",
            ModConfig.optiEntityCulling,
            () -> { ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeSectionHeader("Performance"));

        settingsContainer.child(makeToggleRow("Low Memory Mode", "Optimize memory buffers and garbage collection",
            ModConfig.optiLowMemoryMode,
            () -> { ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Particle Limiter", "Cap maximum simultaneous particles on screen",
            ModConfig.optiLimitParticles,
            () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(12)));
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(6, 2, 2, 2));
        header.child(Components.label(Text.literal(title.toUpperCase()))
            .color(Color.ofArgb(VeloraColors.VIOLET)));
        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));
        return header;
    }

    private FlowLayout makeToggleRow(String label, String description, boolean enabled, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(38));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, enabled ? VeloraColors.CARD_ACTIVE : VeloraColors.CARD_BG);
            if (enabled) ctx.fill(x, y, x + 3, y + h, VeloraColors.VIOLET);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 10, 2, 10));
        row.gap(8);

        FlowLayout info = Containers.verticalFlow(Sizing.content(), Sizing.content());
        info.gap(2);
        info.child(Components.label(Text.literal(label))
            .color(Color.ofArgb(enabled ? VeloraColors.TEXT : VeloraColors.TEXT_M)));
        info.child(Components.label(Text.literal(description))
            .color(Color.ofArgb(VeloraColors.TEXT_F)));
        row.child(info);

        row.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout toggle = Containers.horizontalFlow(Sizing.fixed(28), Sizing.fixed(14));
        toggle.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            VeloraRenderUtil.drawToggleSwitch(ctx, x, y, w, h, enabled);
        });
        row.child(toggle);

        row.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });

        return row;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, VeloraColors.BG_OVERLAY);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
