package tfagaming.projects.minecraft.homestead.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.other.TaxesUtils;

public class Region {
    public UUID id;
    public String displayName;
    public String name;
    public String description;
    public UUID ownerId;
    public SerializableLocation location;
    public long createdAt;
    public long playerFlags;
    public long worldFlags;
    public double bank;
    public int mapColor;
    public List<SerializableChunk> chunks = new ArrayList<>();
    public List<SerializableMember> members = new ArrayList<>();
    public List<SerializableRate> rates = new ArrayList<>();
    public List<UUID> invitedPlayers = new ArrayList<>();
    public List<SerializableBannedPlayer> bannedPlayers = new ArrayList<>();
    public List<SerializableLog> logs = new ArrayList<>();
    public List<SerializableSubArea> subAreas = new ArrayList<>();
    public SerializableRent rent;
    public long upkeepAt;
    public double taxesAmount;
    public int weather;
    public int time;
    public SerializableLocation welcomeSign;
    public String icon;

    public Region(String name, OfflinePlayer player) {
        this.id = UUID.randomUUID();
        this.displayName = name;
        this.name = name;
        this.description = ((String) Homestead.language.get("default.description")).replace("{owner}",
                player.getName() == null ? "Unknown" : player.getName());
        this.ownerId = player.getUniqueId();
        this.location = new SerializableLocation(player.getLocation());
        this.createdAt = System.currentTimeMillis();
        this.playerFlags = Homestead.config.getDefaultPlayerFlags();
        this.worldFlags = Homestead.config.getDefaultWorldFlags();
        this.upkeepAt = 0;
        this.taxesAmount = 0;
        this.welcomeSign = null;
        this.mapColor = 0;
    }

    public UUID getUniqueId() {
        return id;
    }

