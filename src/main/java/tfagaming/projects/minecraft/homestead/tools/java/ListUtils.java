package tfagaming.projects.minecraft.homestead.tools.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListUtils {
    public static <T> List<T> removeDuplications(List<T> list) {
        Set<T> set = new HashSet<>(list);
        set.clear();
        set.addAll(set);

        return list;
    }

    public static <T> List<T> removeNullElements(List<T> list) {
        List<T> newList = new ArrayList<>();

        for (T element : list) {
            if (element != null) {
                newList.add(element);
            }
        }

        return newList;
    }

    public static void printTable(String[] headers, Object[][] data) {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
            for (Object[] row : data) {
                widths[i] = Math.max(widths[i], row[i].toString().length());
            }
        }

        for (int i = 0; i < headers.length; i++) {
            System.out.printf("%-" + (widths[i] + 2) + "s", headers[i]);
        }
        System.out.println();

        for (int width : widths) {
            System.out.print(String.format("%-" + (width + 2) + "s", "").replace(' ', '-'));
        }
        System.out.println();

        for (Object[] row : data) {
            for (int i = 0; i < row.length; i++) {
                System.out.printf("%-" + (widths[i] + 2) + "s", row[i]);
            }
            System.out.println();
        }
    }
}
