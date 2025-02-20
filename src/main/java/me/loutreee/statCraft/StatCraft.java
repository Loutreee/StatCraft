package me.loutreee.statCraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.repository.ObjectRepository;


import static org.dizitart.no2.filters.FluentFilter.$;


public final class StatCraft extends JavaPlugin implements Listener {

    private ObjectRepository<PlayerData> playerRepository;

    @Override
    public void onEnable() {
        // Récupère le repository
        playerRepository = NitriteBuilder.getPlayerRepository();

        // Enregistre le listener
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin StatCraft activé !");
    }

    @Override
    public void onDisable() {
        // Ferme la base
        NitriteBuilder.close();
        getLogger().info("Plugin StatCraft désactivé !");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Vérifie si le joueur est déjà dans la base
        if (playerRepository.find($.eq(playerName)).toList().isEmpty()) {
            PlayerData data = new PlayerData(playerName);
            playerRepository.insert(data);
            getLogger().info("Joueur " + playerName + " ajouté à la base de données.");
        } else {
            getLogger().info("Joueur " + playerName + " déjà présent dans la base de données.");
        }
    }
}
