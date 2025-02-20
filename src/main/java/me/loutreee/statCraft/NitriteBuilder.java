package me.loutreee.statCraft;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.mvstore.MVStoreModule;

public class NitriteBuilder {
    MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath("/player_statistic/stattest.db")
            .build();

    Nitrite db = Nitrite.builder()
            .loadModule(storeModule)
            //.loadModule(new JacksonMapperModule())
            .openOrCreate();

    NitriteCollection collection = db.getCollection("test");
    ObjectRepository<Player> repository = db.getRepository(Player.class);
}
