package com.velora.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class VeloraMainMenuScreen extends BaseOwoScreen<FlowLayout> {

    private static final Identifier LOGO = Identifier.of("velora", "textures/gui/logo.png");
    private static final Identifier PANORAMA = Identifier.of("minecraft", "textures/gui/title/background/panorama");

    private static final int BTN_W = 210;
    private static final int BTN_H = 26;

    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(new CubeMapRenderer(PANORAMA));

    public VeloraMainMenuScreen() {
        super(Text.literal("Velora Client"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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

        root.child(buildTopBar());
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));
        root.child(buildCenter());
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));
        root.child(buildBottomBar());
    }

    private FlowLayout buildTopBar() {
        FlowLayout bar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
        bar.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF2);
            ctx.fill(comp.x(), comp.y() + comp.height() - 1, comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.DIVIDER);
        });
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.padding(Insets.of(0, 16, 0, 16));

        FlowLayout user = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        user.verticalAlignment(VerticalAlignment.CENTER);
        user.gap(6);
        user.child(Components.label(Text.literal("●")).color(Color.ofArgb(VeloraColors.GREEN)));
        String username = (client != null && client.getSession() != null) ? client.getSession().getUsername() : "Player";
        user.child(Components.label(Text.literal(username)).color(Color.ofArgb(VeloraColors.TEXT)).shadow(true));
        bar.child(user);

        bar.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout actions = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        actions.gap(6);

        ButtonComponent modsQuickBtn = Components.button(Text.literal("Mods"), btn -> {
            if (client != null) client.setScreen(new ModMenuScreen());
        });
        modsQuickBtn.sizing(Sizing.fixed(48), Sizing.fixed(20));
        modsQuickBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.VIOLET_F, VeloraColors.SURF3));
        actions.child(modsQuickBtn);

        ButtonComponent optionsBtn = Components.button(Text.literal("Settings"), btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        });
        optionsBtn.sizing(Sizing.fixed(56), Sizing.fixed(20));
        optionsBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.SURF4, VeloraColors.SURF3));
        actions.child(optionsBtn);

        ButtonComponent closeBtn = Components.button(Text.literal("Exit"), btn -> {
            if (client != null) client.scheduleStop();
        });
        closeBtn.sizing(Sizing.fixed(40), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED_F, VeloraColors.SURF3));
        actions.child(closeBtn);

        bar.child(actions);
        return bar;
    }

    private FlowLayout buildCenter() {
        FlowLayout col = Containers.verticalFlow(Sizing.fixed(BTN_W + 24), Sizing.content());
        col.horizontalAlignment(HorizontalAlignment.CENTER);
        col.gap(6);

        col.child(Components.texture(LOGO, 0, 0, 48, 48, 48, 48)
            .sizing(Sizing.fixed(48), Sizing.fixed(48)));

        FlowLayout titleBox = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(16));
        titleBox.surface(Surface.flat(0x00000000));
        titleBox.horizontalAlignment(HorizontalAlignment.CENTER);
        titleBox.child(Components.label(Text.literal("VELORA CLIENT"))
            .color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true)
            .sizing(Sizing.content(), Sizing.content()));
        col.child(titleBox);

        FlowLayout subBox = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(14));
        subBox.surface(Surface.flat(0x00000000));
        subBox.horizontalAlignment(HorizontalAlignment.CENTER);
        subBox.child(Components.label(Text.literal("Minecraft 1.21.4"))
            .color(Color.ofArgb(VeloraColors.TEXT_M))
            .sizing(Sizing.content(), Sizing.content()));
        col.child(subBox);

        FlowLayout sep = Containers.horizontalFlow(Sizing.fixed(100), Sizing.fixed(1));
        sep.surface(Surface.flat(VeloraColors.DIVIDER));
        col.child(sep);

        col.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(4)));

        col.child(makeNavBtn("Singleplayer", false, btn -> {
            if (client != null) client.setScreen(new SelectWorldScreen(this));
        }));
        col.child(makeNavBtn("Multiplayer", false, btn -> {
            if (client != null) client.setScreen(new MultiplayerScreen(this));
        }));
        col.child(makeNavBtn("Velora Mods", true, btn -> {
            if (client != null) client.setScreen(new ModMenuScreen());
        }));
        col.child(makeNavBtn("Cosmetics Locker", false, btn -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));
        bottomRow.gap(8);
        bottomRow.horizontalAlignment(HorizontalAlignment.CENTER);
        bottomRow.child(makeHalfBtn("Options", false, btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        }));
        bottomRow.child(makeHalfBtn("Quit Game", false, btn -> {
            if (client != null) client.scheduleStop();
        }));
        col.child(bottomRow);

        return col;
    }

    private FlowLayout buildBottomBar() {
        FlowLayout bar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        bar.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF2);
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + 1, VeloraColors.DIVIDER);
        });
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.padding(Insets.of(0, 16, 0, 16));

        FlowLayout leftLabel = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(12));
        leftLabel.surface(Surface.flat(0x00000000));
        leftLabel.child(Components.label(Text.literal("Velora Client v1.0.0"))
            .color(Color.ofArgb(VeloraColors.TEXT_F))
            .sizing(Sizing.content(), Sizing.content()));
        bar.child(leftLabel);

        bar.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        FlowLayout rightLabel = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(12));
        rightLabel.surface(Surface.flat(0x00000000));
        rightLabel.child(Components.label(Text.literal("Press Right Shift to open Mod Menu"))
            .color(Color.ofArgb(VeloraColors.TEXT_F))
            .sizing(Sizing.content(), Sizing.content()));
        bar.child(rightLabel);

        return bar;
    }

    private ButtonComponent makeNavBtn(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        btn.sizing(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));
        int normalBg = featured ? VeloraColors.VIOLET_D : VeloraColors.SURF2;
        int hoverBg = featured ? VeloraColors.VIOLET_S : VeloraColors.SURF3;
        btn.renderer(ButtonComponent.Renderer.flat(normalBg, hoverBg, normalBg));
        return btn;
    }

    private ButtonComponent makeHalfBtn(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        int halfW = (BTN_W - 8) / 2;
        btn.sizing(Sizing.fixed(halfW), Sizing.fixed(BTN_H));
        int normalBg = featured ? VeloraColors.VIOLET_D : VeloraColors.SURF2;
        int hoverBg = featured ? VeloraColors.VIOLET_S : VeloraColors.SURF3;
        btn.renderer(ButtonComponent.Renderer.flat(normalBg, hoverBg, normalBg));
        return btn;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
        context.fillGradient(0, 0, this.width, this.height, 0xD0090A0F, 0xF0090A0F);
        super.render(context, mouseX, mouseY, delta);
    }
}
