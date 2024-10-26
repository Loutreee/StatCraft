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

    // Calcul du score global pour un joueur
    public int calculatePlayerScore(Player player, int playTimeDiff) {
        int score = 0;
        int blockScore = 0;
        int mobScore = 0;
        int craftScore = 0;

        // Parcourir tous les types de blocs minés par le joueur
        for (Material material : Material.values()) {
            int blocksMined = player.getStatistic(Statistic.MINE_BLOCK, material);
            // Utiliser le score du configLoader ou 1 par défaut si non spécifié
            int materialScore = configLoader.getBlockScore(material);
            blockScore += blocksMined * (materialScore != 0 ? materialScore : 1);
        }

        // Calcul du score pour chaque monstre tué par le joueur
        for (EntityType entityType : EntityType.values()) {
            if (entityType != EntityType.UNKNOWN) {
                try {
                    int mobsKilled = player.getStatistic(Statistic.KILL_ENTITY, entityType);
                    // Utiliser le score du configLoader ou 1 par défaut si non spécifié
                    int mobScoreValue = configLoader.getMobScore(entityType);
                    mobScore += mobsKilled * (mobScoreValue != 0 ? mobScoreValue : 1);
                } catch (IllegalArgumentException e) {
                    // Ignore les EntityType sans statistique associée
                }
            }
        }

        // Calcul du score pour les objets craftés par le joueur
        for (Material material : Material.values()) {
            int itemsCrafted = player.getStatistic(Statistic.CRAFT_ITEM, material);
            // Utiliser le score du configLoader ou 1 par défaut si non spécifié
            int craftScoreValue = configLoader.getCraftScore(material);
            craftScore += itemsCrafted * (craftScoreValue != 0 ? craftScoreValue : 1);
        }

        // Ajouter le score de temps de jeu et combiner tous les scores
        score += playTimeDiff + blockScore + mobScore + craftScore;

        return score;
    }
}
