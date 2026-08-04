package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ClientSettingsScreen extends BaseOwoScreen<FlowLayout> {

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
    private static final int VIOLET_D = 0xFF6D28D9;
    private static final int GREEN    = 0xFF34D399;
    private static final int GREEN_D  = 0xFF166534;
    private static final int RED      = 0xFFEF4444;

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

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(440), Sizing.fixed(360));
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
        header.gap(8);

        header.child(Components.label(Text.literal("VELORA"))
            .color(Color.ofArgb(VIOLET))
            .shadow(true));
        header.child(Components.label(Text.literal("Performance"))
            .color(Color.ofArgb(TEXT))
            .shadow(true));

        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        int enabledCount = getEnabledCount();
        if (enabledCount > 0) {
            FlowLayout badge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(14));
            badge.surface(Surface.flat(GREEN_D));
            badge.verticalAlignment(VerticalAlignment.CENTER);
            badge.padding(Insets.of(0, 5, 0, 5));
            badge.child(Components.label(Text.literal(enabledCount + " active")).color(Color.ofArgb(GREEN)));
            header.child(badge);
        }

        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        header.child(closeBtn);

        panel.child(header);

        settingsContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        settingsContainer.padding(Insets.of(6, 12, 6, 12));
        settingsContainer.gap(2);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), settingsContainer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
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

        settingsContainer.child(makeToggleRow("Fast Math", "Faster math calculations, minor quality tradeoff",
            ModConfig.optiFastMath,
            () -> { ModConfig.optiFastMath = !ModConfig.optiFastMath; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Disable Fog", "Remove distance fog for clearer vision",
            ModConfig.optiDisableFog,
            () -> { ModConfig.optiDisableFog = !ModConfig.optiDisableFog; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Entity Culling", "Skip rendering hidden entities",
            ModConfig.optiEntityCulling,
            () -> { ModConfig.optiEntityCulling = !ModConfig.optiEntityCulling; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeSectionHeader("Performance"));

        settingsContainer.child(makeToggleRow("Low Memory Mode", "Reduce memory usage at cost of quality",
            ModConfig.optiLowMemoryMode,
            () -> { ModConfig.optiLowMemoryMode = !ModConfig.optiLowMemoryMode; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(makeToggleRow("Particle Limiter", "Cap particle count for FPS boost",
            ModConfig.optiLimitParticles,
            () -> { ModConfig.optiLimitParticles = !ModConfig.optiLimitParticles; ModConfig.saveConfig(); buildSections(); }));

        settingsContainer.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(8)));
    }

    private FlowLayout makeSectionHeader(String title) {
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.padding(Insets.of(4, 0, 2, 0));
        header.child(Components.label(Text.literal(title.toUpperCase()))
            .color(Color.ofArgb(TEXT_F)));
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        return header;
    }

    private FlowLayout makeToggleRow(String label, String description, boolean enabled, Runnable action) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(34));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, enabled ? 0x0AFFFFFF : 0x00000000);
            if (enabled) ctx.fill(x, y, x + 2, y + h, VIOLET);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 8, 2, 8));
        row.gap(6);

        FlowLayout info = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        info.gap(1);
        info.child(Components.label(Text.literal(label))
            .color(Color.ofArgb(enabled ? TEXT : TEXT_M)));
        info.child(Components.label(Text.literal(description))
            .color(Color.ofArgb(TEXT_F))
            .sizing(Sizing.fill(100), Sizing.content()));
        row.child(info);

        ButtonComponent toggle = Components.button(Text.literal(""), btn -> action.run());
        toggle.sizing(Sizing.fixed(28), Sizing.fixed(14));
        toggle.renderer((ctx, comp, delta) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int trackBg = enabled ? GREEN_D : SURF3;
            ctx.fill(x, y, x + w, y + h, trackBg);
            ctx.drawBorder(x, y, w, h, enabled ? GREEN : BORDER_S);
            int knobX = enabled ? x + w - 10 : x + 2;
            ctx.fill(knobX, y + 2, knobX + 8, y + h - 2, enabled ? GREEN : TEXT_F);
        });
        row.child(toggle);

        row.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) { action.run(); return true; }
            return false;
        });

        return row;
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, BG);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
