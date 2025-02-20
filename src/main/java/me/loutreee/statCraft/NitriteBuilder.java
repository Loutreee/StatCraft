package me.loutreee.statCraft;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.mvstore.MVStoreModule;

public class NitriteBuilder {
    private static Nitrite db;
    private static ObjectRepository<PlayerData> playerRepository;

    static {
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath("player_statistic.db")
                .build();

        db = Nitrite.builder()
                .loadModule(storeModule)
                .openOrCreate();

        playerRepository = db.getRepository(PlayerData.class);
    }

    public static ObjectRepository<PlayerData> getPlayerRepository() {
        return playerRepository;
    }

    public static void close() {
        if (db != null) {
            db.close();
        }
    }
}
