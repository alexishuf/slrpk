package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplementSet extends UnarySetOperation {
    private final Map<Set, Set> overrides;

    protected ComplementSet(@Nonnull Set operand, Map<Set, Set> overrides) {
        super(operand, true);
        this.overrides = overrides;
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
        Map<Set, Set> overrides = null;
        if (complement instanceof ComplementSet) {
            overrides = new HashMap<>();
            overrides.put(complement, ((ComplementSet)complement).getOperand());
        }
        return new ComplementSet(other, overrides);
    }

    public static boolean isComplement(@Nonnull Set other) {
        Set complement = other;
        while (complement instanceof UnarySetOperation && !(complement instanceof ComplementSet)) {
            complement = ((UnarySetOperation) other).getOperand();
        }
        return complement instanceof ComplementSet;
    }

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        //this.overrides overrides the overrides parameter
        Map<Set, Set> actual = Helpers.selectOverrideMap(overrides, this.overrides);
        if (this.overrides != null)
            return  getOperand(actual).iterator(actual);
        throw new UnsupportedOperationException("Cannot iterate a infinite set!");
    }
}
