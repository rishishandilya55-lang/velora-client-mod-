package com.velora.client.gui;

import com.velora.client.waypoints.Waypoint;
import com.velora.client.waypoints.WaypointManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class WaypointCreateScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private final Waypoint existingWaypoint;

    private TextBoxComponent nameField;
    private TextBoxComponent xField;
    private TextBoxComponent yField;
    private TextBoxComponent zField;

    private int selectedColor = 0xFF38BDF8; // Default Cyan
    private String selectedDimension = "minecraft:overworld";
    private boolean chroma = false;

    private static final int BG_COLOR = 0xCC08080A;
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
    private static final int GREEN    = 0xFF34D399;
    private static final int CYAN     = 0xFF38BDF8;
    private static final int RED      = 0xFFEF4444;

    private static final int[] PALETTE = new int[]{
        0xFFEF4444, // Red
        0xFFF97316, // Orange
        0xFFFBBF24, // Amber/Gold
        0xFFEAB308, // Yellow
        0xFF84CC16, // Lime
        0xFF22C55E, // Green
        0xFF14B8A6, // Teal
        0xFF38BDF8, // Cyan
        0xFF3B82F6, // Blue
        0xFF8B5CF6, // Violet
        0xFFA855F7, // Purple
        0xFFD946EF, // Magenta
        0xFFEC4899, // Pink
        0xFFFFFFFF  // White
    };

    public WaypointCreateScreen(Screen parent, Waypoint existing) {
        super(Text.literal(existing != null ? "Edit A Waypoint" : "Create Waypoint"));
        this.parent = parent;
        this.existingWaypoint = existing;
        if (existing != null) {
            this.selectedColor = existing.color;
            this.selectedDimension = existing.dimension;
        } else {
            this.selectedDimension = WaypointManager.getCurrentDimensionKey();
        }
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

        FlowLayout panel = Containers.verticalFlow(Sizing.fixed(380), Sizing.content());
        panel.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        panel.padding(Insets.of(16, 20, 16, 20));
        panel.gap(10);

        // Header
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        header.verticalAlignment(VerticalAlignment.CENTER);
        String titleText = (existingWaypoint != null) ? "Edit A Waypoint" : "Create A Waypoint";
        header.child(Components.label(Text.literal(titleText)).color(Color.ofArgb(TEXT)).shadow(true));
        header.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));

        // Live In-Game Preview Box
        String previewName = (nameField != null && !nameField.getText().trim().isEmpty()) ? nameField.getText().trim() : ((existingWaypoint != null) ? existingWaypoint.name : "Waypoint");
        FlowLayout livePreview = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        livePreview.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, 0xAA08080A);
            ctx.drawBorder(x, y, w, h, selectedColor | 0xFF000000);
        });
        livePreview.padding(Insets.of(2, 8, 2, 8));
        livePreview.verticalAlignment(VerticalAlignment.CENTER);
        livePreview.child(Components.label(Text.literal(previewName + " [14m]")).color(Color.ofArgb(selectedColor | 0xFF000000)).shadow(true));
        header.child(livePreview);

        panel.child(header);

        // Name input
        panel.child(Components.label(Text.literal("Waypoint Name")).color(Color.ofArgb(TEXT_M)));
        nameField = Components.textBox(Sizing.fill(100));
        nameField.setText(existingWaypoint != null ? existingWaypoint.name : "My Waypoint");
        nameField.sizing(Sizing.fill(100), Sizing.fixed(20));
        nameField.setMaxLength(36);
        panel.child(nameField);

        // Coordinates (X, Y, Z)
        MinecraftClient mc = MinecraftClient.getInstance();
        double defX = 0.0, defY = 64.0, defZ = 0.0;
        if (existingWaypoint != null) {
            defX = existingWaypoint.x;
            defY = existingWaypoint.y;
            defZ = existingWaypoint.z;
        } else if (mc != null && mc.player != null) {
            defX = Math.round(mc.player.getX() * 10.0) / 10.0;
            defY = Math.round(mc.player.getY() * 10.0) / 10.0;
            defZ = Math.round(mc.player.getZ() * 10.0) / 10.0;
        }

        panel.child(Components.label(Text.literal("Coordinates (X, Y, Z)")).color(Color.ofArgb(TEXT_M)));
        FlowLayout coordRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        coordRow.gap(6);

        xField = Components.textBox(Sizing.fill(33));
        xField.setText(String.format("%.1f", defX).replace(',', '.'));
        xField.sizing(Sizing.fill(33), Sizing.fixed(20));

        yField = Components.textBox(Sizing.fill(33));
        yField.setText(String.format("%.1f", defY).replace(',', '.'));
        yField.sizing(Sizing.fill(33), Sizing.fixed(20));

        zField = Components.textBox(Sizing.fill(34));
        zField.setText(String.format("%.1f", defZ).replace(',', '.'));
        zField.sizing(Sizing.fill(34), Sizing.fixed(20));

        coordRow.child(xField);
        coordRow.child(yField);
        coordRow.child(zField);
        panel.child(coordRow);

        // Auto-Detected Dimension
        panel.child(Components.label(Text.literal("Dimension (Auto-Detected)")).color(Color.ofArgb(TEXT_M)));
        FlowLayout dimBadge = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
        dimBadge.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF3);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        dimBadge.verticalAlignment(VerticalAlignment.CENTER);
        dimBadge.padding(Insets.of(0, 8, 0, 8));
        dimBadge.gap(6);
        dimBadge.child(Components.label(Text.literal("📍")).color(Color.ofArgb(CYAN)));
        dimBadge.child(Components.label(Text.literal(WaypointManager.getHumanReadableDimension(selectedDimension)))
            .color(Color.ofArgb(TEXT)).shadow(true));
        dimBadge.child(Containers.horizontalFlow(Sizing.fixed(40), Sizing.fixed(1)));
        dimBadge.child(Components.label(Text.literal(selectedDimension)).color(Color.ofArgb(TEXT_F)));
        panel.child(dimBadge);

        // Color Picker & Palette
        FlowLayout colorHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
        colorHeader.verticalAlignment(VerticalAlignment.CENTER);
        colorHeader.child(Components.label(Text.literal("Color")).color(Color.ofArgb(TEXT_M)));
        colorHeader.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)));
        
        String hexStr = String.format("#%06X", (selectedColor & 0xFFFFFF));
        colorHeader.child(Components.label(Text.literal(hexStr)).color(Color.ofArgb(selectedColor | 0xFF000000)));
        panel.child(colorHeader);

        // Color Swatches Grid (2 rows of 7)
        FlowLayout paletteGrid = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        paletteGrid.gap(4);

        FlowLayout row1 = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        row1.gap(6);
        FlowLayout row2 = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
        row2.gap(6);

        for (int i = 0; i < PALETTE.length; i++) {
            final int col = PALETTE[i];
            FlowLayout swatch = Containers.horizontalFlow(Sizing.fixed(20), Sizing.fixed(20));
            boolean isSel = (col == selectedColor);
            swatch.surface((ctx, comp) -> {
                int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
                ctx.fill(x, y, x + w, y + h, col);
                if (isSel) {
                    ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, 0xFFFFFFFF);
                    ctx.drawBorder(x - 2, y - 2, w + 4, h + 4, VIOLET);
                } else {
                    ctx.drawBorder(x, y, w, h, 0x44000000);
                }
            });
            swatch.mouseDown().subscribe((mx, my, btn) -> {
                if (btn == 0) {
                    selectedColor = col;
                    root.clearChildren();
                    build(root);
                    return true;
                }
                return false;
            });

            if (i < 7) {
                row1.child(swatch);
            } else {
                row2.child(swatch);
            }
        }
        paletteGrid.child(row1);
        paletteGrid.child(row2);
        panel.child(paletteGrid);

        // Action Buttons Row (Cancel / Save)
        FlowLayout btnRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(26));
        btnRow.gap(8);
        btnRow.padding(Insets.of(6, 0, 0, 0));

        ButtonComponent backBtn = Components.button(Text.literal("< Back"), b -> close());
        backBtn.sizing(Sizing.fill(40), Sizing.fixed(24));
        backBtn.renderer(ButtonComponent.Renderer.flat(SURF3, TEXT_M, SURF3));

        ButtonComponent saveBtn = Components.button(Text.literal("SAVE WAYPOINT"), b -> saveAndClose());
        saveBtn.sizing(Sizing.fill(60), Sizing.fixed(24));
        saveBtn.renderer(ButtonComponent.Renderer.flat(VIOLET_D, VIOLET, VIOLET_D));

        btnRow.child(backBtn);
        btnRow.child(saveBtn);
        panel.child(btnRow);

        root.child(panel);
    }

    private void saveAndClose() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Waypoint";

        double x = parseCoord(xField.getText(), 0.0);
        double y = parseCoord(yField.getText(), 64.0);
        double z = parseCoord(zField.getText(), 0.0);

        String worldKey = WaypointManager.getCurrentWorldOrServerKey();

        if (existingWaypoint != null) {
            existingWaypoint.name = name;
            existingWaypoint.x = x;
            existingWaypoint.y = y;
            existingWaypoint.z = z;
            existingWaypoint.dimension = selectedDimension;
            existingWaypoint.color = selectedColor;
            WaypointManager.updateWaypoint(existingWaypoint);
        } else {
            Waypoint wp = new Waypoint(name, x, y, z, selectedDimension, worldKey, selectedColor);
            WaypointManager.addWaypoint(wp);
        }

        close();
    }

    private double parseCoord(String text, double def) {
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (Exception e) {
            return def;
        }
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
