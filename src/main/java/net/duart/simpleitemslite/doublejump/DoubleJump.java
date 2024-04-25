package net.duart.simpleitemslite.doublejump;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class DoubleJump implements Listener {
    private final JumpItemListener jumpItemListener;
    private final long cooldownTimeSeconds = 5;
    private long lastJumpTime = 0;
    private final BossBar jumpCooldownBar;
    private final CooldownManager cooldownManager;
    private final JavaPlugin plugin;

    public DoubleJump(JavaPlugin plugin, JumpItemListener jumpItemListener) {
        this.jumpItemListener = jumpItemListener;
        this.jumpCooldownBar = Bukkit.createBossBar("§aDouble Jump", BarColor.GREEN, BarStyle.SEGMENTED_6);
        this.jumpCooldownBar.setVisible(false);
        this.cooldownManager = new CooldownManager(plugin);
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL && jumpItemListener.playerHasJumpItem(player)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastJumpTime >= cooldownTimeSeconds * 1000) {
                player.setAllowFlight(true);
                event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(false), 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL && jumpItemListener.playerHasJumpItem(player)) {
            if (!player.isFlying()) {
                if (System.currentTimeMillis() - lastJumpTime >= cooldownTimeSeconds * 1000) {
                    if (!cooldownManager.isOnCooldown(player)) {
                        event.setCancelled(true);
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1));

                        lastJumpTime = System.currentTimeMillis();
                        for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                            if (entity instanceof Player nearbyPlayer) {
                                Location playerLoc = player.getLocation();
                                Location nearbyLoc = nearbyPlayer.getLocation();
                                double distance = playerLoc.distance(nearbyLoc);

                                float volume = (float) (1.0 - (distance / 20.0));
                                float pitch = 1.0f;

                                nearbyPlayer.playSound(nearbyLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, volume, pitch);
                            }
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, -0.5, 0), 30, 0.5, 0.5, 0.5, 1.0, new Particle.DustOptions(Color.WHITE, 3));

                        startCooldown(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (System.currentTimeMillis() - lastJumpTime < cooldownTimeSeconds * 1000) {
                event.setCancelled(true);
            }
        }
    }

    private void startCooldown(Player player) {
        if (!cooldownManager.isOnCooldown(player)) {
            jumpCooldownBar.setProgress(1.0);
            jumpCooldownBar.addPlayer(player);
            cooldownManager.startCooldown(player);
        }
    }

    public static class CooldownManager {
        private final BossBar jumpCooldownBar;
        private final int cooldownTicks;
        private final Plugin plugin;

        public CooldownManager(Plugin plugin) {
            this.plugin = plugin;
            this.jumpCooldownBar = Bukkit.createBossBar("§aDouble Jump", BarColor.GREEN, BarStyle.SEGMENTED_6);
            this.jumpCooldownBar.setVisible(false);
            this.cooldownTicks = 5 * 20;
            Bukkit.getScheduler().runTaskTimer(plugin, this::updateCooldownBar, 0, 1);
        }

        public void startCooldown(Player player) {
            if (!isOnCooldown(player)) {
                jumpCooldownBar.addPlayer(player);

                jumpCooldownBar.setVisible(true);
                jumpCooldownBar.setProgress(1.0);

                new BukkitRunnable() {
                    int remainingTicks = cooldownTicks;

                    @Override
                    public void run() {
                        if (remainingTicks <= 0) {
                            jumpCooldownBar.removePlayer(player);

                            jumpCooldownBar.setVisible(false);
                            cancel();
                            return;
                        }

                        double progress = (double) remainingTicks / cooldownTicks;
                        jumpCooldownBar.setProgress(progress);

                        remainingTicks--;
                    }
                }.runTaskTimer(plugin, 0, 1);
            }
        }

        private void updateCooldownBar() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isOnCooldown(player) && !jumpCooldownBar.getPlayers().contains(player)) {
                    jumpCooldownBar.addPlayer(player);
                } else if (!isOnCooldown(player) && jumpCooldownBar.getPlayers().contains(player)) {
                    jumpCooldownBar.removePlayer(player);
                }
            }
        }

        public boolean isOnCooldown(Player player) {
            return jumpCooldownBar.getPlayers().contains(player);
        }
    }

}