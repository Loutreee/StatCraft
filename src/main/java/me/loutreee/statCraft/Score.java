package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.Map;

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

        // Ajouter le score basé sur le temps de jeu
        score += playTimeDiff;

        // Ajouter le score pour les blocs cassés
        for (Map.Entry<Material, Integer> entry : configLoader.getBlockScores().entrySet()) {
            Material block = entry.getKey();
            int multiplier = entry.getValue();
            int count = player.getStatistic(Statistic.MINE_BLOCK, block);
            score += count * multiplier;
        }

        // Ajouter le score pour les mobs tués
        for (Map.Entry<EntityType, Integer> entry : configLoader.getMobScores().entrySet()) {
            EntityType mob = entry.getKey();
            int multiplier = entry.getValue();
            int count = player.getStatistic(Statistic.KILL_ENTITY, mob);
            score += count * multiplier;
        }

        // Ajouter le score pour les objets craftés
        for (Map.Entry<Material, Integer> entry : configLoader.getCraftScores().entrySet()) {
            Material item = entry.getKey();
            int multiplier = entry.getValue();
            int count = player.getStatistic(Statistic.CRAFT_ITEM, item);
            score += count * multiplier;
        }

        return score;
    }
}
