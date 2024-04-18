package net.duart.simpleitemslite.funbow;

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

public class FunBowItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack funBowItem;

    public FunBowItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFunBowItemFromConfig();
    }

    public ItemStack getFunBowItem() {
        return funBowItem;
    }

    public void loadFunBowItemFromConfig() {
        Material material = Material.BOW;
        funBowItem = new ItemStack(material);
        ItemMeta itemMeta = funBowItem.getItemMeta();

        if (itemMeta != null) {
            File magicwaterConfigFile = new File(plugin.getDataFolder(), "FunBowItem.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(magicwaterConfigFile);

            if (config.contains("FunBowItem")) {
                ConfigurationSection funBowItemSection = config.getConfigurationSection("FunBowItem");
                if (funBowItemSection != null) {
                    String displayName = funBowItemSection.getString("DisplayName");
                    assert displayName != null;
                    displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                    itemMeta.setDisplayName(displayName);
                    if (funBowItemSection.contains("Lore")) {
                        List<String> lore = funBowItemSection.getStringList("Lore");
                        lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                        itemMeta.setLore(lore);
                        itemMeta.setUnbreakable(true);
                    }
                    if (funBowItemSection.contains("Enchantments")) {
                        ConfigurationSection enchantmentsSection = funBowItemSection.getConfigurationSection("Enchantments");
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
                    funBowItem.setItemMeta(itemMeta);
                    plugin.getLogger().info("FunBowItem config loaded correctly");
                    plugin.getLogger().info("Material: " + funBowItem.getType());
                    plugin.getLogger().info("DisplayName: " + funBowItem.getItemMeta().getDisplayName());
                    plugin.getLogger().info("Lore: " + funBowItem.getItemMeta().getLore());
                    for (Map.Entry<Enchantment, Integer> entry : funBowItem.getEnchantments().entrySet()) {
                        plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
                    }

                    ItemStack translatedItem = new ItemStack(funBowItem);
                    ItemMeta translatedMeta = translatedItem.getItemMeta();
                    if (translatedMeta != null) {
                        translatedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', translatedMeta.getDisplayName()));
                        List<String> translatedLore = translatedMeta.getLore();
                        if (translatedLore != null) {
                            translatedLore = translatedLore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                            translatedMeta.setLore(translatedLore);
                        }
                        translatedItem.setItemMeta(translatedMeta);
                        funBowItem = translatedItem;
                    }
                } else {
                    plugin.getLogger().warning("Error creating ItemMeta for FunBowItem");
                }
            } else {
                plugin.getLogger().warning("Material not valid for FunBowItem");
            }
        } else {
            plugin.getLogger().warning("Error creating ItemMeta for FunBowItem");
        }
    }
}