    // Name and displayname
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateCache();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateCache();
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateCache();
    }

    // Owner
    public UUID getOwnerId() {
        return ownerId;
    }

    public OfflinePlayer getOwner() {
        return Homestead.getInstance().getOfflinePlayerSync(ownerId);
    }

    public void setOwner(OfflinePlayer player) {
        this.ownerId = player.getUniqueId();
        updateCache();
    }

    // Location
    public SerializableLocation getLocation() {
        return location;
    }

    public void setLocation(SerializableLocation location) {
        this.location = location;
        updateCache();
    }

    // Welcome sign
    public SerializableLocation getWelcomeSign() {
        return welcomeSign;
    }

    public void setWelcomeSign(SerializableLocation location) {
        this.welcomeSign = location;
        updateCache();
    }

    // Creation date
    public long getCreatedAt() {
        return createdAt;
    }

    // Upkeep
    public void setUpkeepAt(long at) {
        this.upkeepAt = at;
        updateCache();
    }

    public long getUpkeepAt() {
        return upkeepAt;
    }

    // Taxes
    public void setTaxesAmount(double amount) {
        this.taxesAmount = amount;
        updateCache();
    }

    public double getTaxesAmount() {
        return taxesAmount;
    }

    // Client-side weather and time
    public void setWeather(int weather) {
        this.weather = weather;
        updateCache();
    }

    public int getWeather() {
        return weather;
    }

    public void setTime(int time) {
        this.time = time;
        updateCache();
    }

    public int getTime() {
        return time;
    }

    // Flags
    public long getPlayerFlags() {
        return playerFlags;
    }

    public void setPlayerFlags(long flags) {
        this.playerFlags = flags;
        updateCache();
    }

    public boolean isPlayerFlagSet(long flag) {
        return FlagsCalculator.isFlagSet(playerFlags, flag);
    }

    public long getWorldFlags() {
        return worldFlags;
    }

    public void setWorldFlags(long flags) {
        this.worldFlags = flags;
        updateCache();
    }

    public boolean isWorldFlagSet(long flag) {
        return FlagsCalculator.isFlagSet(worldFlags, flag);
    }

    // Bank
    public double getBank() {
        return bank;
    }

    public void setBank(double money) {
        this.bank = money;
        updateCache();
    }

    public void addBalanceToBank(double money) {
        this.bank += money;
        updateCache();
    }

    public void removeBalanceFromBank(double money) {
        this.bank -= money;

        if (this.bank < 0) {
            this.bank = 0;
        }

        updateCache();
    }

    // Map color
    public int getMapColor() {
        return mapColor;
    }

    public void setMapColor(int hexColorCode) {
        this.mapColor = hexColorCode;
        updateCache();
    }

    // Chunks
    public List<SerializableChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<SerializableChunk> chunks) {
        this.chunks = chunks;
        updateCache();
    }

    public void addChunk(SerializableChunk chunk) {
        if (!chunks.contains(chunk)) {
            chunks.add(chunk);
            updateCache();
        }
    }

    public void removeChunk(SerializableChunk chunkToRemove) {
        for (int i = 0; i < chunks.size(); i++) {
            SerializableChunk chunk = chunks.get(i);

            if (chunk.toString(true).equals(chunkToRemove.toString(true))) {
                chunks.remove(i);

                updateCache();

                break;
            }
        }
    }

    // Members
    public List<SerializableMember> getMembers() {
        return members;
    }

    public void setMembers(List<SerializableMember> players) {
        this.members = players;
        updateCache();
    }

    public void addMember(OfflinePlayer player) {
        if (!isPlayerMember(player)) {
            SerializableMember member = new SerializableMember(player, playerFlags, 0);

            member.setTaxesAt(TaxesUtils.getNewTaxesAt());

            members.add(member);
            updateCache();
        }
    }

    public SerializableMember getMember(OfflinePlayer player) {
        for (SerializableMember member : members) {
            if (member.getPlayerId().equals(player.getUniqueId())) {
                return member;
            }
        }

        return null;
    }

    public void setMemberFlags(SerializableMember member, long flags) {
        for (int i = 0; i < members.size(); i++) {
            SerializableMember data = members.get(i);

            if (data.getPlayerId().equals(member.getPlayerId())) {
                data.setFlags(flags);
                members.set(i, data);

                updateCache();

                break;
            }
        }
    }

    public void setMemberRegionControlFlags(SerializableMember member, long flags) {
        for (int i = 0; i < members.size(); i++) {
            SerializableMember data = members.get(i);

            if (data.getPlayerId().equals(member.getPlayerId())) {
                data.setRegionControlFlags(flags);
                members.set(i, data);

                updateCache();

                break;
            }
        }
    }

    public void setMemberTaxesAt(SerializableMember member, long taxesAt) {
        for (int i = 0; i < members.size(); i++) {
            SerializableMember data = members.get(i);

            if (data.getPlayerId().equals(member.getPlayerId())) {
                data.setTaxesAt(taxesAt);
                members.set(i, data);

                updateCache();

                break;
            }
        }
    }

    public boolean isPlayerMember(OfflinePlayer player) {
        for (SerializableMember member : members) {
            if (member.getPlayerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public void removeMember(OfflinePlayer player) {
        for (int i = 0; i < members.size(); i++) {
            SerializableMember member = members.get(i);

            if (member.getPlayerId().equals(player.getUniqueId())) {
                members.remove(i);

                updateCache();

                break;
            }
        }
    }

    public void removeMember(SerializableMember player) {
        for (int i = 0; i < members.size(); i++) {
            SerializableMember member = members.get(i);

            if (member.getPlayerId().equals(player.getPlayerId())) {
                members.remove(i);

                updateCache();

                break;
            }
        }
    }

    // Rates
    public List<SerializableRate> getRates() {
        return rates;
    }

    public void addPlayerRate(OfflinePlayer player, int rate) {
        if (isPlayerRated(player)) {
            setPlayerRateValue(player, rate);
        } else {
            SerializableRate newRate = new SerializableRate(player, rate);

            rates.add(newRate);
            updateCache();
        }
    }

    public void setPlayerRateValue(OfflinePlayer player, int rate) {
        for (int i = 0; i < rates.size(); i++) {
            SerializableRate data = rates.get(i);

            if (data.getPlayerId().equals(player.getUniqueId())) {
                data.setRate(rate);
                rates.set(i, data);

                updateCache();

                break;
            }
        }
    }

    public boolean isPlayerRated(OfflinePlayer player) {
        List<SerializableRate> rates = getRates();

        for (SerializableRate rate : rates) {
            if (rate.getPlayerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public SerializableRate getPlayerRate(OfflinePlayer player) {
        List<SerializableRate> rates = getRates();

        for (SerializableRate rate : rates) {
            if (rate.getPlayerId().equals(player.getUniqueId())) {
                return rate;
            }
        }

        return null;
    }

    public void setRates(List<SerializableRate> rates) {
        this.rates = rates;
        updateCache();
    }

    // Invited players
    public List<OfflinePlayer> getInvitedPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();

        for (int i = 0; i < invitedPlayers.size(); i++) {
            UUID playerId = invitedPlayers.get(i);

            OfflinePlayer player = Homestead.getInstance().getOfflinePlayerSync(playerId);

            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }

    public void setInvitedPlayers(List<OfflinePlayer> players) {
        this.invitedPlayers = players.stream().map((player) -> player.getUniqueId()).collect(Collectors.toList());
        updateCache();
    }

    public boolean isPlayerInvited(OfflinePlayer player) {
        List<OfflinePlayer> invitedPlayers = getInvitedPlayers();

        for (OfflinePlayer invitedPlayer : invitedPlayers) {
            if (invitedPlayer.getUniqueId().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public void addPlayerInvite(OfflinePlayer player) {
        if (!isPlayerInvited(player)) {
            invitedPlayers.add(player.getUniqueId());
            updateCache();
        }
    }

    public void removePlayerInvite(OfflinePlayer player) {
        for (int i = 0; i < invitedPlayers.size(); i++) {
            UUID playerUuid = invitedPlayers.get(i);

            if (playerUuid.equals(player.getUniqueId())) {
                invitedPlayers.remove(i);

                updateCache();

                break;
            }
        }
    }

    // Banned players
    public List<SerializableBannedPlayer> getBannedPlayers() {
        return bannedPlayers;
    }

    public void setBannedPlayers(List<SerializableBannedPlayer> bannedPlayers) {
        this.bannedPlayers = bannedPlayers;
        updateCache();
    }

    public SerializableBannedPlayer getBannedPlayer(OfflinePlayer player) {
        for (SerializableBannedPlayer bannedPlayer : bannedPlayers) {
            if (bannedPlayer.getPlayerId().equals(player.getUniqueId())) {
                return bannedPlayer;
            }
        }

        return null;
    }

    public boolean isPlayerBanned(OfflinePlayer player) {
        List<SerializableBannedPlayer> bannedPlayers = getBannedPlayers();

        for (SerializableBannedPlayer bannedPlayer : bannedPlayers) {
            if (bannedPlayer.getPlayerId().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public void banPlayer(OfflinePlayer player, String reason) {
        if (!isPlayerBanned(player)) {
            bannedPlayers.add(new SerializableBannedPlayer(player, reason));
            updateCache();
        }
    }

    public void banPlayer(OfflinePlayer player) {
        if (!isPlayerBanned(player)) {
            bannedPlayers.add(new SerializableBannedPlayer(player, Homestead.language.get("default.reason")));
            updateCache();
        }
    }

    public void unbanPlayer(OfflinePlayer player) {
        for (int i = 0; i < bannedPlayers.size(); i++) {
            SerializableBannedPlayer playerData = bannedPlayers.get(i);

            if (playerData.getPlayerId().equals(player.getUniqueId())) {
                bannedPlayers.remove(i);

                updateCache();

                break;
            }
        }
    }

    // Logs
    public List<SerializableLog> getLogs() {
        return logs;
    }

    public void setLogs(List<SerializableLog> logs) {
        this.logs = logs;
        updateCache();
    }

    public void addLog(SerializableLog log) {
        if (logs.stream().anyMatch(l -> l.getId().equals(log.getId()))) {
            return;
        }

        if (logs.size() >= 150) {
            removeOldestLog();
        }

        logs.add(log);
        updateCache();
    }

    public void setLogAsRead(UUID logId) {
        for (int i = 0; i < logs.size(); i++) {
            SerializableLog log = logs.get(i);

            if (log.getId().equals(logId)) {
                log.setRead(true);
                logs.set(i, log);

                updateCache();

                break;
            }
        }
    }

    public void removeLog(UUID logId) {
        for (int i = 0; i < logs.size(); i++) {
            SerializableLog log = logs.get(i);

            if (log.getId().equals(logId)) {
                logs.remove(i);

                updateCache();

                break;
            }
        }
    }

    private void removeOldestLog() {
        if (logs.isEmpty()) {
            return;
        }

        SerializableLog oldest = logs.stream()
                .min(Comparator.comparingLong(SerializableLog::getSentAt))
                .orElse(null);

        if (oldest != null) {
            logs.remove(oldest);
        }
    }

    // Sub-Areas
    public List<SerializableSubArea> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(List<SerializableSubArea> subAreas) {
        this.subAreas = subAreas;
        updateCache();
    }

    public SerializableSubArea getSubArea(UUID subAreaId) {
        for (SerializableSubArea subArea : subAreas) {
            if (subArea.getId().equals(subAreaId)) {
                return subArea;
            }
        }

        return null;
    }

    public SerializableSubArea getSubArea(String name) {
        for (SerializableSubArea subArea : subAreas) {
            if (subArea.getName().equalsIgnoreCase(name)) {
                return subArea;
            }
        }

        return null;
    }

    public boolean isSubAreaNameUsed(String name) {
        for (SerializableSubArea subArea : subAreas) {
            if (subArea.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public SerializableSubArea findSubAreaHasBlockInside(Block block) {
        return findSubAreaHasLocationInside(block.getLocation());
    }

    public SerializableSubArea findSubAreaHasLocationInside(Location location) {
        for (int i = 0; i < subAreas.size(); i++) {
            SerializableSubArea subArea = subAreas.get(i);

            if (subArea.isLocationInside(location)) {
                return subArea;
            }
        }

        return null;
    }

    public void addSubArea(SerializableSubArea subArea) {
        if (!subAreas.stream().map(SerializableSubArea::getId).collect(Collectors.toList()).contains(subArea.getId())) {
            subAreas.add(subArea);
            updateCache();
        }
    }

    public void setSubAreaName(UUID subAreaId, String name) {
        for (int i = 0; i < subAreas.size(); i++) {
            SerializableSubArea subArea = subAreas.get(i);

            if (subArea.getId().equals(subAreaId)) {
                subArea.setName(name);
                subAreas.set(i, subArea);

                updateCache();

                break;
            }
        }
    }

    public void setSubAreaFlags(UUID subAreaId, long flags) {
        for (int i = 0; i < subAreas.size(); i++) {
            SerializableSubArea subArea = subAreas.get(i);

            if (subArea.getId().equals(subAreaId)) {
                subArea.setFlags(flags);
                subAreas.set(i, subArea);

                updateCache();

                break;
            }
        }
    }

    public void removeSubArea(UUID subAreaId) {
        for (int i = 0; i < subAreas.size(); i++) {
            SerializableSubArea subArea = subAreas.get(i);

            if (subArea.getId().equals(subAreaId)) {
                subAreas.remove(i);

                updateCache();

                break;
            }
        }
    }

    // Rent
    public SerializableRent getRent() {
        return rent;
    }

    public void setRent(SerializableRent rent) {
        this.rent = rent;
        updateCache();
    }

    // Icon
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // Other
    public void updateCache() {
        Homestead.regionsCache.putOrUpdate(this);
    }
}
