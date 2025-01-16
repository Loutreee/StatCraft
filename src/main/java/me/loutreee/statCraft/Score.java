package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Score {
    private final ConfigLoader configLoader;

    public Score(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    // Calcul du score global pour un joueur
    public int calculatePlayerScore(Player player, int playTimeDiff) {
        int score = 0;
        int blockScore = 0;
        int mobScore = 0;
        int craftScore = 0;


        // Ajouter le score de temps de jeu et combiner tous les scores
        score += playTimeDiff + blockScore + mobScore + craftScore;

        return score;
    }
}
