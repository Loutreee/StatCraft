package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;

import java.util.*;

public class StatsManager extends JavaPlugin {

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

        List<Document> snapshots = new ArrayList<>(NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find(FluentFilter.where("playerName").eq(playerName))
                .toList());

        if (!snapshots.isEmpty()) {
            snapshots.sort((d1, d2) -> d2.get("timestamp", String.class)
                    .compareTo(d1.get("timestamp", String.class)));

            Document lastSnapshot = snapshots.get(0);

            @SuppressWarnings("unchecked")
            Map<String, Integer> blocks = (Map<String, Integer>) lastSnapshot.get("blocksMined", Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Integer> items = (Map<String, Integer>) lastSnapshot.get("itemsCrafted", Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Integer> mobs = (Map<String, Integer>) lastSnapshot.get("mobsKilled", Map.class);

            if (blocks == null) blocks = new HashMap<>();
            if (items == null) items = new HashMap<>();
            if (mobs == null) mobs = new HashMap<>();

            // Récupération du PlayerStats
            PlayerStats ps = statsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats(playerName));
            ps.setBlocksMined(blocks);
            ps.setItemsCrafted(items);
            ps.setMobsKilled(mobs);

        } else {
            getLogger().info("Aucun snapshot trouvé pour " + playerName + ". Stats non restaurées.");
        }
    }

}



