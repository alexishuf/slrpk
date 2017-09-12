package com.github.alexishuf.slrpk.algebra.exceptions;

public class InterpretationException extends RuntimeException {
    public InterpretationException(String s) {
        super(s);
    }

    public InterpretationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
