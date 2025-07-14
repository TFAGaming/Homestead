package tfagaming.projects.minecraft.homestead.events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.other.TaxesUtils;

public class MemberTaxes {
    public MemberTaxes(Homestead instance) {
        for (Region region : RegionsManager.getAll()) {
            double amountToPay = region.getTaxesAmount();

            if (amountToPay == 0) {
                continue;
            }

            for (SerializableMember member : region.getMembers()) {
                if (member.getTaxesAt() == 0) {
                    region.setMemberTaxesAt(member, TaxesUtils.getNewTaxesAt());

                    continue;
                }

                if (System.currentTimeMillis() >= member.getTaxesAt()) {
                    OfflinePlayer targetPlayer = member.getBukkitOfflinePlayer();

                    if (PlayerUtils.getBalance(targetPlayer) >= amountToPay) {
                        PlayerUtils.removeBalance(targetPlayer, amountToPay);
                        region.addBalanceToBank(amountToPay);

                        region.setMemberTaxesAt(member, TaxesUtils.getNewTaxesAt());

                        if (targetPlayer.isOnline()) {
                            Player targetPlayerOnline = (Player) targetPlayer;

                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{amount}", Formatters.formatBalance(amountToPay));
                            replacements.put("{region}", region.getName());
                            replacements.put("{balance}",
                                    Formatters.formatBalance(PlayerUtils.getBalance(targetPlayer)));

                            PlayerUtils.sendMessage(targetPlayerOnline, 106, replacements);
                            PlayerUtils.sendMessage(targetPlayerOnline, 107, replacements);
                        }
                    } else {
                        region.removeMember(targetPlayer);

                        if (targetPlayer.isOnline()) {
                            Player targetPlayerOnline = (Player) targetPlayer;
                            Map<String, String> replacements = new HashMap<String, String>();
                            replacements.put("{region}", region.getName());

                            PlayerUtils.sendMessage(targetPlayerOnline, 108, replacements);
                        }

                        Map<String, String> replacements = new HashMap<String, String>();
                        replacements.put("{playername}", member.getBukkitOfflinePlayer().getName());

                        RegionsManager.addNewLog(region.getUniqueId(), 5, replacements);
                    }
                }
            }
        }
    }
}
