package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    private Map<Material, Integer> blockScores = new HashMap<>();
    private Map<EntityType, Integer> mobScores = new HashMap<>();
    private Map<Material, Integer> craftScores = new HashMap<>();

    public void loadConfig(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Charger les scores des blocs depuis la configuration
        config.getMapList("blocks").forEach(block -> {
            String name = (String) block.get("name");
            int score = (int) block.get("score");
            blockScores.put(Material.valueOf(name), score);
        });

        // Charger les scores des mobs depuis la configuration
        config.getMapList("mobs").forEach(mob -> {
            String name = (String) mob.get("name");
            int score = (int) mob.get("score");
            mobScores.put(EntityType.valueOf(name), score);
        });

        // Charger les scores des crafts depuis la configuration
        config.getMapList("crafts").forEach(craft -> {
            String name = (String) craft.get("name");
            int score = (int) craft.get("score");
            craftScores.put(Material.valueOf(name), score);
        });
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

    public Map<Material, Integer> getBlockScores() {
        return blockScores;
    }

    public Map<EntityType, Integer> getMobScores() {
        return mobScores;
    }

    public Map<Material, Integer> getCraftScores() {
        return craftScores;
    }
}
