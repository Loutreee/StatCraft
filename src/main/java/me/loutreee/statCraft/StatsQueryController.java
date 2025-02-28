package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatsQueryController {

    public StatsQueryController(Javalin app) {
        registerEndpoints(app);
    }

    private void registerEndpoints(Javalin app) {
        // Retourne le snapshot complet le plus récent pour un joueur
        app.get("/api/stats/{playerName}/latest", this::getLatestSnapshot);

        // Retourne la map des blocs minés
        app.get("/api/stats/{playerName}/blocks", this::getBlocksMined);
        // Retourne la quantité d'un bloc spécifique miné
        app.get("/api/stats/{playerName}/blocks/{blockName}", this::getSpecificBlockMined);

        // Retourne la map des items craftés
        app.get("/api/stats/{playerName}/items", this::getItemsCrafted);
        // Retourne la quantité d'un item spécifique crafté
        app.get("/api/stats/{playerName}/items/{itemName}", this::getSpecificItemCrafted);

        // Retourne la map des mobs tués
        app.get("/api/stats/{playerName}/mobs", this::getMobsKilled);
        // Retourne la quantité d'un mob spécifique tué
        app.get("/api/stats/{playerName}/mobs/{mobName}", this::getSpecificMobKilled);

        // Retourne le temps de jeu du joueur (en minutes)
        app.get("/api/stats/{playerName}/playtime", this::getPlayTime);
    }

    /**
     * Retourne le snapshot complet le plus récent pour un joueur.
     */
    private void getLatestSnapshot(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null) {
            ctx.json(snapshot);
        } else {
            ctx.status(404).result("Aucun snapshot trouvé pour " + playerName);
        }
    }

    /**
     * Récupère le snapshot le plus récent pour un joueur.
     */
    private Document getLatestSnapshotForPlayer(String playerName) {
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find(FluentFilter.where("playerName").eq(playerName))
                .toList();

        // Supposons que le champ "timestamp" est une chaîne ISO qui se trie correctement lexicographiquement.
        Optional<Document> latest = snapshots.stream()
                .max(Comparator.comparing(doc -> doc.get("timestamp", String.class)));
        return latest.orElse(null);
    }

    private void getBlocksMined(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("blocksMined")) {
            Map<String, Object> blocks = snapshot.get("blocksMined", Map.class);
            ctx.json(blocks);
        } else {
            ctx.status(404).result("Aucun snapshot ou blocs minés introuvables pour " + playerName);
        }
    }

    private void getSpecificBlockMined(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String blockName = ctx.pathParam("blockName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("blocksMined")) {
            Map<String, Object> blocks = snapshot.get("blocksMined", Map.class);
            Object count = blocks.get(blockName.toUpperCase());
            if (count != null) {
                ctx.result(count.toString());
            } else {
                ctx.status(404).result("Bloc " + blockName + " introuvable pour " + playerName);
            }
        } else {
            ctx.status(404).result("Aucun snapshot trouvé pour " + playerName);
        }
    }

    private void getItemsCrafted(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("itemsCrafted")) {
            Map<String, Object> items = snapshot.get("itemsCrafted", Map.class);
            ctx.json(items);
        } else {
            ctx.status(404).result("Aucun snapshot ou items craftés introuvables pour " + playerName);
        }
    }

    private void getSpecificItemCrafted(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String itemName = ctx.pathParam("itemName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("itemsCrafted")) {
            Map<String, Object> items = snapshot.get("itemsCrafted", Map.class);
            Object count = items.get(itemName.toUpperCase());
            if (count != null) {
                ctx.result(count.toString());
            } else {
                ctx.status(404).result("Item " + itemName + " introuvable pour " + playerName);
            }
        } else {
            ctx.status(404).result("Aucun snapshot trouvé pour " + playerName);
        }
    }

    private void getMobsKilled(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("mobsKilled")) {
            Map<String, Object> mobs = snapshot.get("mobsKilled", Map.class);
            ctx.json(mobs);
        } else {
            ctx.status(404).result("Aucun snapshot ou mobs tués introuvables pour " + playerName);
        }
    }

    private void getSpecificMobKilled(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String mobName = ctx.pathParam("mobName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("mobsKilled")) {
            Map<String, Object> mobs = snapshot.get("mobsKilled", Map.class);
            Object count = mobs.get(mobName.toUpperCase());
            if (count != null) {
                ctx.result(count.toString());
            } else {
                ctx.status(404).result("Mob " + mobName + " introuvable pour " + playerName);
            }
        } else {
            ctx.status(404).result("Aucun snapshot trouvé pour " + playerName);
        }
    }

    private void getPlayTime(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Document snapshot = getLatestSnapshotForPlayer(playerName);
        if (snapshot != null && snapshot.containsKey("playTime")) {
            ctx.result(snapshot.get("playTime", Integer.class).toString());
        } else {
            ctx.status(404).result("Aucun snapshot ou temps de jeu introuvable pour " + playerName);
        }
    }
}
