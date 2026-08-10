package com.velora.client.waypoints;

import java.util.UUID;

public class Waypoint {
    public String id;
    public String name;
    public double x;
    public double y;
    public double z;
    public String dimension; // e.g. "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"
    public String serverOrWorldKey; // e.g. "server:hypixel.net" or "singleplayer:my_world"
    public int color; // ARGB format (e.g. 0xFF38BDF8)
    public boolean enabled;

    public Waypoint() {
        this.id = UUID.randomUUID().toString();
        this.enabled = true;
    }

    public Waypoint(String name, double x, double y, double z, String dimension, String serverOrWorldKey, int color) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.x = Math.round(x * 10.0) / 10.0;
        this.y = Math.round(y * 10.0) / 10.0;
        this.z = Math.round(z * 10.0) / 10.0;
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.serverOrWorldKey = serverOrWorldKey != null ? serverOrWorldKey : "global";
        this.color = color;
        this.enabled = true;
    }
}
