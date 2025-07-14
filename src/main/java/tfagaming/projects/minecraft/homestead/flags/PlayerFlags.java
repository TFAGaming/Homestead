package tfagaming.projects.minecraft.homestead.flags;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class PlayerFlags {
    public static final long BREAK_BLOCKS = 1L; // OK
    public static final long PLACE_BLOCKS = 1L << 1; // OK
    public static final long CONTAINERS = 1L << 2; // OK
    public static final long DOORS = 1L << 3; // OK
    public static final long TRAP_DOORS = 1L << 4; // OK
    public static final long FENCE_GATES = 1L << 5; // OK
    public static final long USE_ANVIL = 1L << 6; // OK
    public static final long REDSTONE = 1L << 7; // OK
    public static final long LEVERS = 1L << 8; // OK
    public static final long BUTTONS = 1L << 9; // OK
    public static final long PRESSURE_PLATES = 1L << 10; // OK
    public static final long USE_BELLS = 1L << 11; // OK
    public static final long TRIGGER_TRIPWIRE = 1L << 12; // OK
    public static final long FROST_WALKER = 1L << 13; // OK
    public static final long HARVEST_CROPS = 1L << 14; // OK
    public static final long BLOCK_TRAMPLING = 1L << 15; // OK
    public static final long GENERAL_INTERACTION = 1L << 16; // OK
    public static final long ARMOR_STANDS = 1L << 17; // OK
    public static final long INTERACT_ENTITIES = 1L << 18; // OK
    public static final long ITEM_FRAME_ROTATION = 1L << 19; // OK
    public static final long DAMAGE_PASSIVE_ENTITIES = 1L << 20; // OK
    public static final long DAMAGE_HOSTILE_ENTITIES = 1L << 21; // OK
    public static final long TRADE_VILLAGERS = 1L << 22; // OK
    public static final long IGNITE = 1L << 23; // OK
    public static final long VEHICLES = 1L << 24; // OK
    public static final long TELEPORT_SPAWN = 1L << 25; // OK
    public static final long PASSTHROUGH = 1L << 26; // OK
    public static final long PVP = 1L << 27; // OK
    public static final long TAKE_FALL_DAMAGE = 1L << 28; // OK
    public static final long TELEPORT = 1L << 29; // OK
    public static final long THROW_POTIONS = 1L << 30; // OK
    public static final long PICKUP_ITEMS = 1L << 31; // OK
    public static final long SLEEP = 1L << 32; // OK
    public static final long TRIGGER_RAID = 1L << 33; // OK
    public static final long ELYTRA = 1L << 34; // OK

    public static List<String> getFlags() {
        return Lists.newArrayList(
                "break-blocks", "place-blocks", "containers", "doors", "trap-doors", "fence-gates", "use-anvil",
                "redstone", "levers", "buttons", "pressure-plates", "use-bells", "trigger-tripwire", "frost-walker",
                "harvest-crops", "block-trampling", "general-interaction", "armor-stands", "interact-entities",
                "item-frame-rotation", "damage-passive-entities", "damage-hostile-entities", "trade-villagers",
                "ignite", "vehicles", "teleport-spawn", "passthrough", "pvp", "take-fall-damage", "teleport",
                "throw-potions", "pickup-items", "sleep", "trigger-raid", "elytra");
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
