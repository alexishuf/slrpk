package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.alexishuf.slrpk.LatexStringUtils.stripCharDecorations;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class Author {
    private static final Pattern initialsRx =
            Pattern.compile("\\s*-?[0-9]?([a-zA-Z]).*");

    public final String surname;
    public final List<String> givenNames;
    public final List<String> initials;


    public Author(String surname, List<String> givenNames, List<String> initials) {
        this.surname = surname;
        this.givenNames = Collections.unmodifiableList(givenNames);
        this.initials = Collections.unmodifiableList(initials);
    }

    public String getCiteInitials() {
        return surname + ", " + initials.stream().reduce((l, r) -> l + "." + r).orElse("");
    }

    public String getCiteFull() {
        return surname + ", " + givenNames.stream().reduce((l, r) -> l + " " + r).orElse("");
    }

    public String getFull() {
        return concat(givenNames.stream(), of(surname)).reduce((l, r) -> l + " " + r).orElse("");
    }

    public double fullGivenNamesRatio() {
        Preconditions.checkState(givenNames.size() == initials.size());
        int has = 0;
        for (int i = 0; i < givenNames.size(); i++)
            has = givenNames.get(i).length() > initials.get(i).length() ? 1 : 0;
        return has/(double)givenNames.size();
    }

    public static Author parse(String string) {
        String[] pieces = string.split(",");
        String surname = pieces[0];
        List<String> givenNames;
        if (pieces.length == 2) {
            givenNames = Arrays.stream(pieces[1].split("[ .]"))
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } else {
            List<String> list = Arrays.stream(string.split("[ .]"))
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            surname = list.get(list.size()-1);
            givenNames = list.stream().limit(list.size()-1).collect(Collectors.toList());
        }

        List<String> initials = givenNames.stream().map(name -> {
            Matcher matcher = initialsRx.matcher(stripCharDecorations(name));
            return matcher.matches() ? matcher.group(1) : name;
        }).collect(Collectors.toList());

        return new Author(surname, givenNames, initials);
    }
}
