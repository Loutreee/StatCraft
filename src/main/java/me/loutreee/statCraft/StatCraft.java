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

import java.time.ZoneId;
import java.util.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public final class StatCraft extends JavaPlugin implements Listener {

    private File overworldToWatch;
    private Score scoreCalculator;
    private final Map<UUID, Integer> playerScores = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        getLogger().info("Bloc cassé : " + blockType); // Log pour voir si l'événement est capturé

        int blockScore = scoreCalculator.getConfigLoader().getBlockScore(blockType);

        if (blockScore > 0) {
            int playerScore = playerScores.getOrDefault(player.getUniqueId(), 0);
            playerScores.put(player.getUniqueId(), playerScore + blockScore);
            player.sendMessage("Vous avez gagné " + blockScore + " points pour avoir miné " + blockType);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntityType();

            int mobScore = scoreCalculator.getConfigLoader().getMobScore(entityType);

            if (mobScore > 0) {
                int playerScore = playerScores.getOrDefault(player.getUniqueId(), 0);
                playerScores.put(player.getUniqueId(), playerScore + mobScore);
                player.sendMessage("Vous avez gagné " + mobScore + " points pour avoir tué un " + entityType);
            }
        }
    }

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer() instanceof Player) {
            Player player = (Player) event.getView().getPlayer();
            if (event.getInventory().getResult() != null) {
                Material craftedItem = event.getInventory().getResult().getType();
                int craftScore = scoreCalculator.getConfigLoader().getCraftScore(craftedItem);

                if (craftScore > 0) {
                    int playerScore = playerScores.getOrDefault(player.getUniqueId(), 0);
                    playerScores.put(player.getUniqueId(), playerScore + craftScore);
                    player.sendMessage("Vous avez gagné " + craftScore + " points pour avoir crafté un " + craftedItem);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Le plugin est activé !");

        saveDefaultConfig();

        // Vérifier si le monde est en mode Hardcore
        World world = this.getServer().getWorlds().getFirst();
        boolean isHardcoreMode = this.getServer().getWorlds().getFirst().isHardcore();
        getLogger().info("Mode de jeu : " + (isHardcoreMode ? "Hardcore" : "Survival"));

        // Charger la configuration
        File configFile = new File(getDataFolder(), "config.yml");
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.loadConfig(configFile, isHardcoreMode);

        this.scoreCalculator = new Score(configLoader);

        // Récupère le premier monde (souvent le monde principal)
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

    public String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.now(ZoneId.of("Europe/Paris")).format(formatter);
    }

    public void writeStatistics(String playerName, int totalBlocksMined, int mobsKilled, int playTimeMinutes, int playerScore, Player player) {
        try {
            String timestamp = getCurrentTimestamp();

            File directory = new File("player_statistics/session" + sessionNumber + "/" + playerName);
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                if (!dirCreated) {
                    getLogger().warning("Impossible de créer le répertoire : " + directory.getPath());
                    // Gère l'erreur ici, comme lancer une exception ou utiliser un répertoire alternatif
                }
            }

            // Création du document XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Racine du document
            Element rootElement = doc.createElement("joueur");
            rootElement.setAttribute("nom", playerName);
            doc.appendChild(rootElement);

            // Détails des blocs minés
            Element blocksElement = doc.createElement("blocsMines");
            rootElement.appendChild(blocksElement);

            for (Material material : Material.values()) {
                int minedBlocks = player.getStatistic(Statistic.MINE_BLOCK, material);  // Renommé en `minedBlocks`
                if (minedBlocks > 0) {
                    Element blockElement = doc.createElement("minerai");
                    blockElement.setAttribute("nom", material.toString());
                    blockElement.appendChild(doc.createTextNode(String.valueOf(minedBlocks)));
                    blocksElement.appendChild(blockElement);
                }
            }

            // Ajouter le total des blocs minés
            Element totalBlocksElement = doc.createElement("totalBlocsMines");
            totalBlocksElement.appendChild(doc.createTextNode(String.valueOf(totalBlocksMined)));
            blocksElement.appendChild(totalBlocksElement);

            // Détails des mobs tués
            Element mobsElement = doc.createElement("mobsTues");
            rootElement.appendChild(mobsElement);

            for (EntityType entityType : EntityType.values()) {
                if (entityType != EntityType.UNKNOWN) {
                    int killedMobs = player.getStatistic(Statistic.KILL_ENTITY, entityType);
                    if (killedMobs > 0) {
                        Element mobElement = doc.createElement("monstre");
                        mobElement.setAttribute("nom", entityType.toString());
                        mobElement.appendChild(doc.createTextNode(String.valueOf(killedMobs)));
                        mobsElement.appendChild(mobElement);
                    }
                }
            }

            // Ajouter le total des mobs tué
            Element totalMobsElement = doc.createElement("totalMobsTues");
            totalMobsElement.appendChild(doc.createTextNode(String.valueOf(mobsKilled)));
            mobsElement.appendChild(totalMobsElement);

            // Détails des objets craftés
            Element craftElement = doc.createElement("objetsCraftes");
            rootElement.appendChild(craftElement);

            for (Material material : Material.values()) {
                int craftedItems = player.getStatistic(Statistic.CRAFT_ITEM, material);  // Renommé en `craftedItems`
                if (craftedItems > 0) {
                    Element craftItemElement = doc.createElement("objet");
                    craftItemElement.setAttribute("nom", material.toString());
                    craftItemElement.appendChild(doc.createTextNode(String.valueOf(craftedItems)));
                    craftElement.appendChild(craftItemElement);
                }
            }

            // Ajouter le total des objets craftés
            int totalItemsCrafted = calculateTotalCraftedItems(player);
            Element totalCraftElement = doc.createElement("totalObjetsCraftes");
            totalCraftElement.appendChild(doc.createTextNode(String.valueOf(totalItemsCrafted)));
            craftElement.appendChild(totalCraftElement);

            // Détails du temps de jeu
            Element playTimeElement = doc.createElement("tempsDeJeu");
            playTimeElement.appendChild(doc.createTextNode(String.valueOf(playTimeMinutes)));
            rootElement.appendChild(playTimeElement);

            // Détails du score global
            Element scoreElement = doc.createElement("scoreTotal");
            scoreElement.appendChild(doc.createTextNode(String.valueOf(playerScore)));
            rootElement.appendChild(scoreElement);

            // Sauvegarder le fichier XML du joueur
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(directory, timestamp + ".xml"));
            transformer.transform(source, result);

            getLogger().info("Statistiques détaillées enregistrées pour " + playerName + " à " + timestamp);
        } catch (Exception e) {
            getLogger().severe("Une erreur est survenue lors de l'enregistrement des statistiques pour " + playerName);
            getLogger().severe(e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                getLogger().severe(element.toString());
            }
        }

    }

    public int calculateTotalCraftedItems(Player player) {
        int totalCraftedItems = 0;

        // Parcourir tous les types d'outils, armes, armures, etc. fabriqués
        Material[] craftableItems = {
                Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
                Material.LEATHER_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
                Material.LEATHER_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
                Material.LEATHER_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
        };

        // Additionner tous les objets fabriqués pour chaque matériau
        for (Material material : craftableItems) {
            int itemsCrafted = player.getStatistic(Statistic.CRAFT_ITEM, material);
            totalCraftedItems += itemsCrafted;
        }

        return totalCraftedItems;
    }

    public void logPlayerStatistics() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

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

                int blocksMinedDiff = (totalBlocksMined - initialBlocksMined);
                int mobsKilledDiff = totalMobsKilled - initialMobsKilled;
                int playTimeDiff = playTimeMinutes - initialPlayTime;

                //Calcul le score
                int playerScore = scoreCalculator.calculatePlayerScore(player, playTimeDiff);

                // Afficher les écarts des statistiques par rapport aux valeurs initiales
                getLogger().info(player.getName() + " a miné " + blocksMinedDiff + " blocs depuis le début de la session.");
                getLogger().info(player.getName() + " a tué " + mobsKilledDiff + " monstres depuis le début de la session.");
                getLogger().info(player.getName() + " a joué " + playTimeDiff + " minutes depuis le début de la session.");
                getLogger().info(player.getName() + " a un score de " + playerScore + " points.");

                // Enregistrer les nouvelles statistiques dans un fichier XML
                writeStatistics(player.getName(), blocksMinedDiff, mobsKilledDiff, playTimeDiff, playerScore, player);
            }
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
                } else if (statistic == Statistic.CRAFT_ITEM) {
                    // Réinitialiser les objets craftés pour chaque type d'objet
                    for (Material material : Material.values()) {
                        try {
                            player.setStatistic(Statistic.CRAFT_ITEM, material, 0);
                        } catch (IllegalArgumentException e) {
                            // Ignorer les exceptions si l'objet n'a pas de statistique associée
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

            playerSessions.put(player.getName(), sessionNumber);  // Associer la session actuelle au joueur
            getLogger().info("Statistiques réinitialisées pour le joueur: " + player.getName() + " dans la session " + sessionNumber);
        }
    }







}