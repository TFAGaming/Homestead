package tfagaming.projects.minecraft.homestead.flags;

public class FlagsCalculator {
    public static long addFlag(long... flags) {
        long combined = 0;

        for (long flag : flags) {
            combined |= flag;
        }

        return combined;
    }

    public static long removeFlag(long flags, long flag) {
        return flags & ~flag;
    }

    public static boolean isFlagSet(long flags, long flag) {
        return (flags & flag) != 0;
    }
}