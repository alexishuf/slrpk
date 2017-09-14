package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.exceptions.SetFileLoadException;
import com.github.alexishuf.slrpk.algebra.exceptions.SetIOException;
import com.github.alexishuf.slrpk.algebra.iterators.ClosingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.DistinctSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import org.apache.commons.csv.CSVParser;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvSet extends FileSet {
    private List<String> headers;

    public CsvSet(@Nonnull File file) throws SetFileLoadException {
        super(file);
        try (FileReader reader = new FileReader(file);
             CSVParser parser = new CSVParser(reader, Work.CSV_FORMAT)) {
            headers = new ArrayList<>(parser.getHeaderMap().keySet());
        } catch (IOException e) {
            throw new SetFileLoadException(file, e);
        }
    }

    @Override
    public List<String> getFields() {
        return headers;
    }

    @Nonnull
    @Override
    public SetIterator iterator(@Nonnull Map<Set, Set> overrides) {
        try {
            FileReader reader = new FileReader(getFile());
            CSVParser parser = new CSVParser(reader, Work.CSV_FORMAT);
            Map<String, Integer> hm = parser.getHeaderMap();
            return new DistinctSetIterator(new ClosingSetIterator(parser.getRecords().stream()
                    .map(Work.loader(hm)).iterator(), parser) {
                @Override
                protected SetIOException wrap(Exception e) {
                    return new SetFileLoadException(getFile(), e);
                }
            });
        } catch (IOException e) {
            throw new SetFileLoadException(getFile(), e);
        }
    }
}
