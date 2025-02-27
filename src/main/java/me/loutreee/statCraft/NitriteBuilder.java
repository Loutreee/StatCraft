package me.loutreee.statCraft;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;

public class NitriteBuilder {
    private static final Nitrite db;
    private static final ObjectRepository<PlayerData> playerRepository;

    static {
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath("player_statistics/player_statistic.db")
                .build();

        // Chainer l'enregistrement du convertisseur avant d'ouvrir la base
        db = Nitrite.builder()
                .loadModule(storeModule)
                .registerEntityConverter(new PlayerDataConverter())
                .openOrCreate("user", "password");

        playerRepository = db.getRepository(PlayerData.class);
    }

    public static ObjectRepository<PlayerData> getPlayerRepository() {
        return playerRepository;
    }

    public static Nitrite getDatabase() {
        return db;
    }

    public static void close() {
        if (db != null) {
            db.close();
        }
    }
}
