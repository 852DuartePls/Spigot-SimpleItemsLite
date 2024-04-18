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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JumpItemListener implements Listener {
    private final JavaPlugin plugin;
    private ItemStack jumpItem;
    private boolean jumpItemWarningSent;

    public JumpItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadJumpItemFromConfig();
    }

    public boolean playerHasJumpItem(Player player) {
        Inventory playerInventory = player.getInventory();
        ItemStack[] contents = playerInventory.getContents();
        boolean hasJumpItem = false;

        for (ItemStack item : contents) {
            if (item != null && item.isSimilar(jumpItem)) {
                hasJumpItem = true;
                break;
            }
        }

        if (!hasJumpItem) {
            if (!jumpItemWarningSent) {
                jumpItemWarningSent = true;
            }
            player.setFlying(false);
            player.setAllowFlight(false);
        } else {
            if (jumpItemWarningSent) {
                player.sendMessage(ChatColor.GREEN + "¡Doble Salto activado!");
                jumpItemWarningSent = false;
            }
        }

        return hasJumpItem;
    }

    public void loadJumpItemFromConfig() {
        File jumpItemFile = new File(plugin.getDataFolder(), "JumpItem.yml");
        if (!jumpItemFile.exists()) {
            plugin.saveResource("JumpItem.yml", false);
        }
        FileConfiguration jumpItemConfig = YamlConfiguration.loadConfiguration(jumpItemFile);
        if (jumpItemConfig.contains("JumpItem")) {
            ConfigurationSection jumpItemSection = jumpItemConfig.getConfigurationSection("JumpItem");
            if (jumpItemSection != null) {
                String materialName = jumpItemSection.getString("Material");
                if (materialName != null && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName.toUpperCase());
                    if (material != null) {
                        jumpItem = new ItemStack(material);
                        ItemMeta itemMeta = jumpItem.getItemMeta();
                        if (itemMeta == null) {
                            plugin.getLogger().warning("Error creating ItemMeta for JumpItem");
                            return;
                        }
                        String displayName = jumpItemSection.getString("DisplayName");
                        assert displayName != null;
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
                        }
                    } else {
                        plugin.getLogger().warning("Material not valid for JumpItem");
                    }
                }
            }
        }
    }
}