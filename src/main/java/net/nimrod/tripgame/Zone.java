package net.nimrod.tripgame;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Zone {

    private boolean claimed;
    private String name;
    private Flag flag;
    private int radius;

    public Zone(String name, int radius, Player player, Tripgame plugin) {
        this.name = name;
        this.radius = radius;
        this.claimed = false;
        this.flag = new Flag(player, plugin);
    }

    public void setZone(Player player) {
        // Place the flag
        flag.spawnFlag(player);

        // Get the flag's location
        Location flagLocation = flag.getLocation();

        // Turn blocks under the flag to white stained glass in a circle
        int x = flagLocation.getBlockX();
        int y = flagLocation.getBlockY();
        int z = flagLocation.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    player.getWorld().getBlockAt(x + dx, y - 1, z + dz).setType(Material.WHITE_STAINED_GLASS);
                }
            }
        }
    }

    public boolean isClaimed()
    {
        return claimed;
    }

    public String getName() {
        return name;
    }

    public Flag getFlag() {
        return flag;
    }

    public int getRadius() {
        return radius;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public void setName(String name) {
        this.name = name;
    }
}

