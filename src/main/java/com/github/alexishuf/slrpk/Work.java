package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.csv.CSVFormat;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.alexishuf.slrpk.LatexStringUtils.stripCharDecorations;
import static org.apache.commons.lang3.StringUtils.replaceChars;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.apache.commons.lang3.StringUtils.stripAccents;

public class Work implements Comparable<Work> {

    public static final String SLRPK_BIB_KEY = "slrpk-bib-key";
    public static final String SLRPK_BIB_TYPE = "slrpk-bib-type";

    public enum Field {
        Id,
        Author,
        Title,
        Abstract,
        Kw,
        DOI
    }
    private static final Pattern doiPattern = Pattern.compile("(?:https?://dx\\.doi\\.org/)?(.*)");

    public static final @Nonnull List<String> fieldNames;
    public static final @Nonnull List<String> fieldNamesLower;
    public static final @Nonnull Map<Key, Field> bibtexKeys;
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord();

    static {
        fieldNames = Arrays.stream(Field.values()).map(Field::name).collect(Collectors.toList());
        fieldNamesLower = fieldNames.stream().map(String::toLowerCase).collect(Collectors.toList());
        bibtexKeys = new LinkedHashMap<>();
        bibtexKeys.put(BibTeXEntry.KEY_AUTHOR,     Field.Author);
        bibtexKeys.put(BibTeXEntry.KEY_TITLE,      Field.Title);
        bibtexKeys.put(new Key("abstract"), Field.Abstract);
        bibtexKeys.put(new Key("keywords"), Field.Kw);
        bibtexKeys.put(BibTeXEntry.KEY_DOI,        Field.DOI);
    }

    private @Nonnull ImmutableMap<Object, Integer> fieldMap;
    private Cache<Field, String> simplifiedCache = CacheBuilder.newBuilder().weakValues().build();
    private WeakReference<List<String>> allFields = new WeakReference<>(null);
    private ImmutableSet<String> extraFields;
    private final @Nonnull List<String> values;

    public Work(@Nonnull Iterable<String> record,
                @Nonnull Map<String, Integer> projection,
                @Nonnull ImmutableMap<Object, Integer> fieldMap) {
        Preconditions.checkArgument(fieldMap.keySet().stream()
                .allMatch(k -> k instanceof String || k instanceof Field));

        ArrayList<String> list = new ArrayList<>();
        record.forEach(list::add);
        values = new ArrayList<>();
        for (int i = 0; i < fieldMap.size(); i++) values.add(null);
        fieldMap.forEach((k, v) -> values.set(v, list.get(projection.get(asString(k)))));
        this.fieldMap = fieldMap;
    }

    private String asString(Object key) {
        if (key instanceof Field)
            return ((Field)key).name();
        return key.toString();
    }

    public Work(Iterable<String> record, @Nonnull Map<String, Integer> projection) {
        this(record, projection, getFieldMap(projection));
    }

