package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.client.ArmorMod;
import com.example.fpsdisplay.config.ModConfig;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * HUD Editor Screen – OwoLib remake.
 *
 * OwoLib handles the bottom toolbar UI.
 * The HUD element bounding boxes & drag logic stay as raw DrawContext
 * because they operate in full screen-space coordinates (not component-tree relative).
 */
public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {

    // ── HUD element data model ────────────────────────────────────────────────
    private enum HudElement {
        FPS("[FPS Box]") {
            @Override public boolean isEnabled() { return ModConfig.showFps; }
            @Override public int getX() { return ModConfig.fpsX; }
            @Override public void setX(int x) { ModConfig.fpsX = x; }
            @Override public int getY() { return ModConfig.fpsY; }
            @Override public void setY(int y) { ModConfig.fpsY = y; }
            @Override public float getScale() { return ModConfig.fpsScale; }
            @Override public void setScale(float s) { ModConfig.fpsScale = s; }
            @Override public int getBaseWidth() { return 56; }
            @Override public int getBaseHeight() { return 18; }
        },
        KEYSTROKES("[Keystrokes]") {
            @Override public boolean isEnabled() { return ModConfig.showKeystrokes; }
            @Override public int getX() { return ModConfig.keystrokesX; }
            @Override public void setX(int x) { ModConfig.keystrokesX = x; }
            @Override public int getY() { return ModConfig.keystrokesY; }
            @Override public void setY(int y) { ModConfig.keystrokesY = y; }
            @Override public float getScale() { return ModConfig.keystrokesScale; }
            @Override public void setScale(float s) { ModConfig.keystrokesScale = s; }
            @Override public int getBaseWidth() { return 52; }
            @Override public int getBaseHeight() { return 70; }
        },
        PING("[Ping Box]") {
            @Override public boolean isEnabled() { return ModConfig.showPing; }
            @Override public int getX() { return ModConfig.pingX; }
            @Override public void setX(int x) { ModConfig.pingX = x; }
            @Override public int getY() { return ModConfig.pingY; }
            @Override public void setY(int y) { ModConfig.pingY = y; }
            @Override public float getScale() { return ModConfig.pingScale; }
            @Override public void setScale(float s) { ModConfig.pingScale = s; }
            @Override public int getBaseWidth() { return 70; }
            @Override public int getBaseHeight() { return 18; }
        },
        CPS("[CPS Box]") {
            @Override public boolean isEnabled() { return ModConfig.showCps; }
            @Override public int getX() { return ModConfig.cpsX; }
            @Override public void setX(int x) { ModConfig.cpsX = x; }
            @Override public int getY() { return ModConfig.cpsY; }
            @Override public void setY(int y) { ModConfig.cpsY = y; }
            @Override public float getScale() { return ModConfig.cpsScale; }
            @Override public void setScale(float s) { ModConfig.cpsScale = s; }
            @Override public int getBaseWidth() { return 70; }
            @Override public int getBaseHeight() { return 18; }
        },
        SPRINT("[Movement HUD]") {
            @Override public boolean isEnabled() { return ModConfig.showToggleSprint; }
            @Override public int getX() { return ModConfig.sprintX; }
            @Override public void setX(int x) { ModConfig.sprintX = x; }
            @Override public int getY() { return ModConfig.sprintY; }
            @Override public void setY(int y) { ModConfig.sprintY = y; }
            @Override public float getScale() { return ModConfig.sprintScale; }
            @Override public void setScale(float s) { ModConfig.sprintScale = s; }
            @Override public int getBaseWidth() { return 120; }
            @Override public int getBaseHeight() { return 18; }
        },
        ARMOR("[Armor Status]") {
            @Override public boolean isEnabled() { return ModConfig.showArmorStatus; }
            @Override public int getX() { return ModConfig.armorX; }
            @Override public void setX(int x) { ModConfig.armorX = x; }
            @Override public int getY() { return ModConfig.armorY; }
            @Override public void setY(int y) { ModConfig.armorY = y; }
            @Override public float getScale() { return ModConfig.armorScale; }
            @Override public void setScale(float s) { ModConfig.armorScale = s; }
            @Override public int getBaseWidth() { return ArmorMod.getArmorWidth(); }
            @Override public int getBaseHeight() { return ArmorMod.getArmorHeight(); }
        },
        COORDINATES("[Coordinates]") {
            @Override public boolean isEnabled() { return ModConfig.showCoordinates; }
            @Override public int getX() { return ModConfig.coordsX; }
            @Override public void setX(int x) { ModConfig.coordsX = x; }
            @Override public int getY() { return ModConfig.coordsY; }
            @Override public void setY(int y) { ModConfig.coordsY = y; }
            @Override public float getScale() { return ModConfig.coordsScale; }
            @Override public void setScale(float s) { ModConfig.coordsScale = s; }
            @Override public int getBaseWidth() { return 110; }
            @Override public int getBaseHeight() { return 18; }
        },
        DAY_COUNTER("[Day Counter]") {
            @Override public boolean isEnabled() { return ModConfig.showDayCounter; }
            @Override public int getX() { return ModConfig.dayX; }
            @Override public void setX(int x) { ModConfig.dayX = x; }
            @Override public int getY() { return ModConfig.dayY; }
            @Override public void setY(int y) { ModConfig.dayY = y; }
            @Override public float getScale() { return ModConfig.dayScale; }
            @Override public void setScale(float s) { ModConfig.dayScale = s; }
            @Override public int getBaseWidth() { return 60; }
            @Override public int getBaseHeight() { return 18; }
        },
        BLOCK_INFO("[Block Info]") {
            @Override public boolean isEnabled() { return ModConfig.showBlockInfo; }
            @Override public int getX() { return ModConfig.blockInfoX; }
            @Override public void setX(int x) { ModConfig.blockInfoX = x; }
            @Override public int getY() { return ModConfig.blockInfoY; }
            @Override public void setY(int y) { ModConfig.blockInfoY = y; }
            @Override public float getScale() { return ModConfig.blockInfoScale; }
            @Override public void setScale(float s) { ModConfig.blockInfoScale = s; }
            @Override public int getBaseWidth() { return 90; }
            @Override public int getBaseHeight() { return 18; }
        },
        MINIMAP("[Minimap]") {
            @Override public boolean isEnabled() { return ModConfig.showMinimap; }
            @Override public int getX() { return ModConfig.minimapX; }
            @Override public void setX(int x) { ModConfig.minimapX = x; }
            @Override public int getY() { return ModConfig.minimapY; }
            @Override public void setY(int y) { ModConfig.minimapY = y; }
            @Override public float getScale() { return ModConfig.minimapScale; }
            @Override public void setScale(float s) { ModConfig.minimapScale = s; }
            @Override public int getBaseWidth() { return com.example.fpsdisplay.client.MinimapClient.getMinimapWidth(); }
            @Override public int getBaseHeight() { return com.example.fpsdisplay.client.MinimapClient.getMinimapHeight(); }
        };

        private final String label;
        HudElement(String label) { this.label = label; }
        public String getLabel() { return label; }
        public abstract boolean isEnabled();
        public abstract int getX();
        public abstract void setX(int x);
        public abstract int getY();
        public abstract void setY(int y);
        public abstract float getScale();
        public abstract void setScale(float scale);
        public abstract int getBaseWidth();
        public abstract int getBaseHeight();
        public int getScaledWidth()  { return (int)(getBaseWidth()  * getScale()); }
        public int getScaledHeight() { return (int)(getBaseHeight() * getScale()); }
    }

    // ── Drag state ────────────────────────────────────────────────────────────
    private HudElement selectedElement = null;
    private HudElement hoveredElement  = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean isDragging = false;
    private int snapGuideX = -1;
    private int snapGuideY = -1;

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        // Transparent root — HUD element boxes are drawn in render() override
        root.verticalAlignment(VerticalAlignment.BOTTOM);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        // ── Bottom toolbar (OwoLib) ────────────────────────────────────────────
        FlowLayout toolbar = Containers.horizontalFlow(Sizing.fixed(380), Sizing.fixed(26));
        toolbar.surface(Surface.flat(0xEE111122));
        toolbar.verticalAlignment(VerticalAlignment.CENTER);
        toolbar.padding(Insets.of(3, 6, 3, 6));
        toolbar.gap(6);

        // Snap toggle button
        ButtonComponent snapBtn = Components.button(
            Text.literal(ModConfig.hudSnap ? "Snap: ON" : "Snap: OFF"),
            btn -> {
                ModConfig.hudSnap = !ModConfig.hudSnap;
                btn.setMessage(Text.literal(ModConfig.hudSnap ? "Snap: ON" : "Snap: OFF"));
                int bg = ModConfig.hudSnap ? 0xAA16A34A : 0xAA3F3F46;
                btn.renderer(ButtonComponent.Renderer.flat(bg, bg, bg));
            });
        snapBtn.sizing(Sizing.fixed(74), Sizing.fixed(20));
        int snapBg = ModConfig.hudSnap ? 0xAA16A34A : 0xAA3F3F46;
        snapBtn.renderer(ButtonComponent.Renderer.flat(snapBg, snapBg, snapBg));

        // Reset layout button
        ButtonComponent resetBtn = Components.button(Text.literal("Reset Layout"), btn -> {
            ModConfig.resetHudPositions();
            selectedElement = null;
        });
        resetBtn.sizing(Sizing.fixed(82), Sizing.fixed(20));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xAA991B1B, 0xAAE11D48, 0xAA991B1B));

        // Done button
        ButtonComponent doneBtn = Components.button(Text.literal("Done"), btn -> this.close());
        doneBtn.sizing(Sizing.fixed(54), Sizing.fixed(20));
        doneBtn.renderer(ButtonComponent.Renderer.flat(0xAA4C1D95, 0xAA6D28D9, 0xAA4C1D95));

        // Hint text
        var hintLabel = Components.label(Text.literal("Drag to move • Scroll resize"))
            .color(Color.ofArgb(0xFFA1A1AA))
            .sizing(Sizing.content(), Sizing.content());

        toolbar.child(snapBtn);
        toolbar.child(resetBtn);
        toolbar.child(doneBtn);
        toolbar.child(hintLabel);

        // Bottom margin
        FlowLayout toolbarWrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(38));
        toolbarWrapper.verticalAlignment(VerticalAlignment.CENTER);
        toolbarWrapper.horizontalAlignment(HorizontalAlignment.CENTER);
        toolbarWrapper.child(toolbar);

        root.child(toolbarWrapper);
    }

    // ── Raw rendering overlay for HUD element boxes ───────────────────────────
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        context.fill(0, 0, this.width, this.height, 0x88000000);

        int cx = this.width / 2;
        int cy = this.height / 2;

        snapGuideX = -1;
        snapGuideY = -1;

        // Snap grid
        if (ModConfig.hudSnap) {
            int gridStep = ModConfig.snapGridSize > 0 ? ModConfig.snapGridSize : 10;
            for (int gx = 0; gx < this.width; gx += gridStep * 2) context.fill(gx, 0, gx + 1, this.height, 0x12FFFFFF);
            for (int gy = 0; gy < this.height; gy += gridStep * 2) context.fill(0, gy, this.width, gy + 1, 0x12FFFFFF);
        }

        // Update hovered element
        hoveredElement = null;
        for (HudElement elem : HudElement.values()) {
            if (!elem.isEnabled()) continue;
            if (isMouseOver(mouseX, mouseY, elem.getX() - 4, elem.getY() - 4, elem.getScaledWidth() + 8, elem.getScaledHeight() + 8)) {
                hoveredElement = elem;
            }
        }

        // Alignment guides
        if (isDragging && selectedElement != null) {
            int elemCX = selectedElement.getX() + selectedElement.getScaledWidth() / 2;
            int elemCY = selectedElement.getY() + selectedElement.getScaledHeight() / 2;
            if (Math.abs(elemCX - cx) < 3) snapGuideX = cx;
            if (Math.abs(elemCY - cy) < 3) snapGuideY = cy;
        }
        if (snapGuideX != -1) context.fill(snapGuideX, 36, snapGuideX + 1, this.height, 0xAAEC4899);
        if (snapGuideY != -1) context.fill(0, snapGuideY, this.width, snapGuideY + 1, 0xAAEC4899);

        // Draw HUD element bounding boxes
        for (HudElement elem : HudElement.values()) {
            if (!elem.isEnabled()) continue;
            int x = elem.getX(), y = elem.getY();
            int w = elem.getScaledWidth(), h = elem.getScaledHeight();
            boolean isSel = (selectedElement == elem), isHov = (hoveredElement == elem);

            int fill = isSel ? 0x888B5CF6 : isHov ? 0x66A855F7 : 0x33A855F7;
            int bx = x - 4, by = y - 4, bw = w + 8, bh = h + 8;
            context.fill(bx, by, bx + bw, by + bh, fill);

            int border = isSel ? 0xFFF472B6 : isHov ? 0xFFC084FC : 0xFFA855F7;
            drawOutline(context, bx, by, bw, bh, border);

            context.drawText(this.textRenderer, elem.getLabel(), x, y + (h > 18 ? 4 : (h - 8) / 2), 0xFFFFFFFF, true);

            if (isSel || isHov) {
                String scaleStr = String.format("%.2fx", elem.getScale());
                int badgeW = this.textRenderer.getWidth(scaleStr) + 6;
                int badgeX = bx + bw - badgeW - 2;
                int badgeY = by - 10;
                if (badgeY < 38) badgeY = by + bh + 2;
                context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 10, 0xDD111122);
                drawOutline(context, badgeX, badgeY, badgeW, 10, 0x88A855F7);
                context.drawText(this.textRenderer, scaleStr, badgeX + 3, badgeY + 1, 0xFFE9D5FF, false);
            }
        }

        // Render OwoLib toolbar on top
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private boolean isMouseOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int snapValue(int val) {
        if (!ModConfig.hudSnap) return val;
        int grid = ModConfig.snapGridSize > 0 ? ModConfig.snapGridSize : 10;
        return Math.round((float) val / grid) * grid;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check HUD elements first (toolbar is in OwoLib layer below)
            for (HudElement elem : HudElement.values()) {
                if (!elem.isEnabled()) continue;
                int x = elem.getX() - 4, y = elem.getY() - 4;
                int w = elem.getScaledWidth() + 8, h = elem.getScaledHeight() + 8;
                if (isMouseOver(mouseX, mouseY, x, y, w, h)) {
                    selectedElement = elem;
                    isDragging = true;
                    dragOffsetX = (int) mouseX - elem.getX();
                    dragOffsetY = (int) mouseY - elem.getY();
                    return true;
                }
            }
            selectedElement = null;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && selectedElement != null) {
            int cx = this.width / 2, cy = this.height / 2;
            int targetX = (int) mouseX - dragOffsetX;
            int targetY = (int) mouseY - dragOffsetY;
            int elemW = selectedElement.getScaledWidth(), elemH = selectedElement.getScaledHeight();

            int elemCX = targetX + elemW / 2, elemCY = targetY + elemH / 2;
            if (Math.abs(elemCX - cx) <= 6) { targetX = cx - elemW / 2; snapGuideX = cx; }
            else { targetX = snapValue(targetX); }
            if (Math.abs(elemCY - cy) <= 6) { targetY = cy - elemH / 2; snapGuideY = cy; }
            else { targetY = snapValue(targetY); }

            targetX = Math.max(0, Math.min(this.width - elemW, targetX));
            targetY = Math.max(0, Math.min(this.height - elemH - 32, targetY));

            selectedElement.setX(targetX);
            selectedElement.setY(targetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        HudElement target = hoveredElement != null ? hoveredElement : selectedElement;
        if (target != null) {
            float newScale = target.getScale() + (float) verticalAmount * 0.05f;
            newScale = Math.max(0.50f, Math.min(3.00f, newScale));
            newScale = Math.round(newScale * 100.0f) / 100.0f;
            target.setScale(newScale);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedElement != null) {
            int step = hasShiftDown() ? 5 : 1;
            if (keyCode == GLFW.GLFW_KEY_UP)    { selectedElement.setY(Math.max(38, selectedElement.getY() - step)); return true; }
            if (keyCode == GLFW.GLFW_KEY_DOWN)  { selectedElement.setY(Math.min(this.height - selectedElement.getScaledHeight(), selectedElement.getY() + step)); return true; }
            if (keyCode == GLFW.GLFW_KEY_LEFT)  { selectedElement.setX(Math.max(0, selectedElement.getX() - step)); return true; }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) { selectedElement.setX(Math.min(this.width - selectedElement.getScaledWidth(), selectedElement.getX() + step)); return true; }
            if (keyCode == GLFW.GLFW_KEY_R)     { selectedElement.setScale(1.0f); return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
