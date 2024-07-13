package net.duart.simpleitemslite.lightningaura;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LightningItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack LightningItem;

    public LightningItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLightningItemFromConfig();
    }

    public ItemStack getLightningItem() {
        return LightningItem;
    }

    public boolean playerHasLightningItem(Player player) {
        Inventory playerInventory = player.getInventory();
        ItemStack[] contents = playerInventory.getContents();
        boolean hasLightningItem = false;

        for (ItemStack item : contents) {
            if (item != null && item.isSimilar(LightningItem)) {
                hasLightningItem = true;
                break;
            }
        }

        return hasLightningItem;
    }

    public void loadLightningItemFromConfig() {
        File lightningItemFile = new File(plugin.getDataFolder(), "LightningItem.yml");
        if (!lightningItemFile.exists()) {
            plugin.saveResource("LightningItem.yml", false);
        }

        FileConfiguration lightningItemConfig = YamlConfiguration.loadConfiguration(lightningItemFile);
        if (!lightningItemConfig.contains("LightningItem")) {
            plugin.getLogger().warning("Configuration section 'LightningItem' not found in LightningItem.yml");
            return;
        }

        ConfigurationSection lightningItemSection = lightningItemConfig.getConfigurationSection("LightningItem");
        if (lightningItemSection == null) {
            plugin.getLogger().warning("Error loading configuration section 'LightningItem' from LightningItem.yml");
            return;
        }

        String materialName = lightningItemSection.getString("Material");
        if (materialName == null || materialName.isEmpty()) {
            plugin.getLogger().warning("Material name not specified or invalid in LightningItem.yml");
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid material specified in LightningItem.yml: " + materialName);
            return;
        }

        LightningItem = new ItemStack(material);
        ItemMeta itemMeta = LightningItem.getItemMeta();
        if (itemMeta == null) {
            plugin.getLogger().warning("Error creating ItemMeta for LightningItem");
            return;
        }

        String displayName = lightningItemSection.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("Display name not specified for LightningItem");
        } else {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }

        if (lightningItemSection.contains("Lore")) {
            List<String> lore = lightningItemSection.getStringList("Lore");
            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            itemMeta.setLore(lore);
            itemMeta.setUnbreakable(true);
        }

        if (lightningItemSection.contains("Enchantments")) {
            ConfigurationSection enchantmentsSection = lightningItemSection.getConfigurationSection("Enchantments");
            if (enchantmentsSection != null) {
                for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                    NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentKey);
                    Enchantment enchantment = Enchantment.getByKey(namespacedKey);
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(enchantmentKey);
                        itemMeta.addEnchant(enchantment, level, true);
                    } else {
                        plugin.getLogger().warning("Invalid enchantment '" + enchantmentKey + "' for LightningItem");
                    }
                }
            }
        }

        LightningItem.setItemMeta(itemMeta);
        plugin.getLogger().info("LightningItem config loaded correctly");
        plugin.getLogger().info("Material: " + LightningItem.getType());
        plugin.getLogger().info("DisplayName: " + LightningItem.getItemMeta().getDisplayName());
        plugin.getLogger().info("Lore: " + LightningItem.getItemMeta().getLore());
        for (Map.Entry<Enchantment, Integer> entry : LightningItem.getEnchantments().entrySet()) {
            plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
        }
    }
}