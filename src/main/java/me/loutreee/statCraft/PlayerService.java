package me.loutreee.statCraft;

import org.dizitart.no2.repository.ObjectRepository;
import java.util.List;

public class PlayerService {

    private final ObjectRepository<PlayerData> playerRepository;

    public PlayerService() {
        // Réutilise le NitriteBuilder de votre plugin
        this.playerRepository = NitriteBuilder.getPlayerRepository();
    }

    public List<PlayerData> getAllPlayers() {
        return playerRepository.find().toList();
    }

    public void addPlayer(PlayerData playerData) {
        playerRepository.insert(playerData);
    }
}
