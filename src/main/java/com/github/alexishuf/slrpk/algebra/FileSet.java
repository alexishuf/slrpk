package com.github.alexishuf.slrpk.algebra;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class FileSet extends AbstractSet {
    private @Nonnull File file;

    public FileSet(@Nonnull File file) {
        super(false);
        this.file = file;
    }

    @Nonnull
    public File getFile() {
        return file;
    }
}
