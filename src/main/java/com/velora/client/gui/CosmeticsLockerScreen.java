package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.cosmetic.CosmeticItem;
import com.velora.client.gui.cosmetic.CosmeticTextureCache;
import com.velora.client.gui.cosmetic.MannequinModelRenderer;
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsLockerScreen extends BaseOwoScreen<FlowLayout> {

    private static boolean isLockerOpen = false;
    private static int selectedCapeIndex = -1;
    private String searchQuery = "";
    private CosmeticItem.Category selectedCategory = CosmeticItem.Category.ALL;
    private int hoveredCardIndex = -1;

    private FlowLayout cardGrid;

    public static boolean isPreviewingCape() { return isLockerOpen; }
    public static int getPreviewingCapeIndex() { return isLockerOpen ? selectedCapeIndex : -1; }

    public CosmeticsLockerScreen() {
        super(Text.literal("Velora Locker"));
    }

    @Override
    public void close() {
        isLockerOpen = false;
        ModConfig.saveConfig();
        super.close();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        isLockerOpen = true;
        selectedCapeIndex = ModConfig.enableCape ? ModConfig.selectedCape : -1;
        CosmeticTextureCache.init();

        root.verticalAlignment(VerticalAlignment.CENTER);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        int canvasW = Math.min(this.width - 16, 780);
        int canvasH = Math.min(this.height - 16, 480);

        FlowLayout mainCanvas = Containers.verticalFlow(Sizing.fixed(canvasW), Sizing.fixed(canvasH));
        mainCanvas.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF2);
            ctx.drawBorder(x, y, w, h, VeloraColors.BORDER_S);
            ctx.fill(x, y, x + w, y + 1, VeloraColors.VIOLET_F);
        });
        mainCanvas.padding(Insets.of(8));
        mainCanvas.gap(6);

        FlowLayout topHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        topHeader.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF3);
        });
        topHeader.verticalAlignment(VerticalAlignment.CENTER);
        topHeader.padding(Insets.of(0, 8, 0, 8));
        topHeader.child(Components.label(Text.literal("Velora Locker"))
                .color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        topHeader.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF2, 0x33EF4444, VeloraColors.SURF2));
        topHeader.child(closeBtn);
        mainCanvas.child(topHeader);

        FlowLayout bodySplit = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
        bodySplit.gap(6);

        bodySplit.child(buildCategoryPanel());
        bodySplit.child(buildCardGridPanel());
        bodySplit.child(buildPlayerPreviewPanel());

        mainCanvas.child(bodySplit);
        root.child(mainCanvas);
    }

    private FlowLayout buildCategoryPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(140), Sizing.fill(100));
        panel.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF3);
            ctx.drawBorder(comp.x(), comp.y(), comp.width(), comp.height(), VeloraColors.BORDER);
        });
        panel.padding(Insets.of(6, 4, 6, 4));
        panel.gap(2);

        for (CosmeticItem.Category cat : CosmeticItem.Category.values()) {
            final CosmeticItem.Category category = cat;
            long count = CosmeticTextureCache.getItems().stream()
                    .filter(item -> category == CosmeticItem.Category.ALL ||
                            (category == CosmeticItem.Category.FAVORITES ? item.isFavorite() : item.getCategory() == category))
                    .count();
            String labelText = category.getDisplayName() + " (" + count + ")";
            FlowLayout catRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
            catRow.verticalAlignment(VerticalAlignment.CENTER);
            catRow.padding(Insets.of(0, 6, 0, 6));
            catRow.surface((ctx2, comp2) -> {
                if (selectedCategory == category) {
                    ctx2.fill(comp2.x(), comp2.y(), comp2.x() + comp2.width(), comp2.y() + comp2.height(), VeloraColors.SURF2);
                }
            });
            catRow.child(Components.label(Text.literal(category.getIcon() + " " + labelText))
                    .color(Color.ofArgb(selectedCategory == category ? VeloraColors.VIOLET : VeloraColors.TEXT_M))
                    .sizing(Sizing.content(), Sizing.content()));
            catRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
            catRow.mouseDown().subscribe((mx, my, btn) -> {
                if (btn == 0) {
                    selectedCategory = category;
                    MinecraftClient.getInstance().execute(this::rebuildCardGrid);
                    return true;
                }
                return false;
            });
            panel.child(catRow);
        }

        panel.child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)));
        return panel;
    }

    private FlowLayout buildCardGridPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        panel.gap(4);

        FlowLayout searchBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        searchBox.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), VeloraColors.SURF3);
            ctx.drawBorder(comp.x(), comp.y(), comp.width(), comp.height(), VeloraColors.BORDER);
        });
        searchBox.padding(Insets.of(2, 6, 2, 6));
        searchBox.verticalAlignment(VerticalAlignment.CENTER);
        searchBox.gap(4);
        TextBoxComponent searchInput = Components.textBox(Sizing.fill(100), "Search cosmetics...");
        searchInput.sizing(Sizing.fill(100), Sizing.fixed(16));
        searchInput.onChanged().subscribe(val -> {
            searchQuery = val.toLowerCase().trim();
            MinecraftClient.getInstance().execute(this::rebuildCardGrid);
        });
        searchBox.child(searchInput);
        panel.child(searchBox);

        cardGrid = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        cardGrid.gap(6);
        rebuildCardGrid();

        ScrollContainer<FlowLayout> scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), cardGrid);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(VeloraColors.BORDER_S)));
        panel.child(scrollContainer);

        return panel;
    }

    private void rebuildCardGrid() {
        cardGrid.clearChildren();

        List<CosmeticItem> allItems = CosmeticTextureCache.getItems();
        List<CosmeticItem> filtered = new ArrayList<>(allItems);

        if (selectedCategory == CosmeticItem.Category.FAVORITES) {
            filtered.removeIf(item -> !item.isFavorite());
        } else if (selectedCategory != CosmeticItem.Category.ALL) {
            filtered.removeIf(item -> item.getCategory() != selectedCategory);
        }

        if (!searchQuery.isEmpty()) {
            filtered.removeIf(item -> !item.getName().toLowerCase().contains(searchQuery));
        }

        FlowLayout currentRow = null;
        int colCount = 0;

        for (CosmeticItem item : filtered) {
            if (colCount == 0) {
                currentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                currentRow.gap(6);
                cardGrid.child(currentRow);
            }

            final int registryIndex = allItems.indexOf(item);
            FlowLayout card = createCosmeticCard(item, registryIndex);
            if (currentRow != null) currentRow.child(card);

            colCount++;
            if (colCount >= 3) colCount = 0;
        }
    }

    private FlowLayout createCosmeticCard(CosmeticItem item, int registryIndex) {
        boolean isSelected = (selectedCapeIndex == registryIndex);
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(130), Sizing.fixed(160));

        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, isSelected ? VeloraColors.SURF3 : VeloraColors.SURF);
            ctx.drawBorder(x, y, w, h, isSelected ? VeloraColors.VIOLET : VeloraColors.BORDER);
            if (isSelected) ctx.fill(x, y, x + w, y + 2, VeloraColors.VIOLET);
        });
        card.padding(Insets.of(4));
        card.gap(2);

        int viewH = 110;
        FlowLayout textureView = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(viewH));
        textureView.surface((ctx, comp) -> {
            MannequinModelRenderer.renderMannequinCard(ctx, comp.x(), comp.y(), comp.width(), comp.height(), item, false);
        });

        textureView.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                if (selectedCapeIndex == registryIndex) {
                    // Clicked already equipped cape -> toggle off & use Mojang cape!
                    selectedCapeIndex = -1;
                    ModConfig.enableCape = false;
                    ModConfig.selectedCape = -1;
                } else {
                    selectedCapeIndex = registryIndex;
                    ModConfig.enableCape = true;
                    ModConfig.selectedCape = registryIndex;
                }
                ModConfig.saveConfig();
                MinecraftClient.getInstance().execute(this::rebuildCardGrid);
                return true;
            }
            return false;
        });

        card.child(textureView);

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);
        bottomRow.padding(Insets.of(0, 2, 0, 2));
        bottomRow.gap(2);

        ButtonComponent starBtn = Components.button(Text.literal(item.isFavorite() ? "+" : "+"), btn -> {
            item.setFavorite(!item.isFavorite());
            saveFavoritesToConfig();
            MinecraftClient.getInstance().execute(this::rebuildCardGrid);
        });
        starBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
        starBtn.renderer((ctx, comp, delta) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF2);
            ctx.drawBorder(x, y, w, h, VeloraColors.BORDER);
            ctx.drawText(MinecraftClient.getInstance().textRenderer,
                    "+", x + 3, y + 3, item.isFavorite() ? VeloraColors.GOLD : VeloraColors.TEXT_F, true);
        });
        bottomRow.child(starBtn);

        FlowLayout nameLabel = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        nameLabel.verticalAlignment(VerticalAlignment.CENTER);
        nameLabel.child(Components.label(Text.literal(item.getName()))
                .color(Color.ofArgb(isSelected ? VeloraColors.VIOLET : VeloraColors.TEXT))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        bottomRow.child(nameLabel);

        card.child(bottomRow);

        if (isSelected) {
            FlowLayout equipBadge = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(10));
            equipBadge.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, VeloraColors.GREEN_D);
                ctx.drawBorder(x, y, w, h, VeloraColors.GREEN);
            });
            equipBadge.padding(Insets.of(0, 3, 0, 3));
            equipBadge.verticalAlignment(VerticalAlignment.CENTER);
            equipBadge.child(Components.label(Text.literal("EQUIPPED"))
                    .color(Color.ofArgb(VeloraColors.GREEN))
                    .sizing(Sizing.content(), Sizing.content()));
            card.child(equipBadge);
        }

        return card;
    }

    private FlowLayout buildPlayerPreviewPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(190), Sizing.fill(100));
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, VeloraColors.SURF3);
            ctx.fill(x, y, x + w, y + 1, VeloraColors.BORDER_S);
            ctx.fill(x + w - 1, y, x + w, y + h, VeloraColors.BORDER);
        });
        panel.padding(Insets.of(8));
        panel.gap(4);

        FlowLayout previewHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        previewHeader.verticalAlignment(VerticalAlignment.CENTER);

        String selectedName = selectedCapeIndex >= 0
                ? CosmeticTextureCache.getItems().get(selectedCapeIndex).getName()
                : "None";
        previewHeader.child(Components.label(Text.literal("Preview"))
                .color(Color.ofArgb(VeloraColors.TEXT_F))
                .sizing(Sizing.content(), Sizing.content()));
        previewHeader.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        previewHeader.child(Components.label(Text.literal("Selected: " + selectedName))
                .color(Color.ofArgb(selectedCapeIndex >= 0 ? VeloraColors.VIOLET : VeloraColors.TEXT_F))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        panel.child(previewHeader);

        ButtonComponent mojangCapeBtn = Components.button(Text.literal("Use Mojang / Account Cape"), btn -> {
            selectedCapeIndex = -1;
            ModConfig.enableCape = false;
            ModConfig.selectedCape = -1;
            ModConfig.saveConfig();
            MinecraftClient.getInstance().execute(this::rebuildCardGrid);
        });
        mojangCapeBtn.sizing(Sizing.fill(100), Sizing.fixed(18));
        mojangCapeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF2, VeloraColors.VIOLET_D, VeloraColors.SURF2));
        panel.child(mojangCapeBtn);

        FlowLayout playerArea = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        playerArea.verticalAlignment(VerticalAlignment.CENTER);
        playerArea.horizontalAlignment(HorizontalAlignment.CENTER);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            EntityComponent<LivingEntity> entityComp = Components.entity(Sizing.fixed(150), mc.player);
            entityComp.scale(0.9f);
            entityComp.allowMouseRotation(true);
            entityComp.showNametag(false);
            playerArea.child(entityComp);
        } else {
            FlowLayout silhouette = Containers.verticalFlow(Sizing.fixed(140), Sizing.fixed(150));
            silhouette.verticalAlignment(VerticalAlignment.CENTER);
            silhouette.horizontalAlignment(HorizontalAlignment.CENTER);
            silhouette.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                int cx = x + w / 2;
                int headS = 32;
                int headX = cx - headS / 2;
                int headY = y + 10;
                ctx.fill(headX, headY, headX + headS, headY + headS, VeloraColors.SURF2);
                ctx.drawBorder(headX, headY, headS, headS, VeloraColors.BORDER);
                int bodyW = 24, bodyH = 36;
                int bodyX = cx - bodyW / 2;
                int bodyY = headY + headS + 4;
                ctx.fill(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH, VeloraColors.SURF2);
                ctx.drawBorder(bodyX, bodyY, bodyW, bodyH, VeloraColors.BORDER);
                int armW = 8, armH = 30;
                ctx.fill(bodyX - armW - 2, bodyY, bodyX - 2, bodyY + armH, VeloraColors.SURF2);
                ctx.fill(bodyX + bodyW + 2, bodyY, bodyX + bodyW + 2 + armW, bodyY + armH, VeloraColors.SURF2);
                int legW = 10, legH = 30;
                ctx.fill(cx - legW - 1, bodyY + bodyH + 2, cx - 1, bodyY + bodyH + 2 + legH, VeloraColors.SURF2);
                ctx.fill(cx + 1, bodyY + bodyH + 2, cx + 1 + legW, bodyY + bodyH + 2 + legH, VeloraColors.SURF2);
            });
            playerArea.child(silhouette);
        }
        panel.child(playerArea);

        if (selectedCapeIndex >= 0) {
            FlowLayout unequipRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            unequipRow.horizontalAlignment(HorizontalAlignment.CENTER);
            unequipRow.gap(4);
            ButtonComponent unequipBtn = Components.button(Text.literal("Unequip"), btn -> {
                selectedCapeIndex = -1;
                ModConfig.enableCape = false;
                ModConfig.saveConfig();
                MinecraftClient.getInstance().execute(this::rebuildCardGrid);
            });
            unequipBtn.sizing(Sizing.fixed(70), Sizing.fixed(14));
            unequipBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF2, VeloraColors.RED, VeloraColors.SURF2));
            unequipRow.child(unequipBtn);
            panel.child(unequipRow);
        }

        FlowLayout helperRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        helperRow.verticalAlignment(VerticalAlignment.CENTER);
        helperRow.horizontalAlignment(HorizontalAlignment.CENTER);
        helperRow.child(Components.label(Text.literal("Click card to equip"))
                .color(Color.ofArgb(VeloraColors.TEXT_F))
                .sizing(Sizing.content(), Sizing.content()));
        panel.child(helperRow);

        return panel;
    }

    private void saveFavoritesToConfig() {
        List<CosmeticItem> items = CosmeticTextureCache.getItems();
        boolean[] favorites = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            favorites[i] = items.get(i).isFavorite();
        }
        ModConfig.favoriteCosmetics = favorites;
        ModConfig.saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, VeloraColors.BG);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
