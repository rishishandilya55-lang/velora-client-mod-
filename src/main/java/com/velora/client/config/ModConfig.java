package com.velora.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    // Mod Toggle Switches
    public static boolean showFps = true;
    public static boolean showKeystrokes = true;
    public static boolean showMouseStrokes = true;
    public static boolean showPing = true;
    public static boolean showCps = true;
    public static boolean showRightCps = true;
    public static boolean showZoom = true;
    public static boolean showToggleSprint = true;
    public static boolean showToggleSneak = false;
    public static boolean showFullbright = false;
    public static boolean showFreeLook = true;
    public static boolean showSnapLook = true;

    // Keybindings Storage
    public static int freeLookKey = 86; // GLFW_KEY_V
    public static int snapLookKey = 66; // GLFW_KEY_B

    // Client Mods Toggles
    public static boolean showArmorStatus = true;
    public static boolean showCoordinates = true;
    public static boolean showDayCounter = true;
    public static boolean showBlockInfo = true;
    public static boolean showMinimap = true;
    public static boolean minimapShowEntities = true;
    public static String minimapShape = "CIRCLE"; // "CIRCLE" or "SQUARE"
    public static boolean minimapRotateMap = false;
    public static boolean minimapShowCoordinates = true;
    public static boolean minimapShowBiome = true;
    public static float minimapZoom = 1.0f;
    public static boolean minimapShowEntityNames = false;
    public static boolean showNoHurtCam = true;
    public static float hurtCamIntensity = 0.0f; // 0.0f = 0% camera wobble (no hurt cam), 1.0f = 100% normal
    public static boolean showNametag = true;
    public static boolean nametagShowBadge = true;

    // Chat & Social
    public static boolean showChatPrefix = true;
    public static boolean showTabListPrefix = true;
    public static boolean showChatColors = true;
    public static boolean showItemTooltips = true;

    // Capes & Cape Physics
    public static boolean enableCape = true;
    public static int selectedCape = 0; // 0 = Velora, 1 = Classic
    public static boolean enableCapePhysics = true;
    public static boolean capeOnlyLocal = true;
    public static boolean overrideDefaultCape = true;
    public static boolean[] favoriteCosmetics = new boolean[]{true, false, false, false, false, false};

    // Toggleable Performance Optimizations (Client Settings)
    public static boolean optiFastMath = true;
    public static boolean optiLimitParticles = false;
    public static boolean optiDisableFog = false;
    public static boolean optiLowMemoryMode = true;
    public static boolean optiEntityCulling = true;

    // Fullbright Gamma Option
    public static double fullbrightGamma = 1.0;

    // Zoom Mod Options
    public static boolean zoomSmooth = true;
    public static boolean zoomCinematic = false;
    public static boolean zoomScaleSensitivity = true;
    public static float zoomAmount = 30.0f;
    public static float currentZoomScroll = 0.0f;

    // Armor Status Options
    public static String armorOrientation = "VERTICAL"; // "VERTICAL" or "HORIZONTAL"
    public static String armorDurabilityMode = "MAX_VALUE"; // "MAX_VALUE", "VALUE", "PERCENT"
    public static String armorBackgroundStyle = "MODERN"; // "MODERN", "TRANSPARENT", "COMPACT"
    public static boolean armorShowCount = true;
    public static boolean armorShowOffhand = true;

    // HUD Editor Snap & Grid Options
    public static boolean hudSnap = true;
    public static int snapGridSize = 10;

    // Keystrokes Opacity (0x80 = 50% opacity, 0xFF = 100% solid)
    public static int keystrokesOpacity = 0x80;

    // HUD Positions (Draggable)
    public static int fpsX = 10;
    public static int fpsY = 10;

    public static int keystrokesX = 10;
    public static int keystrokesY = 30;

    public static int pingX = 10;
    public static int pingY = 100;

    public static int cpsX = 10;
    public static int cpsY = 140;

    public static int sprintX = 10;
    public static int sprintY = 170;

    public static int armorX = 10;
    public static int armorY = 200;

    public static int coordsX = 10;
    public static int coordsY = 230;

    public static int dayX = 10;
    public static int dayY = 260;

    public static int blockInfoX = 10;
    public static int blockInfoY = 290;

    public static int minimapX = 10;
    public static int minimapY = 320;

    // HUD Scales (Resizable!)
    public static float fpsScale = 1.0f;
    public static float keystrokesScale = 1.0f;
    public static float pingScale = 1.0f;
    public static float cpsScale = 1.0f;
    public static float sprintScale = 1.0f;
    public static float armorScale = 1.0f;
    public static float coordsScale = 1.0f;
    public static float dayScale = 1.0f;
    public static float blockInfoScale = 1.0f;
    public static float minimapScale = 1.0f;

    public static void resetHudPositions() {
        fpsX = 10; fpsY = 10; fpsScale = 1.0f;
        keystrokesX = 10; keystrokesY = 30; keystrokesScale = 1.0f;
        pingX = 10; pingY = 100; pingScale = 1.0f;
        cpsX = 10; cpsY = 140; cpsScale = 1.0f;
        sprintX = 10; sprintY = 170; sprintScale = 1.0f;
        armorX = 10; armorY = 200; armorScale = 1.0f;
        coordsX = 10; coordsY = 230; coordsScale = 1.0f;
        dayX = 10; dayY = 260; dayScale = 1.0f;
        blockInfoX = 10; blockInfoY = 290; blockInfoScale = 1.0f;
        minimapX = 10; minimapY = 320; minimapScale = 1.0f;
        saveConfig();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File getConfigFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("velora.json").toFile();
    }

    public static void saveConfig() {
        try {
            File file = getConfigFile();
            LOGGER.debug("[Velora] Saving config to {}", file.getAbsolutePath());
            ConfigData data = ConfigData.fromCurrentConfig();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
            LOGGER.info("[Velora] Config saved successfully");
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to save config", e);
        }
    }

    public static void loadConfig() {
        try {
            File file = getConfigFile();
            LOGGER.debug("[Velora] Loading config from {}", file.getAbsolutePath());
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    ConfigData data = GSON.fromJson(reader, ConfigData.class);
                    if (data != null) {
                        data.applyToConfig();
                        LOGGER.debug("[Velora] Config values applied: selectedCape={}, enableCape={}, showFullbright={}, showCps={}, showKeystrokes={}",
                                selectedCape, enableCape, showFullbright, showCps, showKeystrokes);
                        LOGGER.info("[Velora] Config loaded successfully from file");
                    } else {
                        LOGGER.warn("[Velora] Config file was empty or invalid, using defaults");
                    }
                }
            } else {
                LOGGER.info("[Velora] No config file found, using defaults");
            }
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to load config", e);
        }
    }

    private static class ConfigData {
        public boolean showFps = ModConfig.showFps;
        public boolean showKeystrokes = ModConfig.showKeystrokes;
        public boolean showMouseStrokes = ModConfig.showMouseStrokes;
        public boolean showPing = ModConfig.showPing;
        public boolean showCps = ModConfig.showCps;
        public boolean showRightCps = ModConfig.showRightCps;
        public boolean showZoom = ModConfig.showZoom;
        public boolean showToggleSprint = ModConfig.showToggleSprint;
        public boolean showToggleSneak = ModConfig.showToggleSneak;
        public boolean showFullbright = ModConfig.showFullbright;

        public boolean showArmorStatus = ModConfig.showArmorStatus;
        public boolean showCoordinates = ModConfig.showCoordinates;
        public boolean showDayCounter = ModConfig.showDayCounter;
        public boolean showBlockInfo = ModConfig.showBlockInfo;
        public boolean showMinimap = ModConfig.showMinimap;
        public boolean minimapShowEntities = ModConfig.minimapShowEntities;
        public String minimapShape = ModConfig.minimapShape;
        public boolean minimapRotateMap = ModConfig.minimapRotateMap;
        public boolean minimapShowCoordinates = ModConfig.minimapShowCoordinates;
        public boolean minimapShowBiome = ModConfig.minimapShowBiome;
        public float minimapZoom = ModConfig.minimapZoom;
        public boolean minimapShowEntityNames = ModConfig.minimapShowEntityNames;
        public boolean showNoHurtCam = ModConfig.showNoHurtCam;
        public float hurtCamIntensity = ModConfig.hurtCamIntensity;
        public boolean showNametag = ModConfig.showNametag;
        public boolean nametagShowBadge = ModConfig.nametagShowBadge;

        public boolean showChatPrefix = ModConfig.showChatPrefix;
        public boolean showTabListPrefix = ModConfig.showTabListPrefix;
        public boolean showChatColors = ModConfig.showChatColors;
        public boolean showItemTooltips = ModConfig.showItemTooltips;

        public boolean showFreeLook = ModConfig.showFreeLook;
        public boolean showSnapLook = ModConfig.showSnapLook;
        public int freeLookKey = ModConfig.freeLookKey;
        public int snapLookKey = ModConfig.snapLookKey;

        public boolean enableCape = ModConfig.enableCape;
        public boolean enableCapePhysics = ModConfig.enableCapePhysics;
        public boolean capeOnlyLocal = ModConfig.capeOnlyLocal;
        public boolean overrideDefaultCape = ModConfig.overrideDefaultCape;

        public boolean optiFastMath = ModConfig.optiFastMath;
        public boolean optiLimitParticles = ModConfig.optiLimitParticles;
        public boolean optiDisableFog = ModConfig.optiDisableFog;
        public boolean optiLowMemoryMode = ModConfig.optiLowMemoryMode;
        public boolean optiEntityCulling = ModConfig.optiEntityCulling;

        public boolean zoomSmooth = ModConfig.zoomSmooth;
        public boolean zoomCinematic = ModConfig.zoomCinematic;
        public boolean zoomScaleSensitivity = ModConfig.zoomScaleSensitivity;
        public float zoomAmount = ModConfig.zoomAmount;

        public double fullbrightGamma = ModConfig.fullbrightGamma;

        public boolean hudSnap = ModConfig.hudSnap;
        public int snapGridSize = ModConfig.snapGridSize;
        public int keystrokesOpacity = ModConfig.keystrokesOpacity;

        public int selectedCape = ModConfig.selectedCape;
        public boolean[] favoriteCosmetics = ModConfig.favoriteCosmetics;

        public String armorOrientation = ModConfig.armorOrientation;
        public String armorDurabilityMode = ModConfig.armorDurabilityMode;
        public String armorBackgroundStyle = ModConfig.armorBackgroundStyle;
        public boolean armorShowCount = ModConfig.armorShowCount;
        public boolean armorShowOffhand = ModConfig.armorShowOffhand;

        public int fpsX = ModConfig.fpsX;
        public int fpsY = ModConfig.fpsY;
        public int keystrokesX = ModConfig.keystrokesX;
        public int keystrokesY = ModConfig.keystrokesY;
        public int pingX = ModConfig.pingX;
        public int pingY = ModConfig.pingY;
        public int cpsX = ModConfig.cpsX;
        public int cpsY = ModConfig.cpsY;
        public int sprintX = ModConfig.sprintX;
        public int sprintY = ModConfig.sprintY;
        public int armorX = ModConfig.armorX;
        public int armorY = ModConfig.armorY;
        public int coordsX = ModConfig.coordsX;
        public int coordsY = ModConfig.coordsY;
        public int dayX = ModConfig.dayX;
        public int dayY = ModConfig.dayY;
        public int blockInfoX = ModConfig.blockInfoX;
        public int blockInfoY = ModConfig.blockInfoY;
        public int minimapX = ModConfig.minimapX;
        public int minimapY = ModConfig.minimapY;

        public float fpsScale = ModConfig.fpsScale;
        public float keystrokesScale = ModConfig.keystrokesScale;
        public float pingScale = ModConfig.pingScale;
        public float cpsScale = ModConfig.cpsScale;
        public float sprintScale = ModConfig.sprintScale;
        public float armorScale = ModConfig.armorScale;
        public float coordsScale = ModConfig.coordsScale;
        public float dayScale = ModConfig.dayScale;
        public float blockInfoScale = ModConfig.blockInfoScale;
        public float minimapScale = ModConfig.minimapScale;

        public static ConfigData fromCurrentConfig() {
            return new ConfigData();
        }

        public void applyToConfig() {
            ModConfig.showFps = this.showFps;
            ModConfig.showKeystrokes = this.showKeystrokes;
            ModConfig.showMouseStrokes = this.showMouseStrokes;
            ModConfig.showPing = this.showPing;
            ModConfig.showCps = this.showCps;
            ModConfig.showRightCps = this.showRightCps;
            ModConfig.showZoom = this.showZoom;
            ModConfig.showToggleSprint = this.showToggleSprint;
            ModConfig.showToggleSneak = this.showToggleSneak;
            ModConfig.showFullbright = this.showFullbright;

            ModConfig.showArmorStatus = this.showArmorStatus;
            ModConfig.showCoordinates = this.showCoordinates;
            ModConfig.showDayCounter = this.showDayCounter;
            ModConfig.showBlockInfo = this.showBlockInfo;
            ModConfig.showMinimap = this.showMinimap;
            ModConfig.minimapShowEntities = this.minimapShowEntities;
            if (this.minimapShape != null) ModConfig.minimapShape = this.minimapShape;
            ModConfig.minimapRotateMap = this.minimapRotateMap;
            ModConfig.minimapShowCoordinates = this.minimapShowCoordinates;
            ModConfig.minimapShowBiome = this.minimapShowBiome;
            ModConfig.minimapZoom = Math.max(0.5f, Math.min(3.0f, this.minimapZoom));
            ModConfig.minimapShowEntityNames = this.minimapShowEntityNames;
            ModConfig.showNoHurtCam = this.showNoHurtCam;
            ModConfig.hurtCamIntensity = Math.max(0.0f, Math.min(1.0f, this.hurtCamIntensity));
            ModConfig.showNametag = this.showNametag;
            ModConfig.nametagShowBadge = this.nametagShowBadge;

            ModConfig.showChatPrefix = this.showChatPrefix;
            ModConfig.showTabListPrefix = this.showTabListPrefix;
            ModConfig.showChatColors = this.showChatColors;
            ModConfig.showItemTooltips = this.showItemTooltips;

            ModConfig.showFreeLook = this.showFreeLook;
            ModConfig.showSnapLook = this.showSnapLook;
            ModConfig.freeLookKey = this.freeLookKey;
            ModConfig.snapLookKey = this.snapLookKey;

            ModConfig.enableCape = this.enableCape;
            ModConfig.enableCapePhysics = this.enableCapePhysics;
            ModConfig.capeOnlyLocal = this.capeOnlyLocal;
            ModConfig.overrideDefaultCape = this.overrideDefaultCape;

            ModConfig.optiFastMath = this.optiFastMath;
            ModConfig.optiLimitParticles = this.optiLimitParticles;
            ModConfig.optiDisableFog = this.optiDisableFog;
            ModConfig.optiLowMemoryMode = this.optiLowMemoryMode;
            ModConfig.optiEntityCulling = this.optiEntityCulling;

            ModConfig.zoomSmooth = this.zoomSmooth;
            ModConfig.zoomCinematic = this.zoomCinematic;
            ModConfig.zoomScaleSensitivity = this.zoomScaleSensitivity;
            ModConfig.zoomAmount = Math.max(1.0f, Math.min(120.0f, this.zoomAmount));

            ModConfig.fullbrightGamma = Math.max(1.0, Math.min(16.0, this.fullbrightGamma));

            ModConfig.hudSnap = this.hudSnap;
            ModConfig.snapGridSize = Math.max(1, Math.min(50, this.snapGridSize));
            ModConfig.keystrokesOpacity = Math.max(0, Math.min(255, this.keystrokesOpacity));

            ModConfig.selectedCape = this.selectedCape;
            if (this.favoriteCosmetics != null) ModConfig.favoriteCosmetics = this.favoriteCosmetics;

            if (this.armorOrientation != null) ModConfig.armorOrientation = this.armorOrientation;
            if (this.armorDurabilityMode != null) ModConfig.armorDurabilityMode = this.armorDurabilityMode;
            if (this.armorBackgroundStyle != null) ModConfig.armorBackgroundStyle = this.armorBackgroundStyle;
            ModConfig.armorShowCount = this.armorShowCount;
            ModConfig.armorShowOffhand = this.armorShowOffhand;

            ModConfig.fpsX = this.fpsX;
            ModConfig.fpsY = this.fpsY;
            ModConfig.keystrokesX = this.keystrokesX;
            ModConfig.keystrokesY = this.keystrokesY;
            ModConfig.pingX = this.pingX;
            ModConfig.pingY = this.pingY;
            ModConfig.cpsX = this.cpsX;
            ModConfig.cpsY = this.cpsY;
            ModConfig.sprintX = this.sprintX;
            ModConfig.sprintY = this.sprintY;
            ModConfig.armorX = this.armorX;
            ModConfig.armorY = this.armorY;
            ModConfig.coordsX = this.coordsX;
            ModConfig.coordsY = this.coordsY;
            ModConfig.dayX = this.dayX;
            ModConfig.dayY = this.dayY;
            ModConfig.blockInfoX = this.blockInfoX;
            ModConfig.blockInfoY = this.blockInfoY;
            ModConfig.minimapX = this.minimapX;
            ModConfig.minimapY = this.minimapY;

            ModConfig.fpsScale = Math.max(0.1f, Math.min(5.0f, this.fpsScale));
            ModConfig.keystrokesScale = Math.max(0.1f, Math.min(5.0f, this.keystrokesScale));
            ModConfig.pingScale = Math.max(0.1f, Math.min(5.0f, this.pingScale));
            ModConfig.cpsScale = Math.max(0.1f, Math.min(5.0f, this.cpsScale));
            ModConfig.sprintScale = Math.max(0.1f, Math.min(5.0f, this.sprintScale));
            ModConfig.armorScale = Math.max(0.1f, Math.min(5.0f, this.armorScale));
            ModConfig.coordsScale = Math.max(0.1f, Math.min(5.0f, this.coordsScale));
            ModConfig.dayScale = Math.max(0.1f, Math.min(5.0f, this.dayScale));
            ModConfig.blockInfoScale = Math.max(0.1f, Math.min(5.0f, this.blockInfoScale));
            ModConfig.minimapScale = Math.max(0.1f, Math.min(5.0f, this.minimapScale));
        }
    }
}
