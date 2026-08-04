package com.velora.client.gui.cosmetic;

import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance Caching & Cosmetic Registry System for Velora Client.
 * Local testing phase setup: Hardcodes EXACTLY 2 selectable cape items
 * for local testing: Velora Cape and Mojang Cape. Removes all random cosmetics.
 */
public class CosmeticTextureCache {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    private static final ConcurrentHashMap<String, Identifier> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier, Boolean> VALIDITY_CACHE = new ConcurrentHashMap<>();
    private static final List<CosmeticItem> REGISTRY = new ArrayList<>();

    // Verified Local Test Textures (assets/velora/textures/cape/...)
    public static final Identifier VELORA_CAPE = Identifier.of("velora", "textures/cape/velora_cape.png");
    public static final Identifier MOJANG_CAPE = Identifier.of("velora", "textures/cape/classic_cape.png");
    public static final Identifier WAVE_CAPE = Identifier.of("velora", "textures/cape/wave_cape.png");
    public static final Identifier ROSE_CAPE = Identifier.of("velora", "textures/cape/rose.png");
    public static final Identifier PURPLE_ROSE_CAPE = Identifier.of("velora", "textures/cape/purple_rose.png");
    public static final Identifier WITHERED_ROSE_CAPE = Identifier.of("velora", "textures/cape/withered_rose.png");

    public static synchronized void init() {
        // Always reset registry to ensure zero stale or random item contamination
        REGISTRY.clear();
        TEXTURE_CACHE.clear();
        LOGGER.debug("[Velora] Cosmetic registry cleared, rebuilding...");

        // 1. "Velora Cape" -> Local texture: assets/velora/textures/cape/velora_cape.png
        boolean veloraFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 0 && ModConfig.favoriteCosmetics[0];
        register(new CosmeticItem("velora_cape", "Velora Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, VELORA_CAPE, veloraFav));
        LOGGER.debug("[Velora] Registered cosmetic: Velora Cape (favorite={})", veloraFav);

        // 2. "Mojang Cape" -> Baseline fallback texture: assets/velora/textures/cape/classic_cape.png
        boolean mojangFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 1 && ModConfig.favoriteCosmetics[1];
        register(new CosmeticItem("mojang_cape", "Mojang Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, MOJANG_CAPE, mojangFav));
        LOGGER.debug("[Velora] Registered cosmetic: Mojang Cape (favorite={})", mojangFav);

        // 3. "Wave Cape"
        boolean waveFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 2 && ModConfig.favoriteCosmetics[2];
        register(new CosmeticItem("wave_cape", "Wave Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, WAVE_CAPE, waveFav));
        LOGGER.debug("[Velora] Registered cosmetic: Wave Cape (favorite={})", waveFav);

        // 4. "Rose Cape"
        boolean roseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 3 && ModConfig.favoriteCosmetics[3];
        register(new CosmeticItem("rose_cape", "Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, ROSE_CAPE, roseFav));
        LOGGER.debug("[Velora] Registered cosmetic: Rose Cape (favorite={})", roseFav);

        // 5. "Purple Rose Cape"
        boolean purpleRoseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 4 && ModConfig.favoriteCosmetics[4];
        register(new CosmeticItem("purple_rose_cape", "Purple Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, PURPLE_ROSE_CAPE, purpleRoseFav));
        LOGGER.debug("[Velora] Registered cosmetic: Purple Rose Cape (favorite={})", purpleRoseFav);

        // 6. "Withered Rose Cape"
        boolean witheredRoseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 5 && ModConfig.favoriteCosmetics[5];
        register(new CosmeticItem("withered_rose_cape", "Withered Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, WITHERED_ROSE_CAPE, witheredRoseFav));
        LOGGER.debug("[Velora] Registered cosmetic: Withered Rose Cape (favorite={})", witheredRoseFav);

        LOGGER.info("[Velora] {} cosmetics registered", REGISTRY.size());

        // Pre-warm texture allocations and validate resources
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getResourceManager() != null) {
            int validCount = 0;
            for (CosmeticItem item : REGISTRY) {
                TEXTURE_CACHE.put(item.getId(), item.getTexture());
                boolean exists = mc.getResourceManager().getResource(item.getTexture()).isPresent();
                VALIDITY_CACHE.put(item.getTexture(), exists);
                LOGGER.debug("[Velora] Texture validation: {} -> {} ({})", item.getName(), item.getTexture(), exists ? "VALID" : "MISSING");
                if (exists) validCount++;
                if (exists && mc.getTextureManager() != null) {
                    mc.getTextureManager().getTexture(item.getTexture());
                }
            }
            LOGGER.info("[Velora] Pre-warming complete: {}/{} textures valid", validCount, REGISTRY.size());
        } else {
            LOGGER.warn("[Velora] Cannot pre-warm textures: MinecraftClient or ResourceManager is null");
        }
    }

    private static void register(CosmeticItem item) {
        REGISTRY.add(item);
    }

    public static List<CosmeticItem> getItems() {
        if (REGISTRY.isEmpty()) init();
        return Collections.unmodifiableList(REGISTRY);
    }

    public static boolean isTextureValid(Identifier id) {
        if (id == null) return false;
        return VALIDITY_CACHE.computeIfAbsent(id, identifier -> {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null && mc.getResourceManager() != null) {
                    return mc.getResourceManager().getResource(identifier).isPresent();
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        });
    }

    public static Identifier getValidTextureOrFallback(Identifier texture) {
        if (isTextureValid(texture)) {
            return texture;
        }
        return VELORA_CAPE;
    }
}
