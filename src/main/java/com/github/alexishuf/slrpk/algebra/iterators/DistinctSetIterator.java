package com.github.alexishuf.slrpk.algebra.iterators;

import com.github.alexishuf.slrpk.Work;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DistinctSetIterator extends ForwardingSetIterator {
    private List<Work> history;
    private Work next = null;

    public DistinctSetIterator(Iterator<Work> target) {
        super(target);
        history = new ArrayList<>();
    }

    @Override
    public Work next() {
        if (!hasNext())
            throw new NoSuchElementException();
        Work current = this.next;
        this.next = null;
        return current;
    }

    @Override
    public boolean hasNext() {
        while (this.next == null && target.hasNext()) {
            Work next = target.next();
            boolean isLarge = history.size() > 8*Runtime.getRuntime().availableProcessors();
            if ((isLarge ? history.parallelStream() : history.stream()).noneMatch(next::matches)) {
                history.add(next);
                this.next = next;
            }
        }
        return this.next != null;
    }
}
