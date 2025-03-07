package me.loutreee.statCraft;

public class ScoreResponse {
    private String playerName;
    private int totalScore;

    public ScoreResponse(String playerName, int totalScore) {
        this.playerName = playerName;
        this.totalScore = totalScore;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
