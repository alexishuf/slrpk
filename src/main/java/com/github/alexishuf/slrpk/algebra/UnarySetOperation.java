package com.github.alexishuf.slrpk.algebra;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public abstract class UnarySetOperation extends AbstractSet {
    private  @Nonnull Set operand;

    public UnarySetOperation(@Nonnull Set operand, boolean infinite) {
        super(infinite);
        this.operand = operand;
    }

    @Nonnull
    public Set getOperand() {
        return getOperand(Collections.emptyMap());
    }

    @Nonnull
    public Set getOperand(@Nonnull Map<Set, Set> overrides) {
        return overrides.getOrDefault(operand, operand);
    }
}
