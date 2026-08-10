package com.velora.client.waypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class WaypointManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static File waypointsFile;

    public static void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        waypointsFile = configDir.resolve("velora_waypoints.json").toFile();
        loadWaypoints();
        LOGGER.info("[Velora] Waypoints manager initialized with {} waypoints", waypoints.size());
    }

    public static synchronized void loadWaypoints() {
        if (waypointsFile == null || !waypointsFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(waypointsFile)) {
            Type listType = new TypeToken<ArrayList<Waypoint>>(){}.getType();
            List<Waypoint> loaded = GSON.fromJson(reader, listType);
            if (loaded != null) {
                waypoints.clear();
                for (Waypoint wp : loaded) {
                    sanitizeWaypoint(wp);
                    waypoints.add(wp);
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to load waypoints from file", e);
        }
    }

    private static void sanitizeWaypoint(Waypoint wp) {
        if (wp.id == null || wp.id.isEmpty()) wp.id = UUID.randomUUID().toString();
        if (wp.name == null) wp.name = "Waypoint";
        if (wp.dimension == null || wp.dimension.isEmpty()) wp.dimension = "minecraft:overworld";
        if (wp.serverOrWorldKey == null || wp.serverOrWorldKey.isEmpty()) wp.serverOrWorldKey = "global";
        if (wp.color == 0) wp.color = 0xFF38BDF8;
    }

    public static synchronized void saveWaypoints() {
        if (waypointsFile == null) return;

        try (FileWriter writer = new FileWriter(waypointsFile)) {
            GSON.toJson(waypoints, writer);
        } catch (Exception e) {
            LOGGER.error("[Velora] Failed to save waypoints to file", e);
        }
    }

    public static synchronized List<Waypoint> getAllWaypoints() {
        return new ArrayList<>(waypoints);
    }

    /**
     * Gets all waypoints specifically belonging to the current server or singleplayer world
     */
    public static synchronized List<Waypoint> getWaypointsForCurrentWorld() {
        String worldKey = getCurrentWorldOrServerKey();
        return waypoints.stream()
            .filter(wp -> matchesWorld(wp, worldKey))
            .collect(Collectors.toList());
    }

    /**
     * Gets active, visible waypoints for the current world AND current dimension
     */
    public static synchronized List<Waypoint> getVisibleWaypointsForCurrentDimension() {
        String worldKey = getCurrentWorldOrServerKey();
        String dimension = getCurrentDimensionKey();

        return waypoints.stream()
            .filter(wp -> wp.enabled)
            .filter(wp -> matchesWorld(wp, worldKey))
            .filter(wp -> matchesDimension(wp, dimension))
            .collect(Collectors.toList());
    }

    private static boolean matchesWorld(Waypoint wp, String currentWorldKey) {
        if (wp == null) return false;
        if (wp.serverOrWorldKey == null || wp.serverOrWorldKey.isEmpty()) return true;
        if ("global".equalsIgnoreCase(wp.serverOrWorldKey)) return true;
        return wp.serverOrWorldKey.equalsIgnoreCase(currentWorldKey);
    }

    private static boolean matchesDimension(Waypoint wp, String currentDimension) {
        if (wp == null) return false;
        if (wp.dimension == null || wp.dimension.isEmpty()) return true;
        return wp.dimension.equalsIgnoreCase(currentDimension);
    }

    public static synchronized void addWaypoint(Waypoint wp) {
        if (wp == null) return;
        sanitizeWaypoint(wp);
        waypoints.add(wp);
        saveWaypoints();
    }

    public static synchronized void removeWaypoint(String id) {
        if (id == null) return;
        waypoints.removeIf(w -> Objects.equals(w.id, id));
        saveWaypoints();
    }

    public static synchronized void toggleWaypoint(String id) {
        if (id == null) return;
        for (Waypoint wp : waypoints) {
            if (Objects.equals(wp.id, id)) {
                wp.enabled = !wp.enabled;
                break;
            }
        }
        saveWaypoints();
    }

    public static synchronized void updateWaypoint(Waypoint updated) {
        if (updated == null) return;
        sanitizeWaypoint(updated);
        for (int i = 0; i < waypoints.size(); i++) {
            if (Objects.equals(waypoints.get(i).id, updated.id)) {
                waypoints.set(i, updated);
                break;
            }
        }
        saveWaypoints();
    }

    public static String getCurrentWorldOrServerKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return "global";

        ServerInfo serverEntry = client.getCurrentServerEntry();
        if (serverEntry != null && serverEntry.address != null && !serverEntry.address.isEmpty()) {
            return "server:" + serverEntry.address.toLowerCase().trim();
        }

        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            String levelName = client.getServer().getSaveProperties().getLevelName();
            return "singleplayer:" + (levelName != null ? levelName.trim() : "world");
        }

        if (client.world != null) {
            return "singleplayer:world";
        }

        return "global";
    }

    public static String getCurrentDimensionKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            return client.world.getRegistryKey().getValue().toString();
        }
        return "minecraft:overworld";
    }

    public static String getHumanReadableDimension(String dimKey) {
        if (dimKey == null) return "Overworld";
        if (dimKey.contains("the_nether") || dimKey.contains("nether")) return "The Nether";
        if (dimKey.contains("the_end") || dimKey.contains("end")) return "The End";
        return "Overworld";
    }
}
