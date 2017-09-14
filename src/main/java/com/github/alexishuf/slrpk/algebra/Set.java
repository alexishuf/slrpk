package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Set {
    List<String> getFields();
    boolean isInfinite();
    @Nonnull SetIterator iterator(@Nonnull Map<Set, Set> overrides);

    default @Nonnull SetIterator iterator() {
        return iterator(Collections.emptyMap());
    }

    default List<Work> toList() {
        List<Work> list = new ArrayList<>();
        try (SetIterator it = iterator()) {
            while (it.hasNext())
                list.add(it.next());
        }
        return list;
    }

    default Stream<Work> stream() {
        SetIterator it = iterator();
        int flags = Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, flags), false)
                .onClose(it::close);
    }
}
