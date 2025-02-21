package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.List;

public final class StatCraft extends JavaPlugin implements Listener {

    private ObjectRepository<PlayerData> playerRepository;
    private Javalin app;

    @Override
    public void onEnable() {
        // Récupère le repository
        playerRepository = NitriteBuilder.getPlayerRepository();

        // Démarrage de Javalin avec les fichiers statiques (dossier /web dans les ressources)
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFileConfig -> {
                // Pour éviter toute interférence, on peut servir les fichiers statiques dans un chemin dédié
                staticFileConfig.directory = "/web"; // dossier dans le classpath
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.hostedPath = "/"; // servir les fichiers statiques sous /static
            });
        }).start(27800);

        // Instancier le service et le contrôleur pour enregistrer les endpoints
        PlayerService playerService = new PlayerService();
        new PlayerController(app, playerService);

        getLogger().info("API REST démarrée sur le port 27800");
        getLogger().info("Plugin StatCraft activé !");

        // Planifie l'exécution de logAllPlayers() toutes les 5 secondes (100 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                logAllPlayers();
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    @Override
    public void onDisable() {
        if (app != null) {
            app.stop();
        }
        // Ferme la base
        NitriteBuilder.close();
        getLogger().info("Plugin StatCraft désactivé !");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Vérifie si le joueur est déjà dans la base
        if (playerRepository.find(FluentFilter.where("name").eq(playerName)).toList().isEmpty()) {
            PlayerData data = new PlayerData(playerName);
            playerRepository.insert(data);
            getLogger().info("Joueur " + playerName + " ajouté à la base de données.");
        } else {
            getLogger().info("Joueur " + playerName + " déjà présent dans la base de données.");
        }
    }

    public void logAllPlayers() {
        // Récupère tous les joueurs depuis le repository
        List<PlayerData> players = NitriteBuilder.getPlayerRepository().find().toList();

        if (players.isEmpty()) {
            getLogger().info("Aucun joueur n'est enregistré dans la base de données.");
        } else {
            getLogger().info("Liste des joueurs enregistrés dans la base :");
            for (PlayerData player : players) {
                getLogger().info(" - " + player.getName());
            }
        }
    }
}
