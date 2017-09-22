package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.reflect.Modifier.STATIC;

public class Work2BibTeX implements Function<Work, BibTeXEntry> {
    private Map<String, Key> map;
    private boolean tolerateMissing;
    private static final Map<String, Key> defaultMap;
    private StringValue.Style stringStyle = StringValue.Style.BRACED;

    public Work2BibTeX(Map<String, Key> map, boolean tolerateMissing) {
        this.map = map;
        this.tolerateMissing = tolerateMissing;
    }

    public Work2BibTeX() {
        this(defaultMap, true);
    }

    public Work2BibTeX setStringStyle(StringValue.Style stringStyle) {
        this.stringStyle = stringStyle;
        return this;
    }

    @Override
    public BibTeXEntry apply(Work w) {
        Preconditions.checkArgument(tolerateMissing || map.keySet().containsAll(w.getAllFields()));

        String typeString = w.get(Work.SLRPK_BIB_TYPE);
        Preconditions.checkArgument(typeString != null && !typeString.isEmpty(),
                "Bad value for " + Work.SLRPK_BIB_TYPE + " in " + w);
        Key type = new Key(typeString);

        String keyString = w.get(Work.SLRPK_BIB_KEY);
        Preconditions.checkArgument(keyString != null && !keyString.isEmpty(),
                "Bad value for " + Work.SLRPK_BIB_KEY + " in " + w);
        Key entryKey = new Key(keyString);

        BibTeXEntry entry = new BibTeXEntry(type, entryKey);
        for (String field : w.getAllFields()) {
            Key key = map.getOrDefault(field.toLowerCase(), null);
            if (key == null) continue;
            String value = w.get(field);
            if (value != null && !value.isEmpty())
                entry.addField(key, new StringValue(w.get(field), stringStyle));
        }

        return entry;
    }

    static {
        defaultMap = new HashMap<>();
        Work.bibtexKeys.forEach((key, value) -> defaultMap.put(value.name().toLowerCase(), key));
        for (Field f : BibTeXEntry.class.getFields()) {
            if (Key.class.isAssignableFrom(f.getType()) && (f.getModifiers() & STATIC) != 0) {
                try {
                    Key key = (Key) f.get(null);
                    defaultMap.put(key.getValue().toLowerCase(), key);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
