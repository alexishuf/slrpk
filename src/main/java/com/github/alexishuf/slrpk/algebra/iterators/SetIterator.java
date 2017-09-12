package com.github.alexishuf.slrpk.algebra.iterators;

import com.github.alexishuf.slrpk.algebra.exceptions.SetIOException;
import com.github.alexishuf.slrpk.Work;

import java.io.Closeable;

public interface SetIterator extends Closeable, java.util.Iterator<Work> {
    @Override
    void close() throws SetIOException;
}
