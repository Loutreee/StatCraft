package me.loutreee.statCraft;

import io.javalin.Javalin;

public class Web {
    private Javalin app;

    // Méthode pour démarrer le serveur Javalin
    public void start() {
        app = Javalin.create(/* config si besoin */);
        app.get("/", ctx -> ctx.result("Hello from Javalin inside a plugin!"));
        app.start(7070);
        System.out.println("[Web] Serveur Javalin démarré sur le port 7070");
    }

    // Méthode pour arrêter proprement le serveur Javalin
    public void stop() {
        if (app != null) {
            app.stop();
            System.out.println("[Web] Serveur Javalin arrêté");
        }
    }
}
