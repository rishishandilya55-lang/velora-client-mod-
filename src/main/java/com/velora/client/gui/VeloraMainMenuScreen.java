package com.velora.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
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

    private static final int BTN_W = 200;
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
        FlowLayout bar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        bar.surface(Surface.flat(SURF2));
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.padding(Insets.of(0, 10, 0, 10));

        FlowLayout user = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        user.verticalAlignment(VerticalAlignment.CENTER);
        user.gap(4);
        user.child(Components.label(Text.literal("+")).color(Color.ofArgb(GREEN)));
        String username = (client != null && client.getSession() != null) ? client.getSession().getUsername() : "Player";
        user.child(Components.label(Text.literal(username)).color(Color.ofArgb(TEXT)).shadow(true));
        bar.child(user);

        bar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        FlowLayout actions = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        actions.gap(3);
        ButtonComponent gearBtn = Components.button(Text.literal("E"), btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        });
        gearBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        gearBtn.renderer(ButtonComponent.Renderer.flat(SURF3, VIOLET_F, SURF3));
        actions.child(gearBtn);
        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> {
            if (client != null) client.scheduleStop();
        });
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(18));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF3, 0x33EF4444, SURF3));
        actions.child(closeBtn);
        bar.child(actions);

        return bar;
    }

    private FlowLayout buildCenter() {
        FlowLayout col = Containers.verticalFlow(Sizing.fixed(BTN_W + 16), Sizing.content());
        col.horizontalAlignment(HorizontalAlignment.CENTER);
        col.gap(4);

        col.child(Components.texture(LOGO, 0, 0, 48, 48, 48, 48)
            .sizing(Sizing.fixed(48), Sizing.fixed(48)));

        FlowLayout titleBox = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(14));
        titleBox.surface(Surface.flat(0x00000000));
        titleBox.horizontalAlignment(HorizontalAlignment.CENTER);
        titleBox.child(Components.label(Text.literal("VELORA CLIENT"))
            .color(Color.ofArgb(VIOLET)).shadow(true)
            .sizing(Sizing.content(), Sizing.content()));
        col.child(titleBox);

        FlowLayout subBox = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(12));
        subBox.surface(Surface.flat(0x00000000));
        subBox.horizontalAlignment(HorizontalAlignment.CENTER);
        subBox.child(Components.label(Text.literal("1.21.4 Fabric Mod"))
            .color(Color.ofArgb(TEXT_M))
            .sizing(Sizing.content(), Sizing.content()));
        col.child(subBox);

        FlowLayout sep = Containers.horizontalFlow(Sizing.fixed(120), Sizing.fixed(1));
        sep.surface(Surface.flat(VIOLET_F));
        col.child(sep);

        col.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(3)));

        col.child(makeNavBtn("Singleplayer", false, btn -> {
            if (client != null) client.setScreen(new SelectWorldScreen(this));
        }));
        col.child(makeNavBtn("Multiplayer", false, btn -> {
            if (client != null) client.setScreen(new MultiplayerScreen(this));
        }));
        col.child(makeNavBtn("Options", false, btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        }));

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));
        bottomRow.gap(8);
        bottomRow.horizontalAlignment(HorizontalAlignment.CENTER);
        bottomRow.child(makeHalfBtn("Cosmetics", true, btn -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));
        bottomRow.child(makeHalfBtn("Quit", false, btn -> {
            if (client != null) client.scheduleStop();
        }));
        col.child(bottomRow);

        return col;
    }

    private FlowLayout buildBottomBar() {
        FlowLayout bar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        bar.surface(Surface.flat(SURF2));
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.padding(Insets.of(0, 10, 0, 10));

        FlowLayout leftLabel = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(10));
        leftLabel.surface(Surface.flat(0x00000000));
        leftLabel.child(Components.label(Text.literal("Velora Client 1.21.4"))
            .color(Color.ofArgb(TEXT_F))
            .sizing(Sizing.content(), Sizing.content()));
        bar.child(leftLabel);

        bar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        FlowLayout rightLabel = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(10));
        rightLabel.surface(Surface.flat(0x00000000));
        rightLabel.child(Components.label(Text.literal("Menu: Right Shift"))
            .color(Color.ofArgb(TEXT_F))
            .sizing(Sizing.content(), Sizing.content()));
        bar.child(rightLabel);

        return bar;
    }

    private ButtonComponent makeNavBtn(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        btn.sizing(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));
        int normalBg = featured ? VIOLET_D : SURF2;
        int hoverBg = featured ? VIOLET_S : SURF3;
        btn.renderer(ButtonComponent.Renderer.flat(normalBg, hoverBg, normalBg));
        return btn;
    }

    private ButtonComponent makeHalfBtn(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        int halfW = (BTN_W - 8) / 2;
        btn.sizing(Sizing.fixed(halfW), Sizing.fixed(BTN_H));
        int normalBg = featured ? VIOLET_D : SURF2;
        int hoverBg = featured ? VIOLET_S : SURF3;
        btn.renderer(ButtonComponent.Renderer.flat(normalBg, hoverBg, normalBg));
        return btn;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
        context.fill(0, 0, this.width, this.height, 0xCC08080A);
        super.render(context, mouseX, mouseY, delta);
    }
}
