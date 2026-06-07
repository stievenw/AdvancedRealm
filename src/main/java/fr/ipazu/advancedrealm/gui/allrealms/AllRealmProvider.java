package fr.ipazu.advancedrealm.gui.allrealms;

import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmPlayer;
import fr.ipazu.advancedrealm.utils.ConfigFiles;
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
        List<Realm> allRealms = Realm.allrealm;

        Comparator<Realm> comparator = (a, b) -> {
            boolean aOwned = a.getOwner() == realmPlayer;
            boolean bOwned = b.getOwner() == realmPlayer;
            if (aOwned && !bOwned) return -1;
            if (!aOwned && bOwned) return 1;
            boolean aPrivate = a.getPrivacy();
            boolean bPrivate = b.getPrivacy();
            if (aPrivate && !bPrivate) return 1;
            if (!aPrivate && bPrivate) return -1;
            return a.getOwner().getName().compareToIgnoreCase(b.getOwner().getName());
        };
        allRealms.sort(comparator);

        for (Realm r : allRealms) {
            ClickableItem item;
            boolean isOwned = r.getOwner() == realmPlayer;
            String ownerName = r.getOwner().getName();

            if (r.getPrivacy()) {
                boolean isMember = r.getRealmMembers().contains(realmPlayer);
                if (!isMember && !isOwned) {
                    // Private realm - player can't visit, show red "Private"
                    item = ClickableItem.of(
                        new ItemsUtils(Material.RED_STAINED_GLASS_PANE,
                            "§c§lPrivate",
                            Arrays.asList("§7" + ownerName + "'s Realm", "§cPrivate - No one can visit")).toItemStack(),
                        e -> {
                            e.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            player.sendMessage("§cThis realm is private, just the members of the realm can visit it.");
                        });
                } else {
                    // Private but player is member or owner - can visit
                    item = createVisitItem(r, ownerName, true);
                }
            } else {
                // Public realm
                item = createVisitItem(r, ownerName, false);
            }

            sortedItems.add(item);
        }

        if (allRealms.size() <= 27) {
            contents.fill(filler);
            int col = 0;
            for (ClickableItem ci : sortedItems) {
                contents.set(col / 9, col % 9, ci);
                col++;
            }
        } else {
            ClickableItem next = ClickableItem.of(
                new ItemsUtils(Material.ARROW, "§bNext page", Arrays.asList("§7Click to go to the next page")).toItemStack(),
                e -> {
                    e.setCancelled(true);
                    contents.inventory().open(player, pagination.next().getPage());
                });
            ClickableItem prev = ClickableItem.of(
                new ItemsUtils(Material.ARROW, "§bPrevious page", Arrays.asList("§7Click to go to the previous page")).toItemStack(),
                e -> {
                    e.setCancelled(true);
                    contents.inventory().open(player, pagination.previous().getPage());
                });

            List<ClickableItem> paginated = new ArrayList<>();
            for (ClickableItem ci : sortedItems) {
                paginated.add(ci);
            }
            while (paginated.size() % 27 != 0) {
                paginated.add(filler);
            }
            pagination.setItems(paginated.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(27);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
        }
    }

    private ClickableItem createVisitItem(Realm r, String ownerName, boolean isPrivateMember) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Members: §e" + r.getRealmMembers().size() + "§7/§6" + r.getLevel().getMaxplayer());
        lore.add("§7Level: §e" + r.getLevel().getNumber());
        if (isPrivateMember) {
            lore.add("§cPrivate");
        } else {
            lore.add("§aPublic");
        }
        lore.add("§eClick to visit!");

        ItemStack head = ItemsUtils.getHead(ownerName, "§b" + ownerName + "'s Realm", lore);

        return ClickableItem.of(head, e -> {
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
        });
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
    }
}
