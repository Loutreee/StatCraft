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

        // Récupération des scores individuels
        int blockScore = configLoader.getBlockScore(Material.STONE); // Exemple, ajoutez la logique pour tous les blocs
        int mobScore = configLoader.getMobScore(EntityType.SHEEP); // Exemple, idem pour les mobs
        int craftScore = configLoader.getCraftScore(Material.STONE_SWORD); // Exemple, idem pour les crafts

        // Ajouter tous les scores
        score += playTimeDiff;
        score += blockScore;
        score += mobScore;
        score += craftScore;

        return score;
    }

}
