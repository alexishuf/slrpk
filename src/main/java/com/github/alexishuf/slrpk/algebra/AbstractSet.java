package com.github.alexishuf.slrpk.algebra;

abstract class AbstractSet implements Set {
    private boolean infinite;

    protected AbstractSet(boolean infinite) {
        this.infinite = infinite;
    }

    @Override
    public boolean isInfinite() {
        return infinite;
    }
}
