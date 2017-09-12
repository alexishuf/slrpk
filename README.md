# slrpk
a.k.a Systematic Literature Review Painkiller

## Warning
Before you proceed, be warned that this tool is essentially a gigantic hack pulled together in a 
frustrating weekend. The deduplication is slow and there are not even unit tests! 
**You have been warned**

Issues are welcome though, but I can't promise to fix them quickly.

## What it is?
A tool for dealing with .bib and .csv files that can contain extras in addition to the .bib files.
It can be used to:
* Identify duplicates
* Evaluate expressions with set operations: union, intersection, complement
* Set fields in the .csv according to
** Whether a work is present in a expression
** Regular expressions

## What it is not?
It is not a SLR (Systematic Literature Review) tool. 
It does not follow Kitchenham [1] or any other guidelines from anywhere.


## Building and Installing
Build with `mvn clean package`, this will give you an executable fat-jar on 
`target/slrpk-1.0-SNAPSHOT.jar`.

There is no fancy install. If `somewhere` is in your `$PATH` and you want to put the jar in 
`somewhere_else`, do this from the project dir:
```bash
cp target/slrpk-1.0-SNAPSHOT.jar somewhere_else/
cat > somewhere/slrpk <<EOF
#!/bin/bash
java -jar somewhere_else/slrpk-1.0-SNAPSHOT.jar $@
EOF
chmod +x somewhere/slrpk
```

## Documentation
slrpk has the following sub-commands:
* `expr`: evaluates an [expression](#expressions) 
* `update-csv`: transforms a bib file in a csv file with only [main fields](#main-fields) 
                and writes back a new bib file with assigned ids in the entries `annote` fields. 
* `set-field-expr`: evaluates an [expression](#expressions) and for each work in the given 
                    `--csv` that is also in that result, will assign a special value to a field.
* `set-field-rx`: similar to `set-field-expr` but evaluates a regexp against a field in the 
                  work itself.
                  
`slrpk expr --help` and corresponding commands give detailed usage information. 

### Main Fields
The basic element in slrpk is a  `Work`. It will always have 6 fields (that can be null):
1. `Id`
2. `Author`
3. `Title`
4. `Abstract`
5. `Kw`
5. `DOI`

The fields need not be in this order on a .csv file, but there must be headers with these names. 
When loading from .bib files, these fields are extracted, but Id is not assigned. Only 
`update-csv` assigns the `Id` field.

An id always has a prefix, to avoid mixing up works from different reviews. The local part is 
assigned by counting upwards as ids are assigned. `update-csv` starts counting from `max+1` 
when the csv file already exists. The Id prefix can be set from the command line with `--im` or 
can be put in a file named `.slrpk.id` in the working directory where slrpk is run or somewhere 
up in the directory tree.  

### Expressions

Imagine every .csv and .bib files are *sets* of Work instances and there are no duplicate entries. 
Being sets you can use basic set-theoretic operators:
* `!` and `~` mean complement: The complement of a set is infinite, transformations after parse 
        usually hide this. The expression `x & !y` is OK, but just `!x` is not because an 
        infinite set cannot be iterated
* `&` and `*` mean intersection
* `-` means set difference (`x - y` is the same as `x & !y`)
* `|` and `+` mean union

`()`'s can be used to control precedence (the list before is ordered by decreasing precedence). 
All binary operators are left-associative and *require spaces before and after*. There is also a 
projection operator:

* `term#`: Returns only the [main fields](#main-fields)
* `term#(excluded)`: Returns only the [main fields](#main-fields) plus the `excluded` field
* `term#!(excluded)`: Returns only the [main fields](#main-fields) plus 
                      **all fields other than** `excluded`

A projection (view) is done with the `?` postfix operator:
* `term?(Author = 'Doe, John')`: returns only John Doe's papers
* `term?(Author % 'Doe.*')`: returns papers from anyone with Doe as surname
* `term?(Author % 'Doe.*',Title !% '(?i).*services? * composition.*')`: returns papers from anyone 
        with Doe as surname, but that does not contain service composition in the title 
        (case insensitive). The regex syntax is that of 
        [Pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)
* `term?!(Author % 'Doe.*',Title !% '(?i).*services? * composition.*')` all papers that did not 
        match the previous query

The basic terms of expressions are .bib and .csv files. You do not need to quote file names 
(because whitespace around binary operators are enforced). If needed, you can quote with either 
`'` or `"`. Quoting is also optional for field names in the projection operator. Anywhere a string 
is needed, any of the following three lexical rules are accepted:

```antlrv4
UQSTR  : (~[ \t\r\n()!~#?'"@,])+;
SQSTR  : '\'' ('\\\''|.)+? '\'';
DQSTR  : '"'  ('\\"' |.)+? '"';
```

When specifying a file, `?` can be prepended to it (`?file` or `?"file"`) to mark it as optional. 
If an optional file is not found, an mepty set is used in its place. By default all files are 
mandatory, and a non-existing file will cause an exception to be thrown. 

Typing long file paths is boring, so you can define include paths:

```
#include /home/alexis/somewhere/v3/mendeley-2017-03-05T15:20-03:00
#include /home/alexis/somewhere/v4/bibs-2017-09-08
#include /home/alexis/somewhere/v4
```

And then write `scopus.h` instead of `/home/alexis/somewhere/v4/bibs-2017-09-08/scopus.h` the 
include paths are searched in the declared order, the first match is used regardless of possible 
matches by other include paths.

Expressions can be read from a file (`expr-file`), from stdin (`--stdin`) or from arguments. 
The availability of these methods may vary between commands.  

#### Examples
Assume the file `ip` has some include paths, then you can do stuff like:

Which works I lost with my updated query string?
```bash
slrpk expr --csv /tmp/lost.csv $(cat ip) old.bib & ~new.bib
```
The csv is too bulky... can i have something with only what's important?
```bash
slrpk expr --csv /tmp/lost.csv "$(cat ip) (old.bib & ~new.bib)#" #only main fields
slrpk expr --count "$(cat ip) old.bib & ~new.bib" #only the count
```
Mark all "Proceedings" for exclusion
```bash
slrpk set-field-rx --csv works.csv --field exclude --value 1 --rx-field Title '(?i).*proceedings.*'
``` 
Reuse a previous selection in the form of a .bib file
```bash
slrpk set-field-expr --csv works.csv --field include --value 1 $(cat ip) goodstuff.bib
slrpk set-field-expr --csv works.csv --field exclude --value 1 "$(cat ip) old.csv?(exclude = 1)"
```
Mark proceedings originating from `scopus.bib` (--keep prevents erasing already assinged values 
for entries unaffected by this command)
```bash
slrpk set-field-expr --csv works.csv --field exclude --value 1 --keep $(cat ip) \
"scopus.bib?(Title % '(?i).*proceedings.*')"
``` 

And more to come ... someday.

- - - 

[1]: Kitchenham, B., & Charters, S. (2007). Guidelines for performing Systematic Literature reviews in Software Engineering Version 2.3. Engineering, 45(4ve), 1051. http://doi.org/10.1145/1134285.1134500 
