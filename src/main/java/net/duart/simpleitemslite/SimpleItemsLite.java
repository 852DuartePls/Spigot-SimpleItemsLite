package net.duart.simpleitemslite;

import net.duart.simpleitemslite.commandmanager.SimpleCommandManager;
import net.duart.simpleitemslite.doublejump.DoubleJump;
import net.duart.simpleitemslite.doublejump.JumpItemListener;
import net.duart.simpleitemslite.funbow.FunBow;
import net.duart.simpleitemslite.funbow.FunBowItemListener;
import net.duart.simpleitemslite.infinitewater.MagicWater;
import net.duart.simpleitemslite.infinitewater.MagicWaterItemListener;
import net.duart.simpleitemslite.lightningaura.LightningAura;
import net.duart.simpleitemslite.lightningaura.LightningItemListener;
import net.duart.simpleitemslite.rainbowbridge.RainbowBridge;
import net.duart.simpleitemslite.rainbowbridge.RainbowItemListener;
import net.duart.simpleitemslite.voidbucket.VoidBucket;
import net.duart.simpleitemslite.voidbucket.VoidBucketItemListener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class SimpleItemsLite extends JavaPlugin {
    public static HashMap<UUID, Integer> usesCounter = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("SimpleItemsLite was Enabled!");

        List<String> fileNames = Arrays.asList("JumpItem.yml", "RainbowBridgeItem.yml",
                "LightningItem.yml", "MagicWaterItem.yml", "FunBowItem.yml", "VoidBucketItem.yml");

        for (String fileName : fileNames) {
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                saveResource(fileName, false);
            }
        }

        SimpleCommandManager commandManager = new SimpleCommandManager(this);
        JumpItemListener jumpItemListener = new JumpItemListener(this);
        DoubleJump doubleJump = new DoubleJump(this, jumpItemListener);
        LightningItemListener lightningItemListener = new LightningItemListener(this);
        LightningAura lightningAura = new LightningAura(this, lightningItemListener);
        RainbowItemListener rainbowItemListener = new RainbowItemListener(this);
        RainbowBridge rainbowBridge = new RainbowBridge(this, rainbowItemListener);
        MagicWaterItemListener magicWaterItemListener = new MagicWaterItemListener(this);
        MagicWater magicWater = new MagicWater(this, magicWaterItemListener);
        FunBow funBow = new FunBow(this, new FunBowItemListener(this));
        VoidBucketItemListener voidBucketItemListener = new VoidBucketItemListener(this);
        VoidBucket voidBucket = new VoidBucket(this, voidBucketItemListener);
        getServer().getPluginManager().registerEvents(jumpItemListener, this);
        getServer().getPluginManager().registerEvents(doubleJump, this);
        getServer().getPluginManager().registerEvents(lightningItemListener, this);
        getServer().getPluginManager().registerEvents(lightningAura, this);
        getServer().getPluginManager().registerEvents(rainbowItemListener, this);
        getServer().getPluginManager().registerEvents(rainbowBridge, this);
        getServer().getPluginManager().registerEvents(magicWaterItemListener, this);
        getServer().getPluginManager().registerEvents(magicWater, this);
        getServer().getPluginManager().registerEvents(funBow, this);
        getServer().getPluginManager().registerEvents(voidBucketItemListener, this);
        getServer().getPluginManager().registerEvents(voidBucket, this);
        Objects.requireNonNull(getCommand("simpleitemslite")).setExecutor(commandManager);
        Objects.requireNonNull(getCommand("simpleitemslite")).setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleItemsLite was disabled!");
        usesCounter.clear();
    }
}
