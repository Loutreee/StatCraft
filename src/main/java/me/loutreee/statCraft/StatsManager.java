package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class StatsManager {

    // Map: UUID du joueur -> PlayerStats
    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();

    public void incrementBlockMined(Player player, org.bukkit.Material blockType) {
        PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerStats(player.getName()));
        ps.incrementBlock(blockType);
    }

    public void incrementItemCrafted(Player player, Material itemType, int amount) {
        PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerStats(player.getName()));
        // Incrémente l'item de la quantité 'amount'
        ps.getItemsCrafted().merge(itemType.toString(), amount, Integer::sum);
    }


    public void incrementMobKilled(Player player, org.bukkit.entity.EntityType mobType) {
        PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerStats(player.getName()));
        ps.incrementMob(mobType);
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        return statsMap.get(uuid);
    }

    public Collection<PlayerStats> getAllStats() {
        return statsMap.values();
    }

    /**
     * Initialise les compteurs d'un joueur à partir du dernier snapshot en base.
     */
    public void initializePlayerStats(Player player) {
        String playerName = player.getName();
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find(FluentFilter.where("playerName").eq(playerName))
                .toList();

        if (!snapshots.isEmpty()) {
            // Tri des snapshots par timestamp décroissant (supposant que le champ "timestamp" est une chaîne ISO)
            snapshots.sort((d1, d2) -> d2.get("timestamp", String.class)
                    .compareTo(d1.get("timestamp", String.class)));

            // Récupère le snapshot le plus récent (index 0)
            Document lastSnapshot = snapshots.get(0);

            // Récupération des maps stockées dans le snapshot
            Map<String, Integer> blocks = lastSnapshot.get("blocksMined", Map.class);
            Map<String, Integer> items = lastSnapshot.get("itemsCrafted", Map.class);
            Map<String, Integer> mobs  = lastSnapshot.get("mobsKilled", Map.class);

            // Récupération des sous-scores
            Integer blockScore = lastSnapshot.get("blockScore", Integer.class);
            Integer craftScore = lastSnapshot.get("craftScore", Integer.class);
            Integer mobScore   = lastSnapshot.get("mobScore", Integer.class);
            Integer timeScore  = lastSnapshot.get("timeScore", Integer.class);
            Integer totalScore = lastSnapshot.get("totalScore", Integer.class);

            // Récupération du PlayerStats ou création s'il n'existe pas
            PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(),
                    uuid -> new PlayerStats(player.getName()));
            ps.setBlocksMined(blocks);
            ps.setItemsCrafted(items);
            ps.setMobsKilled(mobs);

            ps.setBlockScore(blockScore != null ? blockScore : 0);
            ps.setCraftScore(craftScore != null ? craftScore : 0);
            ps.setMobScore(mobScore != null ? mobScore : 0);
            ps.setTimeScore(timeScore != null ? timeScore : 0);
            ps.setTotalScore(totalScore != null ? totalScore : 0);

            System.out.println("Initialisation des stats pour " + playerName + " à partir du dernier snapshot.");
        }
    }

}

