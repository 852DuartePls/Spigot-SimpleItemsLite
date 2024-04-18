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

        if (itemMeta != null) {
            File magicwaterConfigFile = new File(plugin.getDataFolder(), "MagicWaterItem.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(magicwaterConfigFile);

            if (config.contains("MagicWaterItem")) {
                ConfigurationSection magicWaterItemSection = config.getConfigurationSection("MagicWaterItem");
                if (magicWaterItemSection != null) {
                    String displayName = magicWaterItemSection.getString("DisplayName");
                    assert displayName != null;
                    displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                    itemMeta.setDisplayName(displayName);
                    if (magicWaterItemSection.contains("Lore")) {
                        List<String> lore = magicWaterItemSection.getStringList("Lore");
                        lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                        itemMeta.setLore(lore);
                        itemMeta.setUnbreakable(true);
                    }
                    if (magicWaterItemSection.contains("Enchantments")) {
                        ConfigurationSection enchantmentsSection = magicWaterItemSection.getConfigurationSection("Enchantments");
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
                    magicWaterItem.setItemMeta(itemMeta);
                    plugin.getLogger().info("MagicWaterItem config loaded correctly");
                    plugin.getLogger().info("Material: " + magicWaterItem.getType());
                    plugin.getLogger().info("DisplayName: " + magicWaterItem.getItemMeta().getDisplayName());
                    plugin.getLogger().info("Lore: " + magicWaterItem.getItemMeta().getLore());
                    for (Map.Entry<Enchantment, Integer> entry : magicWaterItem.getEnchantments().entrySet()) {
                        plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
                    }

                    ItemStack translatedItem = new ItemStack(magicWaterItem);
                    ItemMeta translatedMeta = translatedItem.getItemMeta();
                    if (translatedMeta != null) {
                        translatedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translatedMeta.getDisplayName()));
                        List<String> translatedLore = translatedMeta.getLore();
                        if (translatedLore != null) {
                            translatedLore = translatedLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                            translatedMeta.setLore(translatedLore);
                        }
                        translatedItem.setItemMeta(translatedMeta);
                        magicWaterItem = translatedItem;
                    }
                } else {
                    plugin.getLogger().warning("Error creating ItemMeta for MagicWaterItem");
                }
            } else {
                plugin.getLogger().warning("Material not valid for MagicWaterItem");
            }
        } else {
            plugin.getLogger().warning("Error creating ItemMeta for MagicWaterItem");
        }
    }
}