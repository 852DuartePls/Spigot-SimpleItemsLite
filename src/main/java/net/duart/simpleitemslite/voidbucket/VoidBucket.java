package net.duart.simpleitemslite.voidbucket;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        ItemStack voidBucket = voidBucketItemListener.getVoidBucketItem();
        ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();

        boolean voidBucketInMainHand = mainHandItem.isSimilar(voidBucket);
        boolean voidBucketInOffHand = offHandItem.isSimilar(voidBucket);

        if (!voidBucketInMainHand && !voidBucketInOffHand) {
            return;
        }

        final int delayTicks = 1;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.setCancelled(true);

            ItemStack newBucket = voidBucket.clone();

            if (voidBucketInMainHand) {
                event.getPlayer().getInventory().setItemInMainHand(newBucket);
            } else {
                event.getPlayer().getInventory().setItemInOffHand(newBucket);
            }
        }, delayTicks);
    }
}
