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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class StatCraft extends JavaPlugin implements Listener {

    private File overworldToWatch;
    private Score scoreCalculator;
    // Permet de cumuler les scores issus des événements (casse, craft, mob)
    private final Map<UUID, Integer> playerScores = new HashMap<>();

    private int sessionNumber = 0;
    // On enregistre ici les statistiques initiales de chaque joueur (blocs, mobs et crafts).
    private final Map<String, Integer> initialStats = new HashMap<>();
    private final Map<String, Integer> playerSessions = new HashMap<>();

    private Web webServer;

    @Override
    public void onEnable() {

        // Démarrage du serveur Javalin
        saveDefaultConfig();
        
        int webPort = getConfig().getInt("Web.port",27800);
        webServer = new Web();
        webServer.start(webPort);

        // Enregistrement des listeners et démarrage du plugin
        this.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(" Le plugin est activé !");

        // Détermine le mode de jeu (Hardcore ou Survival) à partir du premier monde
        World world = this.getServer().getWorlds().getFirst();
        boolean isHardcoreMode = world.isHardcore();
        getLogger().info(" Mode de jeu : " + (isHardcoreMode ? "Hardcore" : "Survival"));

        // Création du fichier XML indiquant le mode du serveur
        createServerModeFile(isHardcoreMode);

        // Chargement de la configuration depuis le fichier config.yml
        File configFile = new File(getDataFolder(), "config.yml");
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.loadConfig(configFile, isHardcoreMode);
        this.scoreCalculator = new Score(configLoader);

        // Enregistrement des statistiques initiales pour les joueurs à la connexion
        checkServerGameMode(world);
        sessionNumber = 0;
        //resetAllPlayersStatsInMemory();

        overworldToWatch = new File(getDataFolder().getParent(), "overworld");

        new FolderCheckTask().runTaskTimer(this, 0L, 200L);

        // Enregistrement et log des statistiques toutes les 30 secondes (600 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                logPlayerStatistics();
            }
        }.runTaskTimer(this, 0L, 600L);
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
        }
        getLogger().info(" Le plugin est désactivé !");
    }

    // ----------------- ÉVÉNEMENTS -----------------

    // Lorsqu'un joueur casse un bloc, on logge immédiatement l'action avec la valeur en points
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        int blockScore = scoreCalculator.getConfigLoader().getBlockScore(blockType);
        if (blockScore > 0) {
            int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
            playerScores.put(player.getUniqueId(), currentScore + blockScore);
            getLogger().info(player.getName() + " broke block " + blockType + " awarding " + blockScore + " points.");
        }
    }

    // Lorsqu'un joueur tue une entité, on logge immédiatement l'action avec la valeur en points
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntityType();
            int mobScore = scoreCalculator.getConfigLoader().getMobScore(entityType);
            if (mobScore > 0) {
                int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
                playerScores.put(player.getUniqueId(), currentScore + mobScore);
                getLogger().info(player.getName() + " killed " + entityType + " awarding " + mobScore + " points.");
            }
        }
    }

    // Lorsqu'un joueur craft un objet, on logge immédiatement l'action avec la valeur en points
    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer() instanceof Player player) {
            if (event.getInventory().getResult() != null) {
                Material craftedItem = event.getInventory().getResult().getType();
                int craftScore = scoreCalculator.getConfigLoader().getCraftScore(craftedItem);
                if (craftScore > 0) {
                    int currentScore = playerScores.getOrDefault(player.getUniqueId(), 0);
                    playerScores.put(player.getUniqueId(), currentScore + craftScore);
                    getLogger().info(player.getName() + " crafted " + craftedItem + " awarding " + craftScore + " points.");
                }
            }
        }
    }

    // Lorsqu'un joueur se connecte, on enregistre ses statistiques initiales pour la session (blocs, mobs et crafts)
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int currentSession = sessionNumber;
        if (!playerSessions.containsKey(player.getName()) || playerSessions.get(player.getName()) != currentSession) {
            initialStats.put(player.getName() + "_blocks", getTotalStatistic(player, Statistic.MINE_BLOCK, Material.class));
            initialStats.put(player.getName() + "_mobs", getTotalStatistic(player, Statistic.KILL_ENTITY, EntityType.class));
            initialStats.put(player.getName() + "_playTime", player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 1200);
            // Enregistre aussi les statistiques de crafting
            initialStats.put(player.getName() + "_craft", getTotalStatistic(player, Statistic.CRAFT_ITEM, Material.class));
            playerSessions.put(player.getName(), currentSession);
            getLogger().info(" Statistiques initiales enregistrées pour " + player.getName());
        }
    }

    // Lors du chargement d'un nouveau monde, on réinitialise les statistiques en mémoire
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = this.getServer().getWorlds().getFirst();
        if (world.getName().equals("world")) {
            resetAllPlayersStatsInMemory();
            getLogger().info(" Nouveau monde chargé. Statistiques réinitialisées.");
        }
    }

    // Lors de la mort d'un joueur en mode Hardcore, on incrémente le numéro de session et on réinitialise les stats.
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        World world = event.getEntity().getWorld();
        if (world.isHardcore()) {
            sessionNumber++;
            resetAllPlayersStatsInMemory();
        }
    }

    public void createServerModeFile(boolean isHardcoreMode) {
        try {
            File directory = new File(getDataFolder(), "server_info");
            if (!directory.exists() && !directory.mkdirs()) {
                getLogger().warning(" Impossible de créer le répertoire : " + directory.getPath());
                return;
            }

            File file = new File(directory, "server_mode.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("server");
            doc.appendChild(rootElement);

            Element hardcoreElement = doc.createElement("hardcoreMode");
            hardcoreElement.appendChild(doc.createTextNode(String.valueOf(isHardcoreMode)));
            rootElement.appendChild(hardcoreElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            getLogger().info(" Fichier server_mode.xml créé avec succès.");
        } catch (Exception e) {
            getLogger().severe(" Erreur lors de la création du fichier server_mode.xml.");
            getLogger().log(java.util.logging.Level.SEVERE, "Stack trace :", e);
        }
    }

    // Tâche qui surveille l'existence du dossier "overworld"
    private class FolderCheckTask extends BukkitRunnable {
        @Override
        public void run() {
            if (!overworldToWatch.exists()) {
                getLogger().info(" Le dossier 'overworld' a été supprimé !");
                resetAllPlayersStatsInMemory();
                cancel();
            }
        }
    }

    public void checkServerGameMode(World world) {
        if (world.isHardcore()) {
            getLogger().info(" Le monde principal est en mode HARDCORE !");
        } else {
            getLogger().info(" Le monde principal n'est pas en mode HARDCORE.");
        }
    }

    // Renvoie l'horodatage actuel au format yyyy-MM-dd_HH-mm-ss
    public String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.now(ZoneId.of("Europe/Paris")).format(formatter);
    }

    /**
     * Méthode d'écriture des statistiques détaillées dans un fichier XML.
     * Pour chaque catégorie, on inscrit
     * - Les entrées détaillées (exemple : pour chaque bloc miné, mob tué ou objet craft).
     * - Une balise <score> indiquant le score pondéré (nombre * coefficient config).
     * - Une balise <total…> indiquant le nombre total d'actions
     * Le score global est la somme des scores pondérés, auquel s'ajoute le temps de jeu (1 point/minute).
     */
    public void writeStatistics(String playerName, int playTimeMinutes, Player player) {
        try {
            String timestamp = getCurrentTimestamp();
            File directory = new File("player_statistics/session" + sessionNumber + "/" + playerName);
            if (!directory.exists() && !directory.mkdirs()) {
                getLogger().warning(" Impossible de créer le répertoire : " + directory.getPath());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("joueur");
            rootElement.setAttribute("nom", playerName);
            doc.appendChild(rootElement);

            // --- BLOCS MINÉS ---
            int totalBlocksCount = 0;
            int totalBlocksScore = 0;
            Element blocksElement = doc.createElement("blocsMines");
            rootElement.appendChild(blocksElement);
            for (Material material : Material.values()) {
                // Filtrer les matériaux legacy
                if (material.toString().startsWith("LEGACY_"))
                    continue;
                try {
                    int mined = player.getStatistic(Statistic.MINE_BLOCK, material);
                    if (mined > 0) {
                        totalBlocksCount += mined;
                        int factor = scoreCalculator.getConfigLoader().getBlockScore(material);
                        totalBlocksScore += mined * factor;
                        Element blockElement = doc.createElement("minerai");
                        blockElement.setAttribute("nom", material.toString());
                        blockElement.appendChild(doc.createTextNode(String.valueOf(mined)));
                        blocksElement.appendChild(blockElement);
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
            Element blocksScoreElement = doc.createElement("score");
            blocksScoreElement.appendChild(doc.createTextNode(String.valueOf(totalBlocksScore)));
            blocksElement.appendChild(blocksScoreElement);
            Element totalBlocksElement = doc.createElement("totalBlocsMines");
            totalBlocksElement.appendChild(doc.createTextNode(String.valueOf(totalBlocksCount)));
            blocksElement.appendChild(totalBlocksElement);

            // --- MOBS TUÉS ---
            int totalMobsCount = 0;
            int totalMobsScore = 0;
            Element mobsElement = doc.createElement("mobsTues");
            rootElement.appendChild(mobsElement);
            for (EntityType et : EntityType.values()) {
                if (et == EntityType.UNKNOWN)
                    continue;
                try {
                    int killed = player.getStatistic(Statistic.KILL_ENTITY, et);
                    if (killed > 0) {
                        totalMobsCount += killed;
                        int factor = scoreCalculator.getConfigLoader().getMobScore(et);
                        totalMobsScore += killed * factor;
                        Element mobElement = doc.createElement("monstre");
                        mobElement.setAttribute("nom", et.toString());
                        mobElement.appendChild(doc.createTextNode(String.valueOf(killed)));
                        mobsElement.appendChild(mobElement);
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
            Element mobsScoreElement = doc.createElement("score");
            mobsScoreElement.appendChild(doc.createTextNode(String.valueOf(totalMobsScore)));
            mobsElement.appendChild(mobsScoreElement);
            Element totalMobsElement = doc.createElement("totalMobsTues");
            totalMobsElement.appendChild(doc.createTextNode(String.valueOf(totalMobsCount)));
            mobsElement.appendChild(totalMobsElement);

            // --- OBJETS CRAFTÉS ---
            int totalCraftsCount = 0;
            int totalCraftsScore = 0;
            Element craftsElement = doc.createElement("objetsCraftes");
            rootElement.appendChild(craftsElement);
            for (Material material : Material.values()) {
                if (material.toString().startsWith("LEGACY_"))
                    continue;
                try {
                    int crafted = player.getStatistic(Statistic.CRAFT_ITEM, material);
                    if (crafted > 0) {
                        totalCraftsCount += crafted;
                        int factor = scoreCalculator.getConfigLoader().getCraftScore(material);
                        totalCraftsScore += crafted * factor;
                        Element craftElement = doc.createElement("objet");
                        craftElement.setAttribute("nom", material.toString());
                        craftElement.appendChild(doc.createTextNode(String.valueOf(crafted)));
                        craftsElement.appendChild(craftElement);
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
            Element craftsScoreElement = doc.createElement("score");
            craftsScoreElement.appendChild(doc.createTextNode(String.valueOf(totalCraftsScore)));
            craftsElement.appendChild(craftsScoreElement);
            Element totalCraftsElement = doc.createElement("totalObjetsCraftes");
            totalCraftsElement.appendChild(doc.createTextNode(String.valueOf(totalCraftsCount)));
            craftsElement.appendChild(totalCraftsElement);

            // --- TEMPS DE JEU ---
            Element playTimeElement = doc.createElement("tempsDeJeu");
            playTimeElement.appendChild(doc.createTextNode(String.valueOf(playTimeMinutes)));
            rootElement.appendChild(playTimeElement);

            // --- SCORE GLOBAL ---
            int globalScore = totalBlocksScore + totalMobsScore + totalCraftsScore + playTimeMinutes;
            Element scoreTotalElement = doc.createElement("scoreTotal");
            scoreTotalElement.appendChild(doc.createTextNode(String.valueOf(globalScore)));
            rootElement.appendChild(scoreTotalElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(directory, timestamp + ".xml"));
            transformer.transform(source, result);

            getLogger().info(" Statistiques détaillées enregistrées pour " + playerName + " à " + timestamp);
        } catch (Exception e) {
            getLogger().severe(" Erreur lors de l'enregistrement des statistiques pour " + playerName);

            // Au lieu de printStackTrace(), on log l’exception avec son stacktrace
            getLogger().log(java.util.logging.Level.SEVERE, "Stack trace :", e);
        }
    }

    // Calcule le total d'une statistique (en itérant sur toutes les valeurs d'une enum)
    public int getTotalStatistic(Player player, Statistic statistic, Class<?> enumType) {
        int total = 0;
        if (enumType == Material.class) {
            for (Material material : Material.values()) {
                try {
                    total += player.getStatistic(statistic, material);
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
        } else if (enumType == EntityType.class) {
            for (EntityType et : EntityType.values()) {
                try {
                    total += player.getStatistic(statistic, et);
                } catch (IllegalArgumentException e) {
                    // Ignorer
                }
            }
        }
        return total;
    }

    // Affiche dans la console (toutes les 30 secondes) les totaux et scores pondérés depuis le début de la session
    public void logPlayerStatistics() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) {
            getLogger().info(" Aucun joueur n'est connecté.");
        } else {
            for (Player player : players) {
                String playerName = player.getName();
                getLogger().info(" Statistiques du joueur: " + playerName);

                // Calcul des totaux depuis le début de la session (en filtrant les matériaux legacy)
                int totalBlocks = 0;
                int weightedBlocks = 0;
                for (Material mat : Material.values()) {
                    if (mat.toString().startsWith("LEGACY_"))
                        continue;
                    try {
                        int val = player.getStatistic(Statistic.MINE_BLOCK, mat);
                        totalBlocks += val;
                        weightedBlocks += val * scoreCalculator.getConfigLoader().getBlockScore(mat);
                    } catch (IllegalArgumentException ignored) { }
                }
                int totalMobs = 0;
                int weightedMobs = 0;
                for (EntityType et : EntityType.values()) {
                    if (et == EntityType.UNKNOWN)
                        continue;
                    try {
                        int val = player.getStatistic(Statistic.KILL_ENTITY, et);
                        totalMobs += val;
                        weightedMobs += val * scoreCalculator.getConfigLoader().getMobScore(et);
                    } catch (IllegalArgumentException ignored) { }
                }
                int totalCrafts = 0;
                int weightedCrafts = 0;
                for (Material mat : Material.values()) {
                    if (mat.toString().startsWith("LEGACY_"))
                        continue;
                    try {
                        int val = player.getStatistic(Statistic.CRAFT_ITEM, mat);
                        totalCrafts += val;
                        weightedCrafts += val * scoreCalculator.getConfigLoader().getCraftScore(mat);
                    } catch (IllegalArgumentException ignored) { }
                }
                int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                int playTimeMinutes = playTimeTicks / 1200;

                // Calcul des différences depuis le début de la session
                int initialBlocks = initialStats.getOrDefault(playerName + "_blocks", 0);
                int initialMobs = initialStats.getOrDefault(playerName + "_mobs", 0);
                int initialCrafts = initialStats.getOrDefault(playerName + "_craft", 0);
                int diffBlocks = totalBlocks - initialBlocks;
                int diffMobs = totalMobs - initialMobs;
                int diffCrafts = totalCrafts - initialCrafts;

                int globalScore = weightedBlocks + weightedMobs + weightedCrafts + playTimeMinutes;

                getLogger().info(playerName + " - Blocs minés: " + totalBlocks + " (Score: " + weightedBlocks + ")");
                getLogger().info(playerName + " - Mobs tués: " + totalMobs + " (Score: " + weightedMobs + ")");
                getLogger().info(playerName + " - Objets craftés: " + totalCrafts + " (Score: " + weightedCrafts + ")");
                getLogger().info(playerName + " - Temps de jeu: " + playTimeMinutes + " minutes");
                getLogger().info(playerName + " - Score global: " + globalScore + " points");

                // Enregistrement des statistiques détaillées dans le XML
                writeStatistics(playerName, playTimeMinutes, player);
            }
        }
    }

    // Réinitialise en mémoire les statistiques de tous les joueurs connectés et les statistiques de session
    public void resetAllPlayersStatsInMemory() {
        for (Player player : getServer().getOnlinePlayers()) {
            for (Statistic stat : Statistic.values()) {
                if (stat == Statistic.MINE_BLOCK) {
                    for (Material mat : Material.values()) {
                        try {
                            player.setStatistic(Statistic.MINE_BLOCK, mat, 0);
                        } catch (IllegalArgumentException ignored) { }
                    }
                } else if (stat == Statistic.KILL_ENTITY) {
                    for (EntityType et : EntityType.values()) {
                        try {
                            player.setStatistic(Statistic.KILL_ENTITY, et, 0);
                        } catch (IllegalArgumentException ignored) { }
                    }
                } else if (stat == Statistic.CRAFT_ITEM) {
                    for (Material mat : Material.values()) {
                        try {
                            player.setStatistic(Statistic.CRAFT_ITEM, mat, 0);
                        } catch (IllegalArgumentException ignored) { }
                    }
                } else {
                    try {
                        player.setStatistic(stat, 0);
                    } catch (IllegalArgumentException ignored) { }
                }
            }
            initialStats.put(player.getName() + "_blocks", 0);
            initialStats.put(player.getName() + "_mobs", 0);
            initialStats.put(player.getName() + "_craft", 0);
            initialStats.put(player.getName() + "_playTime", 0);
            playerSessions.put(player.getName(), sessionNumber);
            getLogger().info(" Statistiques réinitialisées pour " + player.getName() + " dans la session " + sessionNumber);
        }
    }
}
