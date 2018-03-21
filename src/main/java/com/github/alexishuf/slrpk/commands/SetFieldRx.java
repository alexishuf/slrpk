package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.Work;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;

public class SetFieldRx extends SetField {
    @Option(name = "--rx-field", required = true, usage = "Apply regexp to the given field name. " +
            "At least one field must be given")
    private String[] rxFields;

    @Argument(usage = "Set of regexps to apply against all fields. As soon as one of " +
            "these regexps matches with a field, the value to be set is determined " +
            "interpreting --value as a replacement string (Mathcer.replaceAll()) for the " +
            "matched regexp")
    private String[] rxs;

    private List<Pattern> patterns;

    public static void main(String[] args) throws Exception {
        Command.main(new SetFieldRx(), args);
    }

    @Override
    protected synchronized PredicateMatch applyPredicate(@Nonnull Work work) {
        return Arrays.stream(rxFields)
                .filter(f -> work.get(f) != null)
                .flatMap(f -> patterns.stream().map(rx -> rx.matcher(work.get(f))))
                .filter(Matcher::find)
                .map(matcher -> new PredicateMatch(work, matcher.replaceAll(value)))
                .findFirst().orElse(null);
    }

    @Override
    protected void initPredicate(@Nonnull List<String> headers) throws Exception {
        for (String name : rxFields)
            Preconditions.checkArgument(headers.indexOf(name) >= 0, "Could not find field " + name);
        patterns = new ArrayList<>(rxs.length);
        for (String rx : rxs) patterns.add(Pattern.compile(rx));
    }
}
