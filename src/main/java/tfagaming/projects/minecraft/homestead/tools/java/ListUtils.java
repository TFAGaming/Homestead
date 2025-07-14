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
}
