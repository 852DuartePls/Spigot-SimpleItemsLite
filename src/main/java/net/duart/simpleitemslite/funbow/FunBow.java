package net.duart.simpleitemslite.funbow;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public class FunBow implements Listener {
    private final JavaPlugin plugin;
    private final FunBowItemListener funBowItemListener;
    private EntityType selectedEntityType;

    public FunBow(JavaPlugin plugin, FunBowItemListener funBowItemListener) {
        this.plugin = plugin;
        this.funBowItemListener = funBowItemListener;
        this.selectedEntityType = null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.isSimilar(funBowItemListener.getFunBowItem()) && event.getAction().toString().contains("LEFT_CLICK")) {
            openFunBowMenu(player);
        }
    }

    public void openFunBowMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, "§9◆ FunBow Menu");

        inventory.setItem(0, createNamedItem(Material.BLAZE_SPAWN_EGG, "§c◆ Shoot Blazes", "BLAZE", "BLAZE_UNIQUE_TAG"));
        inventory.setItem(1, createNamedItem(Material.CAT_SPAWN_EGG, "§c◆ Shoot Cats", "CAT", "CAT_UNIQUE_TAG"));
        inventory.setItem(2, createNamedItem(Material.CAVE_SPIDER_SPAWN_EGG, "§c◆ Shoot Cave Spiders", "CAVE_SPIDER", "CAVE_SPIDER_UNIQUE_TAG"));
        inventory.setItem(3, createNamedItem(Material.CHICKEN_SPAWN_EGG, "§c◆ Shoot Chickens", "CHICKEN", "CHICKEN_UNIQUE_TAG"));
        inventory.setItem(4, createNamedItem(Material.COD_SPAWN_EGG, "§c◆ Shoot Cod", "COD", "COD_UNIQUE_TAG"));
        inventory.setItem(5, createNamedItem(Material.COW_SPAWN_EGG, "§c◆ Shoot Cows", "COW", "COW_UNIQUE_TAG"));
        inventory.setItem(6, createNamedItem(Material.CREEPER_SPAWN_EGG, "§c◆ Shoot Creepers", "CREEPER", "CREEPER_UNIQUE_TAG"));
        inventory.setItem(7, createNamedItem(Material.ENDERMAN_SPAWN_EGG, "§c◆ Shoot Endermen", "ENDERMAN", "ENDERMAN_UNIQUE_TAG"));
        inventory.setItem(8, createNamedItem(Material.GLOW_SQUID_SPAWN_EGG, "§c◆ Shoot Glow Squids", "GLOW_SQUID", "GLOW_SQUID_UNIQUE_TAG"));
        inventory.setItem(9, createNamedItem(Material.MAGMA_CUBE_SPAWN_EGG, "§c◆ Shoot Magma Cubes", "MAGMA_CUBE", "MAGMA_CUBE_UNIQUE_TAG"));
        inventory.setItem(10, createNamedItem(Material.MOOSHROOM_SPAWN_EGG, "§c◆ Shoot Mooshrooms", "MUSHROOM_COW", "MUSHROOM_COW_UNIQUE_TAG"));
        inventory.setItem(11, createNamedItem(Material.PIG_SPAWN_EGG, "§c◆ Shoot Pigs", "PIG", "PIG_UNIQUE_TAG"));
        inventory.setItem(12, createNamedItem(Material.PUFFERFISH_SPAWN_EGG, "§c◆ Shoot Pufferfish", "PUFFERFISH", "PUFFERFISH_UNIQUE_TAG"));
        inventory.setItem(13, createNamedItem(Material.RABBIT_SPAWN_EGG, "§c◆ Shoot Rabbits", "RABBIT", "RABBIT_UNIQUE_TAG"));
        inventory.setItem(14, createNamedItem(Material.SHEEP_SPAWN_EGG, "§c◆ Shoot Sheep", "SHEEP", "SHEEP_UNIQUE_TAG"));
        inventory.setItem(15, createNamedItem(Material.SKELETON_SPAWN_EGG, "§c◆ Shoot Skeletons", "SKELETON", "SKELETON_UNIQUE_TAG"));
        inventory.setItem(16, createNamedItem(Material.SLIME_SPAWN_EGG, "§c◆ Shoot Slimes", "SLIME", "SLIME_UNIQUE_TAG"));
        inventory.setItem(17, createNamedItem(Material.SPIDER_SPAWN_EGG, "§c◆ Shoot Spiders", "SPIDER", "SPIDER_UNIQUE_TAG"));
        inventory.setItem(18, createNamedItem(Material.SQUID_SPAWN_EGG, "§c◆ Shoot Squids", "SQUID", "SQUID_UNIQUE_TAG"));
        inventory.setItem(19, createNamedItem(Material.TROPICAL_FISH_SPAWN_EGG, "§c◆ Shoot Tropical Fish", "TROPICAL_FISH", "TROPICAL_FISH_UNIQUE_TAG"));
        inventory.setItem(20, createNamedItem(Material.WITCH_SPAWN_EGG, "§c◆ Shoot Witches", "WITCH", "WITCH_UNIQUE_TAG"));
        inventory.setItem(21, createNamedItem(Material.WITHER_SKELETON_SPAWN_EGG, "§c◆ Shoot Wither Skeletons", "WITHER_SKELETON", "WITHER_SKELETON_UNIQUE_TAG"));
        inventory.setItem(22, createNamedItem(Material.WOLF_SPAWN_EGG, "§c◆ Shoot Wolves", "WOLF", "WOLF_UNIQUE_TAG"));
        inventory.setItem(23, createNamedItem(Material.ZOGLIN_SPAWN_EGG, "§c◆ Shoot Zoglins", "ZOGLIN", "ZOGLIN_UNIQUE_TAG"));
        inventory.setItem(24, createNamedItem(Material.ZOMBIE_SPAWN_EGG, "§c◆ Shoot Zombies", "ZOMBIE", "ZOMBIE_UNIQUE_TAG"));
        inventory.setItem(25, createNamedItem(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, "§c◆ Shoot Zombified Piglins", "ZOMBIFIED_PIGLIN", "ZOMBIFIED_PIGLIN_UNIQUE_TAG"));
        inventory.setItem(26, createNamedItem(Material.TNT, "§c◆ Shoot TNT!", "PRIMED_TNT", "TNT_UNIQUE_TAG"));

        player.openInventory(inventory);
    }

    private ItemStack createNamedItem(Material material, String displayName, String metadataValue, String uniqueTag) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + displayName);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING, metadataValue);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "uniqueTag"), PersistentDataType.STRING, uniqueTag);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();

            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING)) {
                String entityTypeString = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING);

                assert entityTypeString != null;
                selectedEntityType = EntityType.valueOf(entityTypeString.toUpperCase());
                player.closeInventory();
                player.sendMessage("§a◆ Selected: §c" + entityTypeString);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (!bow.isSimilar(funBowItemListener.getFunBowItem())) return;

        if (selectedEntityType != null) {
            String entityTag = selectedEntityType.toString();
            int maxEntities = 10;
            int radius = 160;

            Location shootLocation = event.getProjectile().getLocation();
            int count = 0;

            for (Entity entity : Objects.requireNonNull(shootLocation.getWorld()).getNearbyEntities(shootLocation, radius, radius, radius)) {
                if (entity.getType() == selectedEntityType && entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "entityTag"), PersistentDataType.STRING)) {
                    String tag = entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "entityTag"), PersistentDataType.STRING);
                    if (entityTag.equals(tag)) {
                        count++;
                    }
                }
            }

            if (count < maxEntities) {
                Entity entity = player.getWorld().spawnEntity(shootLocation, selectedEntityType);
                entity.setVelocity(event.getProjectile().getVelocity());
                entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "entityTag"), PersistentDataType.STRING, entityTag); // Asignar el tag único a la entidad
            } else {
                player.sendMessage(ChatColor.RED + "§c◆ Too many §2" + selectedEntityType.toString() + " §centities in this area.");
            }
        }
        event.setCancelled(true);
    }
}
