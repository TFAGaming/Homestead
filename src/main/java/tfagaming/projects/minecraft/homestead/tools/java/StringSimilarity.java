package tfagaming.projects.minecraft.homestead.tools.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * I used DeepSeek for this.
 */
public class StringSimilarity {
    public static List<String> findTopSimilarStrings(List<String> list, String comparison) {
        if (list == null || comparison == null) {
            throw new IllegalArgumentException("Input list and comparison string cannot be null");
        }

        if (list.size() <= 3) {
            return new ArrayList<>(list);
        }

        List<StringScore> scoredStrings = new ArrayList<>();
        for (String str : list) {
            double score = similarity(str, comparison);
            scoredStrings.add(new StringScore(str, score));
        }

        Collections.sort(scoredStrings, new Comparator<StringScore>() {
            @Override
            public int compare(StringScore o1, StringScore o2) {
                return Double.compare(o2.score, o1.score);
            }
        });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(3, scoredStrings.size()); i++) {
            result.add(scoredStrings.get(i).str);
        }

        return result;
    }

    private static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }

        String longer = s1.length() > s2.length() ? s1 : s2;
        String shorter = s1.length() > s2.length() ? s2 : s1;

        if (longer.length() == 0) {
            return 1.0;
        }

        double distance = levenshteinDistance(longer, shorter);
        return 1.0 - (distance / longer.length());
    }

    private static int levenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    private static class StringScore {
        String str;
        double score;

        StringScore(String str, double score) {
            this.str = str;
            this.score = score;
        }
    }
}
