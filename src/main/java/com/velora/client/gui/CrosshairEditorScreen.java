package com.velora.client.gui;

import com.velora.client.client.CustomCrosshairMod;
import com.velora.client.config.ModConfig;
import com.velora.client.util.HudColorHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class CrosshairEditorScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private int activeTab = 2; // 0 = SIMPLE, 1 = PRESETS, 2 = CUSTOM
    private int previewBgIndex = 0;
    private boolean previewEnemyMode = false;
    private boolean isMouseDown = false;
    private int currentDrawButton = -1; // 0 = Paint, 1 = Erase

    // Theme Colors
    private static final int BG_COLOR = 0xEE09090C;
    private static final int PANEL_BG = 0xCC111116;
    private static final int CARD_BG = 0xFF16161D;
    private static final int BORDER_C = 0x33A78BFA;
    private static final int VIOLET_D = 0xFF7C3AED;
    private static final int VIOLET_S = 0xFFA78BFA;
    private static final int GREEN = 0xFF22C55E;
    private static final int RED = 0xFFEF4444;
    private static final int TEXT_H = 0xFFFFFFFF;
    private static final int TEXT_M = 0xFFE4E4E7;
    private static final int TEXT_F = 0xFFA1A1AA;

    private static final int[] PREVIEW_BGS = new int[]{0xFF111115, 0xFF38BDF8, 0xFF15803D, 0xFFE2E8F0, 0xFF7F1D1D};
    private static final String[] PREVIEW_BG_NAMES = new String[]{"Dark", "Sky", "Grass", "Snow", "Nether"};

    private FlowLayout contentContainer;

    public CrosshairEditorScreen(Screen parent) {
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
        root.padding(Insets.of(12, 16, 12, 16));

        // 1. Top Header Row
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.gap(10);

        ButtonComponent backBtn = Components.button(Text.literal("< Back"), b -> close());
        backBtn.sizing(Sizing.fixed(56), Sizing.fixed(20));
        backBtn.renderer(ButtonComponent.Renderer.flat(0x33FFFFFF, VIOLET_D, 0x33FFFFFF));
        header.child(backBtn);

        FlowLayout titleBox = Containers.verticalFlow(Sizing.content(), Sizing.content());
        titleBox.gap(2);
        titleBox.child(Components.label(Text.literal("CROSSHAIR")).color(Color.ofArgb(TEXT_H)));
        titleBox.child(Components.label(Text.literal("Replace the default Minecraft crosshair with a customizable crosshair.")).color(Color.ofArgb(TEXT_F)));
        header.child(titleBox);

        root.child(header);

        // 2. Scrollable Content Area
        contentContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        contentContainer.gap(10);
        contentContainer.horizontalAlignment(HorizontalAlignment.CENTER);

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), contentContainer);
        root.child(scroll);

        rebuildContent();
    }

    private void rebuildContent() {
        contentContainer.clearChildren();

        // General Options Category
        contentContainer.child(makeCategoryHeader("GENERAL OPTIONS"));

        // Tab Selector Row [ SIMPLE ] [ PRESETS ] [ CUSTOM ]
        FlowLayout tabRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        tabRow.gap(8);
        tabRow.horizontalAlignment(HorizontalAlignment.CENTER);

        String[] tabs = new String[]{"SIMPLE", "PRESETS", "CUSTOM"};
        for (int i = 0; i < tabs.length; i++) {
            final int tabIdx = i;
            ButtonComponent tabBtn = Components.button(Text.literal(tabs[i]), b -> {
                activeTab = tabIdx;
                if (activeTab == 2) ModConfig.crosshairPreset = "CUSTOM_DRAWN";
                else if (activeTab == 0 && "CUSTOM_DRAWN".equalsIgnoreCase(ModConfig.crosshairPreset)) {
                    ModConfig.crosshairPreset = "CLASSIC_CROSS";
                }
                ModConfig.saveConfig();
                rebuildContent();
            });
            tabBtn.sizing(Sizing.fixed(100), Sizing.fixed(22));
            boolean sel = (activeTab == tabIdx);
            tabBtn.renderer(ButtonComponent.Renderer.flat(
                sel ? VIOLET_D : 0x1AFFFFFF,
                sel ? VIOLET_S : 0x33FFFFFF,
                sel ? VIOLET_D : 0x1AFFFFFF
            ));
            tabRow.child(tabBtn);
        }
        contentContainer.child(tabRow);

        // Main Editor Section (Help + Grid + Live Preview)
        FlowLayout editorArea = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        editorArea.gap(12);
        editorArea.horizontalAlignment(HorizontalAlignment.CENTER);

        // Left Column: HELP Card & Templates
        editorArea.child(buildHelpCard());

        // Center Column: Interactive Pixel Canvas
        editorArea.child(buildCanvasCard());

        // Right Column: Live In-Game Preview Card
        editorArea.child(buildPreviewCard());

        contentContainer.child(editorArea);

        // If PRESETS tab is active, show preset picker cards
        if (activeTab == 1) {
            contentContainer.child(buildPresetsRow());
        }

        // Section: EXTRA RENDER OPTIONS
        contentContainer.child(makeCategoryHeader("EXTRA RENDER OPTIONS"));
        contentContainer.child(buildRenderOptionsCard());

        // Section: COLOR & ENEMY CROSSHAIR
        contentContainer.child(makeCategoryHeader("COLOR & ENEMY CROSSHAIR OPTIONS"));
        contentContainer.child(buildColorEnemyCard());
    }

    private FlowLayout buildHelpCard() {
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(150), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, CARD_BG);
            ctx.drawBorder(x, y, w, h, BORDER_C);
        });
        card.padding(Insets.of(8, 8, 8, 8));
        card.gap(6);

        card.child(Components.label(Text.literal("HELP")).color(Color.ofArgb(TEXT_H)));
        card.child(Components.label(Text.literal("Draw your own custom crosshair.")).color(Color.ofArgb(TEXT_M)));
        card.child(Components.label(Text.literal("Select a PRESET to edit it.")).color(Color.ofArgb(TEXT_F)));

        card.child(makeShortcutPill("MOUSE 1", "Draw pixels"));
        card.child(makeShortcutPill("MOUSE 2", "Erase pixels"));
        card.child(makeShortcutPill("DELETE", "Clear canvas"));
        card.child(makeShortcutPill("TAB", "Test preview"));

        // Templates Header
        card.child(Components.label(Text.literal("TEMPLATES")).color(Color.ofArgb(VIOLET_S)));

        FlowLayout templateGrid = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        templateGrid.gap(3);

        FlowLayout row1 = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        row1.gap(3);
        row1.child(makeSmallBtn("Box Feet", () -> {
            ModConfig.crosshairGrid = CustomCrosshairMod.getBoxFeetTemplate();
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        }));
        row1.child(makeSmallBtn("Plus", () -> {
            ModConfig.crosshairGrid = CustomCrosshairMod.getDefaultGrid();
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        }));
        templateGrid.child(row1);

        FlowLayout row2 = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        row2.gap(3);
        row2.child(makeSmallBtn("Circle", () -> {
            ModConfig.crosshairGrid = CustomCrosshairMod.getCircleTemplate();
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        }));
        row2.child(makeSmallBtn("Diamond", () -> {
            ModConfig.crosshairGrid = CustomCrosshairMod.getDiamondTemplate();
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        }));
        templateGrid.child(row2);

        FlowLayout row3 = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        row3.gap(3);
        row3.child(makeSmallBtn("Heart", () -> {
            ModConfig.crosshairGrid = CustomCrosshairMod.getHeartTemplate();
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        }));
        row3.child(makeSmallBtn("Invert", () -> {
            if (ModConfig.crosshairGrid != null) {
                for (int i = 0; i < ModConfig.crosshairGrid.length; i++) {
                    ModConfig.crosshairGrid[i] = !ModConfig.crosshairGrid[i];
                }
                ModConfig.crosshairPreset = "CUSTOM_DRAWN";
                ModConfig.saveConfig(); rebuildContent();
            }
        }));
        templateGrid.child(row3);

        ButtonComponent clearBtn = Components.button(Text.literal("Clear Canvas"), b -> {
            ModConfig.crosshairGrid = new boolean[225];
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig(); rebuildContent();
        });
        clearBtn.sizing(Sizing.fill(100), Sizing.fixed(18));
        clearBtn.renderer(ButtonComponent.Renderer.flat(0x33EF4444, RED, 0x33EF4444));
        templateGrid.child(clearBtn);

        card.child(templateGrid);
        return card;
    }

    private FlowLayout buildCanvasCard() {
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(210), Sizing.fixed(210));
        card.horizontalAlignment(HorizontalAlignment.CENTER);
        card.verticalAlignment(VerticalAlignment.CENTER);
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0xFF14141B);
            ctx.drawBorder(x, y, w, h, BORDER_C);
        });

        int gridSize = CustomCrosshairMod.GRID_SIZE; // 15
        int cellSize = 12;
        int center = gridSize / 2; // 7

        FlowLayout canvas = Containers.verticalFlow(Sizing.fixed(gridSize * cellSize), Sizing.fixed(gridSize * cellSize));
        canvas.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y();
            boolean[] grid = ModConfig.crosshairGrid;
            if (grid == null || grid.length < 225) {
                grid = CustomCrosshairMod.getDefaultGrid();
                ModConfig.crosshairGrid = grid;
            }

            int color = HudColorHelper.getEffectiveColor(ModConfig.crosshairColor, ModConfig.crosshairRainbow);

            // Draw cells
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    int px = x + c * cellSize;
                    int py = y + r * cellSize;
                    int idx = r * gridSize + c;

                    boolean active = grid[idx];
                    if (active) {
                        ctx.fill(px + 1, py + 1, px + cellSize, py + cellSize, color);
                    } else {
                        ctx.fill(px + 1, py + 1, px + cellSize, py + cellSize, 0x0AFFFFFF);
                    }

                    // Subtle grid line
                    ctx.drawBorder(px, py, cellSize + 1, cellSize + 1, 0x1AFFFFFF);

                    // Center crosshair marker
                    if (r == center && c == center && !active) {
                        ctx.fill(px + cellSize / 2 - 1, py + cellSize / 2 - 1, px + cellSize / 2 + 1, py + cellSize / 2 + 1, 0x44FFFFFF);
                    }
                }
            }
        });

        // Mouse click & drag handling for drawing/erasing
        canvas.mouseDown().subscribe((mx, my, btn) -> {
            int col = (int) (mx / cellSize);
            int row = (int) (my / cellSize);
            if (col >= 0 && col < gridSize && row >= 0 && row < gridSize) {
                isMouseDown = true;
                currentDrawButton = btn;
                int idx = row * gridSize + col;
                if (ModConfig.crosshairGrid == null || ModConfig.crosshairGrid.length < 225) {
                    ModConfig.crosshairGrid = CustomCrosshairMod.getDefaultGrid();
                }
                ModConfig.crosshairGrid[idx] = (btn == 0); // 0 = Paint, 1 = Erase
                ModConfig.crosshairPreset = "CUSTOM_DRAWN";
                ModConfig.saveConfig();
                return true;
            }
            return false;
        });

        canvas.mouseDrag().subscribe((mx, my, deltaX, deltaY, btn) -> {
            if (isMouseDown) {
                int col = (int) (mx / cellSize);
                int row = (int) (my / cellSize);
                if (col >= 0 && col < gridSize && row >= 0 && row < gridSize) {
                    int idx = row * gridSize + col;
                    if (ModConfig.crosshairGrid != null && idx < ModConfig.crosshairGrid.length) {
                        ModConfig.crosshairGrid[idx] = (currentDrawButton == 0);
                        ModConfig.crosshairPreset = "CUSTOM_DRAWN";
                        ModConfig.saveConfig();
                    }
                    return true;
                }
            }
            return false;
        });

        card.child(canvas);
        return card;
    }

    private FlowLayout buildPreviewCard() {
        FlowLayout card = Containers.verticalFlow(Sizing.fixed(150), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, CARD_BG);
            ctx.drawBorder(x, y, w, h, BORDER_C);
        });
        card.padding(Insets.of(8, 8, 8, 8));
        card.gap(6);

        card.child(Components.label(Text.literal("PREVIEW")).color(Color.ofArgb(TEXT_H)));

        FlowLayout previewBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(90));
        previewBox.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int bg = PREVIEW_BGS[previewBgIndex % PREVIEW_BGS.length];
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.drawBorder(x, y, w, h, BORDER_C);

            int cx = x + w / 2;
            int cy = y + h / 2;
            CustomCrosshairMod.renderCrosshair(ctx, cx, cy, 0.0f, 1.0f, previewEnemyMode);
        });

        previewBox.mouseDown().subscribe((mx, my, btn) -> {
            if (btn == 0) {
                previewBgIndex = (previewBgIndex + 1) % PREVIEW_BGS.length;
                rebuildContent();
                return true;
            }
            return false;
        });

        card.child(previewBox);

        String bgName = PREVIEW_BG_NAMES[previewBgIndex % PREVIEW_BGS.length];
        card.child(Components.label(Text.literal("Terrain: " + bgName + " (Click box)")).color(Color.ofArgb(TEXT_F)));

        // Button to toggle Enemy Hitbox preview simulation
        ButtonComponent enemyPreviewBtn = Components.button(
            Text.literal(previewEnemyMode ? "Target: HITBOX ON" : "Target: Normal"),
            b -> {
                previewEnemyMode = !previewEnemyMode;
                rebuildContent();
            }
        );
        enemyPreviewBtn.sizing(Sizing.fill(100), Sizing.fixed(18));
        enemyPreviewBtn.renderer(ButtonComponent.Renderer.flat(
            previewEnemyMode ? 0x33EF4444 : 0x1AFFFFFF,
            previewEnemyMode ? RED : TEXT_M,
            previewEnemyMode ? 0x33EF4444 : 0x1AFFFFFF
        ));
        card.child(enemyPreviewBtn);

        return card;
    }

    private FlowLayout buildPresetsRow() {
        FlowLayout container = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.gap(4);
        container.padding(Insets.of(4, 8, 4, 8));

        String[][] presets = new String[][]{
            {"Classic", "CLASSIC_CROSS"},
            {"Dot", "DOT"},
            {"Circle", "CIRCLE"},
            {"Square", "SQUARE"},
            {"Chevron", "CHEVRON"},
            {"Diamond", "DIAMOND"},
            {"T-Shape", "T_SHAPE"},
            {"Box Feet", "BOX_FEET"},
            {"Custom Drawn", "CUSTOM_DRAWN"}
        };

        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.gap(4);
        row.horizontalAlignment(HorizontalAlignment.CENTER);

        for (String[] p : presets) {
            String label = p[0];
            String key = p[1];
            boolean sel = key.equalsIgnoreCase(ModConfig.crosshairPreset);

            ButtonComponent pBtn = Components.button(Text.literal(label), b -> {
                ModConfig.crosshairPreset = key;
                ModConfig.saveConfig();
                rebuildContent();
            });
            pBtn.sizing(Sizing.content(), Sizing.fixed(20));
            pBtn.renderer(ButtonComponent.Renderer.flat(
                sel ? VIOLET_D : 0x1AFFFFFF,
                sel ? VIOLET_S : TEXT_M,
                sel ? VIOLET_D : 0x1AFFFFFF
            ));
            row.child(pBtn);
        }

        container.child(row);
        return container;
    }

    private FlowLayout buildRenderOptionsCard() {
        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, PANEL_BG);
            ctx.drawBorder(x, y, w, h, 0x1AFFFFFF);
        });
        card.padding(Insets.of(8, 12, 8, 12));
        card.gap(6);

        // Use Custom Scale Toggle & Scale Pills
        FlowLayout scaleRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        scaleRow.verticalAlignment(VerticalAlignment.CENTER);
        scaleRow.gap(8);

        ButtonComponent toggleScaleBtn = Components.button(
            Text.literal(ModConfig.crosshairUseCustomScale ? "ON" : "OFF"),
            b -> {
                ModConfig.crosshairUseCustomScale = !ModConfig.crosshairUseCustomScale;
                ModConfig.saveConfig();
                rebuildContent();
            }
        );
        toggleScaleBtn.sizing(Sizing.fixed(36), Sizing.fixed(18));
        toggleScaleBtn.renderer(ButtonComponent.Renderer.flat(
            ModConfig.crosshairUseCustomScale ? GREEN : 0x33FFFFFF,
            TEXT_H,
            ModConfig.crosshairUseCustomScale ? GREEN : 0x33FFFFFF
        ));
        scaleRow.child(toggleScaleBtn);
        scaleRow.child(Components.label(Text.literal("Use Custom Scale")).color(Color.ofArgb(TEXT_H)));

        // Scale Pills: [ SMALL ] [ NORMAL ] [ LARGE ] [ AUTO ]
        scaleRow.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        String[] scales = new String[]{"SMALL", "NORMAL", "LARGE", "AUTO"};
        for (String s : scales) {
            boolean sel = s.equalsIgnoreCase(ModConfig.crosshairScaleMode);
            ButtonComponent sBtn = Components.button(Text.literal(s), b -> {
                ModConfig.crosshairScaleMode = s;
                ModConfig.crosshairUseCustomScale = true;
                ModConfig.saveConfig();
                rebuildContent();
            });
            sBtn.sizing(Sizing.fixed(56), Sizing.fixed(18));
            sBtn.renderer(ButtonComponent.Renderer.flat(
                sel ? VIOLET_D : 0x1AFFFFFF,
                sel ? VIOLET_S : TEXT_M,
                sel ? VIOLET_D : 0x1AFFFFFF
            ));
            scaleRow.child(sBtn);
        }
        card.child(scaleRow);

        // Dynamic Movement Spread Toggle
        card.child(makeInlineToggle("Dynamic Movement Spread", "Expand crosshair while walking, sprinting or jumping",
            ModConfig.crosshairDynamic,
            () -> { ModConfig.crosshairDynamic = !ModConfig.crosshairDynamic; ModConfig.saveConfig(); rebuildContent(); }));

        // Weapon Attack Cooldown Indicator
        card.child(makeInlineToggle("Attack Cooldown Indicator", "Render weapon swing cooldown progress under crosshair",
            ModConfig.crosshairAttackIndicator,
            () -> { ModConfig.crosshairAttackIndicator = !ModConfig.crosshairAttackIndicator; ModConfig.saveConfig(); rebuildContent(); }));

        // 3rd Person Visibility
        card.child(makeInlineToggle("3rd Person Visibility", "Show crosshair in F5 and FreeLook camera modes",
            ModConfig.crosshairThirdPerson,
            () -> { ModConfig.crosshairThirdPerson = !ModConfig.crosshairThirdPerson; ModConfig.saveConfig(); rebuildContent(); }));

        return card;
    }

    private FlowLayout buildColorEnemyCard() {
        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, PANEL_BG);
            ctx.drawBorder(x, y, w, h, 0x1AFFFFFF);
        });
        card.padding(Insets.of(8, 12, 8, 12));
        card.gap(6);

        // 1. Crosshair Color
        String colorName = ModConfig.crosshairRainbow ? "Rainbow / Chroma" : HudColorHelper.getColorName(ModConfig.crosshairColor);
        card.child(makeColorRow("Crosshair Color", colorName, ModConfig.crosshairColor, ModConfig.crosshairRainbow, () -> {
            if (ModConfig.crosshairRainbow) {
                ModConfig.crosshairRainbow = false;
                ModConfig.crosshairColor = 0xFFFFFFFF;
            } else {
                int next = HudColorHelper.cycleColor(ModConfig.crosshairColor);
                if (next == 0xFFFFFFFF && (ModConfig.crosshairColor & 0x00FFFFFF) == 0x00AAAA) {
                    ModConfig.crosshairRainbow = true;
                } else {
                    ModConfig.crosshairColor = next;
                }
            }
            ModConfig.saveConfig();
            rebuildContent();
        }));

        // 2. Outline / Shadow Border
        card.child(makeInlineToggle("Outline / Shadow", "High-contrast dark border around crosshair",
            ModConfig.crosshairOutline,
            () -> { ModConfig.crosshairOutline = !ModConfig.crosshairOutline; ModConfig.saveConfig(); rebuildContent(); }));

        // 3. Enemy Crosshair (Player / Mob Hitbox Target Option)
        card.child(makeInlineToggle("Enemy Crosshair", "Change crosshair style/color when aiming at a player or mob hitbox",
            ModConfig.crosshairEnemyCrosshair,
            () -> { ModConfig.crosshairEnemyCrosshair = !ModConfig.crosshairEnemyCrosshair; ModConfig.saveConfig(); rebuildContent(); }));

        if (ModConfig.crosshairEnemyCrosshair) {
            // Enemy Mode Selector
            String[] enemyModes = new String[]{"COLOR_CHANGE", "TARGET_LOCK_BOX", "RED_DOT", "CROSS_EXPAND"};
            String enemyModeName = ModConfig.crosshairEnemyMode.replace('_', ' ');
            card.child(makeCycleRow("Enemy Hitbox Mode", enemyModeName, () -> {
                int cur = 0;
                for (int i = 0; i < enemyModes.length; i++) {
                    if (enemyModes[i].equalsIgnoreCase(ModConfig.crosshairEnemyMode)) { cur = i; break; }
                }
                ModConfig.crosshairEnemyMode = enemyModes[(cur + 1) % enemyModes.length];
                ModConfig.saveConfig();
                rebuildContent();
            }));

            // Enemy Color
            String enemyColorName = HudColorHelper.getColorName(ModConfig.crosshairEnemyColor);
            card.child(makeColorRow("Enemy Highlight Color", enemyColorName, ModConfig.crosshairEnemyColor, false, () -> {
                ModConfig.crosshairEnemyColor = HudColorHelper.cycleColor(ModConfig.crosshairEnemyColor);
                ModConfig.saveConfig();
                rebuildContent();
            }));
        }

        return card;
    }

    private FlowLayout makeShortcutPill(String key, String desc) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(6);

        FlowLayout badge = Containers.horizontalFlow(Sizing.fixed(54), Sizing.fixed(14));
        badge.horizontalAlignment(HorizontalAlignment.CENTER);
        badge.verticalAlignment(VerticalAlignment.CENTER);
        badge.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0x33FFFFFF);
            ctx.drawBorder(x, y, w, h, 0x44FFFFFF);
        });
        badge.child(Components.label(Text.literal(key)).color(Color.ofArgb(TEXT_H)));

        row.child(badge);
        row.child(Components.label(Text.literal(desc)).color(Color.ofArgb(TEXT_M)));
        return row;
    }

    private ButtonComponent makeSmallBtn(String label, Runnable action) {
        ButtonComponent btn = Components.button(Text.literal(label), b -> action.run());
        btn.sizing(Sizing.fill(50), Sizing.fixed(18));
        btn.renderer(ButtonComponent.Renderer.flat(0x22FFFFFF, VIOLET_S, 0x22FFFFFF));
        return btn;
    }

    private FlowLayout makeInlineToggle(String label, String hint, boolean enabled, Runnable onToggle) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(8);

        ButtonComponent btn = Components.button(
            Text.literal(enabled ? "ON" : "OFF"),
            b -> onToggle.run()
        );
        btn.sizing(Sizing.fixed(36), Sizing.fixed(18));
        btn.renderer(ButtonComponent.Renderer.flat(
            enabled ? GREEN : 0x33FFFFFF,
            TEXT_H,
            enabled ? GREEN : 0x33FFFFFF
        ));
        row.child(btn);

        FlowLayout textBox = Containers.verticalFlow(Sizing.content(), Sizing.content());
        textBox.gap(1);
        textBox.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_H)));
        textBox.child(Components.label(Text.literal(hint)).color(Color.ofArgb(TEXT_F)));
        row.child(textBox);

        return row;
    }

    private FlowLayout makeColorRow(String label, String valueName, int currentColor, boolean rainbow, Runnable onCycle) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(8);

        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_H)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(16), Sizing.fixed(16));
        swatch.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            int c = rainbow ? 0xFF9933FF : (0xFF000000 | (currentColor & 0x00FFFFFF));
            ctx.fill(x, y, x + w, y + h, c);
            ctx.drawBorder(x, y, w, h, 0x66FFFFFF);
        });
        row.child(swatch);

        ButtonComponent btn = Components.button(Text.literal(valueName), b -> onCycle.run());
        btn.sizing(Sizing.content(), Sizing.fixed(18));
        btn.renderer(ButtonComponent.Renderer.flat(0x22FFFFFF, VIOLET_S, 0x22FFFFFF));
        row.child(btn);

        return row;
    }

    private FlowLayout makeCycleRow(String label, String valueName, Runnable onCycle) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.gap(8);

        row.child(Components.label(Text.literal(label)).color(Color.ofArgb(TEXT_H)));
        row.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        ButtonComponent btn = Components.button(Text.literal(valueName), b -> onCycle.run());
        btn.sizing(Sizing.content(), Sizing.fixed(18));
        btn.renderer(ButtonComponent.Renderer.flat(0x22FFFFFF, VIOLET_S, 0x22FFFFFF));
        row.child(btn);

        return row;
    }

    private FlowLayout makeCategoryHeader(String title) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.padding(Insets.of(6, 0, 2, 0));
        row.child(Components.label(Text.literal(title.toUpperCase())).color(Color.ofArgb(VIOLET_S)));
        return row;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isMouseDown = false;
        currentDrawButton = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            ModConfig.crosshairGrid = new boolean[225];
            ModConfig.crosshairPreset = "CUSTOM_DRAWN";
            ModConfig.saveConfig();
            rebuildContent();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            previewEnemyMode = !previewEnemyMode;
            rebuildContent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        if (this.client != null && this.parent != null) {
            this.client.setScreen(this.parent);
        } else {
            super.close();
        }
    }
}
