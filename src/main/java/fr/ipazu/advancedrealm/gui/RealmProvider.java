package fr.ipazu.advancedrealm.gui;

import fr.ipazu.advancedrealm.Main;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmLevel;
import fr.ipazu.advancedrealm.realm.RealmPlayer;
import fr.ipazu.advancedrealm.realm.RealmRank;
import fr.ipazu.advancedrealm.utils.Config;
import fr.ipazu.advancedrealm.utils.ItemsUtils;
import fr.ipazu.advancedrealm.utils.TitleUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RealmProvider implements InventoryProvider {
    private Player player;
    private RealmPlayer realmPlayer;
    private Realm realm;
    private boolean hasRealm;
    private int memberPage;
    private boolean from;
    private YamlConfiguration config;

    public RealmProvider(Player player, RealmPlayer realmPlayer, boolean from, int memberPage) {
        this.player = player;
        this.realmPlayer = realmPlayer;
        this.realm = realmPlayer.getOwned();
        this.hasRealm = (realm != null);
        this.from = from;
        this.memberPage = memberPage;
        this.config = Config.ASPECT.getConfig();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (!hasRealm) {
            initNoRealm(contents);
        } else {
            initWithRealm(contents);
        }
    }

    private void initNoRealm(InventoryContents contents) {
        // Create realm button at slot 0
        ItemStack createItem = new ItemsUtils(Material.GRASS_BLOCK, "§a§lCreate Realm",
            Arrays.asList("§7Click to claim a new realm")).toItemStack();
        contents.set(0, 0, ClickableItem.of(createItem, e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new WholeGUI().openBiomeGUI(player, realmPlayer);
        }));

        // Back button at bottom-right
        contents.set(4, 8, ClickableItem.of(
            new ItemsUtils(Config.getMaterial(config.getString("gui.back.item")),
                Config.pushColor(config.getString("gui.back.name")), (byte) config.getInt("gui.back.data"),
                config.getStringList("gui.back.lore")).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new WholeGUI().openAllRealmGUI(player);
        }));
    }

    private void initWithRealm(InventoryContents contents) {
        // Owner head at slot 0 - click to visit
        ItemStack ownerHead = ItemsUtils.getHead(
            realm.getOwner().getName(),
            "§b§l" + realm.getOwner().getName() + "'s Realm",
            Arrays.asList("§7Click to visit your Realm", "§7Level: §e" + realm.getLevel().getNumber(), "§7Members: §e" + realm.getRealmMembers().size()));
        contents.set(0, 0, ClickableItem.of(ownerHead, e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            realm.teleportToSpawn(player);
            player.sendMessage("§aTeleporting to the realm...");
            player.closeInventory();
        }));

        // Member heads with pagination
        List<RealmPlayer> allMembers = realm.getRealmMembers();
        List<RealmPlayer> displayMembers = new ArrayList<>();
        for (RealmPlayer rp : allMembers) {
            if (rp != realm.getOwner()) displayMembers.add(rp);
        }

        int totalPages = Math.max(1, (int) Math.ceil(displayMembers.size() / 8.0));
        if (memberPage >= totalPages) memberPage = totalPages - 1;
        if (memberPage < 0) memberPage = 0;

        int start = memberPage * 8;
        int end = Math.min(start + 8, displayMembers.size());
        for (int i = start; i < end; i++) {
            RealmPlayer rp = displayMembers.get(i);
            int slot = 1 + (i - start);
            ItemStack head = ItemsUtils.getHead(
                rp.getName(), "§b" + rp.getName(),
                Arrays.asList("§7Rank: §e" + rp.getRankByRealm(realm).toString()));
            contents.set(0, slot, ClickableItem.of(head, e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                realm.teleportToSpawn(player);
                player.sendMessage("§aTeleporting to the realm...");
                player.closeInventory();
            }));
        }

        // Navigation
        if (memberPage > 0) {
            contents.set(1, 0, ClickableItem.of(
                new ItemsUtils(Material.ARROW, "§bPrevious", Arrays.asList("§7Page " + memberPage)).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                new WholeGUI().openRealmGui(player, realmPlayer, from, memberPage - 1);
            }));
        }
        if (memberPage + 1 < totalPages) {
            contents.set(1, 8, ClickableItem.of(
                new ItemsUtils(Material.ARROW, "§bNext", Arrays.asList("§7Page " + (memberPage + 2))).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                new WholeGUI().openRealmGui(player, realmPlayer, from, memberPage + 1);
            }));
        }

        // --- Bottom row action buttons (row 4, sequential) ---
        // Home (slot 36)
        contents.set(4, 0, ClickableItem.of(
            new ItemsUtils(Config.getMaterial(config.getString("gui.realmgui.home.item")),
                Config.getStringWithReplacementRealm(config.getString("gui.realmgui.home.name"), realm),
                (byte) config.getInt("gui.realmgui.home.data"),
                Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.home.lore"), realm)).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            realm.teleportToSpawn(player);
            player.sendMessage(Config.getStringWithReplacementRealm(config.getString("gui.realmgui.home.clickmessage"), realm));
            player.closeInventory();
        }));

        // Privacy (slot 37)
        Material privacyMat = realm.getPrivacy() ? Material.RED_DYE : Material.LIME_DYE;
        contents.set(4, 1, ClickableItem.of(
            new ItemsUtils(privacyMat,
                Config.getStringWithReplacementRealm(config.getString("gui.realmgui.privacy.name"), realm),
                (byte) config.getInt("gui.realmgui.privacy.data"),
                Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.privacy.lore"), realm)).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
            RealmRank rank = realmPlayer.getRankByRealm(realm);
            if (rank == RealmRank.MANAGER || rank == RealmRank.OWNER) {
                realm.setPrivacy(!realm.getPrivacy());
                player.sendMessage(Config.getStringWithReplacementRealm(config.getString("gui.realmgui.privacy.clickmessage"), realm));
                new WholeGUI().openRealmGui(player, realmPlayer, from, memberPage);
            } else {
                player.sendMessage("§cOnly the manager and the owner of the Realm can do this !");
            }
        }));

        // Upgrade (slot 38)
        if (realm.getLevel().getNumber() >= 20) {
            contents.set(4, 2, ClickableItem.of(
                new ItemsUtils(Config.getMaterial(config.getString("gui.realmgui.upgrade.item")),
                    Config.getStringWithReplacementRealm(config.getString("gui.realmgui.upgrade.name"), realm),
                    (byte) config.getInt("gui.realmgui.upgrade.data"),
                    Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.upgrade.maxlevellore"), realm)).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage(Config.pushColor(config.getString("messages.upgrade.maxlevel")));
                player.closeInventory();
            }));
        } else {
            RealmLevel nextlevel = RealmLevel.getLevel(realm.getLevel().getNumber() + 1);
            if (!Main.getInstance().setupEconomy()) {
                contents.set(4, 2, ClickableItem.of(
                    new ItemsUtils(Material.BARRIER, "§cUpgrade unavailable", (byte) 0,
                        Arrays.asList("§7This feature is currently disabled.")).toItemStack(), e -> {
                    e.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    player.sendMessage(Config.pushColor(config.getString("messages.upgrade.unavailable")));
                }));
            } else {
                contents.set(4, 2, ClickableItem.of(
                    new ItemsUtils(Config.getMaterial(config.getString("gui.realmgui.upgrade.item")),
                        Config.getStringWithReplacementRealm(config.getString("gui.realmgui.upgrade.name"), realm),
                        (byte) config.getInt("gui.realmgui.upgrade.data"),
                        Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.upgrade.lore"), realm)).toItemStack(), e -> {
                    e.setCancelled(true);
                    RealmRank rank = realmPlayer.getRankByRealm(realm);
                    if (rank == RealmRank.MEMBER || rank == RealmRank.GUARD) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage(Config.pushColor(config.getString("messages.upgrade.noperm")));
                        return;
                    }
                    if (Main.getInstance().economy.getBalance(player) < nextlevel.getPrice()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        player.sendMessage(Config.pushColor(config.getString("messages.upgrade.notenoughmoney")));
                        String balanceinfo = Config.pushColor(config.getString("messages.upgrade.balanceinfo"));
                        balanceinfo = balanceinfo.replace("%balance%", String.valueOf(Main.getInstance().economy.getBalance(player)));
                        balanceinfo = balanceinfo.replace("%cost%", String.valueOf(nextlevel.getPrice()));
                        player.sendMessage(balanceinfo);
                        return;
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    new WholeGUI().openUpgradeConfirmGui(player, realm);
                }));
            }
        }

        // Members (slot 39)
        contents.set(4, 3, ClickableItem.of(
            new ItemsUtils(Config.getMaterial(config.getString("gui.realmgui.members.item")),
                Config.getStringWithReplacementRealm(config.getString("gui.realmgui.members.name"), realm),
                (byte) config.getInt("gui.realmgui.members.data"),
                Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.members.lore"), realm)).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new WholeGUI().openMembersGui(player, realm);
        }));

        // Banned (slot 40)
        contents.set(4, 4, ClickableItem.of(
            new ItemsUtils(Config.getMaterial(config.getString("gui.realmgui.banned.item")),
                Config.getStringWithReplacementRealm(config.getString("gui.realmgui.banned.name"), realm),
                (byte) config.getInt("gui.realmgui.banned.data"),
                Config.getListWithReplacementRealm(config.getStringList("gui.realmgui.banned.lore"), realm)).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new WholeGUI().openBanned(player, realm);
        }));

        // Delete (slot 41)
        contents.set(4, 5, ClickableItem.of(
            new ItemsUtils(Material.RED_TERRACOTTA, "§cDelete Realm", (byte) 0,
                Arrays.asList("§7Click to delete your Realm.", "§7This cannot be undone!")).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            new WholeGUI().openUnclaimGui(player, realm);
        }));

        // Back (slot 44)
        if (from) {
            contents.set(4, 8, ClickableItem.of(
                new ItemsUtils(Config.getMaterial(config.getString("gui.back.item")),
                    Config.getStringWithReplacementRealm(config.getString("gui.back.name"), realm),
                    (byte) config.getInt("gui.back.data"),
                    Config.getListWithReplacementRealm(config.getStringList("gui.back.lore"), realm)).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.closeInventory();
                new WholeGUI().openAllRealmGUI(player);
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
    }
}
