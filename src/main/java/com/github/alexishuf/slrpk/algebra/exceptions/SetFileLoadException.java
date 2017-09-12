package com.github.alexishuf.slrpk.algebra.exceptions;

import java.io.File;

public class SetFileLoadException extends SetIOException {
    private final File file;

    public SetFileLoadException(File file, Throwable throwable) {
        super(String.format("Failed to load %s", file.getPath()), throwable);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
