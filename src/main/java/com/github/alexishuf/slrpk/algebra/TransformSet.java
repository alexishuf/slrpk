package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ForwardingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TransformSet extends UnarySetOperation {
    private final @Nonnull Function<Work, Work> function;
    private final @Nonnull List<String> fields;

    public TransformSet(@Nonnull Set operand, @Nonnull Function<Work, Work> function) {
        this(operand, function, operand.getFields());
    }

    public TransformSet(@Nonnull Set operand, @Nonnull Function<Work, Work> function,
                        @Nonnull List<String> fields) {
        super(operand, operand.isInfinite());
        this.function = function;
        this.fields = fields;
    }

    public static TransformSet addingField(String field, @Nonnull Set operand,
                                           @Nonnull Function<Work, Work> function) {
        ArrayList<String> fields = new ArrayList<>(operand.getFields());
        int idx = operand.getFields().indexOf(field);
        if (idx < 0) fields.add(field);
        return new TransformSet(operand, Work.fieldsAdder(fields).andThen(function), fields);
    }

    @Nonnull
    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public SetIterator iterator() {
        return new ForwardingSetIterator(getOperand().iterator()) {
            @Override
            public Work next() {
                return function.apply(super.next());
            }
        };
    }
}
