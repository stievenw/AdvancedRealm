package fr.ipazu.advancedrealm.realm;


import com.google.common.collect.Iterables;
import fr.ipazu.advancedrealm.Main;
import fr.ipazu.advancedrealm.realm.themes.ThemeType;
import fr.ipazu.advancedrealm.utils.Config;
import fr.ipazu.advancedrealm.utils.ConfigFiles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RealmConfig {
    private static final Map<Biome, Set<Material>> SURFACE_MATERIALS = new HashMap<>();
    private static final Set<Material> TREE_BLOCKS = Set.of(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
        Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
        Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
        Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD,
        Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD, Material.CHERRY_WOOD,
        Material.MANGROVE_ROOTS, Material.MUDDY_MANGROVE_ROOTS
    );
    private static final Set<Material> LEAF_BLOCKS = Set.of(
        Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES,
        Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES
    );
    private static final Set<Material> VINE_BLOCKS = Set.of(
        Material.VINE, Material.CAVE_VINES, Material.CAVE_VINES_PLANT,
        Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT,
        Material.WEEPING_VINES, Material.WEEPING_VINES_PLANT
    );

    static {
        Set<Material> grassDirt = Set.of(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.ROOTED_DIRT);
        SURFACE_MATERIALS.put(Biome.PLAINS, grassDirt);
        SURFACE_MATERIALS.put(Biome.DESERT, Set.of(Material.SAND, Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CUT_SANDSTONE));
        SURFACE_MATERIALS.put(Biome.FOREST, grassDirt);
        SURFACE_MATERIALS.put(Biome.TAIGA, grassDirt);
        SURFACE_MATERIALS.put(Biome.SWAMP, Set.of(Material.GRASS_BLOCK, Material.DIRT, Material.MUD));
        SURFACE_MATERIALS.put(Biome.JUNGLE, grassDirt);
        SURFACE_MATERIALS.put(Biome.BADLANDS, Set.of(Material.RED_SAND, Material.TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.BROWN_TERRACOTTA, Material.WHITE_TERRACOTTA, Material.RED_SANDSTONE));
        SURFACE_MATERIALS.put(Biome.SAVANNA, grassDirt);
        SURFACE_MATERIALS.put(Biome.SNOWY_PLAINS, Set.of(Material.GRASS_BLOCK, Material.DIRT, Material.SNOW_BLOCK, Material.SNOW));
        SURFACE_MATERIALS.put(Biome.BIRCH_FOREST, grassDirt);
        SURFACE_MATERIALS.put(Biome.DARK_FOREST, grassDirt);
        SURFACE_MATERIALS.put(Biome.FLOWER_FOREST, grassDirt);
        SURFACE_MATERIALS.put(Biome.MEADOW, grassDirt);
        SURFACE_MATERIALS.put(Biome.CHERRY_GROVE, grassDirt);
    }

    private File file = new File(Main.getInstance().getDataFolder(), "realm.yml");
    private FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public RealmConfig() {

    }

    public void updateRealm(Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".owner.uuid", realm.getOwner().getUniqueId());
        config.set("realms." + realm.getOwner().getUniqueId() + ".owner.name", realm.getOwner().getName());
        config.set("realms." + realm.getOwner().getUniqueId() + ".privacy", realm.getPrivacy());
        config.set("realms." + realm.getOwner().getUniqueId() + ".level", realm.getLevel().getNumber());
        updateVote(realm);
        for (RealmPlayer bplayer : realm.getBanned()) {
            config.set("realms." + realm.getOwner().getUniqueId() + ".banned.", bplayer.getUniqueId());
        }
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.id", realm.getTheme().getThemeType().getName());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.x", realm.getTheme().getSpawn().getBlockX());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.y", realm.getTheme().getSpawn().getBlockY());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.z", realm.getTheme().getSpawn().getBlockZ());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.yaw", realm.getTheme().getSpawn().getYaw());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.pitch", realm.getTheme().getSpawn().getPitch());
        config.set("realms." + realm.getOwner().getUniqueId() + ".theme.spawn.world", realm.getWorld().getName());
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }

    }

    public void addPlayer(RealmPlayer rplayer, Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".players." + rplayer.getUniqueId() + ".name", rplayer.getName());
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void promotePlayer(RealmPlayer rplayer, Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".players." + rplayer.getUniqueId() + ".rank", rplayer.getRankByRealm(realm).toString());
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void removePlayer(RealmPlayer rplayer, Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".players." + rplayer.getUniqueId(), null);
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void banPlayer(RealmPlayer rplayer, Realm realm) {
        if (realm.getBanned().size() == 0)
            config.set("realms." + realm.getOwner().getUniqueId() + ".banned", null);
        else
            config.set("realms." + realm.getOwner().getUniqueId() + ".banned." + rplayer.getUniqueId() + ".name", rplayer.getName());
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void unbanPlayer(RealmPlayer rplayer, Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".banned." + rplayer.getUniqueId(), null);
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void addNewPlayer(Player p) {
        config.set("realmplayers." + p.getUniqueId() + ".uuid", p.getUniqueId().toString());
        config.set("realmplayers." + p.getUniqueId() + ".name", p.getName());
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }

        if (RealmPlayer.getPlayer(p.getUniqueId().toString()) == null)
            new RealmPlayer(p.getUniqueId().toString(), p.getName());

    }

    public void setPerk(Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".perk", realm.getPerk());
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    public void setPrivacy(Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".privacy", realm.getPrivacy());
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    public void delete(Realm realm) {
        if (Realm.allrealm.size() == 0) {
            config.set("realms", null);
        } else
            config.set("realms." + realm.getOwner().getUniqueId(), null);
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    public void setLevel(Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".level", realm.getLevel().getNumber());
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    public void updatePlayerName(Player player) {
        RealmPlayer rp = RealmPlayer.getPlayer(player.getUniqueId().toString());
        for (Realm r : rp.getAllRealm()) {
            if (r.getOwner().getUniqueId().equals(player.getUniqueId().toString())) {
                config.set("realms." + r.getOwner().getUniqueId() + ".owner.name", player.getName());
            }
            config.set("realms." + r.getOwner().getUniqueId() + ".players." + rp.getUniqueId() + ".name", player.getName());
            rp.setName(player.getName());
        }
        for (String s : config.getConfigurationSection("realmplayers").getKeys(false)) {
            if (player.getUniqueId().toString().equals(s))
                config.set("realmplayers." + s + ".name", player.getName());
        }
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
        rp.setName(player.getName());
    }

    public void updateLastVote(RealmPlayer rp) {
        config.set("realmplayers." + rp.getUniqueId() + ".lastvote", System.currentTimeMillis());
        rp.setLastvote(System.currentTimeMillis());
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    public void loadRealm(String name) {
        try {
            if (config.getConfigurationSection("realms." + name + ".players") != null) {
                for (String s : config.getConfigurationSection("realms." + name + ".players").getKeys(false)) {
                    if (RealmPlayer.getPlayer(s) == null)
                        new RealmPlayer(s, config.getString("realms." + name + ".players." + s + ".name"));
                }
            }
            String worldName = config.getString("realms." + name + ".theme.spawn.world");
            World world = worldName != null ? Bukkit.getWorld(worldName) : ConfigFiles.getWorld();
            if (world == null) world = ConfigFiles.getWorld();
            Location spawn = new Location(world,
                config.getInt("realms." + name + ".theme.spawn.x"),
                config.getInt("realms." + name + ".theme.spawn.y"),
                config.getInt("realms." + name + ".theme.spawn.z"),
                (float) config.getInt("realms." + name + ".theme.spawn.yaw"),
                (float) config.getInt("realms." + name + ".theme.spawn.pitch"));
            ThemeType themeType = ThemeType.themeTypes.get(config.getString("realms." + name + ".theme.id"));
            if (themeType == null) {
                themeType = ThemeType.allthemeTypes.isEmpty() ? null : ThemeType.allthemeTypes.get(0);
            }
            String ownerUuid = config.getString("realms." + name + ".owner.uuid");
            if (ownerUuid != null && RealmPlayer.getPlayer(ownerUuid) == null) {
                new RealmPlayer(ownerUuid, config.getString("realms." + name + ".owner.name"));
            }
            RealmPlayer owner = ownerUuid != null ? RealmPlayer.getPlayer(ownerUuid) : null;
            if (owner == null) return;
            Realm realm = new Realm(owner, themeType, spawn, Math.max(1, config.getInt("realms." + name + ".level")), Math.max(0, config.getInt("realms." + name + ".vote")));
            if (config.getConfigurationSection("realms." + name + ".players") != null) {
                for (String s : config.getConfigurationSection("realms." + name + ".players").getKeys(false)) {
                    RealmPlayer rp = RealmPlayer.getPlayer(s);
                    if (rp != null && rp.getOwned() != realm) {
                        realm.addPlayer(rp);
                        realm.promote(rp, RealmRank.getRankByString(config.getString("realms." + name + ".players." + s + ".rank")));
                    }
                }
            }
            realm.setPrivacy(config.getBoolean("realms." + name + ".privacy"));
            realm.setPerk(config.getString("realms."+name+".perk"));
            if (config.getConfigurationSection("realms." + name + ".banned") != null) {
                for (String s : config.getConfigurationSection("realms." + name + ".banned").getKeys(false)) {
                    if (RealmPlayer.getPlayer(s) == null)
                        new RealmPlayer(s, config.getString("realms." + name + ".banned." + s + ".name"));
                    RealmPlayer bannedPlayer = RealmPlayer.getPlayer(s);
                    if (bannedPlayer != null) realm.banPlayer(bannedPlayer);
                }
            }
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.WARNING, "Failed to load realm: " + name, ex);
        }
    }

    private void loadNewRealmPlayer() {
        for (String s : config.getConfigurationSection("realmplayers").getKeys(false)) {
            if (RealmPlayer.getPlayer(s) == null)
                new RealmPlayer(config.getString("realmplayers." + s + ".uuid"), config.getString("realmplayers." + s + ".name"));
        }
    }

    public void loadAllRealm() {

        if (config.getConfigurationSection("realms") != null) {
            for (String s : config.getConfigurationSection("realms").getKeys(false)) {
                try {
                    loadRealm(s);
                } catch (Exception ex) {
                    Main.getInstance().getLogger().log(java.util.logging.Level.WARNING, "Skipping invalid realm: " + s, ex);
                }
            }
            try {
                loadNewRealmPlayer();
                for (String s : config.getConfigurationSection("realmplayers").getKeys(false)) {
                    RealmPlayer rp = RealmPlayer.getPlayer(s);
                    if (rp != null) {
                        rp.setLastvote(config.getLong("realmplayers." + s + ".lastvote"));
                        for (String sr : config.getStringList("realmplayers." + s + ".voted")) {
                            if (sr != null) {
                                RealmPlayer votedPlayer = RealmPlayer.getPlayer(sr);
                                if (votedPlayer != null && votedPlayer.getOwned() != null) {
                                    rp.addRealmVoted(votedPlayer.getOwned());
                                }
                            }
                        }
                    }
                }
                Main.getInstance().getLogger().info("Successfully loaded all realms !");
            } catch (Exception ex) {
                Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to load realm players", ex);
            }
        }
    }
    public void removeVotes(Realm deletedrealm) {

        String deletedName = deletedrealm.getOwner().getUniqueId();
        for (String s : config.getConfigurationSection("realmplayers").getKeys(false)) {
            ArrayList<String> newvotelist = new ArrayList<>();
            for (String sr : config.getStringList("realmplayers." + s + ".voted")) {
                if (sr != null && !sr.equalsIgnoreCase(deletedName)) {
                    newvotelist.add(sr);
                }
            }
            config.set("realmplayers." + s + ".voted",newvotelist);
        }
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", ex);
        }
    }

    private Location getLastLocation() {
        if (config.getConfigurationSection("realms") == null) {
            return new Location(ConfigFiles.getWorld(), ConfigFiles.getRealmspacing(), 66, 0);
        }
        String laststring = Iterables.getLast(config.getConfigurationSection("realms").getKeys(false));
        ConfigurationSection section = config.getConfigurationSection("realms." + laststring + ".theme.spawn");
        if (section != null) {
            return new Location(ConfigFiles.getWorld(), section.getDouble("x"), 66, section.getDouble("z"));
        }
        return new Location(ConfigFiles.getWorld(), ConfigFiles.getRealmspacing(), 66, 0);
    }

    public Location getNewLocation() {
        return getNewLocation(ConfigFiles.getWorld());
    }

    public int getNextCellIndex(String worldName) {
        String path = "cell-counters." + worldName;
        int index = config.getInt(path, -1);
        if (index == -1) {
            int existing = 0;
            for (Realm r : Realm.allrealm) {
                if (r.getWorld().getName().equals(worldName)) existing++;
            }
            index = existing;
        }
        config.set(path, index + 1);
        try {
            config.save(file);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to save cell counter", ex);
        }
        return index;
    }

    public Location getNewLocation(World world) {
        int spacing = ConfigFiles.getRealmspacing();
        Random rand = new Random();
        int margin = 50;

        if (ConfigFiles.getRealmType() == RealmType.ISLAND) {
            if (Realm.allrealm.isEmpty()) {
                return new Location(world, spacing, 66, 0);
            }
            Location last = getLastLocation();
            int i = Realm.allrealm.size() % 20;
            if (i == 0)
                return last.clone().add(-last.getX(), 0, spacing);
            else
                return last.clone().add(spacing, 0, 0);
        }
        int cellIndex = getNextCellIndex(world.getName());
        int col = cellIndex % 20;
        int row = cellIndex / 20;
        int baseX = spacing * (col + 1);
        int baseZ = spacing * row;
        int x = baseX + margin + rand.nextInt(spacing - 2 * margin);
        int z = baseZ + margin + rand.nextInt(spacing - 2 * margin);
        return findGround(world, x, z);
    }

    private Location findGround(World world, int x, int z) {
        Biome biome = world.getBiome(x, z);
        Set<Material> allowed = SURFACE_MATERIALS.get(biome);
        if (allowed == null) allowed = Set.of(Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.SAND);

        int[][] offsets = {{0, 0}, {4, 0}, {-4, 0}, {0, 4}, {0, -4}};
        for (int[] off : offsets) {
            int bx = x + off[0];
            int bz = z + off[1];
            int top = world.getHighestBlockYAt(bx, bz);
            for (int y = top; y > -64; y--) {
                Material mat = world.getBlockAt(bx, y, bz).getType();
                if (allowed.contains(mat)) {
                    return new Location(world, bx + 0.5, y + 1, bz + 0.5);
                }
                if (mat == Material.WATER || mat == Material.LAVA) break;
                if (TREE_BLOCKS.contains(mat)) continue;
                if (LEAF_BLOCKS.contains(mat)) continue;
                if (VINE_BLOCKS.contains(mat)) continue;
                if (mat.name().contains("LEAVES") || mat.name().contains("LEAF") || mat.name().contains("STEM")) continue;
                if (mat.isSolid() && mat != Material.SNOW && !mat.name().contains("CARPET")) {
                    return new Location(world, bx + 0.5, y + 1, bz + 0.5);
                }
            }
        }
        return new Location(world, x + 0.5, 64, z + 0.5);
    }

    public void updateVote(Realm realm) {
        config.set("realms." + realm.getOwner().getUniqueId() + ".vote", realm.getVote());
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }

    public void updateMultipleVote(RealmPlayer rp) {
        config.set("realmplayers." + rp.getUniqueId() + ".voted", rp.voteduuid);
        for (String s : rp.voteduuid)
            Main.getInstance().getLogger().info(s);
        try {
            config.save(file);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error while loading the realm.yml file", e);
        }
    }
    private void useless()
    {
        ArrayList<String> strs = new ArrayList<>();
        strs.forEach(s -> Main.getInstance().getLogger().info(s));
    }
}

