package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Set {
    List<String> getFields();
    boolean isInfinite();
    SetIterator iterator();

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
