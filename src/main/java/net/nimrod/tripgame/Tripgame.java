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
import java.util.stream.Collectors;

public class Tripgame extends JavaPlugin {

    private final String FLAG_TAG = "flag";
    private final double ZONE_RADIUS_SQUARED = 4 * 4;
    public ArrayList<Player> players = new ArrayList<>();
    public ArrayList<Zone> activeZones = new ArrayList<>();
    private BossBarTimer timer;
    boolean claiming = false;
    boolean claimTimerRunning = false;

    @Override
    public void onEnable() {
        timer = new BossBarTimer(this);
        setExecutors();
        schedulePlayerCheck();
    }

    private void setExecutors() {
        this.getCommand("player").setExecutor(new MakePlayerExecutor());
        this.getCommand("zone").setExecutor(new ZoneCommandExecutor());
    }

    private void schedulePlayerCheck() {
        this.getServer().getScheduler().runTaskTimer(this, this::checkPlayersNearZone, 0, 20);
    }

    private void checkPlayersNearZone() {
        if (activeZones.isEmpty()) return;

        for (Player player : getServer().getOnlinePlayers()) {
            if (!players.contains(player) && activeZones.isEmpty()) continue;

            Flag flag;
            for (Zone zone : activeZones)
            {
                flag = zone.getFlag();
                double distanceSquared = player.getLocation().distanceSquared(flag.getLocation());

                if (flag.hasFlagTag() && distanceSquared <= ZONE_RADIUS_SQUARED && !claiming) {
                    startClaiming(player);
                } else if (flag.hasFlagTag() && distanceSquared > ZONE_RADIUS_SQUARED && claiming) {
                    stopClaiming();
                }
            }

        }
    }


    public class ZoneCommandExecutor implements CommandExecutor {

        public ZoneCommandExecutor() {

        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
                String zoneName = args[1];

                // Assuming a default radius of 5 for the sake of this example.
                // You can modify it or even accept it as an argument if you like.
                int defaultRadius = 5;

                Zone zone = new Zone(zoneName, defaultRadius, player, Tripgame.this);
                activeZones.add(zone);
                zone.setZone(player);

                player.sendMessage("Zone '" + zoneName + "' set at your location!");

                return true;
            }

            sender.sendMessage("Usage: /zone set [name]");
            return false;
        }
    }

    private void startClaiming(Player player) {
        claiming = true;
        player.sendTitle("Claiming...", null, 5, 10, 5);
        if (!claimTimerRunning) {
            claimTimerRunning = timer.startTimer(player, 10);
        }
    }

    private void stopClaiming() {
        claiming = false;
        claimTimerRunning = timer.stopTimer();
    }



        //Old Flag Command
    /*
    private class FlagCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }

            Player player = (Player) sender;
            Flag flag = new Flag(player, Tripgame.this);

            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("set")) {
                    flag.spawnFlag(player);
                    player.sendMessage("Flag set at your location!");
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (flag.removeFlag()) {
                        player.sendMessage("Flag removed!");
                    } else {
                        player.sendMessage("No flags found near you!");
                    }
                }
            }

            return true;
        }
    }

     */


    private class MakePlayerExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (isNotPlayer(sender)) return true;
            if (args.length == 0) return sendUsageMessage(sender);

            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "add":
                    addToPlayerList(sender, player);
                    break;
                case "remove":
                    removeFromPlayerList(sender, player);
                    break;
                case "list":
                    listPlayers(sender);
                    break;
                default:
                    sender.sendMessage("Invalid argument. Use add, remove, or list.");
            }
            return true;
        }

        private boolean isNotPlayer(CommandSender sender) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }
            return false;
        }

        private boolean sendUsageMessage(CommandSender sender) {
            sender.sendMessage("Usage: /player <add/remove/list>");
            return true;
        }

        private void addToPlayerList(CommandSender sender, Player player) {
            if (!players.contains(player)) {
                players.add(player);
                sender.sendMessage("You have been added to the list!");
            } else {
                sender.sendMessage("You are already in the list!");
            }
        }

        private void removeFromPlayerList(CommandSender sender, Player player) {
            if (players.contains(player)) {
                players.remove(player);
                sender.sendMessage("You have been removed from the list!");
            } else {
                sender.sendMessage("You are not in the list!");
            }
        }

        private void listPlayers(CommandSender sender) {
            if (players.isEmpty()) {
                sender.sendMessage("The list is empty.");
                return;
            }

            String playerNames = players.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));

            sender.sendMessage("Players in the list: " + playerNames);
        }
    }
}
