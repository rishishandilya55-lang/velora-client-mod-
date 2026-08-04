package com.velora.client.client;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class RankSystem {

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

    private static final Map<String, Rank> RANK_MAP = new HashMap<>();

    static {
        RANK_MAP.put("Zouhmi", Rank.DEVELOPER);
        RANK_MAP.put("zouhmi", Rank.DEVELOPER);
        RANK_MAP.put("ZOUHMI", Rank.DEVELOPER);
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
