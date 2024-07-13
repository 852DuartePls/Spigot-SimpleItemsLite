package net.duart.simpleitemslite.infinitewater;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        ItemStack magicWaterItem = magicWaterItemListener.getMagicWaterItem();
        ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();

        boolean magicWaterInMainHand = mainHandItem.isSimilar(magicWaterItem);
        boolean magicWaterInOffHand = offHandItem.isSimilar(magicWaterItem);

        if (!magicWaterInMainHand && !magicWaterInOffHand) {
            return;
        }

        final int delayTicks = 1;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.setCancelled(true);
            ItemStack newBucket = magicWaterItem.clone();

            if (magicWaterInMainHand) {
                event.getPlayer().getInventory().setItemInMainHand(newBucket);
            } else {
                event.getPlayer().getInventory().setItemInOffHand(newBucket);
            }
        }, delayTicks);
    }
}
