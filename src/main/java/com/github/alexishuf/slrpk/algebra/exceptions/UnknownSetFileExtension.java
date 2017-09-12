package com.github.alexishuf.slrpk.algebra.exceptions;

import java.io.File;

public class UnknownSetFileExtension extends InterpretationException {
    private final File file;

    public UnknownSetFileExtension(File file) {
        super(String.format("Unknown extension for resolved file %s", file.getPath()));
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
