package tfagaming.projects.minecraft.homestead.flags;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class RegionControlFlags {
    public static final long TRUST_PLAYERS = 1L; // OK
    public static final long UNTRUST_PLAYERS = 1L << 1L; // OK
    public static final long BAN_PLAYERS = 1L << 2L; // OK
    public static final long UNBAN_PLAYERS = 1L << 3L; // OK
    public static final long CLAIM_CHUNKS = 1L << 4L; // OK
    public static final long UNCLAIM_CHUNKS = 1L << 5L; // OK
    public static final long DEPOSIT_MONEY = 1L << 6L; // OK
    public static final long WITHDRAW_MONEY = 1L << 7L; // OK
    public static final long SET_GLOBAL_FLAGS = 1L << 8L; // OK
    public static final long SET_WORLD_FLAGS = 1L << 9L; // OK
    public static final long SET_MEMBER_FLAGS = 1L << 10L; // OK
    public static final long SET_SPAWN = 1L << 11L; // TODO
    public static final long MANAGE_SUBAREAS = 1L << 12L; // OK
    public static final long RENAME_REGION = 1L << 13L; // OK
    public static final long SET_DESCRIPTION = 1L << 14L; // OK
    public static final long MANAGE_LOGS = 1L << 15L; // TODO
    public static final long KICK_PLAYERS = 1L << 16L; // TODO
    public static final long SET_WEATHER_AND_TIME = 1L << 17L; // TODO

    public static List<String> getFlags() {
        return Lists.newArrayList("trust-players", "untrust-players", "ban-players", "unban-players", "claim-chunks",
                "unclaim-chunks", "deposit-money", "withdraw-money", "set-global-flags", "set-world-flags",
                "set-member-flags", "set-spawn", "manage-subareas", "rename-region", "set-description", "manage-logs",
                "kick-players", "set-weather-and-time");
    }

    public static long valueOf(String name) {
        List<String> flags = getFlags();

        if (flags.indexOf(name) == 0) {
            return 1;
        } else {
            return 1L << (flags.indexOf(name));
        }
    }

    public static String from(long flag) {
        List<String> flags = getFlags();

        for (int i = 0; i < flags.size(); i++) {
            long value = valueOf(flags.get(i));

            if (value == flag) {
                return flags.get(i);
            }
        }

        return "unknown-flag";
    }

    public static List<String> getSet(long flags) {
        List<String> allFlags = getFlags();
        List<String> enabledFlags = new ArrayList<String>();

        for (String flag : allFlags) {
            long flagValue = valueOf(flag);

            if (FlagsCalculator.isFlagSet(flags, flagValue)) {
                enabledFlags.add(flag);
            }
        }

        return enabledFlags;
    }

    public static List<String> getUnset(long flags) {
        List<String> allFlags = getFlags();
        List<String> disabledFlags = new ArrayList<String>();

        for (String flag : allFlags) {
            long flagValue = valueOf(flag);

            if (!FlagsCalculator.isFlagSet(flags, flagValue)) {
                disabledFlags.add(flag);
            }
        }

        return disabledFlags;
    }
}
