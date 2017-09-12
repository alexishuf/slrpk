package com.github.alexishuf.slrpk.algebra;

import javax.annotation.Nonnull;

public abstract class UnarySetOperation extends AbstractSet {
    private  @Nonnull Set operand;

    public UnarySetOperation(@Nonnull Set operand, boolean infinite) {
        super(infinite);
        this.operand = operand;
    }

    @Nonnull
    public Set getOperand() {
        return operand;
    }
}
