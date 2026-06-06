package fr.ipazu.advancedrealm.gui;

import fr.ipazu.advancedrealm.Main;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmLevel;
import fr.ipazu.advancedrealm.realm.RealmPlayer;
import fr.ipazu.advancedrealm.realm.RealmRank;
import fr.ipazu.advancedrealm.utils.ItemsUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class UpgradeConfirmProvider implements InventoryProvider {
    private Player player;
    private Realm realm;
    private ClickableItem confirm, cancel, info, basic;

    public UpgradeConfirmProvider(Player player, Realm realm) {
        this.player = player;
        this.realm = realm;
        setUpItems();
    }

    private void setUpItems() {
        RealmLevel nextlevel = RealmLevel.getLevel(realm.getLevel().getNumber() + 1);

        info = ClickableItem.of(new ItemsUtils(Material.EXPERIENCE_BOTTLE,
                "§b§lUpgrade Confirmation",
                Arrays.asList(
                        "§7Current level: §e" + realm.getLevel().getNumber(),
                        "§7Next level: §e" + nextlevel.getNumber(),
                        "",
                        "§7Cost: §6$" + nextlevel.getPrice()
                )).toItemStack(), e -> e.setCancelled(true));

        confirm = ClickableItem.of(new ItemsUtils(Material.GREEN_TERRACOTTA,
                "§aConfirm Upgrade",
                Arrays.asList("§7Click to confirm upgrading", "§7your Realm to level §e" + nextlevel.getNumber())).toItemStack(), e -> {
            e.setCancelled(true);
            if (RealmPlayer.getPlayer(player.getUniqueId().toString()).getRankByRealm(realm) == RealmRank.MEMBER ||
                    RealmPlayer.getPlayer(player.getUniqueId().toString()).getRankByRealm(realm) == RealmRank.GUARD) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage("§cOnly the manager and the owner of the Realm can do this !");
                return;
            }
            if (Main.getInstance().economy.getBalance(player) < nextlevel.getPrice()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage("§cYou don't have enough money to upgrade your Realm.");
                player.sendMessage("§cYou have §6" + Main.getInstance().economy.getBalance(player) + " §cand you need §6" + nextlevel.getPrice());
                return;
            }

            Main.getInstance().economy.withdrawPlayer(player, nextlevel.getPrice());
            realm.upgrade(realm.getLevel().getNumber() + 1);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            for (RealmPlayer s : realm.getRealmMembers()) {
                if (Bukkit.getPlayer(s.getName()) != null)
                    Bukkit.getPlayer(s.getName()).sendMessage("§e§l" + player.getName() + " §aupgraded §b§l" + realm.getOwner().getName() + "'s §aRealm to the level §e" + realm.getLevel().getNumber());
            }

            player.closeInventory();
        });

        cancel = ClickableItem.of(new ItemsUtils(Material.RED_TERRACOTTA,
                "§cCancel",
                Arrays.asList("§7Go back to Realm options.")).toItemStack(), e -> {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new WholeGUI().openRealmGui(player, realm, true);
        });

        basic = ClickableItem.of(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (byte) 15), e -> e.setCancelled(true));
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        inventoryContents.fill(basic);
        inventoryContents.set(0, 4, info);
        inventoryContents.set(1, 2, confirm);
        inventoryContents.set(1, 6, cancel);
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
    }
}
