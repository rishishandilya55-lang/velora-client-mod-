package com.velora.client.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RankSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Rank> RANK_MAP = new HashMap<>();

    public enum Rank {
        DEVELOPER("Developer", 0xFFA78BFA, "developer.png"),
        ADMIN("Admin", 0xFFEF4444, "developer.png"),
        MODERATOR("Moderator", 0xFFFFD700, "developer.png"),
        DONATOR("Donator", 0xFF34D399, "donator.png"),
        DEFAULT(null, 0xFFA1A1AA, null);

        private final String displayName;
        private final int color;
        private final String badgeFile;

        Rank(String displayName, int color, String badgeFile) {
            this.displayName = displayName;
            this.color = color;
            this.badgeFile = badgeFile;
        }

        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
        public String getBadgeFile() { return badgeFile; }
    }

    static {
        loadRanks();
    }

    public static void loadRanks() {
        RANK_MAP.clear();
        RANK_MAP.put("Zouhmi", Rank.DEVELOPER);
        RANK_MAP.put("zouhmi", Rank.DEVELOPER);
        RANK_MAP.put("ZOUHMI", Rank.DEVELOPER);

        try {
            File file = getRankFile();
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> savedRanks = GSON.fromJson(reader, mapType);
                    if (savedRanks != null) {
                        for (Map.Entry<String, String> entry : savedRanks.entrySet()) {
                            try {
                                Rank rank = Rank.valueOf(entry.getValue().toUpperCase());
                                RANK_MAP.put(entry.getKey(), rank);
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn("[Velora] Unknown rank '{}' for player '{}'", entry.getValue(), entry.getKey());
                            }
                        }
                    }
                }
                LOGGER.info("[Velora] Ranks loaded from config");
            }
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to load ranks", e);
        }
    }

    public static void saveRanks() {
        try {
            Map<String, String> serializable = new HashMap<>();
            for (Map.Entry<String, Rank> entry : RANK_MAP.entrySet()) {
                if (entry.getValue() != Rank.DEFAULT) {
                    serializable.put(entry.getKey(), entry.getValue().name());
                }
            }
            File file = getRankFile();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(serializable, writer);
            }
            LOGGER.info("[Velora] Ranks saved to config");
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to save ranks", e);
        }
    }

    public static void setRank(String username, Rank rank) {
        RANK_MAP.put(username, rank);
        saveRanks();
    }

    public static void removeRank(String username) {
        RANK_MAP.remove(username);
        saveRanks();
    }

    private static File getRankFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("velora_ranks.json").toFile();
    }

    public static Rank getRankForPlayer(String username) {
        if (username == null) return Rank.DEFAULT;
        return RANK_MAP.getOrDefault(username, Rank.DEFAULT);
    }

    public static Identifier getBadgeTexture(Rank rank) {
        if (rank == null || rank.getBadgeFile() == null) return null;
        return Identifier.of("velora", "textures/badge/" + rank.getBadgeFile());
    }

    public static Identifier getBadgeTextureForPlayer(String username) {
        return getBadgeTexture(getRankForPlayer(username));
    }
}
