package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.client.ArmorMod;
import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends Screen {

    // 1. Data model for manageable HUD elements
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

        HudElement(String label) {
            this.label = label;
        }

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

        public int getScaledWidth() {
            return (int) (getBaseWidth() * getScale());
        }

        public int getScaledHeight() {
            return (int) (getBaseHeight() * getScale());
        }
    }

    // 2. Drag & Interaction state variables
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Semi-transparent dark background
        this.renderBackground(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        // Reset frame guide indicators
        snapGuideX = -1;
        snapGuideY = -1;

        // 2. Alignment & Snap Grid Lines (when Snapping is Enabled)
        if (ModConfig.hudSnap) {
            int gridStep = ModConfig.snapGridSize > 0 ? ModConfig.snapGridSize : 10;
            for (int gx = 0; gx < this.width; gx += gridStep * 2) {
                context.fill(gx, 0, gx + 1, this.height, 0x12FFFFFF);
            }
            for (int gy = 0; gy < this.height; gy += gridStep * 2) {
                context.fill(0, gy, this.width, gy + 1, 0x12FFFFFF);
            }
        }

        // 3. Top Header Bar Panel
        context.fill(0, 0, this.width, 36, 0xEE222240);
        context.fill(0, 35, this.width, 36, 0x88A855F7);
        context.drawText(this.textRenderer, "VELORA CLIENT - HUD EDITOR", 14, 8, 0xFFA855F7, true);
        context.drawText(this.textRenderer, "Drag elements to move • Scroll to resize • Arrow keys to fine-tune • R to reset scale", 14, 21, 0xFFA1A1AA, false);

        // Header Action Buttons (Snap Toggle, Reset Layout, Done)
        int snapBtnX = this.width - 290;
        int snapBtnY = 6;
        int snapBtnW = 90;
        int snapBtnH = 22;
        boolean snapHov = isMouseOver(mouseX, mouseY, snapBtnX, snapBtnY, snapBtnW, snapBtnH);
        context.fill(snapBtnX, snapBtnY, snapBtnX + snapBtnW, snapBtnY + snapBtnH, ModConfig.hudSnap ? (snapHov ? 0xAA22C55E : 0xAA16A34A) : (snapHov ? 0xAA52525B : 0xAA3F3F46));
        context.drawCenteredTextWithShadow(this.textRenderer, ModConfig.hudSnap ? "Snap: ON" : "Snap: OFF", snapBtnX + snapBtnW / 2, snapBtnY + 7, 0xFFFFFFFF);

        int resetBtnX = this.width - 192;
        int resetBtnY = 6;
        int resetBtnW = 90;
        int resetBtnH = 22;
        boolean resetHov = isMouseOver(mouseX, mouseY, resetBtnX, resetBtnY, resetBtnW, resetBtnH);
        context.fill(resetBtnX, resetBtnY, resetBtnX + resetBtnW, resetBtnY + resetBtnH, resetHov ? 0xAAE11D48 : 0xAA991B1B);
        context.drawCenteredTextWithShadow(this.textRenderer, "Reset Layout", resetBtnX + resetBtnW / 2, resetBtnY + 7, 0xFFFFFFFF);

        int doneBtnX = this.width - 94;
        int doneBtnY = 6;
        int doneBtnW = 80;
        int doneBtnH = 22;
        boolean doneHov = isMouseOver(mouseX, mouseY, doneBtnX, doneBtnY, doneBtnW, doneBtnH);
        context.fill(doneBtnX, doneBtnY, doneBtnX + doneBtnW, doneBtnY + doneBtnH, doneHov ? 0xAA6D28D9 : 0xAA4C1D95);
        context.drawCenteredTextWithShadow(this.textRenderer, "Done", doneBtnX + doneBtnW / 2, doneBtnY + 7, 0xFFFFFFFF);

        // 4. Update Hovered Element
        hoveredElement = null;
        if (mouseY > 36) {
            for (HudElement elem : HudElement.values()) {
                if (!elem.isEnabled()) continue;
                int x = elem.getX() - 4;
                int y = elem.getY() - 4;
                int w = elem.getScaledWidth() + 8;
                int h = elem.getScaledHeight() + 8;
                if (isMouseOver(mouseX, mouseY, x, y, w, h)) {
                    hoveredElement = elem;
                }
            }
        }

        // 5. Draw Dynamic Alignment Guides (Screen Center)
        if (isDragging && selectedElement != null) {
            int elemCenterX = selectedElement.getX() + selectedElement.getScaledWidth() / 2;
            int elemCenterY = selectedElement.getY() + selectedElement.getScaledHeight() / 2;

            if (Math.abs(elemCenterX - cx) < 3) {
                snapGuideX = cx;
            }
            if (Math.abs(elemCenterY - cy) < 3) {
                snapGuideY = cy;
            }
        }

        if (snapGuideX != -1) {
            context.fill(snapGuideX, 36, snapGuideX + 1, this.height, 0xAAEC4899);
        }
        if (snapGuideY != -1) {
            context.fill(0, snapGuideY, this.width, snapGuideY + 1, 0xAAEC4899);
        }

        // 6. Draw HUD Element Bounding Boxes & Selection Overlays
        for (HudElement elem : HudElement.values()) {
            if (!elem.isEnabled()) continue;

            int x = elem.getX();
            int y = elem.getY();
            int w = elem.getScaledWidth();
            int h = elem.getScaledHeight();

            boolean isSelected = (selectedElement == elem);
            boolean isHovered = (hoveredElement == elem);

            // Bounding box fill colors
            int fillColor;
            if (isSelected) {
                fillColor = 0x888B5CF6;
            } else if (isHovered) {
                fillColor = 0x66A855F7;
            } else {
                fillColor = 0x33A855F7;
            }

            // Outer box with padding
            int boxX = x - 4;
            int boxY = y - 4;
            int boxW = w + 8;
            int boxH = h + 8;

            context.fill(boxX, boxY, boxX + boxW, boxY + boxH, fillColor);

            // Border outline
            int borderColor = isSelected ? 0xFFF472B6 : (isHovered ? 0xFFC084FC : 0xFFA855F7);
            drawOutline(context, boxX, boxY, boxW, boxH, borderColor);

            // Label text
            context.drawText(this.textRenderer, elem.getLabel(), x, y + (h > 18 ? 4 : (h - 8) / 2), 0xFFFFFFFF, true);

            // Scale Pill Badge (on Hover / Selection)
            if (isHovered || isSelected) {
                String scaleStr = String.format("%.2fx", elem.getScale());
                int badgeW = this.textRenderer.getWidth(scaleStr) + 6;
                int badgeX = boxX + boxW - badgeW - 2;
                int badgeY = boxY - 10;
                if (badgeY < 38) badgeY = boxY + boxH + 2;

                context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 10, 0xDD111122);
                drawOutline(context, badgeX, badgeY, badgeW, 10, 0x88A855F7);
                context.drawText(this.textRenderer, scaleStr, badgeX + 3, badgeY + 1, 0xFFE9D5FF, false);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawOutline(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private int snapValue(int val) {
        if (!ModConfig.hudSnap) return val;
        int grid = ModConfig.snapGridSize > 0 ? ModConfig.snapGridSize : 10;
        return Math.round((float) val / grid) * grid;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left Click
            // Header Action Buttons
            int snapBtnX = this.width - 290;
            int snapBtnY = 6;
            int snapBtnW = 90;
            int snapBtnH = 22;
            if (isMouseOver(mouseX, mouseY, snapBtnX, snapBtnY, snapBtnW, snapBtnH)) {
                ModConfig.hudSnap = !ModConfig.hudSnap;
                return true;
            }

            int resetBtnX = this.width - 192;
            int resetBtnY = 6;
            int resetBtnW = 90;
            int resetBtnH = 22;
            if (isMouseOver(mouseX, mouseY, resetBtnX, resetBtnY, resetBtnW, resetBtnH)) {
                ModConfig.resetHudPositions();
                selectedElement = null;
                return true;
            }

            int doneBtnX = this.width - 94;
            int doneBtnY = 6;
            int doneBtnW = 80;
            int doneBtnH = 22;
            if (isMouseOver(mouseX, mouseY, doneBtnX, doneBtnY, doneBtnW, doneBtnH)) {
                this.close();
                return true;
            }

            // Check click on HUD elements
            for (HudElement elem : HudElement.values()) {
                if (!elem.isEnabled()) continue;
                int x = elem.getX() - 4;
                int y = elem.getY() - 4;
                int w = elem.getScaledWidth() + 8;
                int h = elem.getScaledHeight() + 8;

                if (isMouseOver(mouseX, mouseY, x, y, w, h)) {
                    selectedElement = elem;
                    isDragging = true;
                    dragOffsetX = (int) mouseX - elem.getX();
                    dragOffsetY = (int) mouseY - elem.getY();
                    return true;
                }
            }

            // Clicked empty background area
            selectedElement = null;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && selectedElement != null) {
            int cx = this.width / 2;
            int cy = this.height / 2;

            int targetX = (int) mouseX - dragOffsetX;
            int targetY = (int) mouseY - dragOffsetY;

            int elemW = selectedElement.getScaledWidth();
            int elemH = selectedElement.getScaledHeight();

            // Magnetic Center Snapping
            int elemCenterX = targetX + elemW / 2;
            int elemCenterY = targetY + elemH / 2;

            if (Math.abs(elemCenterX - cx) <= 6) {
                targetX = cx - elemW / 2;
                snapGuideX = cx;
            } else {
                targetX = snapValue(targetX);
            }

            if (Math.abs(elemCenterY - cy) <= 6) {
                targetY = cy - elemH / 2;
                snapGuideY = cy;
            } else {
                targetY = snapValue(targetY);
            }

            // Screen boundaries clamping
            targetX = Math.max(0, Math.min(this.width - elemW, targetX));
            targetY = Math.max(38, Math.min(this.height - elemH, targetY));

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
            float deltaScale = (float) verticalAmount * 0.05f;
            float newScale = target.getScale() + deltaScale;
            newScale = Math.max(0.50f, Math.min(3.00f, newScale));
            newScale = Math.round(newScale * 100.0f) / 100.0f; // Round to 2 decimals

            target.setScale(newScale);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedElement != null) {
            int step = hasShiftDown() ? 5 : 1;

            if (keyCode == GLFW.GLFW_KEY_UP) {
                selectedElement.setY(Math.max(38, selectedElement.getY() - step));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedElement.setY(Math.min(this.height - selectedElement.getScaledHeight(), selectedElement.getY() + step));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedElement.setX(Math.max(0, selectedElement.getX() - step));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                selectedElement.setX(Math.min(this.width - selectedElement.getScaledWidth(), selectedElement.getX() + step));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_R) {
                selectedElement.setScale(1.0f);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
