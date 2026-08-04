package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import com.velora.client.gui.cosmetic.CosmeticItem;
import com.velora.client.gui.cosmetic.CosmeticTextureCache;
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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsLockerScreen extends BaseOwoScreen<FlowLayout> {

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
    private static final int VIOLET_F = 0x1FA78BFA;
    private static final int VIOLET_D = 0xFF6D28D9;
    private static final int GREEN    = 0xFF34D399;
    private static final int GOLD     = 0xFFFFD700;

    private static boolean isLockerOpen = false;
    private static int selectedCapeIndex = -1;
    private String searchQuery = "";
    private CosmeticItem.Category selectedCategory = CosmeticItem.Category.ALL;

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
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
            ctx.fill(x, y, x + w, y + 1, VIOLET_F);
        });
        mainCanvas.padding(Insets.of(8));
        mainCanvas.gap(6);

        FlowLayout topHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        topHeader.surface((ctx, comp) -> {
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), SURF3);
        });
        topHeader.verticalAlignment(VerticalAlignment.CENTER);
        topHeader.padding(Insets.of(0, 8, 0, 8));
        topHeader.child(Components.label(Text.literal("Velora Locker"))
                .color(Color.ofArgb(VIOLET)).shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        topHeader.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        ButtonComponent closeBtn = Components.button(Text.literal("X"), btn -> this.close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(SURF2, 0x33EF4444, SURF2));
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
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), SURF3);
            ctx.drawBorder(comp.x(), comp.y(), comp.width(), comp.height(), BORDER);
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
                    ctx2.fill(comp2.x(), comp2.y(), comp2.x() + comp2.width(), comp2.y() + comp2.height(), SURF2);
                }
            });
            catRow.child(Components.label(Text.literal(category.getIcon() + " " + labelText))
                    .color(Color.ofArgb(selectedCategory == category ? VIOLET : TEXT_M))
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
            ctx.fill(comp.x(), comp.y(), comp.x() + comp.width(), comp.y() + comp.height(), SURF3);
            ctx.drawBorder(comp.x(), comp.y(), comp.width(), comp.height(), BORDER);
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
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
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
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(130), Sizing.fixed(150));

        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, isSelected ? SURF3 : SURF);
            ctx.drawBorder(x, y, w, h, isSelected ? VIOLET : BORDER);
            if (isSelected) ctx.fill(x, y, x + w, y + 2, VIOLET);
        });
        card.padding(Insets.of(4));
        card.gap(2);

        int viewH = 100;
        FlowLayout textureView = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(viewH));
        textureView.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0xFF14161F);

            Identifier tex = item.getTexture();
            int texW = 64, texH = 32;
            try {
                var resource = MinecraftClient.getInstance().getResourceManager().getResource(tex);
                if (resource.isPresent()) {
                    try (var stream = resource.get().getInputStream()) {
                        var img = net.minecraft.client.texture.NativeImage.read(stream);
                        texW = img.getWidth();
                        texH = img.getHeight();
                        img.close();
                    }
                }
            } catch (Exception e) { /* defaults */ }

            int drawW = Math.min(w - 8, texW);
            int drawH = (int)((float) drawW * texH / texW);
            if (drawH > h - 8) {
                drawH = h - 8;
                drawW = (int)((float) drawH * texW / texH);
            }
            int drawX = x + (w - drawW) / 2;
            int drawY = y + (h - drawH) / 2;

            try {
                ctx.drawTexture(RenderLayer::getGuiTextured, tex, drawX, drawY, 0.0f, 0.0f, drawW, drawH, texW, texH);
            } catch (Exception e) {
                ctx.fill(drawX, drawY, drawX + drawW, drawY + drawH, SURF3);
            }
        });

        textureView.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                selectedCapeIndex = registryIndex;
                ModConfig.enableCape = true;
                ModConfig.selectedCape = registryIndex;
                ModConfig.saveConfig();
                MinecraftClient.getInstance().execute(this::rebuildCardGrid);
                return true;
            }
            return false;
        });

        card.child(textureView);

        FlowLayout bottomRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        bottomRow.verticalAlignment(VerticalAlignment.CENTER);
        bottomRow.padding(Insets.of(0, 2, 0, 2));
        bottomRow.gap(2);

        ButtonComponent starBtn = Components.button(Text.literal("+"), btn -> {
            item.setFavorite(!item.isFavorite());
            saveFavoritesToConfig();
            MinecraftClient.getInstance().execute(this::rebuildCardGrid);
        });
        starBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
        starBtn.renderer((ctx, comp, delta) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER);
            ctx.drawText(MinecraftClient.getInstance().textRenderer,
                    "+", x + 3, y + 3, item.isFavorite() ? GOLD : TEXT_F, true);
        });
        bottomRow.child(starBtn);

        FlowLayout nameLabel = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        nameLabel.verticalAlignment(VerticalAlignment.CENTER);
        nameLabel.child(Components.label(Text.literal(item.getName()))
                .color(Color.ofArgb(isSelected ? VIOLET : TEXT))
                .shadow(true)
                .sizing(Sizing.content(), Sizing.content()));
        bottomRow.child(nameLabel);

        card.child(bottomRow);

        return card;
    }

    private FlowLayout buildPlayerPreviewPanel() {
        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(190), Sizing.fill(100));
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.fill(x, y, x + w, y + 1, BORDER_S);
            ctx.fill(x + w - 1, y, x + w, y + h, BORDER);
        });
        panel.padding(Insets.of(8));
        panel.gap(4);

        FlowLayout previewHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
        previewHeader.verticalAlignment(VerticalAlignment.CENTER);
        previewHeader.child(Components.label(Text.literal("Preview"))
                .color(Color.ofArgb(TEXT_F))
                .sizing(Sizing.content(), Sizing.content()));
        panel.child(previewHeader);

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
                ctx.fill(headX, headY, headX + headS, headY + headS, SURF2);
                ctx.drawBorder(headX, headY, headS, headS, BORDER);
                int bodyW = 24, bodyH = 36;
                int bodyX = cx - bodyW / 2;
                int bodyY = headY + headS + 4;
                ctx.fill(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH, SURF2);
                ctx.drawBorder(bodyX, bodyY, bodyW, bodyH, BORDER);
                int armW = 8, armH = 30;
                ctx.fill(bodyX - armW - 2, bodyY, bodyX - 2, bodyY + armH, SURF2);
                ctx.fill(bodyX + bodyW + 2, bodyY, bodyX + bodyW + 2 + armW, bodyY + armH, SURF2);
                int legW = 10, legH = 30;
                ctx.fill(cx - legW - 1, bodyY + bodyH + 2, cx - 1, bodyY + bodyH + 2 + legH, SURF2);
                ctx.fill(cx + 1, bodyY + bodyH + 2, cx + 1 + legW, bodyY + bodyH + 2 + legH, SURF2);
            });
            playerArea.child(silhouette);
        }
        panel.child(playerArea);

        FlowLayout helperRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        helperRow.verticalAlignment(VerticalAlignment.CENTER);
        helperRow.horizontalAlignment(HorizontalAlignment.CENTER);
        helperRow.child(Components.label(Text.literal("Click card to equip"))
                .color(Color.ofArgb(TEXT_F))
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
        context.fill(0, 0, this.width, this.height, BG);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
