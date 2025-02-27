package me.loutreee.statCraft;

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

    public void incrementItemCrafted(Player player, org.bukkit.Material itemType) {
        PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerStats(player.getName()));
        ps.incrementItem(itemType);
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
                // Ici, on peut trier par timestamp décroissant (si vous stockez un format comparable)
                .toList();
        if (!snapshots.isEmpty()) {
            // Supposons que le premier document est le plus récent
            Document lastSnapshot = snapshots.getFirst();
            // Récupération des maps stockées dans le snapshot
            Map<String, Integer> blocks = lastSnapshot.get("blocksMined", Map.class);
            Map<String, Integer> items = lastSnapshot.get("itemsCrafted", Map.class);
            Map<String, Integer> mobs = lastSnapshot.get("mobsKilled", Map.class);
            // Récupération du PlayerStats ou création s'il n'existe pas
            PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats(player.getName()));
            ps.setBlocksMined(blocks);
            ps.setItemsCrafted(items);
            ps.setMobsKilled(mobs);
            // Log d'initialisation
            System.out.println("Initialisation des stats pour " + playerName + " à partir du dernier snapshot.");
        }
    }
}
