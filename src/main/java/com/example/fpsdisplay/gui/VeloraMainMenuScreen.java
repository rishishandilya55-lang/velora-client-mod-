package com.example.fpsdisplay.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Velora Client — Overhauled Main Title Screen (Fabric 1.21.4 / owo-lib).
 *
 * Sizing & Scaling Adjustments:
 * - Main Menu 3D Player Preview: Scaled down (Sizing.fixed(42) + scale(0.85f)) so the player
 *   avatar renders small, compact, and perfectly centered next to the main menu buttons.
 */
public class VeloraMainMenuScreen extends BaseOwoScreen<FlowLayout> {

    private static final Identifier LOGO = Identifier.of("fpsdisplay", "textures/gui/logo.png");
    private static final Identifier PANORAMA_PATH = Identifier.of("minecraft", "textures/gui/title/background/panorama");
    private static final int BTN_W = 220;
    private static final int BTN_H = 26;

    // 1. Rotating Panorama Renderer (Vanilla Plains Path)
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
            new CubeMapRenderer(PANORAMA_PATH)
    );

    private EntityComponent<LivingEntity> mainPlayerComponent;

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

        // ── Top Header Bar (Fixed 24px height) ──────────────────────────────────
        FlowLayout topBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        topBar.surface(Surface.flat(0xAA0F1118));
        topBar.verticalAlignment(VerticalAlignment.CENTER);
        topBar.padding(Insets.of(0, 10, 0, 10));

        // Status dot + username label
        FlowLayout userRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        userRow.verticalAlignment(VerticalAlignment.CENTER);
        userRow.gap(5);
        userRow.child(Components.label(Text.literal("●"))
                .color(Color.ofArgb(0xFF22C55E))
                .sizing(Sizing.content(), Sizing.content()));

        String username = (client != null && client.getSession() != null) ? client.getSession().getUsername() : "Player";
        userRow.child(Components.label(Text.literal(username))
                .color(Color.ofArgb(0xFFE2E8F0))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        topBar.child(userRow);

        // Top Spacer
        topBar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        // Action Buttons (⚙ Options | ✕ Exit)
        FlowLayout topActions = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        topActions.gap(6);

        ButtonComponent gearBtn = Components.button(Text.literal("⚙"), btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        });
        gearBtn.sizing(Sizing.fixed(18), Sizing.fixed(16));
        gearBtn.renderer(ButtonComponent.Renderer.flat(0x33FFFFFF, 0x55FFFFFF, 0x33FFFFFF));
        topActions.child(gearBtn);

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> {
            if (client != null) client.scheduleStop();
        });
        closeBtn.sizing(Sizing.fixed(18), Sizing.fixed(16));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x44FF4444, 0x88FF4444, 0x44FF4444));
        topActions.child(closeBtn);

        topBar.child(topActions);
        root.child(topBar);

        // Vertical Spacer
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // ── Main Content Split (Nav Column + Live 3D Player Preview) ───────────
        FlowLayout centerSplit = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        centerSplit.verticalAlignment(VerticalAlignment.CENTER);
        centerSplit.gap(16);

        // Central Navigation Column
        FlowLayout centerCol = Containers.verticalFlow(Sizing.fixed(BTN_W + 20), Sizing.content());
        centerCol.horizontalAlignment(HorizontalAlignment.CENTER);
        centerCol.gap(6);

        // Logo
        centerCol.child(Components.texture(LOGO, 0, 0, 48, 48, 48, 48)
                .sizing(Sizing.fixed(48), Sizing.fixed(48)));

        // Title ("VELORA CLIENT")
        FlowLayout titleBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        titleBox.horizontalAlignment(HorizontalAlignment.CENTER);
        titleBox.child(Components.label(Text.literal("VELORA CLIENT"))
                .color(Color.ofArgb(0xFFFFFFFF))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        centerCol.child(titleBox);

        // Subtitle ("The Velora Experience")
        FlowLayout subTitleBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        subTitleBox.horizontalAlignment(HorizontalAlignment.CENTER);
        subTitleBox.child(Components.label(Text.literal("The Velora Experience"))
                .color(Color.ofArgb(0xFF94A3B8))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        centerCol.child(subTitleBox);

        // Clean subtle divider line
        FlowLayout sep = Containers.horizontalFlow(Sizing.fixed(140), Sizing.fixed(1));
        sep.surface(Surface.flat(0x44334155));
        centerCol.child(sep);

        // Buffer before button list
        centerCol.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(4)));

        // Navigation Buttons
        centerCol.child(makeNavButton("▶  Singleplayer", false, btn -> {
            if (client != null) client.setScreen(new SelectWorldScreen(this));
        }));
        centerCol.child(makeNavButton("⚔  Multiplayer", false, btn -> {
            if (client != null) client.setScreen(new MultiplayerScreen(this));
        }));
        centerCol.child(makeNavButton("✦  Cosmetics Locker", true, btn -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        }));
        centerCol.child(makeNavButton("★  Velora Settings", false, btn -> {
            if (client != null) client.setScreen(new ModMenuScreen());
        }));
        centerCol.child(makeNavButton("⚙  Options", false, btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        }));
        centerCol.child(makeNavButton("✕  Quit Game", false, btn -> {
            if (client != null) client.scheduleStop();
        }));

        centerSplit.child(centerCol);

        // ── Main Menu Live 3D Player Preview Chamber (Small & Proportional) ────
        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity playerEntity = (mc != null && mc.player != null) ? mc.player : null;

        if (playerEntity != null) {
            FlowLayout playerBox = Containers.verticalFlow(Sizing.fixed(140), Sizing.fixed(240));
            playerBox.surface((context, component) -> {
                int x = component.x(), y = component.y(), w = component.width(), h = component.height();
                context.fill(x, y, x + w, y + h, 0xAA141720);
                context.drawBorder(x, y, w, h, 0xFF1F2432);
            });
            playerBox.padding(Insets.of(6));
            playerBox.verticalAlignment(VerticalAlignment.CENTER);
            playerBox.horizontalAlignment(HorizontalAlignment.CENTER);

            // Sizing.fixed(42) + scale(0.85f) for small, compact player avatar
            mainPlayerComponent = Components.entity(Sizing.fixed(42), playerEntity);
            mainPlayerComponent.scale(0.85f);
            mainPlayerComponent.allowMouseRotation(true);
            mainPlayerComponent.showNametag(false);
            playerBox.child(mainPlayerComponent);

            centerSplit.child(playerBox);
        }

        root.child(centerSplit);

        // Vertical Spacer
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // ── Bottom Footer Bar (Fixed 28px height) ──────────────────────────────
        FlowLayout bottomBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        bottomBar.surface(Surface.flat(0xAA0F1118));
        bottomBar.verticalAlignment(VerticalAlignment.CENTER);
        bottomBar.padding(Insets.of(0, 10, 0, 10));

        // Client version
        bottomBar.child(Components.label(Text.literal("Velora Client  1.21.4"))
                .color(Color.ofArgb(0xFF64748B))
                .shadow(false)
                .sizing(Sizing.content(), Sizing.content()));

        bottomBar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()));

        // Keybind hint
        bottomBar.child(Components.label(Text.literal("Client Menu: Right Shift"))
                .color(Color.ofArgb(0xFF64748B))
                .shadow(false)
                .sizing(Sizing.content(), Sizing.content()));

        root.child(bottomBar);
    }

    // ── Helper: Navigation Buttons ───────────────────────────────────────────
    private ButtonComponent makeNavButton(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        btn.sizing(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));

        int normalBg = featured ? 0xDD1E2433 : 0xAA161822;
        int hoverBg  = featured ? 0xFF2563EB : 0xDD222634;

        btn.renderer(ButtonComponent.Renderer.flat(normalBg, hoverBg, normalBg));
        return btn;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
        context.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(context, mouseX, mouseY, delta);
    }
}
