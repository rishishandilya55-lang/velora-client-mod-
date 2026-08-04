package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import com.example.fpsdisplay.gui.cosmetic.CosmeticItem;
import com.example.fpsdisplay.gui.cosmetic.CosmeticTextureCache;
import com.example.fpsdisplay.gui.cosmetic.MannequinModelRenderer;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Velora Client — Velora Locker 3-Panel Cosmetic UI (Fabric 1.21.4 / owo-lib).
 *
 * Sizing & Scaling Adjustments:
 * - Panel 3 Avatar Scale: EntityComponent size parameter set to Sizing.fixed(42) with scale(0.85f)
 *   so the full player model fits cleanly and compactly inside the preview chamber frame.
 * - Card Mini-Previews: 3D mannequin card viewports render custom velora_cape.png and mojang_cape.png textures.
 */
public class CosmeticsLockerScreen extends BaseOwoScreen<FlowLayout> {

    // Background Panorama Fallback System (Vanilla Plains Path)
    private static final Identifier PANORAMA_PATH = Identifier.of("minecraft", "textures/gui/title/background/panorama");
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(new CubeMapRenderer(PANORAMA_PATH));

    // Global Active State
    private static boolean isLockerOpen = false;
    private static int selectedCapeIndex = 0;
    private String searchQuery = "";
    private CosmeticItem.Category selectedCategory = CosmeticItem.Category.ALL;
    private EnvironmentTab currentEnv = EnvironmentTab.DEFAULT;
    private boolean showPanoramaBackground = false;

    public enum EnvironmentTab {
        DEFAULT("Default", 0xFF0D0F17, 0xFF141724),
        WORLD("World", 0xFF0B1B10, 0xFF102617),
        NETHER("Nether", 0xFF220B0B, 0xFF301010),
        END("End", 0xFF160A24, 0xFF200E33);

        private final String label;
        private final int bgColor;
        private final int borderColor;

        EnvironmentTab(String label, int bgColor, int borderColor) {
            this.label = label;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
        }

        public String getLabel() { return label; }
        public int getBgColor() { return bgColor; }
        public int getBorderColor() { return borderColor; }
    }

