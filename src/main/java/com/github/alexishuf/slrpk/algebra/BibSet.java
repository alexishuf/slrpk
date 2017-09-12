package com.github.alexishuf.slrpk.algebra;

import com.github.alexishuf.slrpk.algebra.exceptions.SetFileLoadException;
import com.github.alexishuf.slrpk.algebra.exceptions.SetIOException;
import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.iterators.ClosingSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.DistinctSetIterator;
import com.github.alexishuf.slrpk.algebra.iterators.SetIterator;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BibSet extends FileSet {
    private List<String> headers;
    private final Work.BibLoader loader;

    public BibSet(@Nonnull File file) throws SetFileLoadException {
        super(file);

        try (FileReader reader = new FileReader(getFile())) {
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase db = parser.parse(reader);
            loader = Work.bibLoader(db);
            headers = loader.headerMap.keySet().stream().map(Object::toString)
                    .collect(Collectors.toList());
        } catch (IOException | ParseException e) {
            throw new SetFileLoadException(getFile(), e);
        }
    }

    @Override
    public List<String> getFields() {
        return headers;
    }

    @Override
    public SetIterator iterator() {
        try (FileReader reader = new FileReader(getFile())) {
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase db = parser.parse(reader);
            return new DistinctSetIterator(new ClosingSetIterator(db.getEntries().values()
                    .stream().map(loader).iterator(), reader) {
                @Override
                protected SetIOException wrap(Exception e) {
                    return new SetFileLoadException(getFile(), e);
                }
            });
        } catch (IOException | ParseException e) {
            throw new SetFileLoadException(getFile(), e);
        }
    }
}
