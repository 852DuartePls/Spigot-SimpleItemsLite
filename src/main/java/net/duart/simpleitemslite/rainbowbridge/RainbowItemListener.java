package net.duart.simpleitemslite.rainbowbridge;

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

public class RainbowItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack rainbowBridgeItem;

    public RainbowItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRainbowBridgeItemFromConfig();
    }

    public ItemStack getRainbowBridgeItem() {
        return rainbowBridgeItem;
    }

    public void loadRainbowBridgeItemFromConfig() {
        File rainbowConfigFile = new File(plugin.getDataFolder(), "RainbowBridgeItem.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(rainbowConfigFile);

        if (!config.contains("RainbowBridgeItem")) {
            plugin.getLogger().warning("RainbowBridgeItem configuration section not found.");
            return;
        }

        ConfigurationSection rainbowBridgeItemSection = config.getConfigurationSection("RainbowBridgeItem");
        if (rainbowBridgeItemSection == null) {
            plugin.getLogger().warning("Invalid configuration section for RainbowBridgeItem.");
            return;
        }

        String materialName = rainbowBridgeItemSection.getString("Material");
        if (materialName == null || materialName.isEmpty()) {
            plugin.getLogger().warning("Material name not specified for RainbowBridgeItem.");
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid material specified for RainbowBridgeItem: " + materialName);
            return;
        }

        rainbowBridgeItem = new ItemStack(material); // Asignación al campo de clase
        ItemMeta itemMeta = rainbowBridgeItem.getItemMeta();
        if (itemMeta == null) {
            plugin.getLogger().warning("Failed to create ItemMeta for RainbowBridgeItem.");
            return;
        }

        String displayName = rainbowBridgeItemSection.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("DisplayName not specified for RainbowBridgeItem.");
            return;
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        if (rainbowBridgeItemSection.contains("Lore")) {
            List<String> lore = rainbowBridgeItemSection.getStringList("Lore");
            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            itemMeta.setLore(lore);
        }

        if (rainbowBridgeItemSection.contains("Enchantments")) {
            ConfigurationSection enchantmentsSection = rainbowBridgeItemSection.getConfigurationSection("Enchantments");
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

        itemMeta.setUnbreakable(true);
        rainbowBridgeItem.setItemMeta(itemMeta);

        plugin.getLogger().info("RainbowBridgeItem config loaded correctly");
        plugin.getLogger().info("Material: " + rainbowBridgeItem.getType());
        plugin.getLogger().info("DisplayName: " + rainbowBridgeItem.getItemMeta().getDisplayName());
        plugin.getLogger().info("Lore: " + rainbowBridgeItem.getItemMeta().getLore());
        for (Map.Entry<Enchantment, Integer> entry : rainbowBridgeItem.getEnchantments().entrySet()) {
            plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
        }

        ItemStack translatedItem = new ItemStack(rainbowBridgeItem);
        ItemMeta translatedMeta = translatedItem.getItemMeta();
        if (translatedMeta != null) {
            translatedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translatedMeta.getDisplayName()));
            List<String> translatedLore = translatedMeta.getLore();
            if (translatedLore != null) {
                translatedLore = translatedLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                translatedMeta.setLore(translatedLore);
            }
            translatedItem.setItemMeta(translatedMeta);
            rainbowBridgeItem = translatedItem; // Asignación al campo de clase
        } else {
            plugin.getLogger().warning("Error creating ItemMeta for translated RainbowBridgeItem.");
        }
    }
}
