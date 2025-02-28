package me.loutreee.statCraft;

import org.dizitart.no2.collection.Document;
import java.time.Instant;
import java.util.Map;

/**
 * Service permettant d'insérer un snapshot dans la collection "playerStatsSnapshots".
 */
public class StatsSnapshotService {

    public void insertPlayerSnapshot(String playerName,
                                     Map<String, Integer> blocksMined,
                                     Map<String, Integer> itemsCrafted,
                                     Map<String, Integer> mobsKilled,
                                     int playTime) {

        Document snapshot = Document.createDocument();
        snapshot.put("playerName", playerName);
        snapshot.put("timestamp", Instant.now().toString());
        snapshot.put("blocksMined", blocksMined);
        snapshot.put("itemsCrafted", itemsCrafted);
        snapshot.put("mobsKilled", mobsKilled);
        snapshot.put("playTime", playTime);

        NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .insert(snapshot);
    }

    public void insertPlayerSnapshot(String playerName,
                                     Map<String, Integer> blocksMined,
                                     Map<String, Integer> itemsCrafted,
                                     Map<String, Integer> mobsKilled,
                                     int playTime,
                                     int blockScore,
                                     int craftScore,
                                     int mobScore,
                                     int timeScore,
                                     int totalScore) {

        Document snapshot = Document.createDocument();
        snapshot.put("playerName", playerName);
        snapshot.put("timestamp", Instant.now().toString());
        snapshot.put("blocksMined", blocksMined);
        snapshot.put("itemsCrafted", itemsCrafted);
        snapshot.put("mobsKilled", mobsKilled);
        snapshot.put("playTime", playTime);

        // Sous-scores
        snapshot.put("blockScore", blockScore);
        snapshot.put("craftScore", craftScore);
        snapshot.put("mobScore", mobScore);
        snapshot.put("timeScore", timeScore);

        // Score total
        snapshot.put("totalScore", totalScore);

        NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .insert(snapshot);
    }

}

