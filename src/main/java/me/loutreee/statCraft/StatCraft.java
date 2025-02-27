package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.List;
import java.util.Map;

public final class StatCraft extends JavaPlugin implements Listener {

    private ObjectRepository<PlayerData> playerRepository;
    private Javalin app;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        // Initialisation du StatsManager pour conserver les stats en mémoire
        statsManager = new StatsManager();

        // Récupération du repository Nitrite pour PlayerData
        playerRepository = NitriteBuilder.getPlayerRepository();
        getServer().getPluginManager().registerEvents(this, this);

        // Démarrage de Javalin pour servir l'API REST et les fichiers statiques
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.directory = "/web"; // dossier dans le classpath
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.hostedPath = "/";
            });
        }).start(27800);

        // Instanciation du service et du contrôleur pour l'API REST
        PlayerService playerService = new PlayerService();
        new PlayerController(app, playerService);

        // Enregistrement du contrôleur pour afficher toutes les stats via /api/allstats
        new StatsSnapshotController(app);

        getLogger().info("API REST démarrée sur le port 27800");
        getLogger().info("Plugin StatCraft activé !");

        // Tâche planifiée toutes les 30 secondes (600 ticks) :
        // log et insertion d'un snapshot pour tous les joueurs connectés
        new BukkitRunnable() {
            @Override
            public void run() {
                logAllPlayers();
                insertSnapshotsForAllPlayers();
            }
        }.runTaskTimer(this, 0L, 600L);
    }

    @Override
    public void onDisable() {
        if (app != null) {
            app.stop();
        }
        NitriteBuilder.close();
        getLogger().info("Plugin StatCraft désactivé !");
    }

    // --------------------- EVENEMENTS ---------------------

    // Incrémente le compteur de blocs minés
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        statsManager.incrementBlockMined(player, blockType);
        getLogger().info(player.getName() + " a miné un bloc de " + blockType);
    }

    // Incrémente le compteur d'items craftés (événement sur craft validé)
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getInventory().getResult() != null) {
                Material craftedItem = event.getInventory().getResult().getType();
                statsManager.incrementItemCrafted(player, craftedItem);
                getLogger().info(player.getName() + " a crafté " + craftedItem);
            }
        }
    }

    // Incrémente le compteur de mobs tués (si le tueur est un joueur)
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType mobType = event.getEntityType();
            statsManager.incrementMobKilled(player, mobType);
            getLogger().info(player.getName() + " a tué " + mobType);
        }
    }

    // Lorsqu'un joueur se connecte, on vérifie s'il est déjà dans le repository et on initialise ses stats en mémoire
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (playerRepository.find(FluentFilter.where("name").eq(playerName)).toList().isEmpty()) {
            PlayerData data = new PlayerData(playerName);
            playerRepository.insert(data);
            getLogger().info("Joueur " + playerName + " ajouté à la base de données.");
        } else {
            getLogger().info("Joueur " + playerName + " déjà présent dans la base de données.");
        }
        // Initialisation des compteurs en mémoire à partir du dernier snapshot (si existant)
        statsManager.initializePlayerStats(player);
    }

    // Lorsqu'un joueur se déconnecte, on insère immédiatement un snapshot pour ce joueur
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        insertSnapshotForPlayer(player);
        getLogger().info("Snapshot inséré lors de la déconnexion de " + player.getName());
    }

    // --------------------- METHODES UTILITAIRES ---------------------

    // Affiche dans la console la liste des joueurs enregistrés dans le repository Nitrite
    public void logAllPlayers() {
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

    // Insère un snapshot pour TOUS les joueurs connectés (utilisé dans la tâche planifiée)
    public void insertSnapshotsForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            insertSnapshotForPlayer(player);
        }
    }

    // Insère un snapshot pour un joueur spécifique
    public void insertSnapshotForPlayer(Player player) {
        StatsSnapshotService snapshotService = new StatsSnapshotService();
        String playerId = player.getName();
        getLogger().info("Insertion snapshot pour " + playerId);

        // Récupération des stats en mémoire pour le joueur
        PlayerStats ps = statsManager.getPlayerStats(player.getUniqueId());
        if (ps == null) {
            getLogger().info(" - Aucune stat en mémoire pour " + playerId);
            return;
        }

        // Log des détails pour chaque catégorie

        // Blocs minés
        Map<String, Integer> blocksMap = ps.getBlocksMined();
        if (!blocksMap.isEmpty()) {
            int totalBlocks = 0;
            for (String blockType : blocksMap.keySet()) {
                int count = blocksMap.get(blockType);
                totalBlocks += count;
                getLogger().info(" - " + count + " " + blockType + " miné(s).");
            }
            getLogger().info(" -> Total blocs minés: " + totalBlocks);
        } else {
            getLogger().info(" - Aucun bloc miné récupéré.");
        }

        // Items craftés
        Map<String, Integer> itemsMap = ps.getItemsCrafted();
        if (!itemsMap.isEmpty()) {
            int totalItems = 0;
            for (String itemType : itemsMap.keySet()) {
                int count = itemsMap.get(itemType);
                totalItems += count;
                getLogger().info(" - " + count + " " + itemType + " crafté(s).");
            }
            getLogger().info(" -> Total items craftés: " + totalItems);
        } else {
            getLogger().info(" - Aucun item crafté récupéré.");
        }

        // Mobs tués
        Map<String, Integer> mobsMap = ps.getMobsKilled();
        if (!mobsMap.isEmpty()) {
            int totalMobs = 0;
            for (String mobType : mobsMap.keySet()) {
                int count = mobsMap.get(mobType);
                totalMobs += count;
                getLogger().info(" - " + count + " " + mobType + " tué(s).");
            }
            getLogger().info(" -> Total mobs tués: " + totalMobs);
        } else {
            getLogger().info(" - Aucun mob tué récupéré.");
        }

        // Temps de jeu (en minutes)
        int playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 1200;
        getLogger().info(" - Temps de jeu : " + playTime + " minutes.");

        // Insertion du snapshot dans la base Nitrite
        snapshotService.insertPlayerSnapshot(
                ps.getPlayerName(),
                ps.getBlocksMined(),
                ps.getItemsCrafted(),
                ps.getMobsKilled(),
                playTime
        );
        getLogger().info("Snapshot inséré pour " + playerId);
    }
}