    private static ImmutableMap<Object, Integer> getFieldMap(Map<String, Integer> projection) {
        Map<Object, Integer> tmp = new LinkedHashMap<>();
        tmp.putAll(projection);
        for (String name : fieldNames) {
            if (tmp.containsKey(name)) {
                tmp.put(Field.valueOf(name), tmp.get(name));
                tmp.remove(name);
            } else {
                throw new IllegalArgumentException("Missing Field " + name + " in projection");
            }
        }
        LinkedHashMap<Object, Integer> sorted = new LinkedHashMap<>();
        tmp.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));

        ImmutableMap.Builder<Object, Integer> hmBuilder = ImmutableMap.builder();
        int counter = 0;
        for (Object k : sorted.keySet()) hmBuilder.put(k, counter++);
        return hmBuilder.orderEntriesByValue(Integer::compareTo).build();
    }

    public static Function<Iterable<String>, Work> loader(Map<String, Integer> projection) {
        Preconditions.checkArgument(projection.keySet().containsAll(fieldNames));
        ImmutableMap<Object, Integer> headerMap = getFieldMap(projection);
        return r -> new Work(r, projection, headerMap);
    }

    public static Function<Iterable<String>, Work> loader(List<String> headerList) {
        Map<String, Integer> projection = new HashMap<>();
        for (int i = 0; i < headerList.size(); i++) projection.put(headerList.get(i), i);
        return loader(projection);
    }

    public static class BibLoader implements Function<BibTeXEntry, Work> {
        public final ImmutableMap<Object, Integer> headerMap;

        public BibLoader(ImmutableMap<Object, Integer> headerMap) {
            this.headerMap = headerMap;
        }

        @Override
        public Work apply(BibTeXEntry entry) {
            return new Work(entry, headerMap);
        }
    }

    public static BibLoader bibLoader(BibTeXDatabase db) {

        return bibLoader(Stream.concat(db.getEntries().values().stream()
                .flatMap(e -> e.getFields().keySet().stream()
                        .filter(k -> !bibtexKeys.containsKey(k))
                        .map(k -> k.toString().trim().toLowerCase())),
                Stream.of(SLRPK_BIB_TYPE, SLRPK_BIB_KEY)
                ).distinct().sorted().collect(Collectors.toList()));

    }
    public static BibLoader bibLoader(List<String> keysNormalized) {
        Map<String, Integer> projection = new HashMap<>();
        Iterator<String> it = Stream.concat(fieldNames.stream(), keysNormalized.stream())
                .distinct().iterator();
        int counter = 0;
        for (; it.hasNext(); ++counter)
            projection.put(it.next(), counter);

        return new BibLoader(getFieldMap(projection));
    }

    public static Function<Work, Work> fieldsAdder(List<String> fields) {
        Map<String, Integer> projection = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) projection.put(fields.get(i), i);
        ImmutableMap<Object, Integer> fieldMap = getFieldMap(projection);
        return w -> (w.fieldMap.keySet().equals(fieldMap.keySet())) ? w
                : new Work(enlargeWithNulls(w.toList(), projection.size()), projection, fieldMap);
    }

    private static ArrayList<String> enlargeWithNulls(@Nonnull List<String> list, int size) {
        ArrayList<String> enlarged = new ArrayList<>(size);
        enlarged.addAll(list);
        for (int i = list.size(); i < size; i++) enlarged.add(null);
        return enlarged;
    }

    protected Work(@Nonnull BibTeXEntry entry,
                   @Nonnull ImmutableMap<Object, Integer> fieldMap) {
        this.fieldMap = fieldMap;
        values = new ArrayList<>(fieldMap.size());
        for (int i = 0; i < fieldMap.size(); i++) values.add(null);

        //add all fields, obeying fieldMap positioning
        Value annote = entry.getField(BibTeXEntry.KEY_ANNOTE);
        values.set(fieldMap.get(Field.Id),
                Id.getIdFromAnnote(annote == null ? null : annote.toUserString()));
        bibtexKeys.forEach((k, v) -> {
            Value bibValue = entry.getField(k);
            values.set(fieldMap.get(v), bibValue == null ? null : bibValue.toUserString());
        });
        if (getKw() != null) //normalize keywords
            set(Field.Kw, Keywords.parse(getKw()).toString());

        //add extra fields present in fieldMap
        entry.getFields().forEach((k, v) -> {
            String name = k.toString().trim().toLowerCase();
            Integer idx = fieldMap.get(name);
            if (!bibtexKeys.containsKey(k) && idx != null)
                values.set(idx, v.toUserString());
        });
        //add special fields, if present in fieldMap
        if (fieldMap.containsKey(SLRPK_BIB_KEY))
            values.set(fieldMap.get(SLRPK_BIB_KEY), entry.getKey().toString());
        if (fieldMap.containsKey(SLRPK_BIB_TYPE))
            values.set(fieldMap.get(SLRPK_BIB_TYPE),
                    entry.getType().toString().trim().toLowerCase());
    }

    public Work(Work work) {
        fieldMap = work.fieldMap;
        values = new ArrayList<>(work.values);
    }


    public List<String> toList() {
        return Collections.unmodifiableList(values);
    }

    public @Nonnull ImmutableSet<String> getExtraFields() {
        if (extraFields == null) {
            extraFields = ImmutableSet.<String>builder()
                    .addAll(fieldMap.keySet().stream().filter(k -> !(k instanceof Field))
                            .map(k -> (String)k).iterator()).build();
        }
        return extraFields;
    }

    public @Nonnull List<String> getAllFields() {
        List<String> list = allFields.get();
        if (list == null) {
            list = fieldMap.keySet().stream().map(Object::toString).collect(Collectors.toList());
            allFields = new WeakReference<>(list);
        }
        return list;
    }

    public String get(@Nonnull Field field) {return values.get(fieldMap.get(field));}
    public String set(@Nonnull Field field, @Nullable String value) {
        return values.set(fieldMap.get(field), value);
    }

    public boolean isMapped(@Nonnull String name) {
        return fieldMap.containsKey(name);
    }
    private Object normalizeHeader(@Nonnull Object header) {
        if (header instanceof Field) return header;
        if (!this.fieldMap.containsKey(header)) {
            assert header instanceof String;
            int fieldIdx = fieldNamesLower.indexOf(header.toString().trim().toLowerCase());
            if (fieldIdx >= 0)
                return Field.values()[fieldIdx];
        }
        return header;
    }
    public String get(@Nonnull String name) {
        Integer idx = fieldMap.get(normalizeHeader(name));
        return idx == null ? null : values.get(idx);
    }
    public String set(@Nonnull String name, @Nullable String value) {
        Integer idx = fieldMap.get(normalizeHeader(name));
        if (idx == null) throw new NoSuchElementException("No header " + name);
        return values.set(idx, value);
    }
    public String put(@Nonnull String name, @Nullable String value) {
        Integer idx = fieldMap.get(normalizeHeader(name));
        if (idx == null) {
            fieldMap = ImmutableMap.<Object, Integer>builder().putAll(fieldMap)
                    .put(name, values.size()).orderEntriesByValue(Integer::compareTo).build();
            extraFields = null;
            values.add(value);
            return null;
        }
        return values.set(idx, value);
    }

    public String getId() {return get(Field.Id);}
    public String getAuthor() {return get(Field.Author);}
    public String getAbstract() {return get(Field.Abstract);}
    public String getKw() {return get(Field.Kw);}
    public String getTitle() {return get(Field.Title);}
    public String getDOI() {return get(Field.DOI);}

    @SuppressWarnings("RedundantIfStatement") //for readability
    public boolean matches(@Nullable Work other) {
        if (other == null) return false;
        if (!simplifyId().isEmpty() && simplifyId().equals(other.simplifyId())) return true;
        if (this.equals(other)) return true;

        String author = simplifyAuthor();
        String oAuthor = other.simplifyAuthor();
        String title = simplifyTitle();
        String oTitle = other.simplifyTitle();
        String doi = simplifyDOI();
        String oDoi = other.simplifyDOI();

        if ((!doi.isEmpty() || !oDoi.isEmpty()) && doi.equals(oDoi))
            return true;
        if (author.equals(oAuthor) && title.equals(oTitle))
            return true;
        if ((author.isEmpty() || oAuthor.isEmpty()) && title.equals(oTitle))
            return true;

        return false;
    }

    private @Nonnull String simplifyId() {
        String id = getId();
        return id == null ? "" : id.trim();
    }

    private @Nonnull String simplifyDOI() {
        String doi = get(Field.DOI);
        if (doi == null) return "";
        try {
            return simplifiedCache.get(Field.DOI, () -> {
                Matcher matcher = doiPattern.matcher(doi);
                return matcher.matches() ? matcher.group(1) : doi;
            });
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException)e.getCause();
            throw new RuntimeException(e.getCause());
        }
    }

    private @Nonnull String simplifyAuthor() {
        String author = get(Field.Author);
        if (author == null) return "";
        try {
            return simplifiedCache.get(Field.Author,
                    () -> Authors.parse(replaceChars(stripCharDecorations(
                            stripAccents(author.trim()).toLowerCase()), "[]{}-", ""))
                            .stream().map(Author::getCiteInitials).reduce(Authors::join).orElse("")
            );
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException)e.getCause();
            throw new RuntimeException(e.getCause());
        }
    }

    private @Nonnull String simplifyTitle() {
        String title = get(Field.Title);
        if (title == null) return "";
        try {
            return simplifiedCache.get(Field.Title,
                    () -> stripAccents(replaceChars(title, " .,[]{}():;-+*", "")).toLowerCase());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException)e.getCause();
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public int compareTo(@Nonnull Work rhs) {
        for (Field field : Field.values()) {
            int diff = get(field).compareTo(rhs.get(field));
            if (diff != 0)
                return diff;
        }
        return fieldMap.keySet().stream().filter(h -> !(h instanceof Field))
                .map(h -> get((String)h).compareTo(rhs.get((String)h)))
                .filter(d -> d != 0).findFirst().orElse(0);
    }

    @SuppressWarnings("SimplifiableIfStatement") //auto generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Work work = (Work) o;

        if (!fieldMap.equals(work.fieldMap)) return false;
        return values.equals(work.values);
    }

    @Override
    public int hashCode() {
        int result = fieldMap.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }
}
