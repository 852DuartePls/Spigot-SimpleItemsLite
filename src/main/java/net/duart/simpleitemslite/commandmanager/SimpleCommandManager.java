package net.duart.simpleitemslite.commandmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.util.*;

public class SimpleCommandManager implements CommandExecutor, TabCompleter {
    private final Plugin plugin;


    public SimpleCommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return true;
        }


        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /simpleitemslite give (item)");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /simpleitemslite give (item)");
                return true;
            }

            Player player = (Player) sender;
            String itemName = args[1].toLowerCase();

            File dataFolder = plugin.getDataFolder();
            if (dataFolder.exists()) {
                File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        String fileNameWithoutExtension = file.getName().replace(".yml", "").toLowerCase();
                        if (itemName.equals(fileNameWithoutExtension)) {
                            ItemStack itemStack = loadItemFromYAML(file, itemName);
                            if (itemStack != null) {
                                player.getInventory().addItem(itemStack);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eSimpleItemsLite&6] &aYou have received the item:&2 " + itemName));
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eSimpleItemsLite&6] &cError loading the YAML file"));
                                return true;
                            }
                        }
                    }
                }
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[&eSimpleItemsLite&6] &cAn item with this name was not found."));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Correct usage: /simpleitemslite give (item)");
        return true;

    }

    private ItemStack loadItemFromYAML(File file, String itemName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        boolean sectionFound = false;
        for (String key : config.getKeys(false)) {
            if (key.equalsIgnoreCase(itemName)) {
                sectionFound = true;
                itemName = key;
                break;
            }
        }

        if (!sectionFound) {
            Bukkit.getLogger().warning("Section '" + itemName + "' not found in file '" + file.getName() + "'.");
            return null;
        }

        Material material;
        if (itemName.equalsIgnoreCase("MagicWaterItem")) {
            material = Material.WATER_BUCKET;
        } else if (itemName.equalsIgnoreCase("FunBowItem")) {
            material = Material.BOW;
        } else {
            String materialName = config.getString(itemName + ".Material");
            assert materialName != null;
            material = Material.matchMaterial(materialName);
            if (material == null) {
                Bukkit.getLogger().warning("Invalid material for item '" + itemName + "' in file '" + file.getName() + "'. Material: " + materialName);
                return null;
            }
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            Bukkit.getLogger().warning("Could not get item meta for '" + itemName + "'.");
            return null;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(itemName + ".DisplayName"))));
        List<String> lore = config.getStringList(itemName + ".Lore");
        List<String> translatedLore = new ArrayList<>();
        for (String loreLine : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        }
        meta.setLore(translatedLore);
        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);

        if (config.isConfigurationSection(itemName + ".Enchantments")) {
            Map<String, Object> enchantmentsConfig = Objects.requireNonNull(config.getConfigurationSection(itemName + ".Enchantments")).getValues(false);
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            for (Map.Entry<String, Object> entry : enchantmentsConfig.entrySet()) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey()));
                if (enchantment != null && entry.getValue() instanceof Integer) {
                    enchantments.put(enchantment, (Integer) entry.getValue());
                } else {
                    Bukkit.getLogger().warning("Error loading enchantment '" + entry.getKey() + "' for item '" + itemName + "'. Invalid value: " + entry.getValue());
                }
            }

            if (!enchantments.isEmpty()) {
                itemStack.addUnsafeEnchantments(enchantments);
            }
        }

        return itemStack;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            File dataFolder = plugin.getDataFolder();
            if (dataFolder.exists()) {
                File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        completions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        }
        return completions;
    }
}