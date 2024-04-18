package net.duart.simpleitemslite.rainbowbridge;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BossBar;

import java.util.LinkedList;
import java.util.Map;

public class RainbowBridge implements Listener {
    private final JavaPlugin plugin;
    private final RainbowItemListener rainbowItemListener;
    private final int bridgeDurationTicks = 7 * 20;
    private final int cooldownTicks = 20 * 20;
    private final BossBar rainbowCooldownBar;

    private boolean isBuildingBridge(Player player) {
        return player.hasMetadata("buildingBridge");
    }

    private void setBuildingBridge(Player player, boolean building) {
        if (building) {
            player.setMetadata("buildingBridge", new FixedMetadataValue(plugin, true));
        } else {
            player.removeMetadata("buildingBridge", plugin);
        }
    }

    public RainbowBridge(JavaPlugin plugin, RainbowItemListener rainbowItemListener) {
        this.plugin = plugin;
        this.rainbowItemListener = rainbowItemListener;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.rainbowCooldownBar = Bukkit.createBossBar("§cR§6a§ei§an§3b§9o§5w §6B§er§ai§3d§9g§5e", BarColor.BLUE, BarStyle.SEGMENTED_20);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!itemInHand.isSimilar(rainbowItemListener.getRainbowBridgeItem())) {
            return;
        }

        if (isOnCooldown(player) || isBuildingBridge(player) || rainbowCooldownBar.getPlayers().contains(player)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        startRainbowBridge(player);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);
    }

    private boolean isOnCooldown(Player player) {
        return player.hasMetadata("cooldown");

    }


    private void startRainbowBridge(Player player) {
        LinkedList<Map.Entry<Location, BlockData>> blockList = new LinkedList<>();

        setBuildingBridge(player, true);
        this.rainbowCooldownBar.addPlayer(player);

        new BukkitRunnable() {
            int ticksPassed = 0;
            final String[] rainbowColors = {
                    "RED", "ORANGE", "YELLOW", "LIME", "GREEN", "CYAN", "LIGHT_BLUE", "PURPLE",
                    "MAGENTA", "PINK", "BLACK", "GRAY", "LIGHT_GRAY", "WHITE", "BLUE", "BROWN"
            };

            @Override
            public void run() {
                if (ticksPassed >= bridgeDurationTicks) {
                    cancel();
                    removeBlocks(blockList);
                    setBuildingBridge(player, false);
                    startCooldown(player);
                    return;
                }

                placeRainbowBlocks(player.getLocation(), player.getWorld(), blockList, rainbowColors);
                double progress = (double) ticksPassed / bridgeDurationTicks;
                rainbowCooldownBar.setProgress(progress);

                ticksPassed++;
            }
        }.runTaskTimer(plugin, 0, 1);

    }

    private void markAsIndestructible(Block block) {
        if (block != null && !block.hasMetadata("indestructible")) {
            block.setMetadata("indestructible", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL) {
            Block block = event.getBlock();
            if (block.hasMetadata("indestructible")) {
                event.setCancelled(true);
            }
        }
    }

    private void placeRainbowBlocks(Location playerLocation, World world, LinkedList<Map.Entry<Location, BlockData>> blockList, String[] rainbowColors) {
        int radius = 1;
        int colorIndex = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block blockBelow = world.getBlockAt(playerLocation.getBlockX() + x, playerLocation.getBlockY() - 1, playerLocation.getBlockZ() + z);
                Block blockBelowTwo = world.getBlockAt(playerLocation.getBlockX() + x, playerLocation.getBlockY() - 1, playerLocation.getBlockZ() + z);

                placeBlockIfEmpty(blockBelow, rainbowColors[colorIndex], blockList);
                placeBlockIfEmpty(blockBelowTwo, rainbowColors[colorIndex], blockList);

                colorIndex = (colorIndex + 1) % rainbowColors.length;
            }
        }
    }

    private void placeBlockIfEmpty(Block block, String color, LinkedList<Map.Entry<Location, BlockData>> blockList) {
        if (block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.LAVA) {
            Material woolColor = getWoolColor(color);
            if (woolColor != null) {
                block.setType(woolColor);
                BlockData blockData = Bukkit.createBlockData(woolColor);
                blockList.add(Map.entry(block.getLocation(), blockData));
                markAsIndestructible(block);
            }
        }
    }


    private void removeBlocks(LinkedList<Map.Entry<Location, BlockData>> blockList) {
        new BukkitRunnable() {
            int ticksToRemove = blockList.size() * 10;

            @Override
            public void run() {
                if (ticksToRemove <= 0 || blockList.isEmpty()) {
                    cancel();
                    return;
                }

                Map.Entry<Location, BlockData> lastEntry = blockList.pollFirst();

                if (lastEntry != null) {
                    Location location = lastEntry.getKey();
                    Block blockToRemove = location.getBlock();
                    blockToRemove.setType(Material.AIR);
                }

                ticksToRemove--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private Material getWoolColor(String color) {
        return switch (color.toUpperCase()) {
            case "RED" -> Material.RED_WOOL;
            case "ORANGE" -> Material.ORANGE_WOOL;
            case "YELLOW" -> Material.YELLOW_WOOL;
            case "LIME" -> Material.LIME_WOOL;
            case "GREEN" -> Material.GREEN_WOOL;
            case "CYAN" -> Material.CYAN_WOOL;
            case "LIGHT_BLUE" -> Material.LIGHT_BLUE_WOOL;
            case "PURPLE" -> Material.PURPLE_WOOL;
            case "MAGENTA" -> Material.MAGENTA_WOOL;
            case "PINK" -> Material.PINK_WOOL;
            case "BLACK" -> Material.BLACK_WOOL;
            case "GRAY" -> Material.GRAY_WOOL;
            case "LIGHT_GRAY" -> Material.LIGHT_GRAY_WOOL;
            case "WHITE" -> Material.WHITE_WOOL;
            case "BLUE" -> Material.BLUE_WOOL;
            case "BROWN" -> Material.BROWN_WOOL;
            default -> null;
        };
    }

    private void startCooldown(Player player) {
        new BukkitRunnable() {
            int remainingTicks = cooldownTicks;
            int remainingSeconds = cooldownTicks / 20;
            int prevRemainingSeconds = remainingSeconds;

            @Override
            public void run() {
                if (remainingTicks <= 0) {
                    setBuildingBridge(player, false);
                    rainbowCooldownBar.removePlayer(player);
                    cancel();
                    return;
                }

                remainingSeconds = remainingTicks / 20;
                double progress = (double) remainingTicks / cooldownTicks;
                rainbowCooldownBar.setProgress(progress);
                if (remainingSeconds != prevRemainingSeconds) {
                    prevRemainingSeconds = remainingSeconds;
                }

                remainingTicks--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}