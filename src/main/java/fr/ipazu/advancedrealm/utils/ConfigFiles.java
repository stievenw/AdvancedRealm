package fr.ipazu.advancedrealm.utils;

import fr.ipazu.advancedrealm.Main;
import fr.ipazu.advancedrealm.realm.RealmConfig;
import fr.ipazu.advancedrealm.realm.RealmLevel;
import fr.ipazu.advancedrealm.realm.themes.ThemeConfig;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

public class ConfigFiles {
    private static Location spawn;
    private static Inventory realmchest;
    private static World realmworld;
    private static long cooldown;
    private static List<Biome> availableBiomes = new ArrayList<>();

    private void checkFolder() {
        if (!Main.getInstance().getDataFolder().exists())
            Main.getInstance().getDataFolder().mkdir();
        File themedir = new File(Main.getInstance().getDataFolder().getPath() + "/theme");
        if (!themedir.exists())
            themedir.mkdir();
        pasteFiles();
    }

    public void init() {
        checkFolder();
        initConfigs();
        loadConfig();
        loadUpgrades();
        Main.getInstance().getLogger().info("Starting loading themes ...");
        new ThemeConfig().loadAllThemes();
        Main.getInstance().getLogger().info("Starting loading realms ...");
        new RealmConfig().loadAllRealm();
        Verification.check();
    }


    public void loadConfig() {
        YamlConfiguration config = Config.CONFIG.getConfig();
        loadWorlds(config);
        availableBiomes.clear();
        for (String b : config.getStringList("config.biomes")) {
            try {
                availableBiomes.add(Biome.valueOf(b.toUpperCase()));
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("Invalid biome in config.biomes: " + b);
            }
        }
        spawn = new Location(Bukkit.getWorld(config.getString("config.spawn.world")), config.getInt("config.spawn.x"), config.getInt("config.spawn.y"), config.getInt("config.spawn.z"), (float) config.getInt("config.spawn.yaw"), (float) config.getInt("config.spawn.pitch"));
        realmworld = Bukkit.getWorld(config.getString("config.world"));
        if (config.getString("config.chest") != null) {
            try {
                realmchest = InventorySerialization.fromBase64(config.getString("config.chest"));
            } catch (Exception ex) {
                Main.getInstance().getLogger().severe("Error while loading the config.yml file in the chest section, check if it is deleted or try to reinstall the plugin. If you don't sucess at solving the problem you can contact iPazu#3982 at discord");

            }
        }
        cooldown = getCooldown();

    }

    private void initConfigs() {
        Arrays.stream(Config.values()).forEach(config -> {
            try {
                if (!config.getFile().exists())
                    config.getFile().createNewFile();

                if (config.getCopyDefault())
                    copyDefault(config.getFileName(), config.getFile());

                config.setConfig(YamlConfiguration.loadConfiguration(config.getFile()));
            } catch (Exception e) {
                Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to load configuration file: " + config.getFileName(), e);
            }
        });
    }

    public void copyDefault(String configname, File file) throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), configname));
        YamlConfiguration sourceconfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Main.getInstance().getResource(configname), StandardCharsets.UTF_8));
        for (String s : sourceconfig.getKeys(true)) {
            if (config.get(s) == null) {
                config.set(s, sourceconfig.get(s));
            }
        }
        config.save(file);
    }

    public void sendToSpawn(Player player) {
        player.teleport(spawn);
        WorldBorder.sendBorder(new Location(realmworld, 0, 50, 0), 30000000, player);
    }

    public static int getRealmspacing() {
        return Config.CONFIG.getConfig().getInt("config.spacing");
    }

    public void setRealmchest(Inventory inv) {
        realmchest = inv;
        Config.CONFIG.getConfig().set("config.chest", InventorySerialization.toBase64(inv));
        try {
            Config.CONFIG.getConfig().save(Config.CONFIG.getFile());
        } catch (Exception ex) {
            Main.getInstance().getLogger().severe("Error while editing the config.yml file in the chest section, check if it is deleted or try to reinstall the plugin. If you don't sucess at solving the problem you can contact iPazu#3982 at discord");

        }
    }

    public static Inventory getRealmchest() {
        return realmchest;
    }

    public static Location getSpawn() {
        return spawn;
    }


    public static World getWorld() {
        return realmworld;
    }

    public static List<Biome> getAvailableBiomes() {
        return availableBiomes.isEmpty() ? Collections.singletonList(Biome.PLAINS) : availableBiomes;
    }

    public static World getOrCreateWorldForBiome(Biome biome) {
        String worldName = getBiomeWorldName(biome);
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;

        WorldCreator wc = WorldCreator.name(worldName).environment(World.Environment.NORMAL);
        wc.biomeProvider(new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return biome;
            }
            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return Collections.singletonList(biome);
            }
        });
        world = wc.createWorld();
        if (world != null) {
            Main.getInstance().getLogger().info("Created biome world: " + worldName);
        }
        return world;
    }

    private static String getBiomeWorldName(Biome biome) {
        return Config.CONFIG.getConfig().getString("config.world") + "_" + biome.name().toLowerCase();
    }

    public static long getCooldownValue() {
        return cooldown;
    }

    private long getCooldown() {
        long cooldown = 0;
        for (String s : Config.CONFIG.getConfig().getString("vote.cooldown").split("/")) {
            if (s.contains("d")) {
                cooldown += 86400000 * Integer.parseInt(stripNonDigits(s));
            }
            if (s.contains("h")) {
                cooldown += 3600000 * Integer.parseInt(stripNonDigits(s));
            }
            if (s.contains("m")) {
                cooldown += 60000 * Integer.parseInt(stripNonDigits(s));
            }
            if (s.contains("s")) {
                cooldown += 1000 * Integer.parseInt(stripNonDigits(s));
            }

        }
        return cooldown;
    }

    public static String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void loadUpgrades() {
        YamlConfiguration config = Config.UPGRADES.getConfig();
        ConfigurationSection levelsection = config.getConfigurationSection("levels");
        for (String s : levelsection.getKeys(false)) {
            new RealmLevel(Integer.parseInt(s), levelsection.getInt(s + ".cost"), levelsection.getInt(s + ".bordersize"), levelsection.getInt(s + ".maxplayer"));
        }
    }

    private void loadWorlds(YamlConfiguration config) {
        for (String worldName : new String[]{config.getString("config.world"), config.getString("config.spawn.world")}) {
            if (Bukkit.getWorld(worldName) == null) {
                World world = WorldCreator.name(worldName)
                    .environment(World.Environment.NORMAL)
                    .createWorld();
                if (world != null) {
                    Main.getInstance().getLogger().info("World '" + worldName + "' created/loaded successfully.");
                }
            }
        }
    }

    private void pasteFiles() {
        if (!new File(Main.getInstance().getDataFolder() + "/theme/basictheme.schematic").exists()) {
            Main.getInstance().getLogger().info("Creating basic theme schematic");
            copy(getClass().getResourceAsStream("/schematics/theme/basictheme.schematic"), Main.getInstance().getDataFolder().getAbsolutePath() + "/theme/basictheme.schematic");
        }

    }
    public static boolean copy(InputStream source , String destination) {

        boolean success = true;

        Main.getInstance().getLogger().info("Copying to " + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while copying file: " + source + " to " + destination, ex);
        }

        return success;

    }
}
