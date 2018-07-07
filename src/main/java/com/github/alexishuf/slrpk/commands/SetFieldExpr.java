package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.ExpressionInputHelper;
import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.runtime.Interpreter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class SetFieldExpr extends SetField {
    @Argument(usage = "Splirk expression yielding the Works to have --value assigned.")
    private String[] exprTerms;
    @Option(name = "--expr-file", forbids = {"--stdin"}, usage = "Load expression from given file")
    private File exprFile;
    @Option(name = "--deref-value", usage = "Treat --value as a field name. The value in that " +
            "field for the first matched Work in the given expression will be used as the " +
            "actual new value")
    private boolean derefValue = false;
    @Option(name = "--append", usage = "Instead of discarding the old value, append the new value " +
            "to the existing one, optionally adding with --append-sep as separator")
    private boolean append = false;
    @Option(name = "--append-sep", usage = "Override default separator used with " +
            "--append (default is \"\")")
    private String appendSep = "";
    @Option(name = "--stdin", forbids = {"--expr-file"},
            usage = "Load expression from standard input")
    private boolean stdin = false;

    private List<Work> fromBib;
    private String expression;

    public static void main(String[] args) throws Exception {
        Command.main(new SetFieldExpr(), args);
    }

    @Override
    protected void runCommand() throws Exception {
        expression = ExpressionInputHelper.getExpression(stdin, exprFile, exprTerms,
                expressionPrefix);
        super.runCommand();
    }

    @Override
    protected void initPredicate(@Nonnull List<String> headers)  throws Exception {
        fromBib = new Interpreter().run(expression).toList();
    }

    @Override
    protected PredicateMatch applyPredicate(@Nonnull Work work) {
        Work match = fromBib.stream().filter(work::matches).findFirst().orElse(null);
        if (match == null) return null;
        String value = derefValue ? match.get(this.value) : this.value;
        if (append)
            value = work.get(field) + appendSep + value;
        return new PredicateMatch(work, value != null ? value : defaultValue);
    }
}
