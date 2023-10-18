package net.nimrod.tripgame;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class Flag {

    private final String FLAG_TAG = "flag";
    private ArmorStand armorStand;
    private Tripgame plugin;

    public Flag(Player player, Tripgame plugin) {
        this.plugin = plugin;
    }


    public void spawnFlag(Player player) {
        armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        armorStand.setMetadata(FLAG_TAG, new FixedMetadataValue(plugin, true));
        player.sendMessage("Flag set at your location!");

    }

    public boolean removeFlag() {
        if(armorStand != null) {
            armorStand.remove();
            return true;
        }
        return false;
    }

    public boolean hasFlagTag() {
        return armorStand != null && armorStand.hasMetadata(FLAG_TAG);
    }

    public Location getLocation() {
        return armorStand != null ? armorStand.getLocation() : null;
    }
}
