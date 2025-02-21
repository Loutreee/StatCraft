package me.loutreee.statCraft;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class PlayerDataConverter implements EntityConverter<PlayerData> {

    @Override
    public Class<PlayerData> getEntityType() {
        return PlayerData.class;
    }

    @Override
    public Document toDocument(PlayerData entity, NitriteMapper nitriteMapper) {
        Document document = Document.createDocument(); // Utilise la méthode statique
        document.put("name", entity.getName());
        return document;
    }

    @Override
    public PlayerData fromDocument(Document document, NitriteMapper nitriteMapper) {
            String name = document.get("name", String.class);
            return new PlayerData(name);
    }
}
