package me.loutreee.statCraft;

import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;

import java.io.Serializable;


@Entity
public class PlayerData implements Serializable {

    @Id
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerData(String name) {
        this.name = name;
    }
}
