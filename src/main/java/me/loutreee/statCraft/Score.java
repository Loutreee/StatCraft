package me.loutreee.statCraft;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Score {

    // Calcul du score global pour un joueur
    public int calculatePlayerScore(Player player, int playTimeDiff) {
        int score = 0;
        int blockScore = 0;
        int mobScore = 0;
        int craftScore = 0;

        // Liste des blocs spécifiques pour lesquels des points spéciaux sont attribués
        List<Material> importantBlocks = Arrays.asList(
                Material.DIAMOND_ORE, Material.ANCIENT_DEBRIS, Material.GOLD_ORE, Material.IRON_ORE,
                Material.COAL_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE
        );

        // Parcourir tous les types de blocs minés par le joueur
        for (Material material : Material.values()) {
            int blocksMined = player.getStatistic(Statistic.MINE_BLOCK, material);
            if (importantBlocks.contains(material)) {
                // Donner des points spéciaux pour les blocs importants
                if (material == Material.DIAMOND_ORE) {
                    blockScore += blocksMined * 10;
                } else if (material == Material.ANCIENT_DEBRIS) {
                    blockScore += blocksMined * 15;
                } else if (material == Material.GOLD_ORE) {
                    blockScore += blocksMined * 5;
                } else if (material == Material.IRON_ORE) {
                    blockScore += blocksMined * 2;
                } else if (material == Material.COAL_ORE) {
                    blockScore += blocksMined;
                } else if (material == Material.EMERALD_ORE) {
                    blockScore += blocksMined * 8;
                } else if (material == Material.LAPIS_ORE) {
                    blockScore += blocksMined * 3;
                } else if (material == Material.REDSTONE_ORE) {
                    blockScore += blocksMined * 2;
                }
            } else {
                // Donner 1 point pour tout autre bloc
                blockScore += blocksMined;
            }
        }

        // Calcul du score pour chaque monstre agressif avec des points spécifiques
        mobScore += calculateHostileMobScore(player);

        // Points pour les mobs passifs
        mobScore += player.getStatistic(Statistic.KILL_ENTITY, EntityType.SHEEP);
        mobScore += player.getStatistic(Statistic.KILL_ENTITY, EntityType.COW);
        mobScore += player.getStatistic(Statistic.KILL_ENTITY, EntityType.PIG);

        // Points pour les objets craftés par catégorie
        craftScore += calculateCraftScore(player);

        // Points pour le temps de jeu (1 point par minute)
        score += playTimeDiff;

        // Score total : addition des points pour les blocs, les mobs, le crafting, et l'exploration
        score += blockScore + mobScore + craftScore;

        return score;
    }

    // Calcul du score pour les monstres agressifs avec des points spécifiques
    public int calculateHostileMobScore(Player player) {
        int hostileMobScore = 0;

        // Points pour les monstres agressifs
        List<EntityType> importantMobs = Arrays.asList(
                EntityType.ENDERMAN, EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.GHAST,
                EntityType.CREEPER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PIGLIN_BRUTE,
                EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.GUARDIAN,
                EntityType.ELDER_GUARDIAN, EntityType.PHANTOM, EntityType.SHULKER, EntityType.STRAY,
                EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN
        );

        for (EntityType entityType : EntityType.values()) {
            // Vérifier que l'entité est valide et qu'elle n'est pas UNKNOWN
            if (entityType != EntityType.UNKNOWN && importantMobs.contains(entityType)) {
                int mobsKilled = player.getStatistic(Statistic.KILL_ENTITY, entityType);

                // Ajouter des points spécifiques pour chaque monstre agressif
                if (entityType == EntityType.ENDERMAN) {
                    hostileMobScore += mobsKilled * 10;
                } else if (entityType == EntityType.WITHER_SKELETON) {
                    hostileMobScore += mobsKilled * 12;
                } else if (entityType == EntityType.BLAZE) {
                    hostileMobScore += mobsKilled * 8;
                } else if (entityType == EntityType.GHAST) {
                    hostileMobScore += mobsKilled * 7;
                } else if (entityType == EntityType.CREEPER) {
                    hostileMobScore += mobsKilled * 5;
                } else if (entityType == EntityType.ZOMBIE) {
                    hostileMobScore += mobsKilled * 3;
                } else if (entityType == EntityType.SKELETON) {
                    hostileMobScore += mobsKilled * 3;
                } else if (entityType == EntityType.PIGLIN_BRUTE) {
                    hostileMobScore += mobsKilled * 10;
                } else if (entityType == EntityType.WITHER) {
                    hostileMobScore += mobsKilled * 50;
                } else if (entityType == EntityType.ENDER_DRAGON) {
                    hostileMobScore += mobsKilled * 100;
                } else {
                    // Pour tous les autres monstres agressifs
                    hostileMobScore += mobsKilled * 2;
                }
            }
        }
        return hostileMobScore;
    }

    // Calcul du score de crafting basé sur les catégories de matériaux
    public int calculateCraftScore(Player player) {
        int craftScore = 0;

        // Outils et armes en bois = 2 points
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.WOODEN_SWORD) * 2;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.WOODEN_PICKAXE) * 2;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.WOODEN_AXE) * 2;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.WOODEN_SHOVEL) * 2;

        // Outils et armes en pierre = 4 points
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.STONE_SWORD) * 4;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.STONE_PICKAXE) * 4;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.STONE_AXE) * 4;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.STONE_SHOVEL) * 4;

        // Outils et armes en fer = 6 points
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_SWORD) * 6;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_PICKAXE) * 6;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_AXE) * 6;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_SHOVEL) * 6;

        // Outils et armes en diamant = 10 points
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_SWORD) * 10;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_PICKAXE) * 10;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_AXE) * 10;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_SHOVEL) * 10;

        // Outils et armes en netherite = 15 points
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_SWORD) * 15;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_PICKAXE) * 15;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_AXE) * 15;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_SHOVEL) * 15;

        // Armures (points similaires pour les matériaux)
        // Armures en cuir
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.LEATHER_HELMET) * 3;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.LEATHER_CHESTPLATE) * 5;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.LEATHER_LEGGINGS) * 4;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.LEATHER_BOOTS) * 2;

        // Armures en fer
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_HELMET) * 6;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_CHESTPLATE) * 10;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_LEGGINGS) * 8;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.IRON_BOOTS) * 4;

        // Armures en diamant
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_HELMET) * 12;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_CHESTPLATE) * 20;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_LEGGINGS) * 16;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.DIAMOND_BOOTS) * 8;

        // Armures en netherite
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_HELMET) * 18;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_CHESTPLATE) * 30;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_LEGGINGS) * 24;
        craftScore += player.getStatistic(Statistic.CRAFT_ITEM, Material.NETHERITE_BOOTS) * 12;


        return craftScore;
    }
}
