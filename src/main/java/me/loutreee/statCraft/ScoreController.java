package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScoreController {

    public ScoreController(Javalin app) {
        registerEndpoints(app);
    }

    private void registerEndpoints(Javalin app) {
        // Route pour obtenir le score total d'un joueur
        app.get("/api/score/{playerName}", this::getPlayerTotalScore);

        // Nouvelle route pour obtenir tous les score totaux avec timestamp (historique)
        app.get("/api/scores", this::getAllScoreTotals);

        app.get("/api/score/history/{playerName}", this::getScoreHistoryForPlayer);
    }

    private void getScoreHistoryForPlayer(Context ctx) {
        String playerName = ctx.pathParam("playerName");

        // Récupère tous les snapshots pour le joueur
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find(FluentFilter.where("playerName").eq(playerName))
                .toList();

        if (snapshots.isEmpty()) {
            ctx.status(404).result("Aucun snapshot trouvé pour le joueur " + playerName);
            return;
        }

        // Mappe chaque snapshot à un ScoreHistoryEntry (contenant playerName, timestamp et totalScore)
        List<ScoreHistoryEntry> entries = snapshots.stream()
                .map(doc -> {
                    String timestamp = doc.get("timestamp", String.class);
                    Integer totalScore = doc.get("totalScore", Integer.class);
                    return new ScoreHistoryEntry(playerName, timestamp, totalScore != null ? totalScore : 0);
                })
                .sorted(Comparator.comparing(ScoreHistoryEntry::getTimestamp))
                .collect(Collectors.toList());

        ctx.json(entries);
    }


    private void getPlayerTotalScore(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot == null || !snapshot.containsKey("totalScore")) {
            ctx.status(404).result("Snapshot not found for player " + playerName);
            return;
        }
        int totalScore = snapshot.get("totalScore", Integer.class);
        ScoreResponse response = new ScoreResponse(playerName, totalScore);
        ctx.json(response);
    }

    /**
     * Retourne une liste d'entrées avec le nom du joueur, le timestamp et le score total
     */
    private void getAllScoreTotals(Context ctx) {
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find()
                .toList();

        // Pour chaque document, on crée un objet ScoreHistoryEntry
        List<ScoreHistoryEntry> entries = snapshots.stream()
                .map(doc -> {
                    String playerName = doc.get("playerName", String.class);
                    String timestamp = doc.get("timestamp", String.class);
                    Integer totalScore = doc.get("totalScore", Integer.class);
                    return new ScoreHistoryEntry(playerName, timestamp, totalScore != null ? totalScore : 0);
                })
                .collect(Collectors.toList());

        // Optionnel : trier par timestamp si besoin
        entries.sort(Comparator.comparing(ScoreHistoryEntry::getTimestamp));
        ctx.json(entries);
    }

    private Document getLatestSnapshotForPlayer(String playerName) {
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find(org.dizitart.no2.filters.FluentFilter.where("playerName").eq(playerName))
                .toList();

        if (snapshots.isEmpty()) {
            return null;
        }
        // Tri des snapshots par timestamp décroissant (format ISO)
        Optional<Document> latest = snapshots.stream()
                .max(Comparator.comparing(doc -> doc.get("timestamp", String.class)));
        return latest.orElse(null);
    }
}
