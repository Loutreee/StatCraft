package me.loutreee.statCraft;

public class ScoreHistoryEntry {
    private String playerName;
    private String timestamp;
    private int totalScore;

    public ScoreHistoryEntry(String playerName, String timestamp, int totalScore) {
        this.playerName = playerName;
        this.timestamp = timestamp;
        this.totalScore = totalScore;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
