package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;

import java.util.*;
import java.util.stream.Collectors;

public class TopPlayersController {

    public TopPlayersController(Javalin app) {
        registerEndpoints(app);
    }

    private void registerEndpoints(Javalin app) {
        app.get("/api/top-players/dirtiest", this::getDirtiestPlayer);
        app.get("/api/top-players/highest-score", this::getHighestScorePlayer);
        app.get("/api/top-players/adventurer", this::getAdventurerPlayer);
        app.get("/api/top-players/serial-killer", this::getSerialKillerPlayer);
        app.get("/api/top-players/gold-digger", this::getGoldDiggerPlayer);
        app.get("/api/top-players/enlighter", this::getEnlighterPlayer);
    }

    /**
     * Exemple 1 : "Dirtiest player"
     * On cherche qui a miné le plus de DIRT dans son dernier snapshot.
     */
    private void getDirtiestPlayer(Context ctx) {
        // blocksMined["DIRT"] -> max
        Optional<TopResult> result = findTopByBlock("DIRT");
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    /**
     * Exemple 2 : "Highest score"
     * On regarde totalScore dans le dernier snapshot
     */
    private void getHighestScorePlayer(Context ctx) {
        Optional<TopResult> result = findTopByField("totalScore");
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    /**
     * Exemple 3 : "The adventurer"
     * Suppose qu'on stocke la distance parcourue dans un champ "distanceTraveled" (fictif).
     */
    private void getAdventurerPlayer(Context ctx) {
        Optional<TopResult> result = findTopByField("distanceTraveled");
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    /**
     * Exemple 4 : "Serial Killer"
     * Suppose qu'on stocke le nombre de joueurs tués dans un champ "playerKills" (fictif),
     * ou qu'on l'ait accumulé dans mobsKilled["PLAYER"] (si c'est géré comme un mob).
     */
    private void getSerialKillerPlayer(Context ctx) {
        // Exemple : findTopByMob("PLAYER") si vous l'avez stocké comme un mob
        // ou findTopByField("playerKills") si c'est un champ direct
        Optional<TopResult> result = findTopByMob("PLAYER"); // par ex.
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    /**
     * Exemple 5 : "Gold digger"
     * On cherche blocksMined["GOLD_ORE"] (ou "DEEPSLATE_GOLD_ORE" si vous voulez combiner).
     */
    private void getGoldDiggerPlayer(Context ctx) {
        Optional<TopResult> result = findTopByBlock("GOLD_ORE");
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    /**
     * Exemple 6 : "Enlighter"
     * Suppose qu'on cherche blocksMined["TORCH"] ou placed["TORCH"] si vous stockez
     * les torches placées. Ici, on fait un exemple 'blocksMined["TORCH"]' fictif.
     */
    private void getEnlighterPlayer(Context ctx) {
        Optional<TopResult> result = findTopByBlock("TORCH");
        if (result.isPresent()) {
            ctx.json(Map.of(
                    "playerName", result.get().playerName(),
                    "value", result.get().value()
            ));
        } else {
            ctx.status(404).result("No data found");
        }
    }

    // --------------------- Méthodes d'aide ---------------------

    /**
     * Récupère pour chaque joueur son dernier snapshot, puis
     * lit blocksMined[blockName] et trouve le max.
     */
    private Optional<TopResult> findTopByBlock(String blockName) {
        Map<String, Document> lastSnapshots = getLastSnapshotsForAllPlayers();
        TopResult best = null;
        for (Map.Entry<String, Document> entry : lastSnapshots.entrySet()) {
            String playerName = entry.getKey();
            Document snap = entry.getValue();

            // blocksMined est une map
            @SuppressWarnings("unchecked")
            Map<String, Integer> blocks = (Map<String, Integer>) snap.get("blocksMined", Map.class);
            if (blocks == null) blocks = Collections.emptyMap();

            int val = blocks.getOrDefault(blockName, 0);
            if (best == null || val > best.value()) {
                best = new TopResult(playerName, val);
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Récupère pour chaque joueur son dernier snapshot, puis
     * lit mobsKilled[mobName] et trouve le max.
     */
    private Optional<TopResult> findTopByMob(String mobName) {
        Map<String, Document> lastSnapshots = getLastSnapshotsForAllPlayers();
        TopResult best = null;
        for (Map.Entry<String, Document> entry : lastSnapshots.entrySet()) {
            String playerName = entry.getKey();
            Document snap = entry.getValue();

            @SuppressWarnings("unchecked")
            Map<String, Integer> mobs = (Map<String, Integer>) snap.get("mobsKilled", Map.class);
            if (mobs == null) mobs = Collections.emptyMap();

            int val = mobs.getOrDefault(mobName, 0);
            if (best == null || val > best.value()) {
                best = new TopResult(playerName, val);
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Récupère pour chaque joueur son dernier snapshot, puis
     * lit un champ integer (ex: totalScore, distanceTraveled...) et trouve le max.
     */
    private Optional<TopResult> findTopByField(String fieldName) {
        Map<String, Document> lastSnapshots = getLastSnapshotsForAllPlayers();
        TopResult best = null;
        for (Map.Entry<String, Document> entry : lastSnapshots.entrySet()) {
            String playerName = entry.getKey();
            Document snap = entry.getValue();

            Integer val = snap.get(fieldName, Integer.class);
            if (val == null) val = 0;
            if (best == null || val > best.value()) {
                best = new TopResult(playerName, val);
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Récupère le dernier snapshot pour chaque joueur (par ex. en triant par timestamp).
     */
    private Map<String, Document> getLastSnapshotsForAllPlayers() {
        // On suppose que "playerStatsSnapshots" contient tous les snapshots
        // On va regrouper par playerName, trouver le plus récent par timestamp

        List<Document> all = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find()
                .toList();

        // Map: playerName -> Document le plus récent
        Map<String, Document> latestMap = new HashMap<>();

        for (Document doc : all) {
            String playerName = doc.get("playerName", String.class);
            if (playerName == null) continue;
            String ts = doc.get("timestamp", String.class);
            if (ts == null) continue;

            Document current = latestMap.get(playerName);
            if (current == null) {
                // Premier snapshot pour ce joueur
                latestMap.put(playerName, doc);
            } else {
                // Compare le timestamp
                String currentTs = current.get("timestamp", String.class);
                if (currentTs != null && ts.compareTo(currentTs) > 0) {
                    // doc est plus récent
                    latestMap.put(playerName, doc);
                }
            }
        }
        return latestMap;
    }

    /**
     * Petit record pour stocker le meilleur joueur et sa valeur
     */
    private record TopResult(String playerName, int value) {}
}
