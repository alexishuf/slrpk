package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public abstract class BinarySetOperation extends AbstractSet {
    private  @Nonnull Set left, right;
    protected BiFunction<Work, Work, Work> improve = Helpers::improve;

    public BinarySetOperation(@Nonnull Set left, @Nonnull Set right, boolean infinite) {
        super(infinite);
        this.left = left;
        this.right = right;
    }

    public BinarySetOperation improveWith(BiFunction<Work, Work, Work> function) {
        this.improve = function;
        return this;
    }

    @Nonnull
    public Set getLeft() {
        return left;
    }

    @Nonnull
    public Set getRight() {
        return right;
    }
}
