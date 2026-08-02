package com.example.fpsdisplay.gui;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleSettingsScreen extends Screen {
    private final String moduleName;
    private boolean listeningForKey = false;

    public ModuleSettingsScreen(String moduleName) {
        super(Text.literal(moduleName + " Settings"));
        this.moduleName = moduleName;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int panelW = 460;
        int panelH = 280;
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        // Dark floating panel
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC1A1A2E);

        // Header Title
        context.fill(panelX, panelY, panelX + panelW, panelY + 36, 0xEE222240);
        context.drawText(this.textRenderer, moduleName + " Settings", panelX + 14, panelY + 12, 0xFFFFFFFF, true);

        // Close X button
        int xBtnX = panelX + panelW - 26;
        int xBtnY = panelY + 8;
        boolean xHov = mouseX >= xBtnX && mouseX <= xBtnX + 18 && mouseY >= xBtnY && mouseY <= xBtnY + 18;
        if (xHov) context.fill(xBtnX - 2, xBtnY - 2, xBtnX + 20, xBtnY + 20, 0x55FF4444);
        context.drawCenteredTextWithShadow(this.textRenderer, "X", xBtnX + 9, xBtnY + 5, 0xFFFFFFFF);

        int contentY = panelY + 48;
        int rowH = 32;

        if ("Armor Status".equals(moduleName)) {
            renderRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "HUD Orientation: " + ModConfig.armorOrientation, "[ Click to Toggle ]", 0xFFA855F7);

            renderRow(context, mouseX, mouseY, panelX + 20, contentY + 36, panelW - 40, rowH,
                    "Background Style: " + ModConfig.armorBackgroundStyle, "[ Click to Cycle ]", 0xFFC084FC);

            renderRow(context, mouseX, mouseY, panelX + 20, contentY + 72, panelW - 40, rowH,
                    "Durability Display: " + ModConfig.armorDurabilityMode, "[ Click to Cycle ]", 0xFF38BDF8);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 108, panelW - 40, rowH,
                    "Show Offhand Item", ModConfig.armorShowOffhand);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 144, panelW - 40, rowH,
                    "Count Inventory Items (Blocks/Items)", ModConfig.armorShowCount);

        } else if ("NoHurtCam".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Enable NoHurtCam (Disable Camera Wobble)", ModConfig.showNoHurtCam);

            String intensityText = ModConfig.hurtCamIntensity <= 0.0f ? "0% (No Wobble)" : (int)(ModConfig.hurtCamIntensity * 100) + "%";
            renderRow(context, mouseX, mouseY, panelX + 20, contentY + 38, panelW - 40, rowH,
                    "Hurt Camera Intensity: " + intensityText, "[ Click to Cycle ]", 0xFF22C55E);

        } else if ("Zoom Mod".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Smooth Zoom Animation", ModConfig.zoomSmooth);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 38, panelW - 40, rowH,
                    "Scale Mouse Sensitivity While Zoomed", ModConfig.zoomScaleSensitivity);

            context.drawCenteredTextWithShadow(this.textRenderer, "Hold key 'C' to Zoom. Scroll Mouse Wheel to adjust Zoom level!", cx, contentY + 105, 0xFFA1A1AA);

        } else if ("Free Look".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Enable 360 Camera Free Look", ModConfig.showFreeLook);

            String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.freeLookKey, 0);
            if (keyName == null) keyName = "KEY " + ModConfig.freeLookKey;
            renderRow(context, mouseX, mouseY, panelX + 20, contentY + 38, panelW - 40, rowH,
                    "Free Look Keybind", "[ " + keyName.toUpperCase() + " ]", listeningForKey ? 0xFF00FFCC : 0xFFA855F7);

            context.drawCenteredTextWithShadow(this.textRenderer, "Hold this key to rotate camera freely around player!", cx, contentY + 105, 0xFFA1A1AA);

        } else if ("Snap Look".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Enable Quick Rear View Snap", ModConfig.showSnapLook);

            String keyName = listeningForKey ? "> PRESS ANY KEY <" : GLFW.glfwGetKeyName(ModConfig.snapLookKey, 0);
            if (keyName == null) keyName = "KEY " + ModConfig.snapLookKey;
            renderRow(context, mouseX, mouseY, panelX + 20, contentY + 38, panelW - 40, rowH,
                    "Snap Look Keybind", "[ " + keyName.toUpperCase() + " ]", listeningForKey ? 0xFF00FFCC : 0xFFA855F7);

            context.drawCenteredTextWithShadow(this.textRenderer, "Hold this key to instantly look behind your player!", cx, contentY + 105, 0xFFA1A1AA);

        } else if ("CPS Counter".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Show Right-Click CPS", ModConfig.showRightCps);

        } else if ("WASD Keystrokes".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Show Mouse Buttons (LMB / RMB)", ModConfig.showMouseStrokes);

        } else if ("Minimap".equals(moduleName)) {
            renderRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Minimap Shape: " + ModConfig.minimapShape, "[ Click to Cycle ]", 0xFFA855F7);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 36, panelW - 40, rowH,
                    "Show Entity Radar (Mobs, Players, Items)", ModConfig.minimapShowEntities);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 72, panelW - 40, rowH,
                    "Rotate Compass Bezel with Player Yaw", ModConfig.minimapRotateMap);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 108, panelW - 40, rowH,
                    "Show Position Coordinates Footer", ModConfig.minimapShowCoordinates);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 144, panelW - 40, rowH,
                    "Show Biome Name & Heading Header", ModConfig.minimapShowBiome);

        } else if ("Capes & Physics".equals(moduleName)) {
            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY, panelW - 40, rowH,
                    "Enable Local Velora Cape", ModConfig.enableCape);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 36, panelW - 40, rowH,
                    "Enable Cape Physics Simulation", ModConfig.enableCapePhysics);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 72, panelW - 40, rowH,
                    "Apply Cape to Local Player Only", ModConfig.capeOnlyLocal);

            renderToggleRow(context, mouseX, mouseY, panelX + 20, contentY + 108, panelW - 40, rowH,
                    "Override Vanilla / Default Capes", ModConfig.overrideDefaultCape);

        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, "Module settings for " + moduleName, cx, contentY + 40, 0xFFA855F7);
            context.drawCenteredTextWithShadow(this.textRenderer, "Scroll mouse wheel in HUD Editor to resize this element!", cx, contentY + 65, 0xFFA1A1AA);
        }
    }

    private void renderToggleRow(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String label, boolean enabled) {
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        VeloraRenderUtil.drawRoundedRect(context, x, y, w, h, 6, hov ? 0x55FFFFFF : 0x33FFFFFF);
        context.drawText(this.textRenderer, label, x + 14, y + (h - 8) / 2, 0xFFFFFFFF, false);

        int sw = 32; int sh = 16;
        int sx = x + w - sw - 14;
        int sy = y + (h - sh) / 2;
        VeloraRenderUtil.drawSwitch(context, sx, sy, sw, sh, enabled, hov);
    }

    private void renderRow(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String label, String action, int color) {
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        VeloraRenderUtil.drawRoundedRect(context, x, y, w, h, 6, hov ? 0x55FFFFFF : 0x33FFFFFF);
        context.drawText(this.textRenderer, label, x + 14, y + (h - 8) / 2, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, action, x + w - this.textRenderer.getWidth(action) - 14, y + (h - 8) / 2, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelW = 460;
        int panelH = 280;
        int panelX = cx - panelW / 2;
        int panelY = cy - panelH / 2;

        int xBtnX = panelX + panelW - 26;
        int xBtnY = panelY + 8;
        if (mouseX >= xBtnX && mouseX <= xBtnX + 18 && mouseY >= xBtnY && mouseY <= xBtnY + 18) {
            this.close();
            return true;
        }

        int contentY = panelY + 48;
        int rowH = 32;
        int innerX = panelX + 20;
        int innerW = panelW - 40;

        if ("Free Look".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.showFreeLook = !ModConfig.showFreeLook;
                    return true;
                }
                if (mouseY >= contentY + 38 && mouseY <= contentY + 38 + rowH) {
                    listeningForKey = true;
                    return true;
                }
            }
        } else if ("Snap Look".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.showSnapLook = !ModConfig.showSnapLook;
                    return true;
                }
                if (mouseY >= contentY + 38 && mouseY <= contentY + 38 + rowH) {
                    listeningForKey = true;
                    return true;
                }
            }
        } else if ("Armor Status".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.armorOrientation = "VERTICAL".equalsIgnoreCase(ModConfig.armorOrientation) ? "HORIZONTAL" : "VERTICAL";
                    return true;
                }
                if (mouseY >= contentY + 36 && mouseY <= contentY + 36 + rowH) {
                    if ("MODERN".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "TRANSPARENT";
                    else if ("TRANSPARENT".equalsIgnoreCase(ModConfig.armorBackgroundStyle)) ModConfig.armorBackgroundStyle = "COMPACT";
                    else ModConfig.armorBackgroundStyle = "MODERN";
                    return true;
                }
                if (mouseY >= contentY + 72 && mouseY <= contentY + 72 + rowH) {
                    if ("MAX_VALUE".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "PERCENT";
                    else if ("PERCENT".equalsIgnoreCase(ModConfig.armorDurabilityMode)) ModConfig.armorDurabilityMode = "VALUE";
                    else ModConfig.armorDurabilityMode = "MAX_VALUE";
                    return true;
                }
                if (mouseY >= contentY + 108 && mouseY <= contentY + 108 + rowH) {
                    ModConfig.armorShowOffhand = !ModConfig.armorShowOffhand;
                    return true;
                }
                if (mouseY >= contentY + 144 && mouseY <= contentY + 144 + rowH) {
                    ModConfig.armorShowCount = !ModConfig.armorShowCount;
                    return true;
                }
            }
        } else if ("NoHurtCam".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.showNoHurtCam = !ModConfig.showNoHurtCam;
                    return true;
                }
                if (mouseY >= contentY + 38 && mouseY <= contentY + 38 + rowH) {
                    if (ModConfig.hurtCamIntensity <= 0.0f) ModConfig.hurtCamIntensity = 0.5f;
                    else if (ModConfig.hurtCamIntensity <= 0.5f) ModConfig.hurtCamIntensity = 1.0f;
                    else ModConfig.hurtCamIntensity = 0.0f;
                    return true;
                }
            }
        } else if ("Zoom Mod".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.zoomSmooth = !ModConfig.zoomSmooth;
                    return true;
                }
                if (mouseY >= contentY + 38 && mouseY <= contentY + 38 + rowH) {
                    ModConfig.zoomScaleSensitivity = !ModConfig.zoomScaleSensitivity;
                    return true;
                }
            }
        } else if ("CPS Counter".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW && mouseY >= contentY && mouseY <= contentY + rowH) {
                ModConfig.showRightCps = !ModConfig.showRightCps;
                return true;
            }
        } else if ("WASD Keystrokes".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW && mouseY >= contentY && mouseY <= contentY + rowH) {
                ModConfig.showMouseStrokes = !ModConfig.showMouseStrokes;
                return true;
            }
        } else if ("Capes & Physics".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.enableCape = !ModConfig.enableCape;
                    return true;
                }
                if (mouseY >= contentY + 36 && mouseY <= contentY + 36 + rowH) {
                    ModConfig.enableCapePhysics = !ModConfig.enableCapePhysics;
                    return true;
                }
                if (mouseY >= contentY + 72 && mouseY <= contentY + 72 + rowH) {
                    ModConfig.capeOnlyLocal = !ModConfig.capeOnlyLocal;
                    return true;
                }
                if (mouseY >= contentY + 108 && mouseY <= contentY + 108 + rowH) {
                    ModConfig.overrideDefaultCape = !ModConfig.overrideDefaultCape;
                    return true;
                }
            }
        } else if ("Minimap".equals(moduleName)) {
            if (mouseX >= innerX && mouseX <= innerX + innerW) {
                if (mouseY >= contentY && mouseY <= contentY + rowH) {
                    ModConfig.minimapShape = "CIRCLE".equalsIgnoreCase(ModConfig.minimapShape) ? "SQUARE" : "CIRCLE";
                    return true;
                }
                if (mouseY >= contentY + 36 && mouseY <= contentY + 36 + rowH) {
                    ModConfig.minimapShowEntities = !ModConfig.minimapShowEntities;
                    return true;
                }
                if (mouseY >= contentY + 72 && mouseY <= contentY + 72 + rowH) {
                    ModConfig.minimapRotateMap = !ModConfig.minimapRotateMap;
                    return true;
                }
                if (mouseY >= contentY + 108 && mouseY <= contentY + 108 + rowH) {
                    ModConfig.minimapShowCoordinates = !ModConfig.minimapShowCoordinates;
                    return true;
                }
                if (mouseY >= contentY + 144 && mouseY <= contentY + 144 + rowH) {
                    ModConfig.minimapShowBiome = !ModConfig.minimapShowBiome;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if ("Free Look".equals(moduleName)) {
                    ModConfig.freeLookKey = keyCode;
                } else if ("Snap Look".equals(moduleName)) {
                    ModConfig.snapLookKey = keyCode;
                }
                ModConfig.saveConfig();
            }
            listeningForKey = false;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ModConfig.saveConfig();
        super.close();
    }
}
