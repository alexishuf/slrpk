package com.github.alexishuf.slrpk;

import com.github.alexishuf.slrpk.algebra.CollectionSet;
import com.github.alexishuf.slrpk.algebra.CsvSet;
import com.google.common.base.Preconditions;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetBibId extends Command {
    @Option(name = "--assign", usage = "Assign Ids to works without one. Works as in update-csv")
    private boolean assign = false;

    @Option(name = "--csv", usage = "Csv file to load entries from. If --assign is given, will " +
            "be rewritten to include assigned Ids")
    private File csv;

    @Option(name = "--bib-filename-rx", forbids = {"--bib-path-rx"},
            usage = "Regular expression applied to all file names of bib files given as input. " +
                    "Here file name means the last path segment (including extension) " +
                    "as returned by File.getName().")
    private String bibFilenameRx;
    @Option(name = "--bib-path-rx", forbids = {"--bib-filename-rx"},
            usage = "Regular expression applied to all absolute paths of bib files given as input. " +
                    "The path is obtained with File.getAbsolutePath().")
    private String bibPathRx;

    @Argument(index = 0, required = true)
    private String bibNewName;

    @Argument(index = 1, required = true)
    private File[] bibs;

    private Pattern bibFilenamePattern;
    private Pattern bibPathPattern;

    private static final Work.BibLoader bibLoader = Work.bibLoader(Collections.emptyList());


    public static void main(String[] args) throws Exception {
        Command.main(new SetBibId(), args);
    }

    @Override
    protected void runCommand() throws Exception {
        Preconditions.checkArgument(bibFilenameRx != null || bibPathRx != null);
        Preconditions.checkArgument(bibs.length > 0);
        if (bibFilenameRx != null) bibFilenamePattern = Pattern.compile(bibFilenameRx);
        if (bibPathRx != null) bibPathPattern = Pattern.compile(bibPathRx);

        Map<File, File> newBibs = new HashMap<>();
        for (File bib : bibs) newBibs.put(bib, getOutputFile(bib));

        List<Work> works = new CsvSet(csv).toList();
        int nextId = works.stream().map(Work::getId).map(Id::new).filter(id -> !id.equals(Id.NULL))
                .map(id -> Integer.parseInt(id.local)).max(Integer::compareTo).orElse(0) + 1;
        if (assign) {
            for (Work work : works) {
                if (new Id(work.getId()).equals(Id.NULL))
                    work.set(Work.Field.Id, Id.format(nextId++));
            }
        }

        for (File bibFile : bibs) {
            File outBib = newBibs.get(bibFile);

            try (FileReader reader = new FileReader(bibFile)) {
                BibTeXDatabase db = new BibTeXParser().parse(reader);
                for (BibTeXEntry e : db.getEntries().values()) {
                    Work work = bibLoader.apply(e);
                    Work match = works.stream().filter(work::matches).findFirst().orElse(null);
                    if (match != null)
                        Id.setIdInAnnote(e, match.getId());
                }

                try (FileWriter writer = new FileWriter(outBib)) {
                    BibTeXFormatter formatter = new BibTeXFormatter();
                    formatter.format(db, writer);
                }
            }
        }

    }

    private File getOutputFile(File bibFile) throws IOException {
        File file;
        if (bibFilenamePattern != null)
            file = new File(applyRegexp(bibFilenamePattern, bibFile.getName(), bibNewName));
        else if (bibPathPattern != null)
            file = new File(applyRegexp(bibPathPattern, bibFile.getName(), bibNewName));
        else
            throw new IllegalStateException();
        if (!file.getParentFile().exists() && !file.mkdirs())
            throw new IOException("Failed to mkdirs " + file.getParent());
        return file;
    }

    private String applyRegexp(Pattern pattern, String input, String replacement) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches())
            throw new IllegalArgumentException(String.format("Regexp %s did not match %s", pattern.pattern(), input));
        String str = replacement;
        for (int i = 1; i < matcher.groupCount()+1; i++)
            str = str.replace("$", matcher.group(i));
        return str;
    }
}
