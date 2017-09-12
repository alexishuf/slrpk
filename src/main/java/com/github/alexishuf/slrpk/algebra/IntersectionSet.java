package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntersectionSet extends BinarySetOperation {
    private List<String> headers;

    public IntersectionSet(@Nonnull Set left, @Nonnull Set right) {
        super(left, right, false);
        Preconditions.checkArgument(!(left.isInfinite() && right.isInfinite()));
        headers = Helpers.mergeFields(left, right);
    }

    @Override
    public List<String> getFields() {
        return headers;
    }

    @Override
    public SetIterator iterator() {
        Set first = Helpers.firstFinite(getLeft(), getRight());
        Set second = first == getLeft() ? getRight() : getLeft();
        HashSet<Work> selected = new HashSet<>();
        List<Work> list = first.toList();
        Function<Work, Work> matchFinder = Helpers.matchFinder(second);

        Function<Work, Work> fieldsAdder = Work.fieldsAdder(getFields());

        for (int i = 0; i < list.size(); i++) {
            Work match = matchFinder.apply(list.get(i));
            if (match != null) {
                Work l = first == getLeft() ? list.get(i) : match      ;
                Work r = first == getLeft() ? match       : list.get(i);
                list.set(i, improve.apply(l, r));
                selected.add(list.get(i));
            } else {
                list.set(i, fieldsAdder.apply(list.get(i)));
            }
        }

        ArrayList<Work> result = list.stream().filter(selected::contains)
                .collect(Collectors.toCollection(ArrayList::new));
        return new ForwardingSetIterator(result.iterator());
    }
}
