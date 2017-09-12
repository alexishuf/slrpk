package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SetFieldRx extends SetField {
    @Option(name = "--rx-file", usage = "Load all regexps of the file one " +
            "expression per line (unix line endings), in Java's Pattern syntax.")
    private File[] rxFile;

    @Option(name = "--rx-field", required = true, usage = "Apply regexp to the given field name. " +
            "At least one field must be given")
    private String[] rxFields;

    @Argument
    private String[] rxs;

    private List<Pattern> patterns;

    @Override
    protected boolean applyPredicate(@Nonnull Work work) {
        return Arrays.stream(rxFields).anyMatch(
                rxf -> patterns.stream().anyMatch(
                        p -> p.matcher(work.get(rxf)).matches()));
    }

    @Override
    protected void initPredicate(@Nonnull List<String> headers) throws Exception {
        for (String name : rxFields)
            Preconditions.checkArgument(headers.indexOf(name) >= 0, "Could not find field " + name);
        patterns = new ArrayList<>(rxs.length);
        for (String rx : rxs) patterns.add(Pattern.compile(rx));
    }
}
