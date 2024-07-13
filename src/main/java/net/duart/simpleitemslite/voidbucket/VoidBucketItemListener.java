package net.duart.simpleitemslite.voidbucket;

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

public class VoidBucketItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack voidBucketItem;

    public VoidBucketItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadVoidBucketItemFromConfig();
    }

    public ItemStack getVoidBucketItem() {
        return voidBucketItem;
    }

    public void loadVoidBucketItemFromConfig() {
        voidBucketItem = new ItemStack(Material.BUCKET);
        ItemMeta itemMeta = voidBucketItem.getItemMeta();

        if (itemMeta == null) {
            plugin.getLogger().warning("Error creating ItemMeta for VoidBucketItem");
            return;
        }

        File voidbucketConfigFile = new File(plugin.getDataFolder(), "VoidBucketItem.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(voidbucketConfigFile);

        if (!config.contains("VoidBucketItem")) {
            plugin.getLogger().warning("VoidBucketItem configuration section not found.");
            return;
        }

        ConfigurationSection voidBucketItemSection = config.getConfigurationSection("VoidBucketItem");
        if (voidBucketItemSection == null) {
            plugin.getLogger().warning("Invalid configuration section for VoidBucketItem.");
            return;
        }

        String displayName = voidBucketItemSection.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("DisplayName not specified for VoidBucketItem.");
            return;
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        if (voidBucketItemSection.contains("Lore")) {
            List<String> lore = voidBucketItemSection.getStringList("Lore");
            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            itemMeta.setLore(lore);
            itemMeta.setUnbreakable(true);
        }

        if (voidBucketItemSection.contains("Enchantments")) {
            ConfigurationSection enchantmentsSection = voidBucketItemSection.getConfigurationSection("Enchantments");
            if (enchantmentsSection != null) {
                for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                    NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentKey);
                    Enchantment enchantment = Enchantment.getByKey(namespacedKey);
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(enchantmentKey);
                        itemMeta.addEnchant(enchantment, level, true);
                    }
                }
            }
        }

        voidBucketItem.setItemMeta(itemMeta);

        plugin.getLogger().info("VoidBucketItem config loaded correctly");
        plugin.getLogger().info("Material: " + voidBucketItem.getType());
        plugin.getLogger().info("DisplayName: " + voidBucketItem.getItemMeta().getDisplayName());
        plugin.getLogger().info("Lore: " + voidBucketItem.getItemMeta().getLore());
        for (Map.Entry<Enchantment, Integer> entry : voidBucketItem.getEnchantments().entrySet()) {
            plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
        }

        ItemStack translatedItem = new ItemStack(voidBucketItem);
        ItemMeta translatedMeta = translatedItem.getItemMeta();
        if (translatedMeta != null) {
            translatedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translatedMeta.getDisplayName()));
            List<String> translatedLore = translatedMeta.getLore();
            if (translatedLore != null) {
                translatedLore = translatedLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                translatedMeta.setLore(translatedLore);
            }
            translatedItem.setItemMeta(translatedMeta);
            voidBucketItem = translatedItem;
        } else {
            plugin.getLogger().warning("Error creating ItemMeta for translated VoidBucketItem.");
        }
    }
}