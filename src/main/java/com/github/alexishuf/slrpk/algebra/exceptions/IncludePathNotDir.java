package com.github.alexishuf.slrpk.algebra.exceptions;

public class IncludePathNotDir extends InterpretationException {
    private final String path;

    public IncludePathNotDir(String path) {
        super(String.format("Include path %s is not a directory", path));
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
