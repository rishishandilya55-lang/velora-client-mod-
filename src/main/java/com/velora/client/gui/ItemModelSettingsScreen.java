package com.velora.client.gui;

import com.velora.client.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemModelSettingsScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private static String expandedItemId = null;
    private String searchQuery = "";

    private FlowLayout itemsContainer;
    private ScrollContainer<FlowLayout> scrollContainer;
    private double savedScrollOffset = 0.0;

    private static final int BG_COLOR = VeloraColors.BG_OVERLAY;
    private static final int SURF     = VeloraColors.SURF;
    private static final int SURF2    = VeloraColors.SURF2;
    private static final int SURF3    = VeloraColors.SURF3;
    private static final int TEXT     = VeloraColors.TEXT;
    private static final int TEXT_M   = VeloraColors.TEXT_M;
    private static final int TEXT_F   = VeloraColors.TEXT_F;
    private static final int BORDER   = VeloraColors.BORDER;
    private static final int BORDER_S = VeloraColors.BORDER_S;
    private static final int BLUE     = VeloraColors.CYAN;
    private static final int BLUE_D   = VeloraColors.CYAN_D;
    private static final int VIOLET   = VeloraColors.VIOLET;
    private static final int VIOLET_D = VeloraColors.VIOLET_D;
    private static final int GREEN    = VeloraColors.GREEN;
    private static final int GREEN_D  = VeloraColors.GREEN_D;
    private static final int RED      = VeloraColors.RED;

    public ItemModelSettingsScreen(Screen parent) {
        super(Text.literal("Item Model"));
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.flat(BG_COLOR));
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(500), Sizing.fixed(370));
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

        ButtonComponent backBtn = Components.button(Text.literal("←"), b -> close());
        backBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        backBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.TEXT_M, VeloraColors.SURF3));
        header.child(backBtn);

        header.child(Components.label(Text.literal("ITEM MODEL")).color(Color.ofArgb(VeloraColors.VIOLET)).shadow(true));

        header.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        // ON / OFF master button
        ButtonComponent onOffBtn = Components.button(
            Text.literal(ModConfig.showViewModel ? "ON" : "OFF"),
            b -> {
                ModConfig.showViewModel = !ModConfig.showViewModel;
                ModConfig.saveConfig();
                b.renderer(ButtonComponent.Renderer.flat(
                    ModConfig.showViewModel ? GREEN_D : SURF3,
                    ModConfig.showViewModel ? GREEN : TEXT_F,
                    ModConfig.showViewModel ? GREEN_D : SURF3
                ));
                b.setMessage(Text.literal(ModConfig.showViewModel ? "ON" : "OFF"));
            }
        );
        onOffBtn.sizing(Sizing.fixed(36), Sizing.fixed(20));
        onOffBtn.renderer(ButtonComponent.Renderer.flat(
            ModConfig.showViewModel ? GREEN_D : SURF3,
            ModConfig.showViewModel ? GREEN : TEXT_F,
            ModConfig.showViewModel ? GREEN_D : SURF3
        ));
        header.child(onOffBtn);

        // RESET button
        ButtonComponent resetBtn = Components.button(Text.literal("Reset"), b -> {
            ModConfig.itemScales.clear();
            ModConfig.itemGroundScales.clear();
            ModConfig.itemGuiScales.clear();
            ModConfig.viewModelMainHandScale = 1.0f;
            ModConfig.viewModelOffHandScale = 1.0f;
            ModConfig.saveConfig();
            rebuildItems();
        });
        resetBtn.sizing(Sizing.fixed(46), Sizing.fixed(20));
        resetBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED_F, VeloraColors.SURF3));
        header.child(resetBtn);

        ButtonComponent closeBtn = Components.button(Text.literal("✕"), b -> close());
        closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
        closeBtn.renderer(ButtonComponent.Renderer.flat(VeloraColors.SURF3, VeloraColors.RED_F, VeloraColors.SURF3));
        header.child(closeBtn);

        panel.child(header);

        // 2. Subtitle Bar
        FlowLayout subBar = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        subBar.padding(Insets.of(6, 12, 4, 12));
        subBar.child(Components.label(Text.literal("Displays a 3D model of the item you're holding for better visualization"))
            .color(Color.ofArgb(TEXT_F)));
        panel.child(subBar);

        // 3. Search Input Box
        FlowLayout searchRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        searchRow.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF);
            ctx.drawBorder(x, y, w, h, BORDER);
        });
        searchRow.padding(Insets.of(0, 8, 0, 8));
        searchRow.margins(Insets.of(0, 12, 4, 12));
        searchRow.verticalAlignment(VerticalAlignment.CENTER);
        searchRow.gap(6);

        searchRow.child(Components.label(Text.literal("Search items...")).color(Color.ofArgb(TEXT_F)));
        TextBoxComponent searchBox = Components.textBox(Sizing.fill(100), searchQuery);
        searchBox.sizing(Sizing.fill(100), Sizing.fixed(16));
        searchBox.setMaxLength(50);
        searchBox.onChanged().subscribe(val -> {
            searchQuery = val.trim().toLowerCase();
            rebuildItems();
        });
        searchRow.child(searchBox);
        panel.child(searchRow);

        // 4. Scrollable Items List
        itemsContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        itemsContainer.padding(Insets.of(4, 12, 60, 12));
        itemsContainer.gap(3);

        scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), itemsContainer);
        scrollContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(BORDER_S)));
        panel.child(scrollContainer);

        root.child(panel);

        rebuildItems();
    }

    private void rebuildItems() {
        if (itemsContainer == null) return;
        itemsContainer.clearChildren();

        List<ItemEntry> list = getSearchItems(searchQuery);

        if (list.isEmpty()) {
            FlowLayout empty = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(60));
            empty.verticalAlignment(VerticalAlignment.CENTER);
            empty.horizontalAlignment(HorizontalAlignment.CENTER);
            empty.child(Components.label(Text.literal("No items found matching \"" + searchQuery + "\"")).color(Color.ofArgb(TEXT_F)));
            itemsContainer.child(empty);
            return;
        }

        for (ItemEntry entry : list) {
            itemsContainer.child(buildItemCard(entry));
        }
    }

    private FlowLayout buildItemCard(ItemEntry entry) {
        boolean isExpanded = entry.id.equalsIgnoreCase(expandedItemId);
        float firstPersonScale = ModConfig.getItemScaleById(entry.id);
        float groundScale = ModConfig.getItemGroundScaleById(entry.id);
        float guiScale = ModConfig.getItemGuiScaleById(entry.id);
        boolean hasCustom = (firstPersonScale != 1.0f || groundScale != 1.0f || guiScale != 1.0f);

        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = isExpanded ? SURF3 : (hasCustom ? 0x0CFFFFFF : SURF);
            int bdr = isExpanded ? BLUE : (hasCustom ? GREEN_D : BORDER);
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, bdr);
        });
        card.padding(Insets.of(4, 8, 4, 8));
        card.gap(4);

        // Header Row (Item Icon + Name + Chevron)
        FlowLayout headerRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        headerRow.verticalAlignment(VerticalAlignment.CENTER);
        headerRow.gap(6);

        // Item icon sprite
        headerRow.child(Components.item(entry.stack));

        int nameColor = hasCustom ? TEXT : TEXT_M;
        if ("minecraft:enchanted_golden_apple".equals(entry.id)) {
            nameColor = 0xFFFCD34D;
        }
        headerRow.child(Components.label(Text.literal(entry.name)).color(Color.ofArgb(nameColor)));

        headerRow.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        if (hasCustom && !isExpanded) {
            String badge = String.format("1P:%.2fx | G:%.2fx", firstPersonScale, groundScale);
            headerRow.child(Components.label(Text.literal(badge)).color(Color.ofArgb(GREEN)));
        }

        String chevron = isExpanded ? "v" : ">";
        headerRow.child(Components.label(Text.literal(chevron)).color(Color.ofArgb(isExpanded ? BLUE : TEXT_F)));

        headerRow.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                expandedItemId = isExpanded ? null : entry.id;
                rebuildItems();
                return true;
            }
            return false;
        });

        card.child(headerRow);

        // Expanded Section (Matching Video Frames 00:06 - 00:11)
        if (isExpanded) {
            FlowLayout expandedPanel = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            expandedPanel.surface(Surface.flat(SURF2));
            expandedPanel.padding(Insets.of(6, 8, 6, 8));
            expandedPanel.gap(4);

            FlowLayout previewRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            previewRow.verticalAlignment(VerticalAlignment.CENTER);
            previewRow.child(Components.label(Text.literal("MODEL PREVIEW")).color(Color.ofArgb(TEXT_F)));
            previewRow.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));
            previewRow.child(Components.label(Text.literal(entry.id)).color(Color.ofArgb(0xFF52525B)));
            expandedPanel.child(previewRow);

            // 1. GROUND scale
            expandedPanel.child(makeSliderRow("GROUND", () -> ModConfig.getItemGroundScaleById(entry.id), s -> ModConfig.setItemGroundScale(entry.id, s), () -> ModConfig.setItemGroundScale(entry.id, 1.0f)));

            // 2. 1ST PERSON scale
            expandedPanel.child(makeSliderRow("1ST PERSON", () -> ModConfig.getItemScaleById(entry.id), s -> ModConfig.setItemScale(entry.id, s), () -> ModConfig.setItemScale(entry.id, 1.0f)));

            // 3. GUI scale
            expandedPanel.child(makeSliderRow("GUI", () -> ModConfig.getItemGuiScaleById(entry.id), s -> ModConfig.setItemGuiScale(entry.id, s), () -> ModConfig.setItemGuiScale(entry.id, 1.0f)));

            card.child(expandedPanel);
        }

        return card;
    }

    private FlowLayout makeSliderRow(String label, java.util.function.Supplier<Float> supplier, java.util.function.Consumer<Float> consumer, Runnable resetAction) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        row.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x06FFFFFF);
        });
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(2, 6, 2, 6));
        row.gap(6);

        LabelComponent labelComp = Components.label(Text.literal(label));
        labelComp.color(Color.ofArgb(TEXT_M));
        labelComp.sizing(Sizing.fixed(70), Sizing.content());
        row.child(labelComp);

        row.child(Containers.horizontalFlow(Sizing.expand(1), Sizing.fixed(1)));

        final float minVal = 0.10f;
        final float maxVal = 3.50f;

        float initialVal = supplier.get();
        LabelComponent valLbl = Components.label(Text.literal(String.format("%.2fx", initialVal)));
        valLbl.color(Color.ofArgb((initialVal != 1.0f) ? ((initialVal > 1.0f) ? GREEN : BLUE) : TEXT_F));
        valLbl.sizing(Sizing.fixed(38), Sizing.content());

        // Interactive Slider Track (Clickable & Draggable)
        FlowLayout sliderTrack = Containers.horizontalFlow(Sizing.fixed(130), Sizing.fixed(14));
        sliderTrack.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            float cur = supplier.get();
            float pct = Math.max(0.0f, Math.min(1.0f, (cur - minVal) / (maxVal - minVal)));

            // Track background bar
            int trackY = y + h / 2 - 2;
            ctx.fill(x, trackY, x + w, trackY + 4, 0xFF27272A);
            ctx.drawBorder(x, trackY, w, 4, 0x33FFFFFF);

            // Active blue fill
            int fillW = (int) (w * pct);
            if (fillW > 0) {
                ctx.fill(x, trackY, x + fillW, trackY + 4, BLUE_D);
            }

            // Knob
            int knobX = x + Math.max(0, Math.min(w - 6, (int) (w * pct) - 3));
            int knobY = y + 2;
            ctx.fill(knobX, knobY, knobX + 6, knobY + 10, 0xFFFFFFFF);
            ctx.drawBorder(knobX, knobY, 6, 10, BLUE);
        });

        Runnable updateSlider = () -> {
            float v = supplier.get();
            valLbl.text(Text.literal(String.format("%.2fx", v)));
            valLbl.color(Color.ofArgb((v != 1.0f) ? ((v > 1.0f) ? GREEN : BLUE) : TEXT_F));
        };

        sliderTrack.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                float pct = Math.max(0.0f, Math.min(1.0f, (float) mx / sliderTrack.width()));
                float newVal = minVal + pct * (maxVal - minVal);
                newVal = Math.round(newVal * 20.0f) / 20.0f; // 0.05 increments
                consumer.accept(newVal);
                updateSlider.run();
                return true;
            }
            return false;
        });

        sliderTrack.mouseDrag().subscribe((mx, my, btn, dx, dy) -> {
            if (btn == 0) {
                float pct = Math.max(0.0f, Math.min(1.0f, (float) mx / sliderTrack.width()));
                float newVal = minVal + pct * (maxVal - minVal);
                newVal = Math.round(newVal * 20.0f) / 20.0f;
                consumer.accept(newVal);
                updateSlider.run();
                return true;
            }
            return false;
        });

        row.child(sliderTrack);
        row.child(valLbl);

        ButtonComponent minusBtn = Components.button(Text.literal("-"), b -> {
            float cur = supplier.get();
            consumer.accept(Math.max(minVal, Math.round((cur - 0.05f) * 20.0f) / 20.0f));
            updateSlider.run();
        });
        minusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        minusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, BLUE, SURF3));
        row.child(minusBtn);

        ButtonComponent plusBtn = Components.button(Text.literal("+"), b -> {
            float cur = supplier.get();
            consumer.accept(Math.min(maxVal, Math.round((cur + 0.05f) * 20.0f) / 20.0f));
            updateSlider.run();
        });
        plusBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        plusBtn.renderer(ButtonComponent.Renderer.flat(SURF3, BLUE, SURF3));
        row.child(plusBtn);

        ButtonComponent resetBtn = Components.button(Text.literal("R"), b -> {
            resetAction.run();
            updateSlider.run();
        });
        resetBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF3F3F46, RED, 0xFF3F3F46));
        row.child(resetBtn);

        return row;
    }

    private record ItemEntry(String name, String id, ItemStack stack) {}

    private List<ItemEntry> getSearchItems(String query) {
        List<ItemEntry> results = new ArrayList<>();
        Set<String> added = new HashSet<>();

        if (query == null || query.isEmpty()) {
            String[][] popular = {
                {"Diamond Sword", "minecraft:diamond_sword"},
                {"Netherite Sword", "minecraft:netherite_sword"},
                {"Iron Sword", "minecraft:iron_sword"},
                {"Bow", "minecraft:bow"},
                {"Crossbow", "minecraft:crossbow"},
                {"Enchanted Golden Apple", "minecraft:enchanted_golden_apple"},
                {"Golden Apple", "minecraft:golden_apple"},
                {"Ender Pearl", "minecraft:ender_pearl"},
                {"Totem of Undying", "minecraft:totem_of_undying"},
                {"Shield", "minecraft:shield"},
                {"Mace", "minecraft:mace"},
                {"Wind Charge", "minecraft:wind_charge"},
                {"Golden Carrot", "minecraft:golden_carrot"},
                {"Potion", "minecraft:potion"},
                {"Splash Potion", "minecraft:splash_potion"}
            };
            for (String[] p : popular) {
                Item item = Registries.ITEM.get(Identifier.of(p[1]));
                results.add(new ItemEntry(p[0], p[1], new ItemStack(item)));
                added.add(p[1]);
            }
            return results;
        }

        if ("enchanted golden apple".contains(query) || "god apple".contains(query) || "notch".contains(query) || "enchanted".contains(query)) {
            if (!added.contains("minecraft:enchanted_golden_apple")) {
                results.add(new ItemEntry("Enchanted Golden Apple", "minecraft:enchanted_golden_apple", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
                added.add("minecraft:enchanted_golden_apple");
            }
        }

        for (Identifier id : Registries.ITEM.getIds()) {
            if (results.size() >= 25) break;
            String idStr = id.toString();
            String path = id.getPath();
            String formatted = formatName(idStr);

            if (path.toLowerCase().contains(query) || formatted.toLowerCase().contains(query)) {
                if (!added.contains(idStr)) {
                    Item item = Registries.ITEM.get(id);
                    results.add(new ItemEntry(formatted, idStr, new ItemStack(item)));
                    added.add(idStr);
                }
            }
        }
        return results;
    }

    private static String formatName(String itemId) {
        if ("minecraft:enchanted_golden_apple".equals(itemId)) return "Enchanted Golden Apple";
        if ("minecraft:golden_apple".equals(itemId)) return "Golden Apple";
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void close() {
        if (this.client != null && this.parent != null) {
            this.client.setScreen(this.parent);
        } else {
            super.close();
        }
    }
}
