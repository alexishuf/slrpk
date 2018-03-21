package com.github.alexishuf.slrpk.commands;

import com.github.alexishuf.slrpk.Work;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.kohsuke.args4j.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.function.Function;

public abstract class SetField extends Command {
    @Option(name = "--csv", required = true, usage = "CSV file to be updated")
    private File csv;
    @Option(name = "--field", required = true, usage = "Field name (header) to update. " +
            "If not present in the CSV will be added as the last column.")
    protected String field;
    @Option(name = "--value", required = true, usage = "Value to set on given field when the " +
            "work is present in at least one of the .bib files. When the work is not present, " +
            "--default-value is used")
    protected String value;
    @Option(name = "--default-value", usage = "Default value used when the --field is added " +
            "or when --keep was not given. Default is null (missing value)")
    protected String defaultValue = null;
    @Option(name = "--keep", usage = "If set and the given --field already exists in the CSV, " +
            "old values are not overwritten by --default-value. The overwrite only occurs if " +
            "the work is found in a .bib file")
    private boolean keep = false;
    @Option(name = "--prefer", usage = "If set, old values on the csv file will be preferred in " +
            "place of values computed by this command. This command will only affect works " +
            "which had no value for --field.")
    private boolean prefer = false;
    protected int fieldIndex = -1;

    @Override
    protected void runCommand() throws Exception {
        List<String> headers;
        List<Work> works = new ArrayList<>();
        try (FileReader reader = new FileReader(csv);
             CSVParser parser = new CSVParser(reader, Work.CSV_FORMAT)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            fieldIndex = headerMap.getOrDefault(field, headerMap.size());
            headers = toList(headerMap.keySet(), field);
            Function<Iterable<String>, Work> loader = Work.loader(headers);
            for (CSVRecord record : parser.getRecords())
                works.add(loader.apply(toList(record, defaultValue)));
        }

        initPredicate(headers);
        works.stream().parallel().map(this::applyPredicate)
                .filter(Objects::nonNull)
                .filter(m -> !prefer || m.work.get(field) == null)
                .forEach(m -> m.work.set(field, m.value));

        try (FileWriter writer = new FileWriter(csv);
             CSVPrinter printer = new CSVPrinter(writer, Work.CSV_FORMAT)) {
            printer.printRecord(headers);
            for (Work w : works) printer.printRecord(w.toList());
        }
    }

    public static class PredicateMatch {
        public Work work;
        public String value;

        public PredicateMatch(Work work, String value) {
            this.work = work;
            this.value = value;
        }

        public Work getWork() {
            return work;
        }
        public String getValue() {
            return value;
        }
    }

    protected abstract @Nullable PredicateMatch applyPredicate(@Nonnull Work work);

    protected abstract void initPredicate(@Nonnull List<String> headers) throws Exception;

    private List<String> toList(Iterable<String> record, String defaultValue) {
        List<String> list = new ArrayList<>();
        Iterator<String> it = record.iterator();
        for (int i = 0; it.hasNext(); i++) {
            if (i == fieldIndex && !keep && !prefer) {
                it.next(); //discard
                list.add(defaultValue);
            }
            else list.add(it.next());
        }
        if (list.size() == fieldIndex)
            list.add(defaultValue);

        return list;
    }
}
