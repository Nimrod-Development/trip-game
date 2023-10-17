package net.nimrod.tripgame;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Tripgame extends JavaPlugin {

    private final String TOTEM_TAG = "totem";
    private final double TOTEM_RADIUS_SQUARED = 4 * 4;
    public ArrayList<Player> players = new ArrayList<>();
    private BossBarTimer timer;
    boolean claiming = false;
    boolean claimTimerRunning = false;

    @Override
    public void onEnable() {
        timer = new BossBarTimer(this);
        this.getCommand("claim").setExecutor(new ClaimCommandExecutor());
        this.getCommand("totem").setExecutor(new TotemCommandExecutor());
        this.getCommand("player").setExecutor(new MakePlayer());

        // Repeating task to check player proximity to the totem
        this.getServer().getScheduler().runTaskTimer(this, this::checkPlayersNearTotem, 0, 20); // Check every second
    }

    //Checks if a player is near a totem

    private void checkPlayersNearTotem() {
        if(players != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                for (ArmorStand armorStand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
                    if (players.contains(player)) {
                        if (armorStand.hasMetadata(TOTEM_TAG) && player.getLocation().distanceSquared(armorStand.getLocation()) <= TOTEM_RADIUS_SQUARED && claiming == false) {
                            claiming = true;
                            player.sendTitle("Claiming...", null, 10, 20, 10);
                            if(!claimTimerRunning)
                                claimTimerRunning = timer.startTimer(player, 10);
                            break;
                        } else if (armorStand.hasMetadata(TOTEM_TAG) && player.getLocation().distanceSquared(armorStand.getLocation()) > TOTEM_RADIUS_SQUARED && claiming == true) {
                            claiming = false;
                            claimTimerRunning = timer.stopTimer();
                            break;
                        }
                    }
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
                        player.getWorld().getBlockAt(x, centerY, z).setType(Material.WHITE_STAINED_GLASS);
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

                            int x = armorStand.getLocation().getBlockX();
                            int y = armorStand.getLocation().getBlockY();
                            int z = armorStand.getLocation().getBlockZ();


                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dz = -1; dz <= 1; dz++) {
                                    armorStand.getWorld().getBlockAt(x + dx, y - 1, z + dz).setType(Material.WHITE_WOOL);
                                }
                            }
                            player.sendMessage("Totem removed!");
                            break; // Break after removing one totem. If you have multiple totems and want to remove all at once, you can remove this line.

                        }
                    }
                }
            }

            return true;
        }
    }
        /*
            This method manages a list that contains what players are actually playing the game. I made it for testing
            purposes because it was very annoying to constantly be spammed with the messages when I got near the armor stand
         */
        private class MakePlayer implements CommandExecutor {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by a player!");
                    return true;
                }

                if (args.length == 0) {
                    sender.sendMessage("Usage: /player <add/remove/list>");
                    return true;
                }

                Player player = (Player) sender;
                if (args[0].equalsIgnoreCase("add")) {
                    if (!players.contains(player)) {
                        players.add(player);
                        sender.sendMessage("You have been added to the list!");
                    } else {
                        sender.sendMessage("You are already in the list!");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (players.contains(player)) {
                        players.remove(player);
                        sender.sendMessage("You have been removed from the list!");
                    } else {
                        sender.sendMessage("You are not in the list!");
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (players.isEmpty()) {
                        sender.sendMessage("The list is empty.");
                    } else {
                        StringBuilder playerNames = new StringBuilder("Players in the list: ");
                        for (Player p : players) {
                            playerNames.append(p.getName()).append(", ");
                        }
                        // Remove the last comma and space
                        sender.sendMessage(playerNames.substring(0, playerNames.length() - 2));
                    }
                } else {
                    sender.sendMessage("Invalid argument. Use add, remove, or list.");
                }

                return true;
            }
        }

/*
This method spawns an armor stand and gives it a tag so that we can determine the distance from the armor stand and any players
 */
    public void spawnTotem(Player player, ArmorStand totem) {
        totem.setMetadata(TOTEM_TAG, new FixedMetadataValue(Tripgame.this, true));
        player.sendMessage("Totem set at your location!");
        // Coordinates of the player (or armor stand since they share the same location)
        int x = totem.getLocation().getBlockX();
        int y = totem.getLocation().getBlockY();
        int z = totem.getLocation().getBlockZ();

        // Loop to create a 3x3 of lime stained glass underneath the armor stand
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                player.getWorld().getBlockAt(x + dx, y - 1, z + dz).setType(Material.LIME_STAINED_GLASS);
            }
        }
    }
}
