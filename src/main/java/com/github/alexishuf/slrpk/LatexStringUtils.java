package com.github.alexishuf.slrpk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatexStringUtils {
    private static final Pattern decorationRx =
            Pattern.compile("\\{\\\\[^}]+\\{(.)}}|" +
                               "\\\\[^}]+\\{(.)}");

    public static String stripCharDecorations(String maybelatex) {
        return decorationRx.matcher(maybelatex).replaceAll("$1$2");
    }
}
