package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import java.util.Collections;
import java.util.List;

public class EmptySet implements Set {
    @Override
    public List<String> getFields() {
        return Collections.emptyList();
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public SetIterator iterator() {
        return new ForwardingSetIterator(Collections.emptyIterator());
    }
}
