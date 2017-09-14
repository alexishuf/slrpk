package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectedSet extends UnarySetOperation {
    private final List<String> fieldsList;
    private final boolean isComplement;
    Function<Iterable<String>, Work> loader;

    public ProjectedSet(@Nonnull Set operand, Collection<String> extraFields, boolean isComplement) {
        super(operand, operand.isInfinite());
        HashSet<String> set = new LinkedHashSet<>(extraFields);
        set.removeAll(Work.fieldNames);
        this.isComplement = isComplement;

        Map<String, Integer> projection = new HashMap<>();
        List<String> fields = getOperand().getFields();
        Work.fieldNames.forEach(n -> projection.put(n, fields.indexOf(n)));
        for (int i = 0; i < fields.size(); i++) {
            String name = fields.get(i);
            if (set.contains(name) == !isComplement) projection.put(name, i);
        }
        loader = Work.loader(projection);

        fieldsList = projection.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public boolean isComplement() {
        return isComplement;
    }

    @Override
    public List<String> getFields() {
        return fieldsList;
    }

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        return new ForwardingSetIterator(getOperand(overrides).iterator(overrides)) {
            @Override
            public Work next() {
                return loader.apply(super.next().toList());
            }
        };
    }
}
