buildpackage com.velora.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
    public static boolean showPotionHud = true;
    public static boolean potionHudBackground = true;
    public static boolean potionHudShowIcon = true;
    public static int potionHudTextColor = 0xFFFFFFFF;
    public static boolean potionHudTextRainbow = false;
    // Waypoints Options
    public static boolean showWaypoints = true;
    public static boolean minimapShowWaypoints = true;
    public static boolean waypointsShowDistance = true;
    public static boolean waypointsBeaconBeams = true;

    // Crosshair Mod Options
    public static boolean enableCustomCrosshair = true;
    public static String crosshairPreset = "CLASSIC_CROSS";
    public static int crosshairSize = 4;
    public static int crosshairGap = 3;
    public static int crosshairThickness = 1;
    public static boolean crosshairShowDot = false;
    public static int crosshairDotSize = 2;
    public static int crosshairColor = 0xFFFFFFFF;
    public static boolean crosshairRainbow = false;
    public static boolean crosshairOutline = true;
    public static int crosshairOutlineColor = 0xFF000000;
    public static boolean crosshairHighlightEntity = true;
    public static int crosshairHighlightColor = 0xFFEF4444;
    public static boolean crosshairEnemyCrosshair = true;
    public static String crosshairEnemyMode = "COLOR_CHANGE";
    public static int crosshairEnemyColor = 0xFFEF4444;
    public static boolean crosshairUseCustomScale = false;
    public static String crosshairScaleMode = "NORMAL";
    public static float crosshairScaleFactor = 1.0f;
    public static boolean crosshairDynamic = false;
    public static boolean crosshairAttackIndicator = true;
    public static boolean crosshairThirdPerson = false;
    public static boolean[] crosshairGrid = com.velora.client.client.CustomCrosshairMod.getDefaultGrid();

    // Global HUD Options
    public static boolean hudShowBackground = true;
    public static int hudBackgroundOpacity = 128;
    public static boolean hudTextShadow = true;

    // Individual HUD Options
    public static int fpsTextColor = 0xFFFFFFFF;
    public static boolean fpsTextRainbow = false;
    public static boolean fpsShowPrefix = true;
    public static boolean fpsBackground = true;

    public static boolean pingCustomColor = false;
    public static int pingTextColor = 0xFF55FF55;
    public static boolean pingTextRainbow = false;
    public static boolean pingBackground = true;

    public static int cpsTextColor = 0xFFFFFFFF;
    public static boolean cpsTextRainbow = false;
    public static boolean cpsBackground = true;

    public static int coordsTextColor = 0xFFFFFFFF;
    public static boolean coordsTextRainbow = false;
    public static boolean coordsShowNether = true;
    public static boolean coordsShowDirection = true;
    public static boolean coordsShowBiome = true;
    public static boolean coordsBackground = true;

    public static int dayTextColor = 0xFFFFFFFF;
    public static boolean dayTextRainbow = false;
    public static boolean dayShowTime = true;
    public static boolean dayBackground = true;

    public static int blockInfoTextColor = 0xFFFFFFFF;
    public static boolean blockInfoTextRainbow = false;
    public static boolean blockInfoShowTool = true;
    public static boolean blockInfoBackground = true;

    public static int keystrokesTextColor = 0xFFFFFFFF;
    public static boolean keystrokesRainbow = false;

    // Hit Color Options
    public static boolean showHitColor = true;
    public static int hitColorRed = 255;
    public static int hitColorGreen = 0;
    public static int hitColorBlue = 0;
    public static int hitColorAlpha = 200;
    public static boolean hitColorRainbow = false;
    public static boolean hitColorShowOnArmor = true;

    // Chat & Social
    public static boolean showChatPrefix = true;
    public static boolean showTabListPrefix = true;
    public static boolean showChatColors = true;
    public static boolean chatHighlightMentions = true;
    public static int chatMentionColor = 0xFFFFD700; // Gold
    public static boolean chatMentionSound = true;
    public static boolean chatShowTimestamp = false;
    public static boolean customChatBackground = false;
    public static int chatBackgroundColor = 0x000000;
    public static int chatBackgroundOpacity = 128;

    // Tooltips
    public static boolean showItemTooltips = true;
    public static boolean tooltipShowDurability = true;
    public static boolean tooltipShowId = true;
    public static boolean tooltipShowFood = true;

    // Item Physics
    public static boolean showItemPhysics = true;

    // View Model & Item Scale Options
    public static boolean showViewModel = true;
    public static float viewModelMainHandScale = 1.0f;
    public static float viewModelOffHandScale = 1.0f;
    public static float viewModelMainHandX = 0.0f;
    public static float viewModelMainHandY = 0.0f;
    public static float viewModelMainHandZ = 0.0f;
    public static float viewModelOffHandX = 0.0f;
    public static float viewModelOffHandY = 0.0f;
    public static float viewModelOffHandZ = 0.0f;
    public static float viewModelPitch = 0.0f;
    public static float viewModelYaw = 0.0f;
    public static float viewModelRoll = 0.0f;
    public static Map<String, Float> itemScales = new HashMap<>();
    public static Map<String, Float> itemGroundScales = new HashMap<>();
    public static Map<String, Float> itemGuiScales = new HashMap<>();

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

    public static int potionHudX = 10;
    public static int potionHudY = 350;

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
    public static float potionHudScale = 1.0f;

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
        potionHudX = 10; potionHudY = 350; potionHudScale = 1.0f;
        saveConfig();
    }

    public static float getItemScale(ItemStack stack) {
        if (!showViewModel || stack == null || stack.isEmpty()) return 1.0f;
        // All enchanted items will be the same with normal items, EXCEPT enchanted golden apple which is separate!
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            Float customScale = itemScales.get("minecraft:enchanted_golden_apple");
            return customScale != null ? customScale : 1.0f;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id != null) {
            Float customScale = itemScales.get(id.toString());
            if (customScale != null) return customScale;
        }
        return 1.0f;
    }

    public static float getItemScaleById(String id) {
        if (id == null) return 1.0f;
        Float scale = itemScales.get(id);
        return scale != null ? scale : 1.0f;
    }

    public static void setItemScale(String id, float scale) {
        if (id == null) return;
        if (Math.abs(scale - 1.0f) < 0.01f) {
            itemScales.remove(id);
        } else {
            itemScales.put(id, Math.round(scale * 100.0f) / 100.0f);
        }
        saveConfig();
    }

    public static void resetItemScale(String id) {
        if (id == null) return;
        itemScales.remove(id);
        saveConfig();
    }

    public static float getItemGroundScale(ItemStack stack) {
        if (!showViewModel || stack == null || stack.isEmpty()) return 1.0f;
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            Float customScale = itemGroundScales.get("minecraft:enchanted_golden_apple");
            return customScale != null ? customScale : 1.0f;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id != null) {
            Float customScale = itemGroundScales.get(id.toString());
            if (customScale != null) return customScale;
        }
        return 1.0f;
    }

    public static float getItemGroundScaleById(String id) {
        if (id == null) return 1.0f;
        Float scale = itemGroundScales.get(id);
        return scale != null ? scale : 1.0f;
    }

    public static void setItemGroundScale(String id, float scale) {
        if (id == null) return;
        if (Math.abs(scale - 1.0f) < 0.01f) {
            itemGroundScales.remove(id);
        } else {
            itemGroundScales.put(id, Math.round(scale * 100.0f) / 100.0f);
        }
        saveConfig();
    }

    public static float getItemGuiScale(ItemStack stack) {
        if (!showViewModel || stack == null || stack.isEmpty()) return 1.0f;
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            Float customScale = itemGuiScales.get("minecraft:enchanted_golden_apple");
            return customScale != null ? customScale : 1.0f;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id != null) {
            Float customScale = itemGuiScales.get(id.toString());
            if (customScale != null) return customScale;
        }
        return 1.0f;
    }

    public static float getItemGuiScaleById(String id) {
        if (id == null) return 1.0f;
        Float scale = itemGuiScales.get(id);
        return scale != null ? scale : 1.0f;
    }

    public static void setItemGuiScale(String id, float scale) {
        if (id == null) return;
        if (Math.abs(scale - 1.0f) < 0.01f) {
            itemGuiScales.remove(id);
        } else {
            itemGuiScales.put(id, Math.round(scale * 100.0f) / 100.0f);
        }
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
        public boolean showPotionHud = ModConfig.showPotionHud;
        public boolean potionHudBackground = ModConfig.potionHudBackground;
        public boolean potionHudShowIcon = ModConfig.potionHudShowIcon;
        public int potionHudTextColor = ModConfig.potionHudTextColor;
        public boolean potionHudTextRainbow = ModConfig.potionHudTextRainbow;
        public int potionHudX = ModConfig.potionHudX;
        public int potionHudY = ModConfig.potionHudY;
        public float potionHudScale = ModConfig.potionHudScale;

        public boolean showWaypoints = ModConfig.showWaypoints;
        public boolean minimapShowWaypoints = ModConfig.minimapShowWaypoints;
        public boolean waypointsShowDistance = ModConfig.waypointsShowDistance;
        public boolean waypointsBeaconBeams = ModConfig.waypointsBeaconBeams;

        public boolean enableCustomCrosshair = ModConfig.enableCustomCrosshair;
        public String crosshairPreset = ModConfig.crosshairPreset;
        public int crosshairSize = ModConfig.crosshairSize;
        public int crosshairGap = ModConfig.crosshairGap;
        public int crosshairThickness = ModConfig.crosshairThickness;
        public boolean crosshairShowDot = ModConfig.crosshairShowDot;
        public int crosshairDotSize = ModConfig.crosshairDotSize;
        public int crosshairColor = ModConfig.crosshairColor;
        public boolean crosshairRainbow = ModConfig.crosshairRainbow;
        public boolean crosshairOutline = ModConfig.crosshairOutline;
        public int crosshairOutlineColor = ModConfig.crosshairOutlineColor;
        public boolean crosshairHighlightEntity = ModConfig.crosshairHighlightEntity;
        public int crosshairHighlightColor = ModConfig.crosshairHighlightColor;
        public boolean crosshairEnemyCrosshair = ModConfig.crosshairEnemyCrosshair;
        public String crosshairEnemyMode = ModConfig.crosshairEnemyMode;
        public int crosshairEnemyColor = ModConfig.crosshairEnemyColor;
        public boolean crosshairUseCustomScale = ModConfig.crosshairUseCustomScale;
        public String crosshairScaleMode = ModConfig.crosshairScaleMode;
        public float crosshairScaleFactor = ModConfig.crosshairScaleFactor;
        public boolean crosshairDynamic = ModConfig.crosshairDynamic;
        public boolean crosshairAttackIndicator = ModConfig.crosshairAttackIndicator;
        public boolean crosshairThirdPerson = ModConfig.crosshairThirdPerson;
        public boolean[] crosshairGrid = ModConfig.crosshairGrid;

        public boolean hudShowBackground = ModConfig.hudShowBackground;
        public int hudBackgroundOpacity = ModConfig.hudBackgroundOpacity;
        public boolean hudTextShadow = ModConfig.hudTextShadow;

        public int fpsTextColor = ModConfig.fpsTextColor;
        public boolean fpsTextRainbow = ModConfig.fpsTextRainbow;
        public boolean fpsShowPrefix = ModConfig.fpsShowPrefix;
        public boolean fpsBackground = ModConfig.fpsBackground;

        public boolean pingCustomColor = ModConfig.pingCustomColor;
        public int pingTextColor = ModConfig.pingTextColor;
        public boolean pingTextRainbow = ModConfig.pingTextRainbow;
        public boolean pingBackground = ModConfig.pingBackground;

        public int cpsTextColor = ModConfig.cpsTextColor;
        public boolean cpsTextRainbow = ModConfig.cpsTextRainbow;
        public boolean cpsBackground = ModConfig.cpsBackground;

        public int coordsTextColor = ModConfig.coordsTextColor;
        public boolean coordsTextRainbow = ModConfig.coordsTextRainbow;
        public boolean coordsShowNether = ModConfig.coordsShowNether;
        public boolean coordsShowDirection = ModConfig.coordsShowDirection;
        public boolean coordsShowBiome = ModConfig.coordsShowBiome;
        public boolean coordsBackground = ModConfig.coordsBackground;

        public int dayTextColor = ModConfig.dayTextColor;
        public boolean dayTextRainbow = ModConfig.dayTextRainbow;
        public boolean dayShowTime = ModConfig.dayShowTime;
        public boolean dayBackground = ModConfig.dayBackground;

        public int blockInfoTextColor = ModConfig.blockInfoTextColor;
        public boolean blockInfoTextRainbow = ModConfig.blockInfoTextRainbow;
        public boolean blockInfoShowTool = ModConfig.blockInfoShowTool;
        public boolean blockInfoBackground = ModConfig.blockInfoBackground;

        public int keystrokesTextColor = ModConfig.keystrokesTextColor;
        public boolean keystrokesRainbow = ModConfig.keystrokesRainbow;

        public boolean showHitColor = ModConfig.showHitColor;
        public int hitColorRed = ModConfig.hitColorRed;
        public int hitColorGreen = ModConfig.hitColorGreen;
        public int hitColorBlue = ModConfig.hitColorBlue;
        public int hitColorAlpha = ModConfig.hitColorAlpha;
        public boolean hitColorRainbow = ModConfig.hitColorRainbow;
        public boolean hitColorShowOnArmor = ModConfig.hitColorShowOnArmor;

        public boolean showChatPrefix = ModConfig.showChatPrefix;
        public boolean showTabListPrefix = ModConfig.showTabListPrefix;
        public boolean showChatColors = ModConfig.showChatColors;
        public boolean chatHighlightMentions = ModConfig.chatHighlightMentions;
        public int chatMentionColor = ModConfig.chatMentionColor;
        public boolean chatMentionSound = ModConfig.chatMentionSound;
        public boolean chatShowTimestamp = ModConfig.chatShowTimestamp;
        public boolean customChatBackground = ModConfig.customChatBackground;
        public int chatBackgroundColor = ModConfig.chatBackgroundColor;
        public int chatBackgroundOpacity = ModConfig.chatBackgroundOpacity;

        public boolean showItemTooltips = ModConfig.showItemTooltips;
        public boolean tooltipShowDurability = ModConfig.tooltipShowDurability;
        public boolean tooltipShowId = ModConfig.tooltipShowId;
        public boolean tooltipShowFood = ModConfig.tooltipShowFood;
        public boolean showItemPhysics = ModConfig.showItemPhysics;

        public boolean showViewModel = ModConfig.showViewModel;
        public float viewModelMainHandScale = ModConfig.viewModelMainHandScale;
        public float viewModelOffHandScale = ModConfig.viewModelOffHandScale;
        public float viewModelMainHandX = ModConfig.viewModelMainHandX;
        public float viewModelMainHandY = ModConfig.viewModelMainHandY;
        public float viewModelMainHandZ = ModConfig.viewModelMainHandZ;
        public float viewModelOffHandX = ModConfig.viewModelOffHandX;
        public float viewModelOffHandY = ModConfig.viewModelOffHandY;
        public float viewModelOffHandZ = ModConfig.viewModelOffHandZ;
        public float viewModelPitch = ModConfig.viewModelPitch;
        public float viewModelYaw = ModConfig.viewModelYaw;
        public float viewModelRoll = ModConfig.viewModelRoll;
        public Map<String, Float> itemScales = new HashMap<>(ModConfig.itemScales);
        public Map<String, Float> itemGroundScales = new HashMap<>(ModConfig.itemGroundScales);
        public Map<String, Float> itemGuiScales = new HashMap<>(ModConfig.itemGuiScales);

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
            ModConfig.showPotionHud = this.showPotionHud;
            ModConfig.potionHudBackground = this.potionHudBackground;
            ModConfig.potionHudShowIcon = this.potionHudShowIcon;
            ModConfig.potionHudTextColor = this.potionHudTextColor;
            ModConfig.potionHudTextRainbow = this.potionHudTextRainbow;
            ModConfig.potionHudX = this.potionHudX;
            ModConfig.potionHudY = this.potionHudY;
            ModConfig.potionHudScale = Math.max(0.1f, Math.min(5.0f, this.potionHudScale));

            ModConfig.showWaypoints = this.showWaypoints;
            ModConfig.minimapShowWaypoints = this.minimapShowWaypoints;
            ModConfig.waypointsShowDistance = this.waypointsShowDistance;
            ModConfig.waypointsBeaconBeams = this.waypointsBeaconBeams;

            ModConfig.enableCustomCrosshair = this.enableCustomCrosshair;
            if (this.crosshairPreset != null) ModConfig.crosshairPreset = this.crosshairPreset;
            ModConfig.crosshairSize = Math.max(1, Math.min(30, this.crosshairSize));
            ModConfig.crosshairGap = Math.max(0, Math.min(20, this.crosshairGap));
            ModConfig.crosshairThickness = Math.max(1, Math.min(10, this.crosshairThickness));
            ModConfig.crosshairShowDot = this.crosshairShowDot;
            ModConfig.crosshairDotSize = Math.max(1, Math.min(10, this.crosshairDotSize));
            ModConfig.crosshairColor = this.crosshairColor;
            ModConfig.crosshairRainbow = this.crosshairRainbow;
            ModConfig.crosshairOutline = this.crosshairOutline;
            ModConfig.crosshairOutlineColor = this.crosshairOutlineColor;
            ModConfig.crosshairHighlightEntity = this.crosshairHighlightEntity;
            ModConfig.crosshairHighlightColor = this.crosshairHighlightColor;
            ModConfig.crosshairEnemyCrosshair = this.crosshairEnemyCrosshair;
            if (this.crosshairEnemyMode != null) ModConfig.crosshairEnemyMode = this.crosshairEnemyMode;
            ModConfig.crosshairEnemyColor = this.crosshairEnemyColor;
            ModConfig.crosshairUseCustomScale = this.crosshairUseCustomScale;
            if (this.crosshairScaleMode != null) ModConfig.crosshairScaleMode = this.crosshairScaleMode;
            ModConfig.crosshairScaleFactor = Math.max(0.2f, Math.min(4.0f, this.crosshairScaleFactor));
            ModConfig.crosshairDynamic = this.crosshairDynamic;
            ModConfig.crosshairAttackIndicator = this.crosshairAttackIndicator;
            ModConfig.crosshairThirdPerson = this.crosshairThirdPerson;
            if (this.crosshairGrid != null && this.crosshairGrid.length == 225) {
                ModConfig.crosshairGrid = this.crosshairGrid;
            }

            ModConfig.hudShowBackground = this.hudShowBackground;
            ModConfig.hudBackgroundOpacity = Math.max(0, Math.min(255, this.hudBackgroundOpacity));
            ModConfig.hudTextShadow = this.hudTextShadow;

            ModConfig.fpsTextColor = this.fpsTextColor;
            ModConfig.fpsTextRainbow = this.fpsTextRainbow;
            ModConfig.fpsShowPrefix = this.fpsShowPrefix;
            ModConfig.fpsBackground = this.fpsBackground;

            ModConfig.pingCustomColor = this.pingCustomColor;
            ModConfig.pingTextColor = this.pingTextColor;
            ModConfig.pingTextRainbow = this.pingTextRainbow;
            ModConfig.pingBackground = this.pingBackground;

            ModConfig.cpsTextColor = this.cpsTextColor;
            ModConfig.cpsTextRainbow = this.cpsTextRainbow;
            ModConfig.cpsBackground = this.cpsBackground;

            ModConfig.coordsTextColor = this.coordsTextColor;
            ModConfig.coordsTextRainbow = this.coordsTextRainbow;
            ModConfig.coordsShowNether = this.coordsShowNether;
            ModConfig.coordsShowDirection = this.coordsShowDirection;
            ModConfig.coordsShowBiome = this.coordsShowBiome;
            ModConfig.coordsBackground = this.coordsBackground;

            ModConfig.dayTextColor = this.dayTextColor;
            ModConfig.dayTextRainbow = this.dayTextRainbow;
            ModConfig.dayShowTime = this.dayShowTime;
            ModConfig.dayBackground = this.dayBackground;

            ModConfig.blockInfoTextColor = this.blockInfoTextColor;
            ModConfig.blockInfoTextRainbow = this.blockInfoTextRainbow;
            ModConfig.blockInfoShowTool = this.blockInfoShowTool;
            ModConfig.blockInfoBackground = this.blockInfoBackground;

            ModConfig.keystrokesTextColor = this.keystrokesTextColor;
            ModConfig.keystrokesRainbow = this.keystrokesRainbow;

            ModConfig.showHitColor = this.showHitColor;
            ModConfig.hitColorRed = Math.max(0, Math.min(255, this.hitColorRed));
            ModConfig.hitColorGreen = Math.max(0, Math.min(255, this.hitColorGreen));
            ModConfig.hitColorBlue = Math.max(0, Math.min(255, this.hitColorBlue));
            ModConfig.hitColorAlpha = Math.max(0, Math.min(255, this.hitColorAlpha));
            ModConfig.hitColorRainbow = this.hitColorRainbow;
            ModConfig.hitColorShowOnArmor = this.hitColorShowOnArmor;

            ModConfig.showChatPrefix = this.showChatPrefix;
            ModConfig.showTabListPrefix = this.showTabListPrefix;
            ModConfig.showChatColors = this.showChatColors;
            ModConfig.chatHighlightMentions = this.chatHighlightMentions;
            ModConfig.chatMentionColor = this.chatMentionColor;
            ModConfig.chatMentionSound = this.chatMentionSound;
            ModConfig.chatShowTimestamp = this.chatShowTimestamp;
            ModConfig.customChatBackground = this.customChatBackground;
            ModConfig.chatBackgroundColor = this.chatBackgroundColor;
            ModConfig.chatBackgroundOpacity = Math.max(0, Math.min(255, this.chatBackgroundOpacity));

            ModConfig.showItemTooltips = this.showItemTooltips;
            ModConfig.tooltipShowDurability = this.tooltipShowDurability;
            ModConfig.tooltipShowId = this.tooltipShowId;
            ModConfig.tooltipShowFood = this.tooltipShowFood;
            ModConfig.showItemPhysics = this.showItemPhysics;

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

            ModConfig.showViewModel = this.showViewModel;
            ModConfig.viewModelMainHandScale = Math.max(0.1f, Math.min(3.0f, this.viewModelMainHandScale));
            ModConfig.viewModelOffHandScale = Math.max(0.1f, Math.min(3.0f, this.viewModelOffHandScale));
            ModConfig.viewModelMainHandX = Math.max(-2.0f, Math.min(2.0f, this.viewModelMainHandX));
            ModConfig.viewModelMainHandY = Math.max(-2.0f, Math.min(2.0f, this.viewModelMainHandY));
            ModConfig.viewModelMainHandZ = Math.max(-2.0f, Math.min(2.0f, this.viewModelMainHandZ));
            ModConfig.viewModelOffHandX = Math.max(-2.0f, Math.min(2.0f, this.viewModelOffHandX));
            ModConfig.viewModelOffHandY = Math.max(-2.0f, Math.min(2.0f, this.viewModelOffHandY));
            ModConfig.viewModelOffHandZ = Math.max(-2.0f, Math.min(2.0f, this.viewModelOffHandZ));
            ModConfig.viewModelPitch = Math.max(-180.0f, Math.min(180.0f, this.viewModelPitch));
            ModConfig.viewModelYaw = Math.max(-180.0f, Math.min(180.0f, this.viewModelYaw));
            ModConfig.viewModelRoll = Math.max(-180.0f, Math.min(180.0f, this.viewModelRoll));
            if (this.itemScales != null) {
                ModConfig.itemScales = new HashMap<>(this.itemScales);
            }
            if (this.itemGroundScales != null) {
                ModConfig.itemGroundScales = new HashMap<>(this.itemGroundScales);
            }
            if (this.itemGuiScales != null) {
                ModConfig.itemGuiScales = new HashMap<>(this.itemGuiScales);
            }
        }
    }
}
