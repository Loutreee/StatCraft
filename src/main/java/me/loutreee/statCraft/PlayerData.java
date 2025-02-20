package me.loutreee.statCraft;

import org.dizitart.no2.repository.annotations.Id;

public class PlayerData {
    @Id
    private String name;

    public PlayerData() {}

    public PlayerData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
