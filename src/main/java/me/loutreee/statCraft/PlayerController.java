package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;

public class PlayerController {

    private final Javalin app;
    private final PlayerService playerService;

    public PlayerController(Javalin app, PlayerService playerService) {
        this.app = app;
        this.playerService = playerService;
        registerEndpoints();
    }

    private void registerEndpoints() {
        // Endpoint pour récupérer tous les joueurs
        app.get("/api/players", this::getPlayers);

        // Endpoint pour ajouter un joueur
        app.post("/api/players", this::addPlayer);
    }

    private void getPlayers(Context ctx) {
        List<PlayerData> players = playerService.getAllPlayers();
        ctx.json(players);
    }

    private void addPlayer(Context ctx) {
        PlayerData newPlayer = ctx.bodyAsClass(PlayerData.class);
        playerService.addPlayer(newPlayer);
        ctx.status(201);
    }
}
