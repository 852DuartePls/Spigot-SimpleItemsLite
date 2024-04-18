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

        if (config.contains("RainbowBridgeItem")) {
            ConfigurationSection rainbowBridgeItemSection = config.getConfigurationSection("RainbowBridgeItem");
            if (rainbowBridgeItemSection != null) {
                String materialName = rainbowBridgeItemSection.getString("Material");
                if (materialName != null && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material != null) {
                        rainbowBridgeItem = new ItemStack(material);
                        ItemMeta itemMeta = rainbowBridgeItem.getItemMeta();
                        if (itemMeta != null) {
                            String displayName = rainbowBridgeItemSection.getString("DisplayName");
                            assert displayName != null;
                            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                            itemMeta.setDisplayName(displayName);
                            if (rainbowBridgeItemSection.contains("Lore")) {
                                List<String> lore = rainbowBridgeItemSection.getStringList("Lore");
                                lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                                itemMeta.setLore(lore);
                                itemMeta.setUnbreakable(true);
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
                                rainbowBridgeItem = translatedItem;
                            }
                        } else {
                            plugin.getLogger().warning("Error creating ItemMeta for RainbowBridgeItem");
                        }
                    } else {
                        plugin.getLogger().warning("Material not valid for RainbowBridgeItem");
                    }
                }
            }
        }
    }
}
