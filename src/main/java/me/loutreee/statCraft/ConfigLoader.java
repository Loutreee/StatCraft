package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigLoader extends JavaPlugin {
    private final Map<Material, Integer> blockScores = new HashMap<>();
    private final Map<EntityType, Integer> mobScores = new HashMap<>();
    private final Map<Material, Integer> craftScores = new HashMap<>();

    public void loadConfig(File configFile, boolean isHardcoreMode) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Définir le chemin racine en fonction du mode
        String mode = isHardcoreMode ? "Hardcore" : "Survival";
        getLogger().info("Chargement de la configuration pour le mode : " + mode);

// Charger les blocs
        if (config.contains(mode + ".blocks")) {
            getLogger().info("Section " + mode + ".blocks trouvée.");
            List<Map<?, ?>> blocksList = config.getMapList(mode + ".blocks");
            for (Map<?, ?> item : blocksList) {
                String name = (String) item.get("name");
                int score = (int) item.get("score");
                blockScores.put(Material.valueOf(name), score);
            }
        } else {
            getLogger().info("Section " + mode + ".blocks est introuvable dans la configuration !");
        }

// Charger les mobs
        if (config.contains(mode + ".mobs")) {
            getLogger().info("Section " + mode + ".mobs trouvée.");
            List<Map<?, ?>> mobsList = config.getMapList(mode + ".mobs");
            for (Map<?, ?> item : mobsList) {
                String name = (String) item.get("name");
                int score = (int) item.get("score");
                mobScores.put(EntityType.valueOf(name), score);
            }
        } else {
            getLogger().info("Section " + mode + ".mobs est introuvable dans la configuration !");
        }

// Charger les crafts
        if (config.contains(mode + ".crafts")) {
            getLogger().info("Section " + mode + ".crafts trouvée.");
            List<Map<?, ?>> craftsList = config.getMapList(mode + ".crafts");
            for (Map<?, ?> item : craftsList) {
                String name = (String) item.get("name");
                int score = (int) item.get("score");
                craftScores.put(Material.valueOf(name), score);
            }
        } else {
            getLogger().info("Section " + mode + ".crafts est introuvable dans la configuration !");
        }

    }

    public int getBlockScore(Material material) {
        return blockScores.getOrDefault(material, 0);
    }

    public int getMobScore(EntityType entityType) {
        return mobScores.getOrDefault(entityType, 0);
    }

    public int getCraftScore(Material material) {
        return craftScores.getOrDefault(material, 0);
    }

}
