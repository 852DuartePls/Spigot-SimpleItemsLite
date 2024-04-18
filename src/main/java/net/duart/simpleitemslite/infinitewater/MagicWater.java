package net.duart.simpleitemslite.infinitewater;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MagicWater implements Listener {
    private final JavaPlugin plugin;
    private final MagicWaterItemListener magicWaterItemListener;


    public MagicWater(JavaPlugin plugin, MagicWaterItemListener magicWaterItemListener) {
        this.plugin = plugin;
        this.magicWaterItemListener = magicWaterItemListener;
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Material bucketMaterial = event.getBucket();

        if (bucketMaterial == Material.WATER_BUCKET) {
            ItemStack bucket = event.getPlayer().getInventory().getItemInMainHand();

            if (bucket.isSimilar(magicWaterItemListener.getMagicWaterItem())) {
                final int delayTicks = 1;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    event.setCancelled(true);

                    ItemStack newBucket = magicWaterItemListener.getMagicWaterItem().clone();
                    event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), newBucket);
                }, delayTicks);
            }
        }
    }
}