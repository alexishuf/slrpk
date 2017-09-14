package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import com.google.common.base.Preconditions;
import com.github.alexishuf.slrpk.Work;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

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

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        List<Work> list = getLeft(overrides).toList();
        Helpers.appendUnique(list, getRight(overrides), improve);
        Helpers.addFields(list, getFields());
        return new ForwardingSetIterator(list.iterator());
    }
}
