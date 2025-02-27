package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.dizitart.no2.collection.Document;

import java.util.List;

public class StatsSnapshotController {

    public StatsSnapshotController(Javalin app) {
        registerEndpoints(app);
    }

    private void registerEndpoints(Javalin app) {
        // Endpoint GET qui renvoie tout le contenu de la collection "playerStatsSnapshots"
        app.get("/api/allstats", this::getAllStats);
    }

    private void getAllStats(Context ctx) {
        List<Document> snapshots = NitriteBuilder.getDatabase()
                .getCollection("playerStatsSnapshots")
                .find().toList();
        ctx.json(snapshots);
    }
}
