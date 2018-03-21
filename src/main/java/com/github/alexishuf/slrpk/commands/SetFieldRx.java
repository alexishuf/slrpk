package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.Work;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;

public class SetFieldRx extends SetField {
    @Option(name = "--rx-file", usage = "File with regexps, one per line")
    private File rxFile;

    @Option(name = "--rx-field", required = true, usage = "Apply regexp to the given field name. " +
            "At least one field must be given")
    private String[] rxFields = {};

    @Option(name = "--acc", usage = "Changes the behavior of regexps evaluation to accumulate: " +
            "replaceAll() calls are chained. This requires that a single --rx-field was provided")
    private boolean acc = false;

    @Argument(usage = "Set of regexps to apply against all fields. All regexps are applied, " +
            "interpreting --value as a replacement string (Matcher.replaceAll()). If --acc is " +
            "given, the result of one replaceAll() call is used as the input of the next. " +
            "Otherwise, only the first replaceAll() that has some effect (for some field) " +
            "is executed.")
    private String[] rxs = {};

    private List<Pattern> patterns;

    public static void main(String[] args) throws Exception {
        Command.main(new SetFieldRx(), args);
    }

    @Override
    protected synchronized PredicateMatch applyPredicate(@Nonnull Work work) {
        if (acc) {
            String string = work.get(rxFields[0]);
            if (string == null) return null;
            for (Pattern pattern : patterns)
                string = pattern.matcher(string).replaceAll(value);
            return new PredicateMatch(work, string);
        } else {
            return Arrays.stream(rxFields)
                    .filter(f -> work.get(f) != null)
                    .flatMap(f -> patterns.stream().map(rx -> rx.matcher(work.get(f))))
                    .filter(Matcher::find)
                    .map(matcher -> new PredicateMatch(work, matcher.replaceAll(value)))
                    .findFirst().orElse(null);
        }
    }

    @Override
    protected void initPredicate(@Nonnull List<String> headers) throws Exception {
        for (String name : rxFields)
            Preconditions.checkArgument(headers.indexOf(name) >= 0, "Could not find field " + name);
        Preconditions.checkArgument(!acc || rxFields.length == 1,
                "Cannot -acc with multiple --rx-fields");

        patterns = new ArrayList<>();
        try (FileInputStream in = new FileInputStream(rxFile)) {
            IOUtils.readLines(in, "UTF-8").forEach(l -> patterns.add(Pattern.compile(l)));
        }
        for (String rx : rxs) patterns.add(Pattern.compile(rx));
        Preconditions.checkArgument(!patterns.isEmpty(), "Provide at least one regexp!");
    }
}
