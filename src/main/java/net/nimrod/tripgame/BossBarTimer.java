
package net.nimrod.tripgame;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

    public class BossBarTimer {
        private final JavaPlugin plugin;
        private BossBar timerBar;
        private int taskID;

        public BossBarTimer(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public boolean startTimer(Player player, int durationInSeconds) {
            if (timerBar != null) {
                timerBar.removeAll(); // Remove all players from the existing boss bar (if it exists) before creating a new one
            }

            // Create a new boss bar
            timerBar = Bukkit.createBossBar("Timer", BarColor.RED, BarStyle.SOLID);
            timerBar.addPlayer(player); // Add the player to see this boss bar

            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                private int secondsLeft = durationInSeconds;

                @Override
                public void run() {
                    if (secondsLeft <= 0) {
                        // When timer finishes
                        Bukkit.getScheduler().cancelTask(taskID); // Cancel this repeating task
                        timerBar.removeAll(); // Remove all players from this boss bar to hide it
                        return;
                    }

                    // Update the progress on the boss bar
                    double progress = (double) secondsLeft / durationInSeconds;
                    timerBar.setProgress(progress);

                    // Decrement the seconds left
                    secondsLeft--;
                }
            }, 0L, 20L); // 20L means the run() method will be called every second (20 ticks = 1 second in Minecraft)
            return true;
        }

        public boolean stopTimer() {
            System.out.println("Attempting to stop the timer."); // Debug message

            if (taskID != -1) {
                System.out.println("Cancelling the task with ID: " + taskID); // Debug message
                Bukkit.getScheduler().cancelTask(taskID);
                taskID = -1;
            } else {
                System.out.println("No task to cancel."); // Debug message
            }

            if (timerBar != null) {
                System.out.println("Removing all players from the timer bar."); // Debug message
                timerBar.removeAll();
                timerBar = null;
            } else {
                System.out.println("No active timer bar to remove players from."); // Debug message
            }
            return false;
        }


    }
