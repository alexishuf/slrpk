package com.github.alexishuf.slrpk.algebra.exceptions;

public class IncludePathNotFound extends ExpressionException {
    private final String path;

    public IncludePathNotFound(String path) {
        super(String.format("Include path %s was not found", path));
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
