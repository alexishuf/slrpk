package com.github.alexishuf.slrpk.algebra.predicates;

import com.github.alexishuf.slrpk.Work;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FieldPredicates {
    public static Predicate<Work> createEquals(@Nonnull String field, @Nullable String value) {
        return w -> Objects.equals(w.get(field), value);
    }

    public static Predicate<Work> createNotEquals(@Nonnull String field, @Nullable String value) {
        return w -> !Objects.equals(w.get(field), value);
    }

    public static Predicate<Work> createMatches(@Nonnull String field, @Nonnull Pattern pattern) {
        return w -> pattern.matcher(orEmpty(w.get(field))).matches();
    }

    public static Predicate<Work> createNotMatches(@Nonnull String field, @Nonnull Pattern pattern){
        return w -> !pattern.matcher(orEmpty(w.get(field))).matches();
    }

    public static Predicate<Work> createNull(String field) {
        return w -> w.get(field) == null;
    }

    public static Predicate<Work> createNotNull(String field) {
        return w -> w.get(field) != null;
    }

    private static String orEmpty(String string) {
        return string == null ? "" : string;
    }
}
