package net.duart.simpleitemslite.voidbucket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class VoidBucket implements Listener {
    private final JavaPlugin plugin;
    private final VoidBucketItemListener voidBucketItemListener;

    public VoidBucket(JavaPlugin plugin, VoidBucketItemListener voidBucketItemListener) {
        this.plugin = plugin;
        this.voidBucketItemListener = voidBucketItemListener;
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Material bucketMaterial = event.getBucket();

        if (bucketMaterial == Material.BUCKET) {
            ItemStack bucket = event.getPlayer().getInventory().getItemInMainHand();

            if (bucket.isSimilar(voidBucketItemListener.getVoidBucketItem())) {
                final int delayTicks = 1;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    event.setCancelled(true);

                    ItemStack newBucket = voidBucketItemListener.getVoidBucketItem().clone();
                    event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), newBucket);
                }, delayTicks);
            }
        }
    }
}
