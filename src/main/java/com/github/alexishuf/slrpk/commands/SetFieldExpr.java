package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.ExpressionInputHelper;
import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.runtime.Interpreter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public class SetFieldExpr extends SetField {
    @Argument(usage = "Splirk expression yielding the Works to have --value assigned.")
    private String[] exprTerms;
    @Option(name = "--expr-file", forbids = {"--stdin"}, usage = "Load expression from given file")
    private File exprFile;
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
        return fromBib.stream().anyMatch(work::matches) ? new PredicateMatch(work, value) : null;
    }
}
