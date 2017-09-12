package com.github.alexishuf.slrpk.algebra.iterators;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.exceptions.SetIOException;
import com.github.alexishuf.slrpk.algebra.exceptions.InterpretationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public abstract class ClosingSetIterator extends ForwardingSetIterator {
    private final Closeable[] closeables;

    public ClosingSetIterator(Iterator<Work> target, Closeable... closeables) {
        super(target);
        this.closeables = closeables;
    }

    @Override
    public void close() throws SetIOException {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw wrap(e);
            }
        }
    }

    @Override
    public Work next() {
        try {
            return super.next();
        } catch (RuntimeException e) {
            if (e instanceof InterpretationException) throw e;
            throw wrap(e);
        }
    }

    protected abstract SetIOException wrap(Exception e);
}
