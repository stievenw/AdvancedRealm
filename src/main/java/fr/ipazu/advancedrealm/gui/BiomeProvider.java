package fr.ipazu.advancedrealm.gui;

import fr.ipazu.advancedrealm.Main;
import org.bukkit.Bukkit;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmConfig;
import fr.ipazu.advancedrealm.realm.RealmPlayer;
import fr.ipazu.advancedrealm.utils.ConfigFiles;
import fr.ipazu.advancedrealm.utils.ItemsUtils;
import fr.ipazu.advancedrealm.utils.TitleUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class BiomeProvider implements InventoryProvider {
    private Player player;
    private RealmPlayer realmPlayer;
    private ClickableItem basic;

    private static final List<BiomeEntry> BIOMES = Arrays.asList(
        new BiomeEntry(Biome.PLAINS, "§ePlains", Material.GRASS_BLOCK),
        new BiomeEntry(Biome.DESERT, "§6Desert", Material.SAND),
        new BiomeEntry(Biome.FOREST, "§2Forest", Material.OAK_SAPLING),
        new BiomeEntry(Biome.TAIGA, "§3Taiga", Material.SPRUCE_SAPLING),
        new BiomeEntry(Biome.SWAMP, "§2Swamp", Material.LILY_PAD),
        new BiomeEntry(Biome.JUNGLE, "§aJungle", Material.JUNGLE_LOG),
        new BiomeEntry(Biome.BADLANDS, "§6Badlands", Material.RED_SAND),
        new BiomeEntry(Biome.SAVANNA, "§eSavanna", Material.ACACIA_SAPLING),
        new BiomeEntry(Biome.SNOWY_PLAINS, "§fSnowy Plains", Material.SNOW_BLOCK),
        new BiomeEntry(Biome.BIRCH_FOREST, "§7Birch Forest", Material.BIRCH_SAPLING),
        new BiomeEntry(Biome.DARK_FOREST, "§8Dark Forest", Material.DARK_OAK_SAPLING),
        new BiomeEntry(Biome.FLOWER_FOREST, "§dFlower Forest", Material.POPPY),
        new BiomeEntry(Biome.MEADOW, "§aMeadow", Material.GRASS_BLOCK),
        new BiomeEntry(Biome.CHERRY_GROVE, "§dCherry Grove", Material.CHERRY_SAPLING)
    );

    public BiomeProvider(Player player, RealmPlayer realmPlayer) {
        this.player = player;
        this.realmPlayer = realmPlayer;
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        basic = ClickableItem.of(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (byte) 15), e -> e.setCancelled(true));
        inventoryContents.fill(basic);

        List<Biome> available = ConfigFiles.getAvailableBiomes();
        int slot = 0;
        for (BiomeEntry entry : BIOMES) {
            if (!available.contains(entry.biome)) continue;
            if (slot >= 36) break;
            int row = slot / 9;
            int col = slot % 9;
            inventoryContents.set(row, col, ClickableItem.of(
                new ItemsUtils(entry.icon, entry.displayName, (byte) 0, Arrays.asList(
                    "§7Click to claim a realm", "§7in the " + entry.displayName + " §7biome."
                )).toItemStack(), e -> {
                    e.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    claimRealm(entry.biome);
                }
            ));
            slot++;
        }

        inventoryContents.set(5, 0, ClickableItem.of(
            new ItemsUtils(Material.RED_BED, "⬅ §bGo back", Arrays.asList("", "§7Click to cancel.")).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.closeInventory();
            }
        ));
    }

    private void claimRealm(Biome targetBiome) {
        BossBar bossBar = Bukkit.createBossBar("§6Preparing world...", BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(0.05);

        bossBar.setProgress(0.2);
        World world = ConfigFiles.getOrCreateWorldForBiome(targetBiome);

        bossBar.setProgress(0.35);
        bossBar.setTitle("§6Scanning for safe location...");
        Location base = new RealmConfig().getNewLocation(world);
        Location spawn = base;

        bossBar.setProgress(0.55);
        bossBar.setTitle("§6Creating realm...");
        Realm realm = new Realm(realmPlayer, spawn, 1, 0);
        realm.pasteIsland();
        realm.fillChest();
        new RealmConfig().updateRealm(realmPlayer.getOwned());

        bossBar.setProgress(0.8);
        bossBar.setTitle("§6Teleporting...");
        realm.teleportToSpawn(player);
        player.setInvulnerable(true);
        bossBar.setProgress(1.0);
        bossBar.setTitle("§aDone!");

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
                bossBar.removeAll();
            }
        }.runTaskLater(Main.getInstance(), 60);
        TitleUtils.titlePacket(player, 20, 30, 20, "§bRealm claimed", "§aUse §6/realm §ato teleport");
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
    }

    private static class BiomeEntry {
        final Biome biome;
        final String displayName;
        final Material icon;

        BiomeEntry(Biome biome, String displayName, Material icon) {
            this.biome = biome;
            this.displayName = displayName;
            this.icon = icon;
        }
    }
}
