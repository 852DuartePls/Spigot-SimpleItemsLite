package net.duart.simpleitemslite.doublejump;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JumpItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack jumpItem;

    public JumpItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadJumpItemFromConfig();
    }

    public boolean playerHasJumpItem(Player player) {
        Inventory playerInventory = player.getInventory();
        return Arrays.stream(playerInventory.getContents())
                .anyMatch(item -> item != null && item.isSimilar(jumpItem));
    }

    public void loadJumpItemFromConfig() {
        File jumpItemFile = new File(plugin.getDataFolder(), "JumpItem.yml");
        if (!jumpItemFile.exists()) {
            plugin.saveResource("JumpItem.yml", false);
        }

        FileConfiguration jumpItemConfig = YamlConfiguration.loadConfiguration(jumpItemFile);
        if (!jumpItemConfig.contains("JumpItem")) {
            plugin.getLogger().warning("JumpItem config section not found");
            return;
        }

        ConfigurationSection jumpItemSection = jumpItemConfig.getConfigurationSection("JumpItem");
        if (jumpItemSection == null) {
            plugin.getLogger().warning("JumpItem section is empty");
            return;
        }

        String materialName = jumpItemSection.getString("Material");
        if (materialName == null || materialName.isEmpty()) {
            plugin.getLogger().warning("Missing or invalid Material in JumpItem config");
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Material not valid for JumpItem: " + materialName);
            return;
        }

        jumpItem = new ItemStack(material);
        ItemMeta itemMeta = jumpItem.getItemMeta();
        if (itemMeta == null) {
            plugin.getLogger().warning("Error creating ItemMeta for JumpItem");
            return;
        }

        String displayName = jumpItemSection.getString("DisplayName");
        if (displayName == null) {
            plugin.getLogger().warning("Missing DisplayName in JumpItem config");
            return;
        }
        displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        itemMeta.setDisplayName(displayName);

        if (jumpItemSection.contains("Lore")) {
            List<String> lore = jumpItemSection.getStringList("Lore");
            lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            itemMeta.setLore(lore);
            itemMeta.setUnbreakable(true);

            if (jumpItemSection.contains("Enchantments")) {
                ConfigurationSection enchantmentsSection = jumpItemSection.getConfigurationSection("Enchantments");
                if (enchantmentsSection != null) {
                    for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                        NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentKey);
                        Enchantment enchantment = Enchantment.getByKey(namespacedKey);
                        if (enchantment != null) {
                            int level = enchantmentsSection.getInt(enchantmentKey);
                            itemMeta.addEnchant(enchantment, level, true);
                        } else {
                            plugin.getLogger().warning("Invalid Enchantment '" + enchantmentKey + "' in JumpItem config");
                        }
                    }
                }
            }

            jumpItem.setItemMeta(itemMeta);
            plugin.getLogger().info("JumpItem config loaded correctly");
            plugin.getLogger().info("Material: " + jumpItem.getType());
            plugin.getLogger().info("DisplayName: " + jumpItem.getItemMeta().getDisplayName());
            plugin.getLogger().info("Lore: " + jumpItem.getItemMeta().getLore());
            for (Map.Entry<Enchantment, Integer> entry : jumpItem.getEnchantments().entrySet()) {
                plugin.getLogger().info("Enchantment: " + entry.getKey().getKey() + ", Level: " + entry.getValue());
            }
        } else {
            plugin.getLogger().warning("Missing Lore in JumpItem config");
        }
    }
}