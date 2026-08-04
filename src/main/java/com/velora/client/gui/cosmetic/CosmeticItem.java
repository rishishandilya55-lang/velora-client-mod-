package com.velora.client.gui.cosmetic;

import net.minecraft.util.Identifier;

/**
 * Model class representing a cosmetic item in Velora Client.
 * Designed for API serialization readiness (can be loaded locally or dynamically fetched via JSON API).
 */
public class CosmeticItem {

    public enum Category {
        ALL("All", "⊞", null),
        FAVORITES("Favorites", "★", 0xFFFFD700),
        CAPE("Cape", "🎽", 0xFF22C55E),
        HAT("Hat", "👑", 0xFF22C55E),
        FACE("Face", "🕶", 0xFF22C55E),
        WINGS("Wings", "🪽", 0xFF22C55E),
        AURA("Aura", "✨", 0xFF22C55E);

        private final String displayName;
        private final String icon;
        private final Integer dotColor;

        Category(String displayName, String icon, Integer dotColor) {
            this.displayName = displayName;
            this.icon = icon;
            this.dotColor = dotColor;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public Integer getDotColor() { return dotColor; }
    }

    public enum CosmeticType {
        CAPE,
        HAT,
        FACE,
        WINGS,
        AURA
    }

    private final String id;
    private final String name;
    private final Category category;
    private final CosmeticType type;
    private final Identifier texture;
    private boolean isFavorite;

    public CosmeticItem(String id, String name, Category category, CosmeticType type, Identifier texture, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.texture = texture;
        this.isFavorite = isFavorite;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public CosmeticType getType() { return type; }
    public Identifier getTexture() { return texture; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
