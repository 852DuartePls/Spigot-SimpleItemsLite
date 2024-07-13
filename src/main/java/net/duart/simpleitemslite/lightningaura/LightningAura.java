package net.duart.simpleitemslite.lightningaura;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LightningAura implements Listener {
    private final JavaPlugin plugin;
    private final LightningItemListener lightningItemListener;
    private final Map<Player, Long> chargingPlayers;
    private final Map<Player, Boolean> chargedPlayers;
    private final BossBar chargingBar;
    private final BossBar cooldownBar;
    private final BossBar thirdCooldownBar;
    private BukkitRunnable cooldownTask;
    private BukkitRunnable damageTask;
    private boolean isThirdCooldownActive = false;

    public LightningAura(JavaPlugin plugin, LightningItemListener lightningItemListener) {
        this.plugin = plugin;
        this.lightningItemListener = lightningItemListener;
        this.chargingPlayers = new HashMap<>();
        this.chargedPlayers = new HashMap<>();
        this.chargingBar = Bukkit.createBossBar(ChatColor.YELLOW + "⚡ Charging lightning aura... ⚡", BarColor.YELLOW, BarStyle.SOLID);
        this.cooldownBar = Bukkit.createBossBar(ChatColor.RED + "⚡ Charge ⚡", BarColor.RED, BarStyle.SOLID);
        this.thirdCooldownBar = Bukkit.createBossBar(ChatColor.GOLD + "⚡ Cooldown ⚡", BarColor.YELLOW, BarStyle.SEGMENTED_10);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (itemInHand.isSimilar(lightningItemListener.getLightningItem()) && lightningItemListener.playerHasLightningItem(player)) {
                if (!isOnCooldown()) {
                    startCharging(player);
                } else {
                    event.setCancelled(true);
                }
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (chargedPlayers.containsKey(player)) {
                shootLightning(player);
            }
        }
    }

    private boolean isOnCooldown() {
        return isThirdCooldownActive;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block playerBlock = player.getLocation().getBlock();

        for (int xOffset = -3; xOffset <= 3; xOffset++) {
            for (int zOffset = -3; zOffset <= 3; zOffset++) {
                Block blockBelowPlayer = playerBlock.getRelative(xOffset, -1, zOffset);

                if (chargedPlayers.containsKey(player) && blockBelowPlayer.getType() == Material.WATER) {
                    boolean isSource = blockBelowPlayer.getBlockData() instanceof org.bukkit.block.data.Levelled &&
                            ((org.bukkit.block.data.Levelled) blockBelowPlayer.getBlockData()).getLevel() == 0;

                    if (isSource) {
                        blockBelowPlayer.setMetadata("isWaterSource", new FixedMetadataValue(plugin, true));
                    } else {
                        blockBelowPlayer.setMetadata("isNotWaterSource", new FixedMetadataValue(plugin, true));
                    }
                    blockBelowPlayer.setType(Material.BARRIER);
                    replaceBlockAfterDelay(blockBelowPlayer);
                }
            }
        }
    }

    private void replaceBlockAfterDelay(Block block) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.BARRIER) {
                if (block.hasMetadata("isWaterSource") && block.getMetadata("isWaterSource").get(0).asBoolean()) {
                    block.setType(Material.WATER);
                } else if (block.hasMetadata("isNotWaterSource") && block.getMetadata("isNotWaterSource").get(0).asBoolean()) {
                    block.setType(Material.AIR);
                }
            }
        }, 20L);
    }

    private void startCharging(Player player) {
        if (chargingPlayers.containsKey(player)) {
            return;
        }

        chargingPlayers.put(player, System.currentTimeMillis());
        chargingBar.addPlayer(player);
        startChargingBar(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                applyChargedEffect(player);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void applyChargedEffect(Player player) {
        Long startTime = chargingPlayers.get(player);
        if (startTime == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= 5000) {
            chargedPlayers.put(player, true);

            for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                if (entity instanceof Player nearbyPlayer) {
                    nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 1.0f, 0.3f);
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, SoundCategory.MASTER, 1.0f, 0.3f);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100 * 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100 * 20, 2));

            damageTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                        if (entity instanceof LivingEntity livingEntity && livingEntity.getHealth() > 0) {
                            double distanceSquared = entity.getLocation().distanceSquared(player.getLocation());
                            if (distanceSquared <= 9 && !(entity instanceof Player) && !(entity instanceof Animals) && !(entity instanceof ZombieVillager)) {
                                livingEntity.damage(5);
                                entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                            }
                        }
                    }
                }
            };
            damageTask.runTaskTimer(plugin, 0, 20);

            spawnChargedParticles(player);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    applyChargedEffect(player);
                }
            }.runTaskLater(plugin, 20L);
        }
    }

    private void spawnChargedParticles(Player player) {
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!chargedPlayers.containsKey(player)) {
                    cancel();
                    return;
                }

                for (double i = 0; i < Math.PI * 2; i += Math.PI / 4) {
                    double x = player.getLocation().getX() + Math.cos(angle + i) * 1.5;
                    double y = player.getLocation().getY() + 1;
                    double z = player.getLocation().getZ() + Math.sin(angle + i) * 1.5;

                    player.getWorld().spawnParticle(Particle.REDSTONE, new Location(player.getWorld(), x, y, z), 1, 0, 0, 0, 1, new Particle.DustOptions(Color.YELLOW, 1));
                    player.getWorld().spawnParticle(Particle.REDSTONE, new Location(player.getWorld(), x, y + 0.5, z), 1, 0, 0, 0, 1, new Particle.DustOptions(Color.YELLOW, 1));
                    player.getWorld().spawnParticle(Particle.REDSTONE, new Location(player.getWorld(), x, y - 0.5, z), 1, 0, 0, 0, 1, new Particle.DustOptions(Color.YELLOW, 1));
                }

                angle += Math.PI / 8;
                if (angle >= Math.PI * 2) {
                    angle = 0;
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private void shootLightning(Player player) {
        Location playerLocation = player.getLocation();
        List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
        int entitiesHit = 0;
        boolean lightningStruck = false;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity && hasCustomNameTag(entity) && !(entity instanceof ZombieVillager)) {
                double damageAmount = calculateDamage(livingEntity);

                if (entity instanceof Player) {
                    damageAmount = 16;
                    applyUnmitigatedDamage((Player) entity, damageAmount);
                }

                if (entity.getLocation().distance(playerLocation) <= 20) {
                    entity.getWorld().strikeLightningEffect(entity.getLocation());
                    entitiesHit++;
                    livingEntity.damage(damageAmount, player);

                    lightningStruck = true;
                    if (entitiesHit >= 5) {
                        break;
                    }
                }
            }
        }

        if (lightningStruck && cooldownTask != null) {
            cooldownTask.cancel();
            cooldownBar.setProgress(0.0);
            cooldownBar.removePlayer(player);
            startThirdCooldownBar(player);
            endCharging(player);
        }
    }

    private double calculateDamage(LivingEntity entity) {
        double baseDamage = 21.0;
        String customName = entity.getCustomName();

        if (customName != null && (customName.contains("lvl") || customName.contains("level") || customName.contains("lv") || customName.contains("nivel") || customName.contains("nv"))) {
            return baseDamage * 2.0;
        } else {
            return baseDamage;
        }
    }

    private boolean hasCustomNameTag(Entity entity) {
        String customName = entity.getCustomName();
        return customName == null || customName.isEmpty();
    }

    private void applyUnmitigatedDamage(Player player, double damage) {
        AttributeInstance armorAttribute = player.getAttribute(Attribute.GENERIC_ARMOR);
        double originalArmorValue = armorAttribute != null ? armorAttribute.getValue() : 0;

        AttributeModifier armorModifier = new AttributeModifier(UUID.randomUUID(), "Temporary armor reduction", -originalArmorValue, AttributeModifier.Operation.ADD_NUMBER);
        if (armorAttribute != null) {
            armorAttribute.addModifier(armorModifier);
        }

        player.damage(damage);

        if (armorAttribute != null) {
            armorAttribute.removeModifier(armorModifier);
        }
    }

    private void endCharging(Player player) {
        chargingPlayers.remove(player);
        chargedPlayers.remove(player);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 0.2f, 0.8f);

        for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player nearbyPlayer) {
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 0.8f);
            }
        }

        if (damageTask != null) {
            damageTask.cancel();
        }
    }

    private void startChargingBar(Player player) {
        new BukkitRunnable() {
            int chargingTicks = 0;

            @Override
            public void run() {
                double chargingProgress = Math.min(1.0, (double) chargingTicks / 120);

                chargingBar.setProgress(chargingProgress);

                if (chargingTicks >= 120) {
                    chargingBar.setProgress(1.0);
                    chargingBar.removePlayer(player);
                    cooldownBar.addPlayer(player);
                    startCooldownBar(player);
                    cancel();
                }

                chargingTicks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void startCooldownBar(Player player) {
        cooldownTask = new BukkitRunnable() {
            int cooldownTicks = 200;

            @Override
            public void run() {
                double cooldownProgress = Math.max(0.0, (double) cooldownTicks / 200);

                cooldownBar.setProgress(cooldownProgress);

                if (cooldownTicks <= 0) {
                    cooldownBar.setProgress(0.0);
                    cooldownBar.removePlayer(player);
                    endCharging(player);
                    startThirdCooldownBar(player);
                    cancel();
                }
                cooldownTicks--;
            }
        };
        cooldownTask.runTaskTimer(plugin, 0, 1);
    }

    private void startThirdCooldownBar(Player player) {
        isThirdCooldownActive = true;
        thirdCooldownBar.addPlayer(player);
        thirdCooldownBar.setProgress(1.0);

        new BukkitRunnable() {
            int cooldownTicks = 15 * 20;

            @Override
            public void run() {
                double cooldownProgress = Math.max(0.0, (double) cooldownTicks / (15 * 20));

                thirdCooldownBar.setProgress(cooldownProgress);

                if (cooldownTicks <= 0) {
                    thirdCooldownBar.setProgress(0.0);
                    thirdCooldownBar.removePlayer(player);
                    isThirdCooldownActive = false;
                    cancel();
                }
                cooldownTicks--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}