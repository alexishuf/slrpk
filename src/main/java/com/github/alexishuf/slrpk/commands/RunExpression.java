package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.Work2BibTeX;
import com.github.alexishuf.slrpk.algebra.Set;
import com.github.alexishuf.slrpk.algebra.UnionSet;
import com.google.common.base.Preconditions;
import org.apache.commons.csv.CSVPrinter;
import com.github.alexishuf.slrpk.algebra.CsvSet;
import com.github.alexishuf.slrpk.algebra.EmptySet;
import com.github.alexishuf.slrpk.algebra.runtime.Interpreter;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import static com.github.alexishuf.slrpk.ExpressionInputHelper.getExpression;

public class RunExpression extends Command {
    @Option(name = "--count", usage = "Output a total count of Works")
    private boolean count;

    @Option(name = "--csv", usage = "CSV output file")
    private File csv;

    @Option(name = "--bib", usage = "BibTeX output file. Always truncates the file")
    private File bib;

    @Option(name = "--trunc", depends = {"--csv"},
            usage = "If set, will discard any data in the CSV output file")
    private boolean truncate;

    @Option(name = "--stdin", forbids = {"--expr-file"},
            usage = "Load expresison from standard input")
    private boolean stdin = false;

    @Option(name = "--expr-file", forbids = {"--stdin"}, usage = "Load expression from given file")
    private File exprFile;

    @Argument(usage = "Expression to execute, if multiple arguments are, given, they will " +
            "be joined by spaces reassembling the expression.")
    private String[] exprTerms = new String[0];

    public static void main(String[] args) throws Exception {
        Command.main(new RunExpression(), args);
    }

    @Override
    protected void runCommand() throws Exception {
        Preconditions.checkArgument(csv != null || bib != null || count,
                "Either --csv, --bib or --count must be given");

        String expr = getExpression(stdin, exprFile, exprTerms, expressionPrefix);
        Set set = new Interpreter().run(expr);
        if (!truncate && csv != null)
            set = new UnionSet(csv.exists() ? new CsvSet(csv) : new EmptySet(), set);
        List<Work> list = set.toList();
        if (csv != null) {
            try (FileWriter writer = new FileWriter(csv);
                 CSVPrinter printer = new CSVPrinter(writer, Work.CSV_FORMAT)) {
                printer.printRecord(set.getFields());
                for (Work w : list) printer.printRecord(w.toList());
            }
        }
        if (bib != null) {
            BibTeXDatabase db = new BibTeXDatabase();
            list.parallelStream().map(new Work2BibTeX()).forEachOrdered(db::addObject);
            try (FileWriter writer = new FileWriter(bib)) {
                new BibTeXFormatter().format(db, writer);
            }
        }
        if (count)
            System.out.println(list.size());
    }
}
