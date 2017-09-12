package com.github.alexishuf.slrpk;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class Keywords extends ArrayList<String> {
    public Keywords(Collection<? extends String> collection) {
        super(collection);
    }

    public static Keywords parse(@Nonnull String string) {
        return new Keywords(Arrays.stream(string.split("[,;]"))
                .filter(s -> !s.isEmpty()).collect(Collectors.toList()));
    }

    public void merge(@Nonnull Keywords other) {
        Set<String> mine = stream().map(Keywords::simplify).collect(Collectors.toSet());
        other.stream().filter(kw -> !mine.contains(simplify(kw))).forEach(this::add);
    }

    private static String simplify(String string) {
        String victims = "./-+|(){}[]";
        for (int i = 0; i < victims.length(); i++) {
            String c = victims.substring(i, i + 1);
            string = string.replace(c, "");
        }
        return string.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return stream().reduce((l, r) -> l + ", " + r).orElse("");
    }
}
