package fr.ipazu.advancedrealm.realm.themes;

import org.bukkit.Location;

public class Theme {

    private Location spawn;

    public Theme(Location spawn) {
        this.spawn = spawn;
    }

    public Location getSpawn() {
        return spawn;
    }
}
