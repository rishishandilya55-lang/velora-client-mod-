package com.example.fpsdisplay.gui.cosmetic;

import com.example.fpsdisplay.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

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

    private static final ConcurrentHashMap<String, Identifier> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier, Boolean> VALIDITY_CACHE = new ConcurrentHashMap<>();
    private static final List<CosmeticItem> REGISTRY = new ArrayList<>();

    // Verified Local Test Textures (assets/fpsdisplay/textures/cape/...)
    public static final Identifier VELORA_CAPE = Identifier.of("fpsdisplay", "textures/cape/velora_cape.png");
    public static final Identifier MOJANG_CAPE = Identifier.of("fpsdisplay", "textures/cape/classic_cape.png");
    public static final Identifier WAVE_CAPE = Identifier.of("fpsdisplay", "textures/cape/wave_cape.png");
    public static final Identifier ROSE_CAPE = Identifier.of("fpsdisplay", "textures/cape/rose.png");
    public static final Identifier PURPLE_ROSE_CAPE = Identifier.of("fpsdisplay", "textures/cape/purple_rose.png");
    public static final Identifier WITHERED_ROSE_CAPE = Identifier.of("fpsdisplay", "textures/cape/withered_rose.png");

    public static synchronized void init() {
        // Always reset registry to ensure zero stale or random item contamination
        REGISTRY.clear();
        TEXTURE_CACHE.clear();

        // 1. "Velora Cape" -> Local texture: assets/fpsdisplay/textures/cape/velora_cape.png
        boolean veloraFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 0 && ModConfig.favoriteCosmetics[0];
        register(new CosmeticItem("velora_cape", "Velora Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, VELORA_CAPE, veloraFav));

        // 2. "Mojang Cape" -> Baseline fallback texture: assets/fpsdisplay/textures/cape/classic_cape.png
        boolean mojangFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 1 && ModConfig.favoriteCosmetics[1];
        register(new CosmeticItem("mojang_cape", "Mojang Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, MOJANG_CAPE, mojangFav));

        // 3. "Wave Cape"
        boolean waveFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 2 && ModConfig.favoriteCosmetics[2];
        register(new CosmeticItem("wave_cape", "Wave Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, WAVE_CAPE, waveFav));

        // 4. "Rose Cape"
        boolean roseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 3 && ModConfig.favoriteCosmetics[3];
        register(new CosmeticItem("rose_cape", "Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, ROSE_CAPE, roseFav));

        // 5. "Purple Rose Cape"
        boolean purpleRoseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 4 && ModConfig.favoriteCosmetics[4];
        register(new CosmeticItem("purple_rose_cape", "Purple Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, PURPLE_ROSE_CAPE, purpleRoseFav));

        // 6. "Withered Rose Cape"
        boolean witheredRoseFav = ModConfig.favoriteCosmetics != null && ModConfig.favoriteCosmetics.length > 5 && ModConfig.favoriteCosmetics[5];
        register(new CosmeticItem("withered_rose_cape", "Withered Rose Cape", CosmeticItem.Category.CAPE, CosmeticItem.CosmeticType.CAPE, WITHERED_ROSE_CAPE, witheredRoseFav));

        // Pre-warm texture allocations and validate resources
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getResourceManager() != null) {
            for (CosmeticItem item : REGISTRY) {
                TEXTURE_CACHE.put(item.getId(), item.getTexture());
                boolean exists = mc.getResourceManager().getResource(item.getTexture()).isPresent();
                VALIDITY_CACHE.put(item.getTexture(), exists);
                if (exists && mc.getTextureManager() != null) {
                    mc.getTextureManager().getTexture(item.getTexture());
                }
            }
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
