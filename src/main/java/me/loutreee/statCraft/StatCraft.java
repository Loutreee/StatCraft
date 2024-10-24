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

public final class StatCraft extends JavaPlugin implements Listener {

    private File overworldToWatch;
    private Score scoreCalculator;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Le plugin est activé !");
        scoreCalculator = new Score();

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

    public void writeStatistics(String playerName, int blocksMined, int mobsKilled, int playTimeMinutes, int playerScore, Score scoreCalculator, Player player) {
        try {
            String timestamp = getCurrentTimestamp();

            File directory = new File("player_statistics/session" + sessionNumber + "/" + playerName);
            if (!directory.exists()) {
                directory.mkdirs();
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

            // Ajouter les détails de chaque minerai miné
            addBlockDetails(doc, blocksElement, player, Material.DIAMOND_ORE, "Diamant");
            addBlockDetails(doc, blocksElement, player, Material.ANCIENT_DEBRIS, "Débris antiques");
            addBlockDetails(doc, blocksElement, player, Material.GOLD_ORE, "Or");
            addBlockDetails(doc, blocksElement, player, Material.IRON_ORE, "Fer");
            addBlockDetails(doc, blocksElement, player, Material.COAL_ORE, "Charbon");
            addBlockDetails(doc, blocksElement, player, Material.EMERALD_ORE, "Émeraude");
            addBlockDetails(doc, blocksElement, player, Material.LAPIS_ORE, "Lapis Lazuli");
            addBlockDetails(doc, blocksElement, player, Material.REDSTONE_ORE, "Redstone");

            // Ajouter le total des blocs minés
            Element totalBlocksElement = doc.createElement("totalBlocsMines");
            totalBlocksElement.appendChild(doc.createTextNode(String.valueOf(blocksMined)));
            blocksElement.appendChild(totalBlocksElement);

            // Détails des mobs tués
            Element mobsElement = doc.createElement("mobsTues");
            rootElement.appendChild(mobsElement);

            // Ajouter les détails de chaque monstre tué
            addMobDetails(doc, mobsElement, player, EntityType.ENDERMAN, "Enderman");
            addMobDetails(doc, mobsElement, player, EntityType.WITHER_SKELETON, "Wither Skeleton");
            addMobDetails(doc, mobsElement, player, EntityType.BLAZE, "Blaze");
            addMobDetails(doc, mobsElement, player, EntityType.CREEPER, "Creeper");
            addMobDetails(doc, mobsElement, player, EntityType.GHAST, "Ghast");
            addMobDetails(doc, mobsElement, player, EntityType.ZOMBIE, "Zombie");
            addMobDetails(doc, mobsElement, player, EntityType.SKELETON, "Squelette");

            // Ajouter le total des mobs tués
            Element totalMobsElement = doc.createElement("totalMobsTues");
            totalMobsElement.appendChild(doc.createTextNode(String.valueOf(mobsKilled)));
            mobsElement.appendChild(totalMobsElement);

            // Détails des objets craftés
            Element craftElement = doc.createElement("objetsCraftes");
            rootElement.appendChild(craftElement);

            // Ajouter les détails pour les outils/armes de chaque type et matériau
            // Épées
            addCraftDetails(doc, craftElement, player, Material.WOODEN_SWORD, "Épée en bois");
            addCraftDetails(doc, craftElement, player, Material.STONE_SWORD, "Épée en pierre");
            addCraftDetails(doc, craftElement, player, Material.IRON_SWORD, "Épée en fer");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_SWORD, "Épée en diamant");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_SWORD, "Épée en netherite");

            // Pioches
            addCraftDetails(doc, craftElement, player, Material.WOODEN_PICKAXE, "Pioche en bois");
            addCraftDetails(doc, craftElement, player, Material.STONE_PICKAXE, "Pioche en pierre");
            addCraftDetails(doc, craftElement, player, Material.IRON_PICKAXE, "Pioche en fer");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_PICKAXE, "Pioche en diamant");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_PICKAXE, "Pioche en netherite");

            // Haches
            addCraftDetails(doc, craftElement, player, Material.WOODEN_AXE, "Hache en bois");
            addCraftDetails(doc, craftElement, player, Material.STONE_AXE, "Hache en pierre");
            addCraftDetails(doc, craftElement, player, Material.IRON_AXE, "Hache en fer");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_AXE, "Hache en diamant");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_AXE, "Hache en netherite");

            // Pelles
            addCraftDetails(doc, craftElement, player, Material.WOODEN_SHOVEL, "Pelle en bois");
            addCraftDetails(doc, craftElement, player, Material.STONE_SHOVEL, "Pelle en pierre");
            addCraftDetails(doc, craftElement, player, Material.IRON_SHOVEL, "Pelle en fer");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_SHOVEL, "Pelle en diamant");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_SHOVEL, "Pelle en netherite");

            // Armures en cuir
            addCraftDetails(doc, craftElement, player, Material.LEATHER_HELMET, "Casque en cuir");
            addCraftDetails(doc, craftElement, player, Material.LEATHER_CHESTPLATE, "Plastron en cuir");
            addCraftDetails(doc, craftElement, player, Material.LEATHER_LEGGINGS, "Jambières en cuir");
            addCraftDetails(doc, craftElement, player, Material.LEATHER_BOOTS, "Bottes en cuir");

            // Armures en fer
            addCraftDetails(doc, craftElement, player, Material.IRON_HELMET, "Casque en fer");
            addCraftDetails(doc, craftElement, player, Material.IRON_CHESTPLATE, "Plastron en fer");
            addCraftDetails(doc, craftElement, player, Material.IRON_LEGGINGS, "Jambières en fer");
            addCraftDetails(doc, craftElement, player, Material.IRON_BOOTS, "Bottes en fer");

            // Armures en diamant
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_HELMET, "Casque en diamant");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_CHESTPLATE, "Plastron en diamant");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_LEGGINGS, "Jambières en diamant");
            addCraftDetails(doc, craftElement, player, Material.DIAMOND_BOOTS, "Bottes en diamant");

            // Armures en netherite
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_HELMET, "Casque en netherite");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_CHESTPLATE, "Plastron en netherite");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_LEGGINGS, "Jambières en netherite");
            addCraftDetails(doc, craftElement, player, Material.NETHERITE_BOOTS, "Bottes en netherite");

            // Ajouter le total des objets craftés (si tu veux également un total ici, tu peux le calculer)
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
            e.printStackTrace();
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


    // Fonction pour ajouter des détails sur chaque bloc miné
    private void addBlockDetails(Document doc, Element parentElement, Player player, Material material, String materialName) {
        int blocksMined = player.getStatistic(Statistic.MINE_BLOCK, material);
        if (blocksMined > 0) {
            Element blockElement = doc.createElement("minerai");
            blockElement.setAttribute("nom", materialName);
            blockElement.appendChild(doc.createTextNode(String.valueOf(blocksMined)));
            parentElement.appendChild(blockElement);
        }
    }

    // Fonction pour ajouter des détails sur chaque monstre tué
    private void addMobDetails(Document doc, Element parentElement, Player player, EntityType entityType, String entityName) {
        int mobsKilled = player.getStatistic(Statistic.KILL_ENTITY, entityType);
        if (mobsKilled > 0) {
            Element mobElement = doc.createElement("monstre");
            mobElement.setAttribute("nom", entityName);
            mobElement.appendChild(doc.createTextNode(String.valueOf(mobsKilled)));
            parentElement.appendChild(mobElement);
        }
    }

    // Fonction pour ajouter des détails sur chaque objet crafté
    private void addCraftDetails(Document doc, Element parentElement, Player player, Material material, String materialName) {
        int itemsCrafted = player.getStatistic(Statistic.CRAFT_ITEM, material);
        if (itemsCrafted > 0) {
            Element craftElement = doc.createElement("objet");
            craftElement.setAttribute("nom", materialName);
            craftElement.appendChild(doc.createTextNode(String.valueOf(itemsCrafted)));
            parentElement.appendChild(craftElement);
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

                //Calcul le score
                int playerScore = scoreCalculator.calculatePlayerScore(player, playTimeDiff);

                // Afficher les écarts des statistiques par rapport aux valeurs initiales
                getLogger().info(player.getName() + " a miné " + blocksMinedDiff + " blocs depuis le début de la session.");
                getLogger().info(player.getName() + " a tué " + mobsKilledDiff + " monstres depuis le début de la session.");
                getLogger().info(player.getName() + " a joué " + playTimeDiff + " minutes depuis le début de la session.");
                getLogger().info(player.getName() + " a un score de " + playerScore + " points.");

                // Enregistrer les nouvelles statistiques dans un fichier XML
                writeStatistics(player.getName(), blocksMinedDiff, mobsKilledDiff, playTimeDiff, playerScore, scoreCalculator, player);
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

            lastStats.put(player.getName() + "_blocks", 0);  // Réinitialiser aussi les dernières stats
            lastStats.put(player.getName() + "_mobs", 0);
            lastStats.put(player.getName() + "_playTime", 0);

            playerSessions.put(player.getName(), sessionNumber);  // Associer la session actuelle au joueur
            getLogger().info("Statistiques réinitialisées pour le joueur: " + player.getName() + " dans la session " + sessionNumber);
        }
    }







}