    // Components & Containers
    private FlowLayout cardGrid;
    private ScrollContainer<FlowLayout> scrollContainer;
    private TextBoxComponent searchInput;
    private EntityComponent<LivingEntity> playerEntityComponent;

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Client — Velora Locker"));
    }

    public static boolean isPreviewingCape() { return isLockerOpen; }
    public static int getPreviewingCapeIndex() { return isLockerOpen ? selectedCapeIndex : -1; }

    @Override
    public void close() {
        isLockerOpen = false;
        super.close();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        isLockerOpen = true;

        // Reset and initialize clean 2-cape test registry (Velora Cape & Mojang Cape)
        CosmeticTextureCache.init();

        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Main Canvas Box (Constrained to 780px) ─────────────────────────────
        int canvasWidth = Math.min(this.width - 16, 780);
        int canvasHeight = Math.min(this.height - 16, 480);

        FlowLayout mainCanvas = Containers.verticalFlow(Sizing.fixed(canvasWidth), Sizing.fixed(canvasHeight));
        mainCanvas.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF11131A);
            context.drawBorder(x, y, w, h, 0xFF1E222E);
            context.drawBorder(x + 1, y + 1, w - 2, h - 2, 0xFF171A24);
        });
        mainCanvas.padding(Insets.of(8));
        mainCanvas.gap(6);

        // ── Top Header Bar ("Velora Locker" Branding) ─────────────────────────
        FlowLayout topHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        topHeader.verticalAlignment(VerticalAlignment.CENTER);
        topHeader.padding(Insets.of(0, 4, 0, 4));

        // Logo + Brand Title ("Velora Locker")
        FlowLayout titleGroup = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        titleGroup.verticalAlignment(VerticalAlignment.CENTER);
        titleGroup.gap(6);
        titleGroup.child(Components.label(Text.literal("🍃"))
                .color(Color.ofArgb(0xFF22C55E))
                .sizing(Sizing.content(), Sizing.content()));
        titleGroup.child(Components.label(Text.literal("Velora Locker"))
                .color(Color.ofArgb(0xFFF1F5F9))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        topHeader.child(titleGroup);

        // Header Spacer
        topHeader.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        // Action Buttons (? Help | ✕ Close)
        FlowLayout headerActions = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        headerActions.gap(6);

        ButtonComponent helpBtn = Components.button(Text.literal("? Help"), btn -> {});
        helpBtn.sizing(Sizing.fixed(60), Sizing.fixed(20));
        helpBtn.renderer(ButtonComponent.Renderer.flat(0xFF1B1F2A, 0xFF282E3D, 0xFF1B1F2A));
        headerActions.child(helpBtn);

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(0xFF1B1F2A, 0xFFDC2626, 0xFF1B1F2A));
        headerActions.child(closeBtn);

        topHeader.child(headerActions);
        mainCanvas.child(topHeader);

        // ── 3-PANEL SPLIT WINDOW (Explicit Panel Bounds: 160px | 380px | 200px) ─
        FlowLayout bodySplit = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
        bodySplit.gap(6);

        // ── PANEL 1: Far-Left Category Navigation Sidebar (160px Wide) ────────
        FlowLayout panel1 = Containers.verticalFlow(Sizing.fixed(160), Sizing.fill(100));
        panel1.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF151821);
            context.drawBorder(x, y, w, h, 0xFF1F2432);
        });
        panel1.padding(Insets.of(6, 4, 6, 4));
        panel1.gap(4);

        // Category List Rows
        for (CosmeticItem.Category cat : CosmeticItem.Category.values()) {
            final CosmeticItem.Category category = cat;

            long count = CosmeticTextureCache.getItems().stream()
                    .filter(item -> category == CosmeticItem.Category.ALL ||
                            (category == CosmeticItem.Category.FAVORITES ? item.isFavorite() : item.getCategory() == category))
                    .count();

            String labelText = category.getDisplayName() + " (" + count + ")";

            FlowLayout catRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
            catRow.verticalAlignment(VerticalAlignment.CENTER);
            catRow.padding(Insets.of(0, 6, 0, 6));

            catRow.surface((context, component) -> {
                int x = component.x(), y = component.y(), w = component.width(), h = component.height();
                boolean isSelected = (selectedCategory == category);
                if (isSelected) {
                    context.fill(x, y, x + w, y + h, 0xFF1E2433);
                }
            });

            // Icon + Category Name
            catRow.child(Components.label(Text.literal(category.getIcon() + "  " + labelText))
                    .color(Color.ofArgb(selectedCategory == category ? 0xFFFFFFFF : 0xFF94A3B8))
                    .shadow(false)
                    .sizing(Sizing.content(), Sizing.content()));

            // Spacer
            catRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

            // Colored Indicator Dot
            if (category.getDotColor() != null) {
                FlowLayout dot = Containers.horizontalFlow(Sizing.fixed(6), Sizing.fixed(6));
                final int dotClr = category.getDotColor();
                dot.surface((context, component) -> {
                    int x = component.x(), y = component.y();
                    context.fill(x, y, x + 6, y + 6, dotClr);
                });
                catRow.child(dot);
            }

            catRow.mouseDown().subscribe((mx, my, btn) -> {
                if (btn == 0) {
                    selectedCategory = category;
                    rebuildCardGrid();
                    return true;
                }
                return false;
            });

            panel1.child(catRow);
        }

        // Sidebar Spacer
        panel1.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));

        // Footer Settings Button
        ButtonComponent settingsBtn = Components.button(Text.literal("⚙ Settings"), btn -> {});
        settingsBtn.sizing(Sizing.fill(100), Sizing.fixed(24));
        settingsBtn.renderer(ButtonComponent.Renderer.flat(0xFF1B1F2A, 0xFF282E3D, 0xFF1B1F2A));
        panel1.child(settingsBtn);

        // Footer Shop Cosmetics CTA Button (Full-width vibrant purple)
        ButtonComponent shopBtn = Components.button(Text.literal("↗ Shop Cosmetics"), btn -> {});
        shopBtn.sizing(Sizing.fill(100), Sizing.fixed(26));
        shopBtn.renderer(ButtonComponent.Renderer.flat(0xFF7C3AED, 0xFF8B5CF6, 0xFF6D28D9));
        panel1.child(shopBtn);

        bodySplit.child(panel1);

        // ── PANEL 2: Central Cosmetic Item Grid (380px Wide Container) ──────────
        FlowLayout panel2 = Containers.verticalFlow(Sizing.fixed(380), Sizing.fill(100));
        panel2.gap(6);

        // Top Search Bar Block
        FlowLayout searchBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
        searchBox.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, 0xFF161922);
            context.drawBorder(x, y, w, h, 0xFF222632);
        });
        searchBox.padding(Insets.of(4, 8, 4, 8));
        searchBox.verticalAlignment(VerticalAlignment.CENTER);
        searchBox.gap(6);

        searchBox.child(Components.label(Text.literal("🔍"))
                .color(Color.ofArgb(0xFF64748B))
                .sizing(Sizing.content(), Sizing.content()));

        searchInput = Components.textBox(Sizing.fill(100), "Search cosmetics...");
        searchInput.sizing(Sizing.fill(100), Sizing.fixed(20));
        searchInput.onChanged().subscribe(val -> {
            searchQuery = val.toLowerCase().trim();
            rebuildCardGrid();
        });
        searchBox.child(searchInput);

        panel2.child(searchBox);

        // 3-Column Scrollable Grid Flow
        cardGrid = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        cardGrid.gap(6);
        buildCardGrid();

        scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), cardGrid);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xFF282E3D)));
        panel2.child(scrollContainer);

        bodySplit.child(panel2);

        // ── PANEL 3: Live 3D Player Preview Chamber (Scaled Small & Compact) ────
        FlowLayout panel3 = Containers.verticalFlow(Sizing.fixed(200), Sizing.fill(100));
        panel3.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            context.fill(x, y, x + w, y + h, currentEnv.getBgColor());
            context.drawBorder(x, y, w, h, currentEnv.getBorderColor());
        });
        panel3.padding(Insets.of(6));
        panel3.gap(4);

        // Top Environmental Selection Tabs (Default, World, Nether, End)
        FlowLayout envTabsRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        envTabsRow.verticalAlignment(VerticalAlignment.CENTER);
        envTabsRow.horizontalAlignment(HorizontalAlignment.RIGHT);
        envTabsRow.gap(2);

        for (EnvironmentTab tab : EnvironmentTab.values()) {
            final EnvironmentTab targetTab = tab;
            ButtonComponent envBtn = Components.button(Text.literal(tab.getLabel()), btn -> {
                currentEnv = targetTab;
            });
            envBtn.sizing(Sizing.fixed(44), Sizing.fixed(18));
            envBtn.renderer(ButtonComponent.Renderer.flat(0xFF161924, 0xFF252A3B, 0xFF161924));
            envTabsRow.child(envBtn);
        }
        panel3.child(envTabsRow);

        // 3D Player Viewport Frame (Sizing.fixed(42) + scale(0.85f) for small, compact player avatar)
        FlowLayout previewViewport = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        previewViewport.verticalAlignment(VerticalAlignment.CENTER);
        previewViewport.horizontalAlignment(HorizontalAlignment.CENTER);

        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity playerEntity = (mc != null && mc.player != null) ? mc.player : null;

        if (playerEntity != null) {
            // Sizing.fixed(42) with scale(0.85f) renders a small, fully proportional player model
            playerEntityComponent = Components.entity(Sizing.fixed(42), playerEntity);
            playerEntityComponent.scale(0.85f);
            playerEntityComponent.allowMouseRotation(true);
            playerEntityComponent.showNametag(false);
            previewViewport.child(playerEntityComponent);
        } else {
            previewViewport.child(Components.label(Text.literal("Player Preview\nAvailable In-Game"))
                    .color(Color.ofArgb(0xFF64748B))
                    .sizing(Sizing.content(), Sizing.content()));
        }

        panel3.child(previewViewport);

        // Bottom Helper Text
        FlowLayout helperRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        helperRow.verticalAlignment(VerticalAlignment.CENTER);
        helperRow.horizontalAlignment(HorizontalAlignment.CENTER);
        helperRow.child(Components.label(Text.literal("Select a cosmetic to preview it"))
                .color(Color.ofArgb(0xFF64748B))
                .sizing(Sizing.content(), Sizing.content()));
        panel3.child(helperRow);

        bodySplit.child(panel3);
        mainCanvas.child(bodySplit);
        root.child(mainCanvas);
    }

    // ── Build 3-Column Cosmetic Card Grid ────────────────────────────────────
    private void buildCardGrid() {
        cardGrid.clearChildren();

        List<CosmeticItem> items = new ArrayList<>(CosmeticTextureCache.getItems());

        // Category Filter
        if (selectedCategory == CosmeticItem.Category.FAVORITES) {
            items.removeIf(item -> !item.isFavorite());
        } else if (selectedCategory != CosmeticItem.Category.ALL) {
            items.removeIf(item -> item.getCategory() != selectedCategory);
        }

        // Search Query Filter
        if (!searchQuery.isEmpty()) {
            items.removeIf(item -> !item.getName().toLowerCase().contains(searchQuery));
        }

        FlowLayout currentRow = null;
        int colCount = 0;
        final int COLS = 3;

        for (int i = 0; i < items.size(); i++) {
            CosmeticItem item = items.get(i);

            if (colCount == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.gap(6);
                cardGrid.child(currentRow);
            }

            final int itemIndex = i;
            FlowLayout card = buildItemCard(item, itemIndex);
            if (currentRow != null) {
                currentRow.child(card);
            }

            colCount++;
            if (colCount >= COLS) colCount = 0;
        }
    }

    // ── Build Individual Card Component ──────────────────────────────────────
    private FlowLayout buildItemCard(CosmeticItem item, int idx) {
        boolean isSelected = (selectedCapeIndex == idx);

        FlowLayout card = Containers.verticalFlow(Sizing.fixed(116), Sizing.fixed(138));

        card.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();
            int bg = isSelected ? 0xFF1E2433 : 0xFF151821;
            int border = isSelected ? 0xFF3B82F6 : 0xFF1F2432;

            context.fill(x, y, x + w, y + h, bg);
            context.drawBorder(x, y, w, h, border);
        });
        card.padding(Insets.of(3));
        card.gap(3);

        // 3D Mini-Mannequin Viewport Rendering velora_cape.png / classic_cape.png
        FlowLayout mannequinViewport = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(106));
        mannequinViewport.surface((context, component) -> {
            int x = component.x(), y = component.y(), w = component.width(), h = component.height();

            // Render 3D mini mannequin showing actual cape texture preview
            MannequinModelRenderer.renderMannequinCard(context, x, y, w, h, item, false);

            // Render Top-Left Star Favorite Icon
            int starColor = item.isFavorite() ? 0xFFFFD700 : 0xFF475569;
            context.drawText(this.textRenderer, "★", x + 5, y + 5, starColor, true);
        });

        // Click Handler: Selects Velora Cape or Mojang Cape & updates preview immediately
        mannequinViewport.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                if (mx <= mannequinViewport.x() + 20 && my <= mannequinViewport.y() + 20) {
                    item.setFavorite(!item.isFavorite());
                    rebuildCardGrid();
                    return true;
                }
                selectedCapeIndex = idx;
                if (item.getType() == CosmeticItem.CosmeticType.CAPE) {
                    ModConfig.enableCape = true;
                    ModConfig.selectedCape = idx;
                    ModConfig.saveConfig();
                }
                rebuildCardGrid();
                return true;
            }
            return false;
        });

        card.child(mannequinViewport);

        // High-Contrast Centered White Item Name Text
        FlowLayout labelBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        labelBox.verticalAlignment(VerticalAlignment.CENTER);
        labelBox.horizontalAlignment(HorizontalAlignment.CENTER);

        labelBox.child(Components.label(Text.literal(item.getName()))
                .color(Color.ofArgb(0xFFFFFFFF))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));

        card.child(labelBox);

        return card;
    }

    private void rebuildCardGrid() {
        buildCardGrid();
    }

    // ── Render Background & Fallback Panorama ────────────────────────────────
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (showPanoramaBackground) {
            panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
            context.fill(0, 0, this.width, this.height, 0x88000000);
        } else {
            context.fill(0, 0, this.width, this.height, 0xCC090A0F);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
