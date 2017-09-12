package com.github.alexishuf.slrpk.algebra.exceptions;

public class ExpressionException extends RuntimeException {
    public ExpressionException(String s) {
        super(s);
    }

    public ExpressionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
