package com.github.alexishuf.slrpk.algebra.iterators;

import com.github.alexishuf.slrpk.algebra.exceptions.SetIOException;
import com.github.alexishuf.slrpk.Work;

public class ForwardingSetIterator implements SetIterator {
    protected java.util.Iterator<Work> target;

    public ForwardingSetIterator(java.util.Iterator<Work> target) {
        this.target = target;
    }

    @Override
    public void close() throws SetIOException { }

    @Override
    public boolean hasNext() {
        return target.hasNext();
    }

    @Override
    public Work next() {
        return target.next();
    }
}
