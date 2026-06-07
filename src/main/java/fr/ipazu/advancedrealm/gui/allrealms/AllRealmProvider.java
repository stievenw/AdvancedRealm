package fr.ipazu.advancedrealm.gui.allrealms;

import fr.ipazu.advancedrealm.gui.WholeGUI;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmPlayer;
import fr.ipazu.advancedrealm.utils.ItemsUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AllRealmProvider implements InventoryProvider {

    private RealmPlayer realmPlayer;
    private List<ClickableItem> sortedItems = new ArrayList<>();
    private ClickableItem filler;

    public AllRealmProvider(Player player) {
        this.realmPlayer = RealmPlayer.getPlayer(player.getUniqueId().toString());
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        filler = ClickableItem.of(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (byte) 15), e -> e.setCancelled(true));
        Pagination pagination = contents.pagination();
        List<Realm> allRealms = new ArrayList<>(Realm.allrealm);

        allRealms.sort((a, b) -> {
            boolean aOwned = a.getOwner() == realmPlayer;
            boolean bOwned = b.getOwner() == realmPlayer;
            if (aOwned && !bOwned) return -1;
            if (!aOwned && bOwned) return 1;
            boolean aPrivate = a.getPrivacy();
            boolean bPrivate = b.getPrivacy();
            if (aPrivate && !bPrivate) return 1;
            if (!aPrivate && bPrivate) return -1;
            return a.getOwner().getName().compareToIgnoreCase(b.getOwner().getName());
        });

        // If player has no realm, show Create Realm button first
        if (realmPlayer.getOwned() == null) {
            sortedItems.add(ClickableItem.of(
                new ItemsUtils(Material.GRASS_BLOCK, "§a§lCreate Realm",
                    Arrays.asList("§7Click to claim a new realm")).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.closeInventory();
                new WholeGUI().openBiomeGUI(player, realmPlayer);
            }));
        }

        for (Realm r : allRealms) {
            boolean isOwned = r.getOwner() == realmPlayer;
            boolean isMember = r.getRealmMembers().contains(realmPlayer);
            String ownerName = r.getOwner().getName();

            if (r.getPrivacy() && !isMember && !isOwned) {
                // Private realm - show head with red private lore
                List<String> lore = new ArrayList<>();
                lore.add("§7Members: §e" + r.getRealmMembers().size() + "§7/§6" + r.getLevel().getMaxplayer());
                lore.add("§7Level: §e" + r.getLevel().getNumber());
                lore.add("§cPrivate - No one can visit");

                ItemStack head = ItemsUtils.getHead(ownerName, "§c§l" + ownerName + "'s Realm §7(Private)", lore);
                sortedItems.add(ClickableItem.of(head, e -> {
                    e.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    player.sendMessage("§cThis realm is private, just the members of the realm can visit it.");
                }));
            } else {
                // Public or own/member realm - show player head
                List<String> lore = new ArrayList<>();
                lore.add("§7Members: §e" + r.getRealmMembers().size() + "§7/§6" + r.getLevel().getMaxplayer());
                lore.add("§7Level: §e" + r.getLevel().getNumber());
                if (r.getPrivacy()) {
                    lore.add("§cPrivate");
                } else {
                    lore.add("§aPublic");
                }
                lore.add("§eClick to visit!");

                ItemStack head = ItemsUtils.getHead(ownerName, "§b" + ownerName + "'s Realm", lore);
                sortedItems.add(ClickableItem.of(head, e -> {
                    e.setCancelled(true);
                    Player p = (Player) e.getWhoClicked();
                    RealmPlayer clicker = RealmPlayer.getPlayer(p.getUniqueId().toString());

                    if (r.getBanned().contains(clicker) && !p.isOp()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage("§cYou are banned from this realm.");
                        return;
                    }
                    if (r.getPrivacy() && !r.getRealmMembers().contains(clicker) && !p.isOp()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage("§cThis realm is private, just the members of the realm can visit it.");
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    r.teleportToSpawn(p);
                    p.sendMessage("§aTeleporting to " + ownerName + "'s Realm...");
                    p.closeInventory();
                }));
            }
        }

        // Fill row 3 with filler + manage button + navigation
        for (int col = 0; col < 9; col++) {
            contents.set(3, col, filler);
        }

        // Manage own realm button
        if (realmPlayer.getOwned() != null) {
            contents.set(3, 0, ClickableItem.of(
                new ItemsUtils(Material.RED_BED, "§b§lManage your Realm",
                    Arrays.asList("§7Click to manage your Realm settings.")).toItemStack(), e -> {
                e.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                new WholeGUI().openRealmGui(player, realmPlayer.getOwned(), true);
            }));
        }

        if (allRealms.size() <= 27) {
            // No pagination needed - fill rows 0-2
            int col = 0;
            for (ClickableItem ci : sortedItems) {
                contents.set(col / 9, col % 9, ci);
                col++;
            }
            // Fill remaining slots in rows 0-2
            for (int i = col; i < 27; i++) {
                contents.set(i / 9, i % 9, filler);
            }
        } else {
            // Navigation arrows in row 3
            int totalPages = (int) Math.ceil(sortedItems.size() / 27.0);
            if (pagination.getPage() > 0) {
                contents.set(3, 7, ClickableItem.of(
                    new ItemsUtils(Material.ARROW, "§bPrevious page", Arrays.asList("§7Click for previous page")).toItemStack(),
                    e -> {
                        e.setCancelled(true);
                        contents.inventory().open(player, pagination.previous().getPage());
                    }));
            }
            if (pagination.getPage() + 1 < totalPages) {
                contents.set(3, 8, ClickableItem.of(
                    new ItemsUtils(Material.ARROW, "§bNext page", Arrays.asList("§7Click for next page")).toItemStack(),
                    e -> {
                        e.setCancelled(true);
                        contents.inventory().open(player, pagination.next().getPage());
                    }));
            }

            List<ClickableItem> paginated = new ArrayList<>(sortedItems);
            while (paginated.size() % 27 != 0) {
                paginated.add(filler);
            }
            pagination.setItems(paginated.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(27);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
    }
}
