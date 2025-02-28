package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {

    private final String playerName;

    private final Map<String, Integer> blocksMined = new HashMap<>();
    private final Map<String, Integer> itemsCrafted = new HashMap<>();
    private final Map<String, Integer> mobsKilled = new HashMap<>();

    // Sous-scores
    private int blockScore = 0;
    private int craftScore = 0;
    private int mobScore = 0;
    private int timeScore = 0;

    // Score total
    private int totalScore = 0;

    // Pour gérer le temps de jeu (on stocke la dernière valeur connue)
    private int lastPlayTime = 0;

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

    // Sous-scores : getters
    public int getBlockScore() { return blockScore; }
    public int getCraftScore() { return craftScore; }
    public int getMobScore()   { return mobScore; }
    public int getTimeScore()  { return timeScore; }

    public int getTotalScore() { return totalScore; }

    public int getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(int lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    // --- Incréments des compteurs en mémoire (pour blocs, items, mobs) ---
    public void incrementBlock(Material blockType) {
        blocksMined.merge(blockType.toString(), 1, Integer::sum);
    }

    public void incrementItem(Material itemType) {
        itemsCrafted.merge(itemType.toString(), 1, Integer::sum);
    }

    public void incrementMob(EntityType mobType) {
        mobsKilled.merge(mobType.toString(), 1, Integer::sum);
    }

    // --- Incréments des sous-scores (chaque action) ---
    public void addBlockScore(int amount) {
        blockScore += amount;
        totalScore += amount;
    }

    public void addCraftScore(int amount) {
        craftScore += amount;
        totalScore += amount;
    }

    public void addMobScore(int amount) {
        mobScore += amount;
        totalScore += amount;
    }

    public void addTimeScore(int amount) {
        timeScore += amount;
        totalScore += amount;
    }

    // --- Méthodes "set" pour restaurer à partir d'un snapshot ---

    public void setBlocksMined(Map<String, Integer> blocks) {
        blocksMined.clear();
        if (blocks != null) {
            blocksMined.putAll(blocks);
        }
    }

    public void setItemsCrafted(Map<String, Integer> items) {
        itemsCrafted.clear();
        if (items != null) {
            itemsCrafted.putAll(items);
        }
    }

    public void setMobsKilled(Map<String, Integer> mobs) {
        mobsKilled.clear();
        if (mobs != null) {
            mobsKilled.putAll(mobs);
        }
    }

    public void setBlockScore(int blockScore) {
        this.blockScore = blockScore;
    }

    public void setCraftScore(int craftScore) {
        this.craftScore = craftScore;
    }

    public void setMobScore(int mobScore) {
        this.mobScore = mobScore;
    }

    public void setTimeScore(int timeScore) {
        this.timeScore = timeScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
