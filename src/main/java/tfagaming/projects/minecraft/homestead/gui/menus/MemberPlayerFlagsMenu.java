package tfagaming.projects.minecraft.homestead.gui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.PaginationMenu;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.menus.MenuUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class MemberPlayerFlagsMenu {
    private final HashSet<UUID> cooldowns = new HashSet<>();

    // index 0 = Bulk item; alle anderen Items sind die eigentlichen Flags (+1 Index-Offset)
    private static final int BULK_INDEX = 0;

    public MemberPlayerFlagsMenu(Player player, Region region, SerializableMember member) {
        List<ItemStack> items = buildItemsList(member);

        PaginationMenu gui = new PaginationMenu(
                MenuUtils.getTitle(6).replace("{playername}", member.getBukkitOfflinePlayer().getName()),
                9 * 5,
                MenuUtils.getNextPageButton(),
                MenuUtils.getPreviousPageButton(),
                items,
                (_player, event) -> new RegionMembersMenu(player, region),
                (_player, context) -> {
                    if (cooldowns.contains(player.getUniqueId())) return;

                    if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                            RegionControlFlags.SET_MEMBER_FLAGS)) {
                        return;
                    }

                    int index = context.getIndex();

                    // === Bulk toggle item ===
                    if (index == BULK_INDEX) {
                        boolean enableAll = context.getEvent().isLeftClick();
                        boolean disableAll = context.getEvent().isRightClick();

                        if (!enableAll && !disableAll) return;

                        @SuppressWarnings("unchecked")
                        List<String> disabledFlags = Homestead.config.get("disabled-flags");

                        long current = member.getFlags();
                        long newFlags = current;

                        int changed = 0;
                        for (String flagString : PlayerFlags.getFlags()) {
                            if (disabledFlags.contains(flagString)) continue; // locked -> skip
                            long flag = PlayerFlags.valueOf(flagString);

                            boolean isSet = FlagsCalculator.isFlagSet(newFlags, flag);
                            if (enableAll && !isSet) {
                                newFlags = FlagsCalculator.addFlag(newFlags, flag);
                                changed++;
                            } else if (disableAll && isSet) {
                                newFlags = FlagsCalculator.removeFlag(newFlags, flag);
                                changed++;
                            }
                        }

                        if (changed > 0) {
                            region.setMemberFlags(member, newFlags);
                            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);
                            player.sendMessage(ChatColor.GREEN + "Bulk change applied: "
                                    + ChatColor.WHITE + changed + ChatColor.GREEN + " flag(s) "
                                    + (enableAll ? "enabled" : "disabled")
                                    + ChatColor.GRAY + " (locked flags were skipped).");

                            // UI neu aufbauen
                            PaginationMenu instance = context.getInstance();
                            instance.setItems(buildItemsList(member));

                            cooldowns.add(player.getUniqueId());
                            Homestead.getInstance().runAsyncTaskLater(() ->
                                    cooldowns.remove(player.getUniqueId()), 1);
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "No changes were made (either already in desired state or all relevant flags are locked).");
                        }
                        return;
                    }

                    // === Einzelnes Flag toggeln ===
                    int flagListIndex = index - 1; // wegen Bulk-Item
                    if (flagListIndex < 0 || flagListIndex >= PlayerFlags.getFlags().size()) return;

                    String flagString = PlayerFlags.getFlags().get(flagListIndex);

                    @SuppressWarnings("unchecked")
                    List<String> disabledFlags = Homestead.config.get("disabled-flags");
                    if (disabledFlags.contains(flagString)) {
                        PlayerUtils.sendMessage(player, 42);
                        return;
                    }

                    long flag = PlayerFlags.valueOf(flagString);

                    if (context.getEvent().isLeftClick()) {
                        PaginationMenu instance = context.getInstance();

                        long flags = member.getFlags();

                        boolean isSet = FlagsCalculator.isFlagSet(flags, flag);
                        long newFlags;

                        if (isSet) {
                            newFlags = FlagsCalculator.removeFlag(flags, flag);
                        } else {
                            newFlags = FlagsCalculator.addFlag(flags, flag);
                        }

                        region.setMemberFlags(member, newFlags);

                        cooldowns.add(player.getUniqueId());

                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 500.0f, 1.0f);

                        Map<String, String> replacements = new HashMap<>();
                        replacements.put("{flag}", flagString);
                        replacements.put("{state}", Formatters.getFlag(!isSet));
                        replacements.put("{region}", region.getName());
                        replacements.put("{player}", member.getBukkitOfflinePlayer().getName());

                        PlayerUtils.sendMessage(player, 43, replacements);

                        instance.replaceSlot(index, MenuUtils.getFlagButton(flagString, !isSet));

                        Homestead.getInstance().runAsyncTaskLater(() -> cooldowns.remove(player.getUniqueId()), 1);
                    }
                });

        gui.open(player, MenuUtils.getEmptySlot());
    }

    private List<ItemStack> buildItemsList(SerializableMember member) {
        List<ItemStack> items = new ArrayList<>();

        // Bulk toggle item (emerald block icon)
        ItemStack bulk = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = bulk.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Bulk toggle flags");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Toggle all flags at once");
        lore.add(ChatColor.DARK_GRAY + "(locked flags in config are skipped).");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click: " + ChatColor.WHITE + "Enable all");
        lore.add(ChatColor.YELLOW + "Right-click: " + ChatColor.WHITE + "Disable all");
        meta.setLore(lore);
        bulk.setItemMeta(meta);
        items.add(bulk);

        // Einzelne Flag-Buttons
        for (String flagString : PlayerFlags.getFlags()) {
            boolean value = FlagsCalculator.isFlagSet(member.getFlags(), PlayerFlags.valueOf(flagString));
            items.add(MenuUtils.getFlagButton(flagString, value));
        }
        return items;
    }
}
