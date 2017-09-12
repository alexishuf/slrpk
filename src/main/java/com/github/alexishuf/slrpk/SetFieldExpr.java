package com.github.alexishuf.slrpk;

import com.github.alexishuf.slrpk.algebra.runtime.Interpreter;
import org.kohsuke.args4j.Argument;

import javax.annotation.Nonnull;
import java.util.List;

public class SetFieldExpr extends SetField {
    @Argument(usage = "Splirk expression yielding the Works to have --value assigned.")
    private String[] exprTerms;
    private List<Work> fromBib;

    public static void main(String[] args) throws Exception {
        SetField.main(new SetFieldExpr(), args);
    }

    @Override
    protected void initPredicate(@Nonnull List<String> headers)  throws Exception {
        fromBib = new Interpreter().run(String.join(" ", exprTerms)).toList();
    }

    @Override
    protected boolean applyPredicate(@Nonnull Work work) {
        return fromBib.stream().anyMatch(work::matches);
    }
}
