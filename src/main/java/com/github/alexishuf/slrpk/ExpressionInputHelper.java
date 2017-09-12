package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExpressionInputHelper {
    public static String getExpression(boolean stdin, @Nullable File expressionFile,
                                       @Nullable String[] expressionTerms) throws IOException {
        expressionTerms = expressionTerms == null ? new String[0] : expressionTerms;
        int provided = (expressionTerms.length > 0 ? 1 : 0) + (stdin ? 1 : 0)
                + (expressionFile != null ? 1 : 0);
        if (provided == 0) throw new IllegalArgumentException("No expressions provided!");
        if (provided >  1) throw new IllegalArgumentException("Multiple expressions provided!");

        if (expressionTerms.length > 0)
            return String.join(" ", expressionTerms);
        if (expressionFile != null)
            return IOUtils.toString(new FileReader(expressionFile));
        if (stdin)
            return IOUtils.toString(System.in, StandardCharsets.UTF_8);
        throw new IllegalArgumentException("No expression given!");
    }
}
