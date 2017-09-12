package com.github.alexishuf.slrpk.algebra.exceptions;

import com.github.alexishuf.slrpk.algebra.runtime.IncludePath;

public class SetFileNotFoundException extends SetIOException {
    private final String path;
    private final IncludePath includePath;

    public SetFileNotFoundException(String path, IncludePath includePath) {
        super(String.format("File %s not found on include path %s", path, includePath));
        this.path = path;
        this.includePath = includePath;
    }

    public String getPath() {
        return path;
    }

    public IncludePath getIncludePath() {
        return includePath;
    }
}
