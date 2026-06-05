package fr.ipazu.advancedrealm;

import fr.ipazu.advancedrealm.commands.*;
import fr.ipazu.advancedrealm.events.EventManager;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInvsPlugin;

import java.lang.reflect.Field;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.utils.ARExpansion;
import fr.ipazu.advancedrealm.utils.ConfigFiles;
import fr.ipazu.advancedrealm.utils.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;

public class Main extends JavaPlugin {
    private static Main instance;
    public Economy economy = null;
    public static Metrics metrics;
    private InventoryManager invManager;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        invManager = new InventoryManager(this);
        invManager.init();
        try {
            Field managerField = SmartInvsPlugin.class.getDeclaredField("invManager");
            managerField.setAccessible(true);
            managerField.set(null, invManager);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize SmartInvs manager");
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new ARExpansion(this).register();
        }
        new ConfigFiles().init();
        new EventManager(this);
        getCommand("unclaim").setExecutor(new Unclaim());
        getCommand("claim").setExecutor(new Claim());
        getCommand("realm").setExecutor(new RealmCommand());
        getCommand("home").setExecutor(new Home());
        getCommand("visit").setExecutor(new Visit());
        getCommand("configrealm").setExecutor(new ConfigRealm());
        pushMetrics();
    }

    @Override
    public void onDisable() {
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Main.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static Metrics getMetrics() {
        return metrics;
    }

    private void pushMetrics() {
        metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("realms_created", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Realm.allrealm.size();
            }
        }));
        getLogger().info("Metrics successfully pushed (" + Realm.allrealm.size() + " realms)");
    }
}
