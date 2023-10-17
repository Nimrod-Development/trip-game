package net.nimrod.tripgame;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Tripgame extends JavaPlugin {

    private final String TOTEM_TAG = "totem";
    private final double TOTEM_RADIUS_SQUARED = 4 * 4;

    @Override
    public void onEnable() {
        ArrayList<PluginCommand> commands = new ArrayList<>();
        commands.add(this.getCommand("claim"));
        commands.add(this.getCommand("totem"));

        for (PluginCommand command : commands) {
            if (command == null) {
                getLogger().severe("Missing command in plugin.yml!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            command.setExecutor(new ClaimCommandExecutor());
        }


        // Repeating task to check player proximity to the totem
        this.getServer().getScheduler().runTaskTimer(this, this::checkPlayersNearTotem, 0, 20); // Check every second
    }

    private void checkPlayersNearTotem() {
        for (Player player : getServer().getOnlinePlayers()) {
            for (ArmorStand armorStand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
                if (armorStand.hasMetadata(TOTEM_TAG) && player.getLocation().distanceSquared(armorStand.getLocation()) <= TOTEM_RADIUS_SQUARED) {
                    player.sendMessage("You are trying to claim land");
                }
            }
        }
    }

    private class ClaimCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }

            Player player = (Player) sender;
            int centerX = player.getLocation().getBlockX();
            int centerY = player.getLocation().getBlockY();
            int centerZ = player.getLocation().getBlockZ();
            int radiusSquared = 5 * 5;

            for (int x = centerX - 5; x <= centerX + 5; x++) {
                for (int z = centerZ - 5; z <= centerZ + 5; z++) {
                    if ((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) <= radiusSquared) {
                        player.getWorld().getBlockAt(x, centerY, z).setType(Material.LIME_STAINED_GLASS);
                    }
                }
            }

            player.sendMessage("Claimed and changed blocks to lime stained glass!");

            return true;
        }
    }

    private class TotemCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("set")) {
                    ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                    spawnTotem(player, armorStand);
                    // Removing the line that made the armor stand invisible

                } else if (args[0].equalsIgnoreCase("remove")) {
                    for (ArmorStand armorStand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
                        if (armorStand.hasMetadata(TOTEM_TAG)) {
                            armorStand.remove();
                            player.sendMessage("Totem removed!");
                            break; // Break after removing one totem. If you have multiple totems and want to remove all at once, you can remove this line.
                        }
                    }
                }
            }

            return true;
        }
    }

    public void spawnTotem(Player player, ArmorStand totem)
    {
        totem.setMetadata(TOTEM_TAG, new FixedMetadataValue(Tripgame.this, true));
        player.sendMessage("Totem set at your location!");
        // Coordinates of the player (or armor stand since they share the same location)
        int x = totem.getLocation().getBlockX();
        int y = totem.getLocation().getBlockY();
        int z = totem.getLocation().getBlockZ();

        // Loop to create a 3x3 of lime stained-glass underneath the armor stand
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                player.getWorld().getBlockAt(x + dx, y - 1, z + dz).setType(Material.LIME_STAINED_GLASS);
            }
        }


    }
}
