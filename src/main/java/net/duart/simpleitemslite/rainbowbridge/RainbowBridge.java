package net.duart.simpleitemslite.rainbowbridge;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RainbowBridge implements Listener {
    private final JavaPlugin plugin;
    private final RainbowItemListener rainbowItemListener;
    private final int bridgeDurationTicks = 10 * 20;
    private final int cooldownTicks = 20 * 20;
    private final BossBar rainbowCooldownBar;
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    private enum RainbowColor {
        RED(Material.RED_WOOL),
        ORANGE(Material.ORANGE_WOOL),
        YELLOW(Material.YELLOW_WOOL),
        LIME(Material.LIME_WOOL),
        GREEN(Material.GREEN_WOOL),
        CYAN(Material.CYAN_WOOL),
        LIGHT_BLUE(Material.LIGHT_BLUE_WOOL),
        PURPLE(Material.PURPLE_WOOL),
        MAGENTA(Material.MAGENTA_WOOL),
        PINK(Material.PINK_WOOL),
        BLACK(Material.BLACK_WOOL),
        GRAY(Material.GRAY_WOOL),
        LIGHT_GRAY(Material.LIGHT_GRAY_WOOL),
        WHITE(Material.WHITE_WOOL),
        BLUE(Material.BLUE_WOOL),
        BROWN(Material.BROWN_WOOL);

        private final Material material;

        RainbowColor(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }
    }

    public RainbowBridge(JavaPlugin plugin, RainbowItemListener rainbowItemListener) {
        this.plugin = plugin;
        this.rainbowItemListener = rainbowItemListener;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.rainbowCooldownBar = Bukkit.createBossBar(ChatColor.RED + "R" + ChatColor.GOLD + "a" + ChatColor.YELLOW + "i" +
                        ChatColor.GREEN + "n" + ChatColor.AQUA + "b" + ChatColor.BLUE + "o" + ChatColor.LIGHT_PURPLE + "w " +
                        ChatColor.GOLD + "B" + ChatColor.RED + "r" + ChatColor.LIGHT_PURPLE + "i" +
                        ChatColor.AQUA + "d" + ChatColor.BLUE + "g" + ChatColor.DARK_PURPLE + "e",
                BarColor.BLUE, BarStyle.SEGMENTED_20);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!itemInHand.isSimilar(rainbowItemListener.getRainbowBridgeItem())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (isOnCooldown(player) || isBuildingBridge(player) || rainbowCooldownBar.getPlayers().contains(player)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        startRainbowBridge(player);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);
        for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player nearbyPlayer) {
                Location playerLoc = player.getLocation();
                Location nearbyLoc = nearbyPlayer.getLocation();
                double distance = playerLoc.distance(nearbyLoc);

                float volume = (float) (1.0 - (distance / 20.0));
                float pitch = 1.0f;

                nearbyPlayer.playSound(nearbyLoc, Sound.BLOCK_AMETHYST_BLOCK_HIT, volume, pitch);
            }
        }
    }

    private boolean isOnCooldown(Player player) {
        return player.hasMetadata("cooldown");
    }

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

    private void startRainbowBridge(Player player) {
        LinkedList<Map.Entry<Location, BlockData>> blockList = new LinkedList<>();

        setBuildingBridge(player, true);
        this.rainbowCooldownBar.addPlayer(player);

        new BukkitRunnable() {
            int ticksPassed = 0;
            final RainbowColor[] rainbowColors = RainbowColor.values();

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

    private void placeRainbowBlocks(Location playerLocation, World world, LinkedList<Map.Entry<Location, BlockData>> blockList, RainbowColor[] rainbowColors) {
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

    private void placeBlockIfEmpty(Block block, RainbowColor color, LinkedList<Map.Entry<Location, BlockData>> blockList) {
        if (block.getType() == Material.AIR) {
            block.setType(color.getMaterial());
            BlockData blockData = Bukkit.createBlockData(color.getMaterial());
            blockList.add(Map.entry(block.getLocation(), blockData));
            block.setMetadata("isAir", new FixedMetadataValue(plugin, true));
            markAsIndestructible(block);
            originalBlocks.put(block.getLocation(), Material.AIR);
        } else if (block.getType() == Material.WATER) {
            Levelled levelledBlock = (Levelled) block.getBlockData();
            if (levelledBlock.getLevel() == 0) {
                block.setType(color.getMaterial());
                BlockData blockData = Bukkit.createBlockData(color.getMaterial());
                blockList.add(Map.entry(block.getLocation(), blockData));
                block.setMetadata("isWaterSource", new FixedMetadataValue(plugin, true));
                markAsIndestructible(block);
                originalBlocks.put(block.getLocation(), Material.WATER);
            } else if (levelledBlock.getLevel() >= 1) {
                block.setType(color.getMaterial());
                BlockData blockData = Bukkit.createBlockData(color.getMaterial());
                blockList.add(Map.entry(block.getLocation(), blockData));
                block.setMetadata("isAir", new FixedMetadataValue(plugin, true));
                markAsIndestructible(block);
                originalBlocks.put(block.getLocation(), Material.AIR);
            }
        } else if (block.getType() == Material.LAVA) {
            Levelled levelledBlock = (Levelled) block.getBlockData();
            if (levelledBlock.getLevel() == 0) {
                block.setType(color.getMaterial());
                BlockData blockData = Bukkit.createBlockData(color.getMaterial());
                blockList.add(Map.entry(block.getLocation(), blockData));
                block.setMetadata("isLavaSource", new FixedMetadataValue(plugin, true));
                markAsIndestructible(block);
                originalBlocks.put(block.getLocation(), Material.LAVA);
            } else if (levelledBlock.getLevel() >= 1) {
                block.setType(color.getMaterial());
                BlockData blockData = Bukkit.createBlockData(color.getMaterial());
                blockList.add(Map.entry(block.getLocation(), blockData));
                block.setMetadata("isAir", new FixedMetadataValue(plugin, true));
                markAsIndestructible(block);
                originalBlocks.put(block.getLocation(), Material.AIR);
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

                    if (blockToRemove.hasMetadata("isAir")) {
                        blockToRemove.setType(Material.AIR);
                    } else if (blockToRemove.hasMetadata("isWaterSource")) {
                        blockToRemove.setType(Material.WATER);
                    } else if (blockToRemove.hasMetadata("isLavaSource")) {
                        blockToRemove.setType(Material.LAVA);
                    } else {
                        Material originalType = originalBlocks.get(location);
                        if (originalType != null) {
                            blockToRemove.setType(originalType);
                        }
                    }
                }

                ticksToRemove--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void markAsIndestructible(Block block) {
        block.setMetadata("indestructible", new FixedMetadataValue(plugin, true));
    }

    private void startCooldown(Player player) {
        player.setMetadata("cooldown", new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            int ticksLeft = cooldownTicks;

            @Override
            public void run() {
                if (ticksLeft <= 0) {
                    cancel();
                    player.removeMetadata("cooldown", plugin);
                    rainbowCooldownBar.removePlayer(player);
                    return;
                }

                double progress = (double) ticksLeft / cooldownTicks;
                rainbowCooldownBar.setProgress(progress);
                ticksLeft--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().hasMetadata("indestructible")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata("indestructible")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().hasMetadata("indestructible")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.getBlock().hasMetadata("indestructible")) {
            event.setCancelled(true);
        }
    }
}
