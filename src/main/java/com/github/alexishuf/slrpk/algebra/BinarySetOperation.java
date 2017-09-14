package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class BinarySetOperation extends AbstractSet {
    private  @Nonnull Set left, right;
    protected BiFunction<Work, Work, Work> improve = Helpers::improve;

    public BinarySetOperation(@Nonnull Set left, @Nonnull Set right, boolean infinite) {
        super(infinite);
        this.left = left;
        this.right = right;
    }

    @Nonnull
    public Set getLeft() {
        return getLeft(Collections.emptyMap());
    }

    @Nonnull
    public Set getLeft(@Nonnull Map<Set, Set> overrides) {
        return overrides.getOrDefault(left, left);
    }

    @Nonnull
    public Set getRight() {
        return getRight(Collections.emptyMap());
    }

    @Nonnull
    public Set getRight(@Nonnull Map<Set, Set> overrides) {
        return overrides.getOrDefault(right, right);
    }
}
