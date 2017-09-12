package com.github.alexishuf.slrpk;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Authors extends ArrayList<Author> {
    public Authors(List<Author> list) {
        super(list);
    }

    public static Authors parse(@Nullable String string) {
        if (string == null) return null;
        if (string.trim().isEmpty()) return new Authors(Collections.emptyList());
        String[] authors = string.split(" and ");
        return new Authors(Arrays.stream(authors).map(Author::parse).collect(Collectors.toList()));
    }

    public String getCiteInitials() {
        return stream().map(Author::getCiteInitials).reduce(Authors::join).orElse("");
    }

    public String getCiteFull() {
        return stream().map(Author::getCiteFull).reduce(Authors::join).orElse("");
    }

    public String getFull() {
        return stream().map(Author::getFull).reduce(Authors::join).orElse("");
    }

    public static String join(String left, String right) {
        return left + " and " + right;
    }
}
