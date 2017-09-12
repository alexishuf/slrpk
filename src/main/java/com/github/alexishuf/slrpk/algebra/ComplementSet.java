package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.List;

public class ComplementSet extends UnarySetOperation {
    protected ComplementSet(@Nonnull Set operand) {
        super(operand, true);
    }

    @Override
    public List<String> getFields() {
        return getOperand().getFields();
    }

    public static Set wrap(@Nonnull Set other) {
        Set complement = other;
        while (complement instanceof UnarySetOperation && !(complement instanceof ComplementSet)) {
            complement = ((UnarySetOperation) other).getOperand();
        }
        if (complement instanceof ComplementSet)
            return ((UnarySetOperation)complement).getOperand();
        return new ComplementSet(other);
    }

    public static boolean isComplement(@Nonnull Set other) {
        Set complement = other;
        while (complement instanceof UnarySetOperation && !(complement instanceof ComplementSet)) {
            complement = ((UnarySetOperation) other).getOperand();
        }
        return complement instanceof ComplementSet;
    }

    @Override
    public SetIterator iterator() {
        throw new UnsupportedOperationException("Cannot iterate a infinite set!");
    }
}
