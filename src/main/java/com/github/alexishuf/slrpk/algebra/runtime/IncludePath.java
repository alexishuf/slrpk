package com.github.alexishuf.slrpk.algebra.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedHashSet;

public class IncludePath {
    private IncludePath fallback = null;
    private final LinkedHashSet<File> list = new LinkedHashSet<>();

    public IncludePath() { }

    public IncludePath(@Nonnull IncludePath fallback) {
        this.fallback = fallback;
    }

    public void setFallback(IncludePath fallback) {
        this.fallback = fallback;
    }

    public IncludePath getFallback() {
        return fallback;
    }

    public boolean addIncludeDir(@Nonnull File dir) {
        return list.add(dir);
    }

    public boolean removeIncludeDir(@Nonnull File dir) {
        return list.remove(dir);
    }

    public void clearIncludePath() {
        list.clear();
    }

    @Nullable
    public File resolve(@Nonnull String path) {
        File file = new File(path);
        if (file.isAbsolute()) return file;
        if (getFallback() != null) {
            file = getFallback().resolve(path);
            if (file != null && file.exists()) return file;
        }
        for (File dir : list) {
            file = dir.toPath().resolve(path).toFile();
            if (file.exists())
                return file;
        }
        return null;
    }
}
