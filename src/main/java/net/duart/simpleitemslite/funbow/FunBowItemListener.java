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
        File configFile = new File(plugin.getDataFolder(), "FunBowItem.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains("FunBowItem")) {
            plugin.getLogger().warning("FunBowItem configuration missing or invalid.");
            return;
        }

        ConfigurationSection itemConfig = config.getConfigurationSection("FunBowItem");
        if (itemConfig == null) {
            plugin.getLogger().warning("Error reading FunBowItem configuration section.");
            return;
        }

        Material material = Material.matchMaterial(itemConfig.getString("Material", "BOW"));
        if (material == null) {
            plugin.getLogger().warning("Invalid material specified for FunBowItem.");
            return;
        }

        ItemStack newItemStack = new ItemStack(material);
        ItemMeta meta = newItemStack.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("Failed to create ItemMeta for FunBowItem.");
            return;
        }

        String displayName = itemConfig.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("Missing display name for FunBowItem.");
            return;
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = itemConfig.getStringList("Lore");
        if (!lore.isEmpty()) {
            lore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }

        meta.setUnbreakable(true);

        ConfigurationSection enchantmentsSection = itemConfig.getConfigurationSection("Enchantments");
        if (enchantmentsSection != null) {
            for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, enchantmentsSection.getInt(enchantmentKey), true);
                } else {
                    plugin.getLogger().warning("Invalid enchantment '" + enchantmentKey + "' for FunBowItem.");
                }
            }
        }

        newItemStack.setItemMeta(meta);
        funBowItem = newItemStack;

        plugin.getLogger().info("FunBowItem configuration loaded successfully:");
        plugin.getLogger().info("Material: " + funBowItem.getType());

        if (funBowItem.hasItemMeta()) {
            ItemMeta itemMeta = funBowItem.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                plugin.getLogger().info("DisplayName: " + itemMeta.getDisplayName());
            } else {
                plugin.getLogger().warning("No valid DisplayName found for FunBowItem.");
            }

            if (itemMeta != null && itemMeta.hasLore()) {
                List<String> itemLore = itemMeta.getLore();
                if (itemLore != null && !itemLore.isEmpty()) {
                    plugin.getLogger().info("Lore: " + itemLore);
                } else {
                    plugin.getLogger().warning("No valid Lore found for FunBowItem.");
                }
            } else {
                plugin.getLogger().warning("No Lore found for FunBowItem.");
            }

            funBowItem.getEnchantments().forEach((enchantment, level) ->
                    plugin.getLogger().info("Enchantment: " + enchantment.getKey() + ", Level: " + level));
        } else {
            plugin.getLogger().warning("No ItemMeta found for FunBowItem.");
        }
    }
}