package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.Id;
import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.*;
import org.apache.commons.csv.CSVPrinter;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateCsv extends Command {
    private int nextId = 1;

    @Option(name = "--trunc", usage = "Truncates the csv file before writing the new works")
    private boolean truncate;

    @Option(name = "--csv", required = true, usage = "CSV file to update with new Works. " +
            "Duplicates won't be added, but may be used to update missing information")
    private File csv;

    @Option(name = "--in-bib", required = true, usage = "Source .bib files")
    private File inBib;

    @Option(name = "--out-bib", usage = "Write the contents of --in-bib with the assigned ids in " +
            "the annote field")
    private File outBib = null;
    private final Work.BibLoader bibLoader = Work.bibLoader(Collections.emptyList());

    public static void main(String[] args) throws Exception {
        Command.main(new UpdateCsv(), args);
    }

    @Override
    protected void runCommand() throws Exception {
        if (outBib == null) {
            File parent = inBib.getParentFile();
            String newName = inBib.getName().replace(".bib", "-withId.bib");
            outBib = new File(parent, newName);
        }
        Set csvSet = truncate || !csv.exists() ? new EmptySet() : new CsvSet(csv);
        ArrayList<String> extraFields = new ArrayList<>(csvSet.getFields());
        extraFields.removeAll(Work.fieldNames);
        nextId = findNextId(csvSet.toList());

        List<Work> works = new ProjectedSet(
                TransformSet.addingField("annote",
                        new UnionSet(csvSet, new BibSet(inBib)), w -> {
                            if (w.get(Work.Field.Id) == null)
                                w.set(Work.Field.Id, Id.format(nextId++));
                            return w;
                        }),
                extraFields, false).toList();

        try (FileReader reader = new FileReader(inBib)) {
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase db = parser.parse(reader);
            for (BibTeXEntry e : db.getEntries().values()) {
                Work work = bibLoader.apply(e);
                Work match = works.stream().filter(work::matches).findFirst().orElse(null);
                assert match != null;
                Id.setIdInAnnote(e, match.getId());
            }

            try (FileWriter writer = new FileWriter(outBib)) {
                BibTeXFormatter formatter = new BibTeXFormatter();
                formatter.format(db, writer);
            }
        }

        try (FileWriter writer = new FileWriter(csv);
             CSVPrinter printer = new CSVPrinter(writer, Work.CSV_FORMAT)) {
            printer.printRecord(Work.fieldNames);
            for (Work work : works) printer.printRecord(work.toList());
        }
    }

    private int findNextId(List<Work> list) {
        return list.stream().map(Work::getId).map(Id::new).filter(id -> !id.equals(Id.NULL))
                .map(id -> Integer.parseInt(id.local)).max(Integer::compare).orElse(0) + 1;
    }
}
