package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.World;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;



public final class StatCraft extends JavaPlugin implements Listener {

    private File overworldToWatch;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Le plugin est activé !");

        World world = this.getServer().getWorlds().getFirst(); // Récupère le premier monde (souvent le monde principal)
        checkServerGameMode(world);

        sessionNumber = 0;
        //resetAllPlayersStatsInMemory();

        overworldToWatch = new File(getDataFolder().getParent(), "overworld");
        new FolderCheckTask().runTaskTimer(this, 0L, 200L);

        new BukkitRunnable() {
            @Override
            public void run() {
                logPlayerStatistics();
            }
        }.runTaskTimer(this, 0L, 600L);
    }

    private class FolderCheckTask extends BukkitRunnable {
        @Override
        public void run() {
            if (!overworldToWatch.exists()) {
                getLogger().info("Le dossier 'overworld' a été supprimé !");
                resetAllPlayersStatsInMemory();
                cancel(); // Annule la tâche si le dossier est supprimé
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Le plugin est désactivé !");
    }

    public void checkServerGameMode(World world) {
        boolean isHardcore = world.isHardcore();

        if (isHardcore) {
            getLogger().info("Le monde principal est en mode HARDCORE !");
        } else {
            getLogger().info("Le monde principal n'est pas en mode HARDCORE.");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        World world = event.getEntity().getWorld();

        if (world.isHardcore()) {
            sessionNumber++;  // Incrémente le numéro de session
            resetAllPlayersStatsInMemory();
        }
    }

    public void deletePlayerStatFiles() {
        World world = getServer().getWorlds().getFirst(); // Récupère le premier monde
        File statsDirectory = new File(world.getWorldFolder(), "stats");

        if (statsDirectory.exists() && statsDirectory.isDirectory()) {
            File[] files = statsDirectory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        if (file.delete()) {
                            getLogger().info("Fichier de stats " + file.getName() + " supprimé.");
                        } else {
                            getLogger().warning("Impossible de supprimer " + file.getName());
                        }
                    }
                }
            }
        }
    }


    private int sessionNumber = 0;  // Identifiant de la session
    private final Map<String, Integer> playerSessions = new HashMap<>();
    private final Map<String, Integer> initialStats = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int currentSession = sessionNumber;

        if (!playerSessions.containsKey(player.getName()) || playerSessions.get(player.getName()) != currentSession) {
            // Enregistre les statistiques initiales du joueur pour la session
            initialStats.put(player.getName() + "_blocks", getTotalStatistic(player, Statistic.MINE_BLOCK, Material.class));
            initialStats.put(player.getName() + "_mobs", getTotalStatistic(player, Statistic.KILL_ENTITY, EntityType.class));
            initialStats.put(player.getName() + "_playTime", player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 1200);
            playerSessions.put(player.getName(), currentSession);  // Associe le joueur à la session actuelle
            getLogger().info("Statistiques initiales enregistrées pour le joueur: " + player.getName());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = getServer().getWorlds().getFirst();

        if (world.getName().equals("world")) {  // Remplace par le nom de ton monde si nécessaire
            // Ici, tu peux réinitialiser les statistiques une fois que le nouveau monde est chargé
            resetAllPlayersStatsInMemory();
            getLogger().info("Le nouveau monde a été régénéré. Les statistiques sont réinitialisées.");
        }
    }

    public void resetSinglePlayerStatistics(Player player) {
        lastStats.put(player.getName() + "_blocks", 0);
        lastStats.put(player.getName() + "_mobs", 0);
        lastStats.put(player.getName() + "_playTime", 0);
    }

    public String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.now(ZoneId.of("Europe/Paris")).format(formatter);
    }

    public void writeStatistics(String playerName, int blocksMined, int mobsKilled, int playTimeMinutes) {
        try {
            // Récupérer la date et l'heure actuelles
            String timestamp = getCurrentTimestamp();

            // Chemin vers le dossier de la session actuelle, puis le dossier du joueur
            File directory = new File("player_statistics/session" + sessionNumber + "/" + playerName);
            if (!directory.exists()) {
                directory.mkdirs(); // Créer le dossier s'il n'existe pas
            }

            // Création du document XML (même logique que précédemment)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("joueur");
            rootElement.setAttribute("nom", playerName);
            doc.appendChild(rootElement);

            Element mortElement = doc.createElement("statistiques");
            rootElement.appendChild(mortElement);

            Element blocksElement = doc.createElement("blocsMines");
            blocksElement.appendChild(doc.createTextNode(String.valueOf(blocksMined)));
            mortElement.appendChild(blocksElement);

            Element mobsElement = doc.createElement("mobsTues");
            mobsElement.appendChild(doc.createTextNode(String.valueOf(mobsKilled)));
            mortElement.appendChild(mobsElement);

            Element playTimeElement = doc.createElement("tempsDeJeu");
            playTimeElement.appendChild(doc.createTextNode(String.valueOf(playTimeMinutes)));
            mortElement.appendChild(playTimeElement);

            // Sauvegarder le fichier XML du joueur dans son propre dossier, horodaté
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(directory, timestamp + ".xml"));
            transformer.transform(source, result);

            getLogger().info("Statistiques enregistrées pour " + playerName + " à " + timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Integer> lastStats = new HashMap<>();

    public void logPlayerStatistics() {
        List<Player> players = (List<Player>) getServer().getOnlinePlayers();

        if (players.isEmpty()) {
            getLogger().info("Aucun joueur n'est connecté.");
        } else {
            for (Player player : players) {
                getLogger().info("Statistiques du joueur: " + player.getName());

                // Récupérer les statistiques actuelles
                int totalBlocksMined = getTotalStatistic(player, Statistic.MINE_BLOCK, Material.class);
                int totalMobsKilled = getTotalStatistic(player, Statistic.KILL_ENTITY, EntityType.class);
                int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE); // Temps joué en ticks
                int playTimeMinutes = playTimeTicks / 1200; // Conversion en minutes

                // Calculer les écarts par rapport aux statistiques initiales de la session
                int initialBlocksMined = initialStats.getOrDefault(player.getName() + "_blocks", 0);
                int initialMobsKilled = initialStats.getOrDefault(player.getName() + "_mobs", 0);
                int initialPlayTime = initialStats.getOrDefault(player.getName() + "_playTime", 0);

                int blocksMinedDiff = (totalBlocksMined - initialBlocksMined) / 2;
                int mobsKilledDiff = totalMobsKilled - initialMobsKilled;
                int playTimeDiff = playTimeMinutes - initialPlayTime;

                // Afficher les écarts des statistiques par rapport aux valeurs initiales
                getLogger().info(player.getName() + " a miné " + blocksMinedDiff + " blocs depuis le début de la session.");
                getLogger().info(player.getName() + " a tué " + mobsKilledDiff + " monstres depuis le début de la session.");
                getLogger().info(player.getName() + " a joué " + playTimeDiff + " minutes depuis le début de la session.");

                // Enregistrer les nouvelles statistiques dans un fichier XML
                writeStatistics(player.getName(), blocksMinedDiff, mobsKilledDiff, playTimeDiff);
            }
        }
    }


    public void deleteAllPlayersDataFiles() {
        try {
            World world = getServer().getWorlds().getFirst();
            File playerDataDirectory = new File(world.getWorldFolder(), "stats");

            if (playerDataDirectory.exists() && playerDataDirectory.isDirectory()) {
                File[] files = playerDataDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) {
                            boolean deleted = file.delete();
                            if (deleted) {
                                getLogger().info("Fichier " + file.getName() + " supprimé.");
                            } else {
                                getLogger().info("Impossible de supprimer " + file.getName());
                            }
                        }
                    }
                } else {
                    getLogger().warning("Aucun fichier trouvé dans 'playerdata'.");
                }
            } else {
                getLogger().warning("Le répertoire 'playerdata' n'existe pas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTotalStatistic(Player player, Statistic statistic, Class<?> enumType) {
        int total = 0;

        if (enumType == Material.class) {
            for (Material material : Material.values()) {
                try {
                    total += player.getStatistic(statistic, material);
                } catch (IllegalArgumentException e) {
                    // Ignorer les exceptions si le matériel n'a pas de statistique associée
                }
            }
        } else if (enumType == EntityType.class) {
            for (EntityType entityType : EntityType.values()) {
                try {
                    total += player.getStatistic(statistic, entityType);
                } catch (IllegalArgumentException e) {
                    // Ignorer les exceptions si l'entité n'a pas de statistique associée
                }
            }
        }

        return total;
    }

    public void resetPlayerStatistics() {
        int currentSession = sessionNumber;  // Obtenir la session actuelle

        for (Player player : getServer().getOnlinePlayers()) {
            // Réinitialise les statistiques pour chaque joueur connecté
            initialStats.put(player.getName() + "_blocks", 0);
            initialStats.put(player.getName() + "_mobs", 0);
            initialStats.put(player.getName() + "_playTime", 0);

            lastStats.put(player.getName() + "_blocks", 0);
            lastStats.put(player.getName() + "_mobs", 0);
            lastStats.put(player.getName() + "_playTime", 0);

            playerSessions.put(player.getName(), currentSession);  // Associer chaque joueur à la session actuelle
            getLogger().info("Statistiques réinitialisées pour le joueur : " + player.getName() + " dans la session " + currentSession);
        }
    }

    public void resetPlayerStatsInMemory(Player player) {
        for (Statistic stat : Statistic.values()) {
            try {
                // Réinitialiser toutes les statistiques qui n'ont pas besoin de paramètres supplémentaires
                player.setStatistic(stat, 0);
            } catch (IllegalArgumentException e) {
                // Certaines statistiques nécessitent des paramètres, tu peux les gérer ici
            }
        }
    }

    public void resetAllPlayersStatsInMemory() {
        for (Player player : getServer().getOnlinePlayers()) {
            // Réinitialiser toutes les statistiques associées à chaque joueur
            for (Statistic statistic : Statistic.values()) {
                if (statistic == Statistic.MINE_BLOCK) {
                    // Réinitialiser les blocs minés pour chaque type de bloc
                    for (Material material : Material.values()) {
                        try {
                            player.setStatistic(Statistic.MINE_BLOCK, material, 0);
                        } catch (IllegalArgumentException e) {
                            // Ignorer les exceptions si le matériel n'a pas de statistique associée
                        }
                    }
                } else if (statistic == Statistic.KILL_ENTITY) {
                    // Réinitialiser les monstres tués pour chaque type d'entité
                    for (EntityType entityType : EntityType.values()) {
                        try {
                            player.setStatistic(Statistic.KILL_ENTITY, entityType, 0);
                        } catch (IllegalArgumentException e) {
                            // Ignorer les exceptions si l'entité n'a pas de statistique associée
                        }
                    }
                } else {
                    // Réinitialiser les autres statistiques globales (comme PLAY_ONE_MINUTE)
                    try {
                        player.setStatistic(statistic, 0);
                    } catch (IllegalArgumentException e) {
                        // Ignorer les statistiques qui nécessitent des paramètres (comme MINE_BLOCK)
                    }
                }
            }

            // Réinitialiser les statistiques de session pour chaque joueur
            initialStats.put(player.getName() + "_blocks", 0);
            initialStats.put(player.getName() + "_mobs", 0);
            initialStats.put(player.getName() + "_playTime", 0);

            lastStats.put(player.getName() + "_blocks", 0);  // Réinitialiser aussi les dernières stats
            lastStats.put(player.getName() + "_mobs", 0);
            lastStats.put(player.getName() + "_playTime", 0);

            playerSessions.put(player.getName(), sessionNumber);  // Associer la session actuelle au joueur
            getLogger().info("Statistiques réinitialisées pour le joueur: " + player.getName() + " dans la session " + sessionNumber);
        }
    }









}