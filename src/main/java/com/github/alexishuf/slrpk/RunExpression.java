package com.github.alexishuf.slrpk;

import com.github.alexishuf.slrpk.algebra.Set;
import com.github.alexishuf.slrpk.algebra.UnionSet;
import com.google.common.base.Preconditions;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import com.github.alexishuf.slrpk.algebra.CsvSet;
import com.github.alexishuf.slrpk.algebra.EmptySet;
import com.github.alexishuf.slrpk.algebra.runtime.Interpreter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RunExpression extends Command {
    @Option(name = "--help", aliases = {"-h"}, help = true)
    private boolean help;

    @Option(name = "--count", usage = "Output a total count of Works")
    private boolean count;

    @Option(name = "--csv", usage = "CSV output file")
    private File csv;

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
        RunExpression app = new RunExpression();
        CmdLineParser parser = new CmdLineParser(app);
        parser.parseArgument(args);
        Preconditions.checkArgument(app.csv != null || app.count,
                "Either --csv or --count must be given");
        int provided = (app.exprTerms.length > 0 ? 1 : 0) + (app.stdin ? 1 : 0)
                                                          + (app.exprFile != null ? 1 : 0);
        if (provided == 0) throw new IllegalArgumentException("No expressions provided!");
        if (provided >  1) throw new IllegalArgumentException("Multiple expressions provided!");


        if (app.help)
            parser.printUsage(System.out);
        else
            app.run();
    }

    @Override
    protected void runCommand() throws Exception {
        String expr = getExpression();

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
        if (count)
            System.out.printf("Count: %d\n", list.size());
    }

    private String getExpression() throws IOException {
        if (exprTerms.length > 0)
            return String.join(" ", exprTerms);
        if (exprFile != null)
            return IOUtils.toString(new FileReader(exprFile));
        if (stdin)
            return IOUtils.toString(System.in, StandardCharsets.UTF_8);
        throw new IllegalArgumentException("No expression given!");
    }
}
