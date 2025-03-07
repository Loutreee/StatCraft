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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.repository.ObjectRepository;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.util.List;
import java.util.Map;

public final class StatCraft extends JavaPlugin implements Listener {

    private ObjectRepository<PlayerData> playerRepository;
    private Javalin app;
    private StatsManager statsManager;
    private ConfigLoader configLoader;
    private ScoreService scoreService;

    @Override
    public void onEnable() {
        // Initialisation du StatsManager pour conserver les stats en mémoire
        statsManager = new StatsManager();
        configLoader = new ConfigLoader(this);
        scoreService = new ScoreService(configLoader);

        // Récupération du port
        int port = configLoader.getWebPort();

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
        }).start(port);

        // Instanciation du service et du contrôleur pour l'API REST
        PlayerService playerService = new PlayerService();
        new PlayerController(app, playerService);

        // Enregistrement du contrôleur pour afficher toutes les stats via /api/allstats
        new StatsSnapshotController(app);

        new StatsQueryController(app);

        new ScoreController(app);

        getLogger().info("API REST démarrée sur le port 27800");
        getLogger().info("Plugin StatCraft activé !");

        // Tâche planifiée toutes les 30 secondes (600 ticks) :
        // log et insertion d'un snapshot pour tous les joueurs connectés
        new BukkitRunnable() {
            @Override
            public void run() {
                // logAllPlayers();
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

        // Incrémente le compteur de blocs minés en mémoire
        statsManager.incrementBlockMined(player, blockType);

        // Récupère le PlayerStats du joueur
        PlayerStats ps = statsManager.getPlayerStats(player.getUniqueId());
        if (ps != null) {
            int points = scoreService.getBlockScore(blockType);
            ps.addBlockScore(points); // Incrémente blockScore ET totalScore
            getLogger().info(player.getName() + " a miné un bloc de " + blockType
                    + " et a gagné " + points + " points (Total Block Score: " + ps.getBlockScore() + ")");
        } else {
            getLogger().info(player.getName() + " a miné un bloc de " + blockType + " (PlayerStats non trouvé)");
        }
    }


    // Incrémente le compteur d'items craftés (événement sur craft validé)
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRecipe() == null) return;

        // Récupère le résultat de la recette craftée
        ItemStack result = event.getRecipe().getResult();
        Material craftedItem = result.getType();
        int baseAmount = result.getAmount();

        // Récupère le PlayerStats du joueur
        PlayerStats ps = statsManager.getPlayerStats(player.getUniqueId());
        if (ps == null) return;

        // Gestion du shift-click : si c'est un shift-click, on calcule le nombre total crafté
        int totalCrafted;
        if (event.isShiftClick()) {
            int maxRepeats = getMaxCraftableTimes(event);
            totalCrafted = baseAmount * maxRepeats;
        } else {
            totalCrafted = baseAmount;
        }

        // Incrémente le compteur en mémoire pour cet item, avec la quantité réelle craftée
        statsManager.incrementItemCrafted(player, craftedItem, totalCrafted);

        // Récupère le score configuré pour cet item depuis le config
        int pointsPerUnit = scoreService.getCraftScore(craftedItem);
        int totalPoints = pointsPerUnit * totalCrafted;
        ps.addCraftScore(totalPoints); // ajoute au sous-score de craft (et totalScore)

        getLogger().info(player.getName() + " a crafté " + totalCrafted + " " + craftedItem
                + " pour " + totalPoints + " points (score unitaire: " + pointsPerUnit + ").");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType mobType = event.getEntityType();

            // Incrémente le compteur de mobs tués en mémoire
            statsManager.incrementMobKilled(player, mobType);

            // Récupère le PlayerStats pour ce joueur
            PlayerStats ps = statsManager.getPlayerStats(player.getUniqueId());
            if (ps != null) {
                // Récupère le score défini dans la config pour ce mob
                int points = scoreService.getMobScore(mobType);
                ps.addMobScore(points);
                getLogger().info(player.getName() + " a tué " + mobType
                        + " et a gagné " + points + " points (Total Mob Score: " + ps.getMobScore() + ")");
            } else {
                getLogger().info(player.getName() + " a tué " + mobType + " (PlayerStats non trouvé)");
            }
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
        PlayerStats ps = statsManager.getPlayerStats(player.getUniqueId());
        if (ps == null) {
            return; // Aucune stat en mémoire pour ce joueur
        }

        // Calcul du temps de jeu actuel (en minutes)
        int playTimeNow = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 1200;
        int diff = playTimeNow - ps.getLastPlayTime();
        if (diff > 0) {
            // +1 point par minute
            ps.addTimeScore(diff); // Incrémente timeScore ET totalScore
            ps.setLastPlayTime(playTimeNow);
        }

        String playerId = player.getName();
        getLogger().info("Insertion snapshot pour " + playerId);

        // Log des détails pour chaque catégorie
        // --- Blocs minés ---
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

        // --- Items craftés ---
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

        // --- Mobs tués ---
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

        // Log du temps de jeu actuel
        getLogger().info(" - Temps de jeu : " + playTimeNow + " minutes.");

        // Insertion d'un seul snapshot dans la base Nitrite
        StatsSnapshotService snapshotService = new StatsSnapshotService();
        snapshotService.insertPlayerSnapshot(
                ps.getPlayerName(),
                ps.getBlocksMined(),
                ps.getItemsCrafted(),
                ps.getMobsKilled(),
                playTimeNow,
                ps.getBlockScore(),
                ps.getCraftScore(),
                ps.getMobScore(),
                ps.getTimeScore(),
                ps.getTotalScore()
        );


        getLogger().info("Snapshot inséré pour " + playerId);
    }

    private int getMaxCraftableTimes(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return 1;
        }
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        int maxCrafts = Integer.MAX_VALUE;

        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            String[] shape = shaped.getShape();
            Map<Character, ItemStack> ingredientMap = shaped.getIngredientMap();
            // Calculer la quantité requise pour chaque ingrédient (clé = caractère)
            Map<Character, Integer> required = new HashMap<>();
            for (String row : shape) {
                for (char c : row.toCharArray()) {
                    if (c != ' ') {
                        required.merge(c, 1, Integer::sum);
                    }
                }
            }
            // Pour chaque ingrédient requis, compter combien on en a dans le matrix
            for (Map.Entry<Character, Integer> entry : required.entrySet()) {
                char key = entry.getKey();
                int needed = entry.getValue();
                ItemStack ingredient = ingredientMap.get(key);
                if (ingredient == null) continue; // recette invalide ?
                Material mat = ingredient.getType();
                int available = 0;
                for (ItemStack stack : matrix) {
                    if (stack != null && stack.getType() == mat) {
                        available += stack.getAmount();
                    }
                }
                int craftsForThisIngredient = available / needed;
                if (craftsForThisIngredient < maxCrafts) {
                    maxCrafts = craftsForThisIngredient;
                }
            }
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
            List<ItemStack> ingredients = shapeless.getIngredientList();
            // Compter la quantité requise pour chaque type d'ingrédient
            Map<Material, Integer> required = new HashMap<>();
            for (ItemStack ingredient : ingredients) {
                if (ingredient != null) {
                    required.merge(ingredient.getType(), 1, Integer::sum);
                }
            }
            // Pour chaque ingrédient requis, compter le total dans le matrix
            for (Map.Entry<Material, Integer> entry : required.entrySet()) {
                Material mat = entry.getKey();
                int needed = entry.getValue();
                int available = 0;
                for (ItemStack stack : matrix) {
                    if (stack != null && stack.getType() == mat) {
                        available += stack.getAmount();
                    }
                }
                int craftsForThisIngredient = available / needed;
                if (craftsForThisIngredient < maxCrafts) {
                    maxCrafts = craftsForThisIngredient;
                }
            }
        } else {
            // Pour les autres types de recettes, renvoie 1 par défaut
            return 1;
        }
        return (maxCrafts == Integer.MAX_VALUE || maxCrafts < 1) ? 1 : maxCrafts;
    }

}
