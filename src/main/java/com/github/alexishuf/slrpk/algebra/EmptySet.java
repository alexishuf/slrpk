package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmptySet extends AbstractSet {
    public EmptySet() {
        super(false);
    }

    @Override
    public List<String> getFields() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        return new ForwardingSetIterator(Collections.emptyIterator());
    }
}
