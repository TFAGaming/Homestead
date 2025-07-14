package tfagaming.projects.minecraft.homestead.tools.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoCompleteFilter {
    public static List<String> filter(List<String> suggestions, String[] args) {
        // Remove duplications
        Set<String> set = new HashSet<>(suggestions);
        suggestions.clear();
        suggestions.addAll(set);
        
        // Filtering
        List<String> filtered = new ArrayList<String>();

        for (String suggestion : suggestions) {
            if (suggestion == null) {
                continue;
            }

            if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                filtered.add(suggestion);
            }
        }

        if (filtered.size() == 0) {
            filtered.add("?");
        }

        return filtered;
    }
}
