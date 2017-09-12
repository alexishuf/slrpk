package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import com.google.common.base.Preconditions;
import com.github.alexishuf.slrpk.Work;

import javax.annotation.Nonnull;
import java.util.List;

public class UnionSet extends BinarySetOperation {
    private final List<String> fields;

    public UnionSet(@Nonnull Set left, @Nonnull Set right) {
        super(left, right, false);
        Preconditions.checkArgument(!left.isInfinite() && !right.isInfinite());
        fields = Helpers.mergeFields(left, right);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public SetIterator iterator() {
        List<Work> list = getLeft().toList();
        Helpers.appendUnique(list, getRight(), improve);
        Helpers.addFields(list, getFields());
        return new ForwardingSetIterator(list.iterator());
    }
}
