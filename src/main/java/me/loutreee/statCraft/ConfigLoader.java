package me.loutreee.statCraft;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    private final JavaPlugin plugin;
    private final Map<Material, Integer> blockScores = new HashMap<>();
    private final Map<Material, Integer> craftScores = new HashMap<>();
    private final Map<EntityType, Integer> mobScores = new HashMap<>();
    private int webPort = 27800; // valeur par défaut

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig(); // crée le config.yml si inexistant
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        // Récupère le port
        webPort = config.getInt("Web.port", 27800);

        // Lecture des blocks
        ConfigurationSection survivalSection = config.getConfigurationSection("Survival");
        if (survivalSection != null) {
            // Blocks
            List<Map<?, ?>> blocksList = survivalSection.getMapList("blocks");
            for (Map<?, ?> entry : blocksList) {
                String name = (String) entry.get("name");
                int score = (int) entry.get("score");
                Material mat = Material.matchMaterial(name);
                if (mat != null) {
                    blockScores.put(mat, score);
                }
            }

            // Mobs
            List<Map<?, ?>> mobsList = survivalSection.getMapList("mobs");
            for (Map<?, ?> entry : mobsList) {
                String name = (String) entry.get("name");
                int score = (int) entry.get("score");
                try {
                    EntityType et = EntityType.valueOf(name);
                    mobScores.put(et, score);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Mob inconnu dans config: " + name);
                }
            }

            // Crafts
            List<Map<?, ?>> craftsList = survivalSection.getMapList("crafts");
            for (Map<?, ?> entry : craftsList) {
                String name = (String) entry.get("name");
                int score = (int) entry.get("score");
                Material mat = Material.matchMaterial(name);
                if (mat != null) {
                    craftScores.put(mat, score);
                }
            }
        }
    }

    public int getWebPort() {
        return webPort;
    }

    public int getBlockScore(Material mat) {
        return blockScores.getOrDefault(mat, 0);
    }

    public int getMobScore(EntityType et) {
        return mobScores.getOrDefault(et, 0);
    }

    public int getCraftScore(Material mat) {
        return craftScores.getOrDefault(mat, 0);
    }
}
