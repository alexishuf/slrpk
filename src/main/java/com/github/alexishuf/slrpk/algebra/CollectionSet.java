package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CollectionSet extends AbstractSet {
    private Collection<Work> collection;
    private final List<String> fields;

    public CollectionSet(Collection<Work> collection, List<String> fields) {
        super(false);
        this.collection = collection;
        this.fields = fields;
    }
    public CollectionSet(Collection<Work> collection) {
        this(collection, scanFields(collection));
    }

    private static List<String> scanFields(Collection<Work> works) {
        if (works.isEmpty()) return Collections.emptyList();
        List<String> allFields = works.iterator().next().getAllFields();
        Preconditions.checkArgument(works.stream().allMatch(allFields::equals));
        return allFields;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        return new ForwardingSetIterator(collection.iterator());
    }
}
