package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.StringValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Id {
    private static String idMagic;
    private static Pattern annoteIdPattern;
    private static Pattern pattern;
    public static final @Nonnull Id NULL = new Id("", "");

    static {
        setIdMagic("W");
    }

    public static void setIdMagic(String value) {
        idMagic = value;
        annoteIdPattern = Pattern.compile(".*ID" + idMagic + "=(" + idMagic + "-[0-9]+).*", Pattern.DOTALL);
        pattern = Pattern.compile(idMagic+"-(\\d+)");
    }

    public static String getIdMagic() {
        return idMagic;
    }

    public final @Nonnull String prefix;
    public final @Nonnull String local;


    public Id(@Nonnull String prefix, @Nonnull String local) {
        this.prefix = prefix;
        this.local = local;
    }

    public Id(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            prefix = NULL.prefix;
            local = NULL.local;
        } else {
            prefix = idMagic;
            Matcher matcher = pattern.matcher(string);
            Preconditions.checkArgument(matcher.matches());
            local = matcher.group(1);
        }
    }

    public static @Nonnull String format(int value) {
        return idMagic + "-" + value;
    }

    @Nullable
    public static String getIdFromAnnote(@Nullable String annote) {
        if (annote == null) return null;
        Matcher matcher = annoteIdPattern.matcher(annote);
        return matcher.matches() ? matcher.group(1) : null;
    }

    public static void setIdInAnnote(@Nonnull BibTeXEntry entry, @Nonnull String id) {
        StringValue value = (StringValue)entry.getField(BibTeXEntry.KEY_ANNOTE);
        String string = value == null ? null : value.toUserString();
        string = setIdInAnnote(string, id);
        StringValue.Style style = value == null ? StringValue.Style.BRACED : value.getStyle();
        entry.addField(BibTeXEntry.KEY_ANNOTE, new StringValue(string, style));
    }

    @Nonnull
    public static String setIdInAnnote(@Nullable String annote, @Nonnull String id) {
        if (annote == null) annote = "";
        if (!id.startsWith(idMagic +"-"))
            id = idMagic + "-" + id;
        Matcher matcher = annoteIdPattern.matcher(annote);
        String string = String.format("ID%s=%s", idMagic, id);
        return matcher.matches() ? matcher.replaceAll(string) : string + "\n" + annote;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Id id = (Id) o;

        if (!prefix.equals(id.prefix)) return false;
        return local.equals(id.local);
    }

    @Override
    public int hashCode() {
        int result = prefix.hashCode();
        result = 31 * result + local.hashCode();
        return result;
    }
}
