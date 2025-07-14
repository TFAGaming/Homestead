package tfagaming.projects.minecraft.homestead.integrations;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

public class PlaceholderAPI extends PlaceholderExpansion {
    private final Homestead plugin;

    public PlaceholderAPI(Homestead plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "Homestead";
    }

    @Override
    public @NotNull String getAuthor() {
        return "T.F.A";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Region region = TargetRegionSession.getRegion(player);
        Region currentRegion = ChunksManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

        switch (params.toLowerCase()) {
            case "region_bank":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_bank");
                }

                return Formatters.formatBalance(region.getBank());
            case "region_name":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_name");
                }

                return region.getName();
            case "region_claimed_chunks":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_claimed_chunks");
                }

                return String.valueOf(region.getChunks().size());
            case "region_max_chunks":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_max_chunks");
                }

                return String.valueOf(PlayerLimits.getLimitValue(player, PlayerLimits.LimitType.CHUNKS_PER_REGION));
            case "region_trusted_members":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_trusted_members");
                }

                return String.valueOf(region.getMembers().size());
            case "region_max_members":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.region_max_members");
                }

                return String.valueOf(PlayerLimits.getLimitValue(player, PlayerLimits.LimitType.MEMBERS_PER_REGION));
            case "region_current":
                if (currentRegion == null) {
                    return Homestead.config.get("placeholderapi.default.region_current");
                }

                return currentRegion.getName();
            case "upkeep_amount":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.upkeep_amount");
                }

                return Formatters.formatBalance(UpkeepUtils.getAmountToPay(region.getChunks().size()));
            case "upkeep_at":
                if (region == null) {
                    return Homestead.config.get("placeholderapi.default.upkeep_at");
                }

                return Formatters.formatDate(region.getUpkeepAt());
            default:
                return null;
        }
    }
}