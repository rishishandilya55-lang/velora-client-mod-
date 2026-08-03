package com.example.fpsdisplay.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * VeloraMainMenuScreen — OwoLib remake with rotating panorama background.
 */
public class VeloraMainMenuScreen extends BaseOwoScreen<FlowLayout> {

    private static final Identifier LOGO = Identifier.of("fpsdisplay", "textures/gui/logo.png");
    private static final Identifier BG_TEX = Identifier.of("fpsdisplay", "textures/gui/background.png");
    private static final int BTN_W = 230;
    private static final int BTN_H = 28;
    private float animTick = 0f;

    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(
        new net.minecraft.client.gui.CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"))
    );

    public VeloraMainMenuScreen() {
        super(Text.literal("Velora Client"));
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000)); // transparent — background drawn in render()
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Top bar (22px fixed at top) ───────────────────────────────────────
        FlowLayout topBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        topBar.surface(Surface.flat(0xAA060A12));
        topBar.verticalAlignment(VerticalAlignment.CENTER);
        topBar.padding(Insets.of(0, 8, 0, 8));

        // Status dot + username label
        FlowLayout userRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        userRow.verticalAlignment(VerticalAlignment.CENTER);
        userRow.gap(4);
        userRow.child(Components.label(Text.literal("●")).color(Color.ofArgb(0xFF22C55E)).sizing(Sizing.content(), Sizing.content()));
        userRow.child(Components.label(Text.literal(
            (client != null && client.getSession() != null) ? client.getSession().getUsername() : "Player"))
            .color(Color.ofArgb(0xFFDDDDDD))
            .sizing(Sizing.content(), Sizing.content()));
        topBar.child(userRow);

        topBar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content())); // spacer

        // ⚙ gear button
        ButtonComponent gearBtn = Components.button(Text.literal("⚙"), btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        });
        gearBtn.sizing(Sizing.fixed(16), Sizing.fixed(14));
        gearBtn.renderer(ButtonComponent.Renderer.flat(0x22FFFFFF, 0x44FFFFFF, 0x22FFFFFF));
        topBar.child(gearBtn);

        // × close button
        ButtonComponent closeBtn = Components.button(Text.literal("×"), btn -> {
            if (client != null) client.scheduleStop();
        });
        closeBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0x33FF4444, 0x66FF4444, 0x33FF4444));
        topBar.child(closeBtn);

        root.child(topBar);

        // ── Spacer to push content to center ─────────────────────────────────
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // ── Center section ────────────────────────────────────────────────────
        FlowLayout centerCol = Containers.verticalFlow(Sizing.fixed(BTN_W + 40), Sizing.content());
        centerCol.horizontalAlignment(HorizontalAlignment.CENTER);
        centerCol.gap(6);

        // Logo
        centerCol.child(Components.texture(LOGO, 0, 0, 48, 48, 48, 48)
            .sizing(Sizing.fixed(48), Sizing.fixed(48)));

        // Title
        centerCol.child(Components.label(Text.literal("VELORA CLIENT"))
            .color(Color.WHITE)
            .shadow(true)
            .sizing(Sizing.fill(100), Sizing.content()));

        // Subtitle
        centerCol.child(Components.label(Text.literal("The Velora Experience"))
            .color(Color.ofArgb(0xFF8888AA))
            .sizing(Sizing.fill(100), Sizing.content()));

        // Separator
        FlowLayout sep = Containers.horizontalFlow(Sizing.fixed(140), Sizing.fixed(1));
        sep.surface(Surface.flat(0x44A855F7));
        centerCol.child(sep);

        // Main nav buttons
        centerCol.child(makeNavButton("▶  Singleplayer", false, btn -> {
            if (client != null) client.setScreen(new SelectWorldScreen(this));
        }));
        centerCol.child(makeNavButton("⚔  Multiplayer", false, btn -> {
            if (client != null) client.setScreen(new MultiplayerScreen(this));
        }));
        centerCol.child(makeNavButton("★  Velora Settings", true, btn -> {
            if (client != null) client.setScreen(new ModMenuScreen());
        }));
        centerCol.child(makeNavButton("⚙  Options", false, btn -> {
            if (client != null) client.setScreen(new OptionsScreen(this, client.options));
        }));

        // Quit row
        ButtonComponent quitBtn = Components.button(Text.literal("Quit Game"), btn -> {
            if (client != null) client.scheduleStop();
        });
        quitBtn.sizing(Sizing.fixed(110), Sizing.fixed(16));
        quitBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x00000000, 0x00000000));
        centerCol.child(quitBtn);

        root.child(centerCol);

        // ── Spacer ────────────────────────────────────────────────────────────
        root.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // ── Bottom bar (28px) ─────────────────────────────────────────────────
        FlowLayout bottomBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        bottomBar.surface(Surface.flat(0xAA060A12));
        bottomBar.verticalAlignment(VerticalAlignment.CENTER);
        bottomBar.padding(Insets.of(0, 10, 0, 10));

        // Version text
        bottomBar.child(Components.label(Text.literal("Velora Client  1.21.4"))
            .color(Color.ofArgb(0xFF445566))
            .sizing(Sizing.content(), Sizing.content()));

        bottomBar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content())); // spacer

        // Cosmetics locker button
        ButtonComponent cosmBtn = Components.button(Text.literal("✦  Cosmetics Locker"), btn -> {
            if (client != null) client.setScreen(new CosmeticsLockerScreen());
        });
        cosmBtn.sizing(Sizing.fixed(140), Sizing.fixed(20));
        cosmBtn.renderer(ButtonComponent.Renderer.flat(0xBB180D2E, 0xCC2A1545, 0xBB180D2E));
        bottomBar.child(cosmBtn);

        bottomBar.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content())); // spacer

        // Keybind hint
        bottomBar.child(Components.label(Text.literal("Mods: RShift"))
            .color(Color.ofArgb(0xFF445566))
            .sizing(Sizing.content(), Sizing.content()));

        root.child(bottomBar);
    }

    private ButtonComponent makeNavButton(String label, boolean featured, java.util.function.Consumer<ButtonComponent> action) {
        ButtonComponent btn = Components.button(Text.literal(label), action);
        btn.sizing(Sizing.fixed(BTN_W), Sizing.fixed(BTN_H));
        int bg = featured ? 0xCC111D17 : 0xBB0E111C;
        int hv = featured ? 0xDD1A3322 : 0xCC1A1D28;
        btn.renderer(ButtonComponent.Renderer.flat(bg, hv, bg));
        return btn;
    }

    // ── Raw render for cinematic background ───────────────────────────────────
    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        animTick += delta;

        // 1. Render custom 360 rotating cube map panorama
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);

        // 2. Dark vignette overlay for contrast & readability
        context.fill(0, 0, this.width, this.height, 0x88060A12);

        // 3. Purple ambient glow
        int gx = this.width / 2 - 180, gy = this.height / 2 - 160;
        VeloraRenderUtil.drawGlowBorder(context, gx, gy, 360, 320, 0x18A855F7, 40);
        VeloraRenderUtil.drawGlowBorder(context, gx + 60, gy + 60, 240, 200, 0x10C084FC, 20);

        // 4. Logo
        int logoSize = 48;
        int logoX = this.width / 2 - logoSize / 2;
        int logoY = this.height / 2 - 130;
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(RenderLayer::getGuiTextured, LOGO, logoX, logoY, 0f, 0f, logoSize, logoSize, logoSize, logoSize);

        // 5. OwoLib UI components
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() { animTick += 0.05f; }
}
