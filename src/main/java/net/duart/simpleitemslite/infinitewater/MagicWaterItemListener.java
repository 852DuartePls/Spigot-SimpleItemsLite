package net.duart.simpleitemslite.infinitewater;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MagicWaterItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack magicWaterItem;

    public MagicWaterItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMagicWaterItemFromConfig();
    }

    public ItemStack getMagicWaterItem() {
        return magicWaterItem;
    }

    public void loadMagicWaterItemFromConfig() {
        Material material = Material.WATER_BUCKET;
        magicWaterItem = new ItemStack(material);
        ItemMeta itemMeta = magicWaterItem.getItemMeta();

        if (itemMeta == null) {
            plugin.getLogger().warning("Error creating ItemMeta for MagicWaterItem");
            return;
        }

        File magicwaterConfigFile = new File(plugin.getDataFolder(), "MagicWaterItem.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(magicwaterConfigFile);

        if (!config.contains("MagicWaterItem")) {
            plugin.getLogger().warning("Section 'MagicWaterItem' not found in file '" + magicwaterConfigFile.getName() + "'.");
            return;
        }

        ConfigurationSection magicWaterItemSection = config.getConfigurationSection("MagicWaterItem");
        if (magicWaterItemSection == null) {
            plugin.getLogger().warning("Error creating ItemMeta for MagicWaterItem");
            return;
        }

        String displayName = magicWaterItemSection.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("Display name not defined for MagicWaterItem in file '" + magicwaterConfigFile.getName() + "'.");
            return;
        }

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = magicWaterItemSection.getStringList("Lore");
        if (!lore.isEmpty()) {
            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            itemMeta.setLore(lore);
        }

        itemMeta.setUnbreakable(true);
        magicWaterItem.setItemMeta(itemMeta);

        if (magicWaterItemSection.contains("Enchantments")) {
            ConfigurationSection enchantmentsSection = magicWaterItemSection.getConfigurationSection("Enchantments");
            if (enchantmentsSection != null) {
                for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                    NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentKey);
                    Enchantment enchantment = Enchantment.getByKey(namespacedKey);
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(enchantmentKey);
                        magicWaterItem.addUnsafeEnchantment(enchantment, level);
                    } else {
                        plugin.getLogger().warning("Invalid enchantment '" + enchantmentKey + "' for MagicWaterItem in file '" + magicwaterConfigFile.getName() + "'.");
                    }
                }
            }
        }

        plugin.getLogger().info("MagicWaterItem config loaded correctly");
        plugin.getLogger().info("Material: " + magicWaterItem.getType());
        plugin.getLogger().info("DisplayName: " + magicWaterItem.getItemMeta().getDisplayName());
        List<String> itemLore = magicWaterItem.getItemMeta().getLore();
        if (itemLore != null) {
            plugin.getLogger().info("Lore: " + itemLore);
        }
        for (Map.Entry<Enchantment, Integer> entry : magicWaterItem.getEnchantments().entrySet()) {
            plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
        }
    }
}