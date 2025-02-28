package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class ScoreService {

    private final ConfigLoader configLoader;

    public ScoreService(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    // Score pour un bloc miné
    public int getBlockScore(Material mat) {
        return configLoader.getBlockScore(mat);
    }

    // Score pour un mob tué
    public int getMobScore(EntityType et) {
        return configLoader.getMobScore(et);
    }

    // Score pour un item crafté
    public int getCraftScore(Material mat) {
        return configLoader.getCraftScore(mat);
    }

    // Score pour 1 minute de jeu
    public int getTimeScore() {
        return 1; // 1 point par minute
    }
}
