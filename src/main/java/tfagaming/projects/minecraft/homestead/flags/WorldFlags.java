package tfagaming.projects.minecraft.homestead.flags;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class WorldFlags {
    public static final long PASSIVE_ENTITIES_SPAWN = 1L; // OK
    public static final long HOSTILE_ENTITIES_SPAWN = 1L << 1L; // OK
    public static final long ENTITIES_GRIEF = 1L << 2; // OK
    public static final long ENTITIES_DAMAGE_ENTITIES = 1L << 3; // OK
    public static final long LEAVES_DECAY = 1L << 4; // OK
    public static final long FIRE_SPREAD = 1L << 5; // OK
    public static final long LIQUID_FLOW = 1L << 6; // OK
    public static final long EXPLOSIONS_DAMAGE = 1L << 7; // OK
    public static final long WITHER_DAMAGE = 1L << 8; // OK
    public static final long WILDERNESS_PISTONS = 1L << 9; // OK
    public static final long WILDERNESS_DISPENSERS = 1L << 10; // OK
    public static final long WILDERNESS_MINECARTS = 1L << 11; // OK
    public static final long PLANT_GROWTH = 1L << 12; // OK
    public static final long GRASS_GROWTH = 1L << 13; // OK
    public static final long SCULK_SPREAD = 1L << 14; // OK
    public static final long PLAYER_GLOWING = 1L << 15; // OK
    public static final long SNOW_MELTING = 1L << 16; // OK
    public static final long ICE_MELTING = 1L << 17; // OK
    public static final long SNOWMAN_TRAILS = 1L << 18; // OK
    public static final long WINDCHARGE_BURST = 1L << 19; // OK
    public static final long WILDERNESS_COPPER_GOLEMS = 1L << 20; // OK

    public static List<String> getFlags() {
        return Lists.newArrayList("passive-entities-spawn", "hostile-entities-spawn", "entities-grief",
                "entities-damage-entities", "leaves-decay", "fire-spread", "liquid-flow", "explosions-damage",
                "wither-damage", "wilderness-pistons", "wilderness-dispensers", "wilderness-minecarts", "plant-growth",
                "grass-growth", "sculk-spread", "player-glowing", "snow-melting", "ice-melting", "snowman-trails",
                "windcharge-burst", "wilderness-copper-golems");
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
