package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Author;
import com.github.alexishuf.slrpk.Authors;
import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class Helpers {
    public static  @Nonnull Map<Set, Set> selectOverrideMap(@Nullable Map<Set, Set> a, @Nullable Map<Set, Set> b) {
        if (a == null || a.isEmpty()) {
            return b == null ? Collections.emptyMap() : b;
        } else if (b == null || b.isEmpty()) {
            return a;
        } else {
            Map<Set, Set> merged = new HashMap<>(a);
            merged.putAll(b);
            return merged;
        }
    }

    public static void appendUnique(@Nonnull List<Work> list, @Nonnull Set set,
                                    @Nonnull BiFunction<Work, Work, Work> improve) {
        try (SetIterator it = set.iterator()) {
            it.forEachRemaining(w -> {
                for (int i = 0; i < list.size(); i++) {
                    if (w.matches(list.get(i))) {
                        list.set(i, improve.apply(list.get(i), w));
                        return;
                    }
                }
                list.add(w);
            });
        }
    }

    public static @Nonnull Function<Work, Work> matchFinder(@Nonnull Set set) {
        if (ComplementSet.isComplement(set)) {
            List<Work> list = ComplementSet.wrap(set).toList();
            return w -> list.stream().noneMatch(w::matches) ? w : null;
        } else {
            List<Work> list = set.toList();
            return w -> list.stream().filter(w::matches).findFirst().orElse(null);
        }
    }

    public static @Nonnull Set firstFinite(@Nonnull Set... sets) {
        for (Set set : sets) {
            if (!set.isInfinite())
                return set;
        }
        throw new NoSuchElementException();
    }

    public static @Nonnull Work improve(@Nonnull Work left, @Nonnull Work right) {
        String id = nonEmpty(left.getId(), right.getId());
        String title = longer(left.getTitle(), right.getTitle());
        String abs = longer(left.getAbstract(), right.getAbstract());
        String doi = nonEmpty(left.getDOI(), right.getDOI());
        String authors = nonEmpty(left.getAuthor(), right.getAuthor());

        Authors lAuthors = Authors.parse(authors);
        Authors rAuthors = Authors.parse(right.getAuthor());
        if (lAuthors != null && rAuthors != null) {
            //add missing authors
            boolean update = rAuthors.size() > lAuthors.size();
            if (update) {
                Authors tmp = lAuthors;
                lAuthors = rAuthors;
                rAuthors = tmp;
            }

            //add missing full given names
            for (int i = 0; i < lAuthors.size(); i++) {
                Author a = lAuthors.get(i);
                List<Author> candidates = rAuthors.stream().filter(o -> o.surname.equals(a.surname))
                        .collect(Collectors.toList());
                if (candidates.size() != 1) continue;
                if (candidates.get(0).fullGivenNamesRatio() > a.fullGivenNamesRatio()) {
                    update = true;
                    lAuthors.set(i, candidates.get(0));
                }
            }
            if (update) authors = lAuthors.getCiteFull();
        }

        Work improved = new Work(left);
        improved.set(Work.Field.Id, id);
        improved.set(Work.Field.Title, title);
        improved.set(Work.Field.Abstract, abs);
        improved.set(Work.Field.DOI, doi);
        improved.set(Work.Field.Author, authors);
        mergeExtraFields(improved, right);
        return improved;

    }

    public static List<String> mergeFields(@Nonnull Set left, @Nonnull Set right) {
        ArrayList<String> list = new ArrayList<>(left.getFields());
        list.addAll(additionalFields(left, right));
        return list;
    }

    public static List<String> additionalFields(@Nonnull Set left, @Nonnull Set right) {
        List<String> l = left.getFields();
        return right.getFields().stream().filter(h -> !l.contains(h)).collect(Collectors.toList());
    }

    private static void mergeExtraFields(Work left, Work right) {
        right.getExtraFields().stream().filter(h -> !left.isMapped(h))
                .forEach(h -> left.put(h, right.get(h)));
    }

    private static String longer(String a, String b) {
        if (a == null && b != null) return b;
        else if (a != null && b == null) return a;
        else if (a == null) return null;
        else return b.length() > a.length() ? b : a;
    }

    private static String nonEmpty(String a, String b) {
        if (a == null && b != null) return b;
        else if (a != null && b == null) return a;
        else if (a == null) return null;
        else if (a.isEmpty() && !b.isEmpty()) return b;
        else return a;
    }

    public static void addFields(List<Work> list, List<String> allFields) {
        Function<Work, Work> adder = Work.fieldsAdder(allFields);
        for (int i = 0; i < list.size(); i++)
            list.set(i, adder.apply(list.get(i)));
    }
}
