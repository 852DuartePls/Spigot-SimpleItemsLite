package net.duart.simpleitemslite.funbow;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

import java.util.EnumMap;
import java.util.Objects;

public class FunBow implements Listener {
    private final JavaPlugin plugin;
    private final FunBowItemListener funBowItemListener;
    private EntityType selectedEntityType;
    private final EnumMap<EntityType, Material> spawnEggCache = new EnumMap<>(EntityType.class);
    private final EnumMap<EntityType, String> translationCacheString = new EnumMap<>(EntityType.class);

    public FunBow(JavaPlugin plugin, FunBowItemListener funBowItemListener) {
        this.plugin = plugin;
        this.funBowItemListener = funBowItemListener;
        this.selectedEntityType = null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!item.isSimilar(funBowItemListener.getFunBowItem())) {
            return;
        }

        if (event.getAction().toString().contains("LEFT_CLICK")) {
            openFunBowMenu(player);
        }
    }

    public void openFunBowMenu(Player player) {
        String[] entityNames = {
                "Blaze", "Cat", "Cave Spider", "Chicken", "Cod", "Cow", "Creeper", "Enderman", "Glow Squid",
                "Magma Cube", "Mooshroom", "Pig", "Pufferfish", "Rabbit", "Sheep", "Skeleton", "Slime",
                "Spider", "Squid", "Tropical Fish", "Witch", "Wither Skeleton", "Wolf", "Zoglin", "Zombie",
                "Zombified Piglin", "Primed TNT"
        };

        int inventorySize = 27;
        String inventoryTitle = ChatColor.BLUE + "◆ FunBow Menu";
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryTitle);

        int slot = 0;
        for (String entityName : entityNames) {
            EntityType entityType = getEntityTypeByName(entityName);
            if (entityType == null) {
                continue;
            }

            Material spawnEggType = getSpawnEgg(entityType);
            if (spawnEggType == null && entityType != EntityType.PRIMED_TNT) {
                continue;
            }

            ItemStack item;
            if (entityType == EntityType.PRIMED_TNT) {
                item = new ItemStack(Material.TNT);
            } else {
                item = new ItemStack(spawnEggType, 1);
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            String displayName = getTranslation(entityType);
            meta.setDisplayName(displayName);


            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING, entityType.name());
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "uniqueTag"), PersistentDataType.STRING, entityType.getTranslationKey() + "_UNIQUE_TAG");
            item.setItemMeta(meta);

            if (slot < inventorySize) {
                inventory.setItem(slot, item);
                slot++;
            } else {
                break;
            }
        }

        player.openInventory(inventory);
    }

    private EntityType getEntityTypeByName(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Material getSpawnEgg(EntityType entityType) {
        return spawnEggCache.computeIfAbsent(entityType, type -> {
            try {
                return Material.valueOf(type.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    private String getTranslation(EntityType entityType) {
        return translationCacheString.computeIfAbsent(entityType, type -> ChatColor.RED + "◆ Shoot " + type.name());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        String displayName = Objects.requireNonNull(event.getView().getTitle());
        if (!displayName.contains("◆ FunBow Menu")) {
            return;
        }

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING)) {
            return;
        }

        String entityTypeString = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "entityType"), PersistentDataType.STRING);
        if (entityTypeString == null) {
            return;
        }

        selectedEntityType = EntityType.valueOf(entityTypeString.toUpperCase());
        String message = ChatColor.GREEN + "◆ Selected: " +
                ChatColor.RED + entityTypeString;

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
        player.closeInventory();
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (!bow.isSimilar(funBowItemListener.getFunBowItem())) {
            return;
        }

        if (selectedEntityType == null) {
            event.setCancelled(true);
            return;
        }

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

        if (count >= maxEntities) {
            String message = ChatColor.RED + "◆ Too many " +
                    ChatColor.GREEN + selectedEntityType.toString() +
                    ChatColor.RED + " entities in this area.";

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO , 1.0f, 1.0f);
            event.setCancelled(true);
            return;
        }

        Entity entity = player.getWorld().spawnEntity(shootLocation, selectedEntityType);
        entity.setVelocity(event.getProjectile().getVelocity());
        entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "entityTag"), PersistentDataType.STRING, entityTag);
        event.setCancelled(true);
    }
}
