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
        File LightningItemFile = new File(plugin.getDataFolder(), "LightningItem.yml");
        if (!LightningItemFile.exists()) {
            plugin.saveResource("LightningItem.yml", false);
        }
        FileConfiguration LightningItemConfig = YamlConfiguration.loadConfiguration(LightningItemFile);
        if (LightningItemConfig.contains("LightningItem")) {
            ConfigurationSection LightningItemSection = LightningItemConfig.getConfigurationSection("LightningItem");
            if (LightningItemSection != null) {
                String materialName = LightningItemSection.getString("Material");
                if (materialName != null && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material != null) {
                        LightningItem = new ItemStack(material);
                        ItemMeta itemMeta = LightningItem.getItemMeta();
                        if (itemMeta == null) {
                            plugin.getLogger().warning("Error creating ItemMeta for LightningItem");
                            return;
                        }
                        String displayName = LightningItemSection.getString("DisplayName");
                        assert displayName != null;
                        displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                        itemMeta.setDisplayName(displayName);
                        if (LightningItemSection.contains("Lore")) {
                            List<String> lore = LightningItemSection.getStringList("Lore");
                            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
                            itemMeta.setLore(lore);
                            itemMeta.setUnbreakable(true);
                            if (LightningItemSection.contains("Enchantments")) {
                                ConfigurationSection enchantmentsSection = LightningItemSection.getConfigurationSection("Enchantments");
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
                            LightningItem.setItemMeta(itemMeta);
                            plugin.getLogger().info("LightningItem config loaded correctly");
                            plugin.getLogger().info("Material: " + LightningItem.getType());
                            plugin.getLogger().info("DisplayName: " + LightningItem.getItemMeta().getDisplayName());
                            plugin.getLogger().info("Lore: " + LightningItem.getItemMeta().getLore());
                            for (Map.Entry<Enchantment, Integer> entry : LightningItem.getEnchantments().entrySet()) {
                                plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Material not valid for LightningItem");
                    }
                }
            }
        }
    }
}