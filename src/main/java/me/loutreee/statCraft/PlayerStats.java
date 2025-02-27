package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Contient les stats d'un joueur (blocs minés, items craftés, mobs tués).
 */
public class PlayerStats {

    private final String playerName;
    private final Map<String, Integer> blocksMined = new HashMap<>();
    private final Map<String, Integer> itemsCrafted = new HashMap<>();
    private final Map<String, Integer> mobsKilled = new HashMap<>();

    public PlayerStats(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Map<String, Integer> getBlocksMined() {
        return blocksMined;
    }

    public Map<String, Integer> getItemsCrafted() {
        return itemsCrafted;
    }

    public Map<String, Integer> getMobsKilled() {
        return mobsKilled;
    }

    // Incrémente les compteurs lors des événements
    public void incrementBlock(Material blockType) {
        blocksMined.merge(blockType.toString(), 1, Integer::sum);
    }

    public void incrementItem(Material itemType) {
        itemsCrafted.merge(itemType.toString(), 1, Integer::sum);
    }

    public void incrementMob(EntityType mobType) {
        mobsKilled.merge(mobType.toString(), 1, Integer::sum);
    }

    // Méthodes pour initialiser les compteurs à partir d'une Map
    public void setBlocksMined(Map<String, Integer> blocks) {
        blocksMined.clear();
        if(blocks != null) {
            blocksMined.putAll(blocks);
        }
    }

    public void setItemsCrafted(Map<String, Integer> items) {
        itemsCrafted.clear();
        if(items != null) {
            itemsCrafted.putAll(items);
        }
    }

    public void setMobsKilled(Map<String, Integer> mobs) {
        mobsKilled.clear();
        if(mobs != null) {
            mobsKilled.putAll(mobs);
        }
    }
}
