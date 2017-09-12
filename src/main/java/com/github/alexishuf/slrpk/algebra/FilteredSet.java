package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteredSet extends UnarySetOperation {
    private final Predicate<Work> predicate;

    public FilteredSet(@Nonnull Set operand, Predicate<Work> predicate) {
        super(operand, operand.isInfinite());
        this.predicate = predicate;
    }

    @Override
    public List<String> getFields() {
        return getOperand().getFields();
    }

    @Override
    public SetIterator iterator() {
        return new ForwardingSetIterator(getOperand().iterator()) {
            private Work next = null;

            @Override
            public Work next() {
                if (!hasNext()) throw new NoSuchElementException();
                Work current = this.next;
                this.next = null;
                return current;
            }

            @Override
            public boolean hasNext() {
                while (this.next == null && target.hasNext()) {
                    Work next = target.next();
                    if (predicate.test(next))
                        this.next = next;
                }
                return this.next != null;
            }
        };
    }
}
