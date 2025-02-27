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
}
