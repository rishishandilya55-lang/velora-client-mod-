package com.velora.client.gui;

import com.velora.client.client.ArmorMod;
import com.velora.client.config.ModConfig;
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

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {

    private enum HudElement {
        FPS("[FPS]") {
            public boolean isEnabled() { return ModConfig.showFps; }
            public int getX() { return ModConfig.fpsX; }
            public void setX(int x) { ModConfig.fpsX = x; }
            public int getY() { return ModConfig.fpsY; }
            public void setY(int y) { ModConfig.fpsY = y; }
            public float getScale() { return ModConfig.fpsScale; }
            public void setScale(float s) { ModConfig.fpsScale = s; }
            public int getBaseWidth() { return 56; }
            public int getBaseHeight() { return 18; }
        },
        KEYS("[Keys]") {
            public boolean isEnabled() { return ModConfig.showKeystrokes; }
            public int getX() { return ModConfig.keystrokesX; }
            public void setX(int x) { ModConfig.keystrokesX = x; }
            public int getY() { return ModConfig.keystrokesY; }
            public void setY(int y) { ModConfig.keystrokesY = y; }
            public float getScale() { return ModConfig.keystrokesScale; }
            public void setScale(float s) { ModConfig.keystrokesScale = s; }
            public int getBaseWidth() { return 52; }
            public int getBaseHeight() { return 70; }
        },
        PING("[Ping]") {
            public boolean isEnabled() { return ModConfig.showPing; }
            public int getX() { return ModConfig.pingX; }
            public void setX(int x) { ModConfig.pingX = x; }
            public int getY() { return ModConfig.pingY; }
            public void setY(int y) { ModConfig.pingY = y; }
            public float getScale() { return ModConfig.pingScale; }
            public void setScale(float s) { ModConfig.pingScale = s; }
            public int getBaseWidth() { return 70; }
            public int getBaseHeight() { return 18; }
        },
        CPS("[CPS]") {
            public boolean isEnabled() { return ModConfig.showCps; }
            public int getX() { return ModConfig.cpsX; }
            public void setX(int x) { ModConfig.cpsX = x; }
            public int getY() { return ModConfig.cpsY; }
            public void setY(int y) { ModConfig.cpsY = y; }
            public float getScale() { return ModConfig.cpsScale; }
            public void setScale(float s) { ModConfig.cpsScale = s; }
            public int getBaseWidth() { return 70; }
            public int getBaseHeight() { return 18; }
        },
        SPRINT("[Sprint]") {
            public boolean isEnabled() { return ModConfig.showToggleSprint; }
            public int getX() { return ModConfig.sprintX; }
            public void setX(int x) { ModConfig.sprintX = x; }
            public int getY() { return ModConfig.sprintY; }
            public void setY(int y) { ModConfig.sprintY = y; }
            public float getScale() { return ModConfig.sprintScale; }
            public void setScale(float s) { ModConfig.sprintScale = s; }
            public int getBaseWidth() { return 120; }
            public int getBaseHeight() { return 18; }
        },
        ARMOR("[Armor]") {
            public boolean isEnabled() { return ModConfig.showArmorStatus; }
            public int getX() { return ModConfig.armorX; }
            public void setX(int x) { ModConfig.armorX = x; }
            public int getY() { return ModConfig.armorY; }
            public void setY(int y) { ModConfig.armorY = y; }
            public float getScale() { return ModConfig.armorScale; }
            public void setScale(float s) { ModConfig.armorScale = s; }
            public int getBaseWidth() { return ArmorMod.getArmorWidth(); }
            public int getBaseHeight() { return ArmorMod.getArmorHeight(); }
        },
        COORDS("[Coords]") {
            public boolean isEnabled() { return ModConfig.showCoordinates; }
            public int getX() { return ModConfig.coordsX; }
            public void setX(int x) { ModConfig.coordsX = x; }
            public int getY() { return ModConfig.coordsY; }
            public void setY(int y) { ModConfig.coordsY = y; }
            public float getScale() { return ModConfig.coordsScale; }
            public void setScale(float s) { ModConfig.coordsScale = s; }
            public int getBaseWidth() { return 110; }
            public int getBaseHeight() { return 18; }
        },
        DAY("[Day]") {
            public boolean isEnabled() { return ModConfig.showDayCounter; }
            public int getX() { return ModConfig.dayX; }
            public void setX(int x) { ModConfig.dayX = x; }
            public int getY() { return ModConfig.dayY; }
            public void setY(int y) { ModConfig.dayY = y; }
            public float getScale() { return ModConfig.dayScale; }
            public void setScale(float s) { ModConfig.dayScale = s; }
            public int getBaseWidth() { return 60; }
            public int getBaseHeight() { return 18; }
        },
        BLOCK("[Block]") {
            public boolean isEnabled() { return ModConfig.showBlockInfo; }
            public int getX() { return ModConfig.blockInfoX; }
            public void setX(int x) { ModConfig.blockInfoX = x; }
            public int getY() { return ModConfig.blockInfoY; }
            public void setY(int y) { ModConfig.blockInfoY = y; }
            public float getScale() { return ModConfig.blockInfoScale; }
            public void setScale(float s) { ModConfig.blockInfoScale = s; }
            public int getBaseWidth() { return 90; }
            public int getBaseHeight() { return 18; }
        },
        MINIMAP("[Map]") {
            public boolean isEnabled() { return ModConfig.showMinimap; }
            public int getX() { return ModConfig.minimapX; }
            public void setX(int x) { ModConfig.minimapX = x; }
            public int getY() { return ModConfig.minimapY; }
            public void setY(int y) { ModConfig.minimapY = y; }
            public float getScale() { return ModConfig.minimapScale; }
            public void setScale(float s) { ModConfig.minimapScale = s; }
            public int getBaseWidth() { return com.velora.client.client.MinimapClient.getMinimapWidth(); }
            public int getBaseHeight() { return com.velora.client.client.MinimapClient.getMinimapHeight(); }
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

    private static final int BG       = 0xCC08080A;
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
    private static final int VIOLET_F = 0x1FA78BFA;
    private static final int GREEN    = 0xFF34D399;
    private static final int GREEN_D  = 0xFF166534;
    private static final int RED      = 0xFFEF4444;

    private HudElement selectedElement = null;
    private HudElement hoveredElement = null;
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
        root.verticalAlignment(VerticalAlignment.BOTTOM);
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.surface(Surface.flat(0x00000000));
        root.sizing(Sizing.fill(100), Sizing.fill(100));

        FlowLayout toolbar = Containers.horizontalFlow(Sizing.fixed(420), Sizing.fixed(24));
        toolbar.surface((ctx, comp) -> {
            int x = comp.x(), y = comp.y(), w = comp.width(), h = comp.height();
            ctx.fill(x, y, x + w, y + h, SURF2);
            ctx.drawBorder(x, y, w, h, BORDER_S);
        });
        toolbar.verticalAlignment(VerticalAlignment.CENTER);
        toolbar.padding(Insets.of(2, 6, 2, 6));
        toolbar.gap(4);

        ButtonComponent snapBtn = Components.button(
            Text.literal(ModConfig.hudSnap ? "Snap ON" : "Snap OFF"),
            btn -> {
                ModConfig.hudSnap = !ModConfig.hudSnap;
                btn.setMessage(Text.literal(ModConfig.hudSnap ? "Snap ON" : "Snap OFF"));
            });
        snapBtn.sizing(Sizing.fixed(52), Sizing.fixed(18));
        snapBtn.renderer(ButtonComponent.Renderer.flat(
            ModConfig.hudSnap ? GREEN_D : SURF3,
            ModConfig.hudSnap ? GREEN : SURF2,
            ModConfig.hudSnap ? GREEN_D : SURF3));

        ButtonComponent resetBtn = Components.button(Text.literal("Reset"), btn -> {
            ModConfig.resetHudPositions();
            selectedElement = null;
        });
        resetBtn.sizing(Sizing.fixed(46), Sizing.fixed(18));
        resetBtn.renderer(ButtonComponent.Renderer.flat(0xFF7F1D1D, RED, 0xFF7F1D1D));

        ButtonComponent doneBtn = Components.button(Text.literal("Done"), btn -> this.close());
        doneBtn.sizing(Sizing.fixed(46), Sizing.fixed(18));
        doneBtn.renderer(ButtonComponent.Renderer.flat(VIOLET_S, VIOLET, VIOLET_S));

        FlowLayout infoBox = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
        infoBox.verticalAlignment(VerticalAlignment.CENTER);
        infoBox.horizontalAlignment(HorizontalAlignment.RIGHT);

        if (selectedElement != null) {
            infoBox.child(Components.label(Text.literal(
                selectedElement.getLabel() + " | " +
                String.format("%.0fx%.0f", selectedElement.getScaledWidth(), selectedElement.getScaledHeight()) +
                " | " + String.format("%.2fx", selectedElement.getScale())))
                .color(Color.ofArgb(TEXT_M)));
        } else {
            infoBox.child(Components.label(Text.literal("Click element to select, drag to move, scroll to resize"))
                .color(Color.ofArgb(TEXT_F)));
        }

        toolbar.child(snapBtn);
        toolbar.child(resetBtn);
        toolbar.child(doneBtn);
        toolbar.child(infoBox);

        FlowLayout wrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(30));
        wrapper.verticalAlignment(VerticalAlignment.CENTER);
        wrapper.horizontalAlignment(HorizontalAlignment.CENTER);
        wrapper.child(toolbar);

        root.child(wrapper);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, BG);

        if (ModConfig.hudSnap) {
            int gridStep = ModConfig.snapGridSize > 0 ? ModConfig.snapGridSize : 10;
            for (int gx = 0; gx < this.width; gx += gridStep * 2)
                context.fill(gx, 0, gx + 1, this.height, 0x08FFFFFF);
            for (int gy = 0; gy < this.height; gy += gridStep * 2)
                context.fill(0, gy, this.width, gy + 1, 0x08FFFFFF);
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        snapGuideX = -1;
        snapGuideY = -1;

        hoveredElement = null;
        for (HudElement elem : HudElement.values()) {
            if (!elem.isEnabled()) continue;
            if (isMouseOver(mouseX, mouseY, elem.getX() - 4, elem.getY() - 4, elem.getScaledWidth() + 8, elem.getScaledHeight() + 8)) {
                hoveredElement = elem;
            }
        }

        if (isDragging && selectedElement != null) {
            int elemCX = selectedElement.getX() + selectedElement.getScaledWidth() / 2;
            int elemCY = selectedElement.getY() + selectedElement.getScaledHeight() / 2;
            if (Math.abs(elemCX - cx) < 3) snapGuideX = cx;
            if (Math.abs(elemCY - cy) < 3) snapGuideY = cy;
        }

        if (snapGuideX != -1) context.fill(snapGuideX, 30, snapGuideX + 1, this.height, 0xAA7C3AED);
        if (snapGuideY != -1) context.fill(0, snapGuideY, this.width, snapGuideY + 1, 0xAA7C3AED);

        for (HudElement elem : HudElement.values()) {
            if (!elem.isEnabled()) continue;
            int x = elem.getX(), y = elem.getY();
            int w = elem.getScaledWidth(), h = elem.getScaledHeight();
            boolean isSel = (selectedElement == elem), isHov = (hoveredElement == elem);

            int fill = isSel ? 0x55A78BFA : isHov ? 0x33A78BFA : 0x14A78BFA;
            int bx = x - 4, by = y - 4, bw = w + 8, bh = h + 8;
            context.fill(bx, by, bx + bw, by + bh, fill);

            int border = isSel ? 0xFFA78BFA : isHov ? 0xFF8B5CF6 : 0xFF6D28D9;
            drawOutline(context, bx, by, bw, bh, border);

            context.drawText(this.textRenderer, elem.getLabel(), x, y + (h > 18 ? 4 : (h - 8) / 2), 0xFFFFFFFF, true);

            if (isSel || isHov) {
                String scaleStr = String.format("%.2fx", elem.getScale());
                int badgeW = this.textRenderer.getWidth(scaleStr) + 6;
                int badgeX = bx + bw - badgeW - 2;
                int badgeY = by - 10;
                if (badgeY < 32) badgeY = by + bh + 2;
                context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 10, 0xFF16161A);
                drawOutline(context, badgeX, badgeY, badgeW, 10, 0x44A78BFA);
                context.drawText(this.textRenderer, scaleStr, badgeX + 3, badgeY + 1, 0xFFE4E4E7, false);
            }
        }

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
            else targetX = snapValue(targetX);
            if (Math.abs(elemCY - cy) <= 6) { targetY = cy - elemH / 2; snapGuideY = cy; }
            else targetY = snapValue(targetY);

            targetX = Math.max(0, Math.min(this.width - elemW, targetX));
            targetY = Math.max(0, Math.min(this.height - elemH - 28, targetY));

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
            if (keyCode == GLFW.GLFW_KEY_UP)    { selectedElement.setY(Math.max(30, selectedElement.getY() - step)); return true; }
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
