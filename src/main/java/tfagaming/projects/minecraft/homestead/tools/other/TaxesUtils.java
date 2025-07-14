package tfagaming.projects.minecraft.homestead.tools.other;

import tfagaming.projects.minecraft.homestead.Homestead;

public class TaxesUtils {
    public static long getNewTaxesAt() {
        if ((boolean) Homestead.config.get("taxes.enabled")) {
            return System.currentTimeMillis() + ((int) Homestead.config.get("taxes.tax-timer") * 1000);
        }

        return 0;
    }
}
