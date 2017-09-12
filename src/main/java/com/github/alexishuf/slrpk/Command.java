package com.github.alexishuf.slrpk;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class Command {
    @Option(name = "--im", aliases = {"-i"}, usage = "Set the id magic prefix. Assigned ids " +
            "will have the form String.format(\"%s-%d\", prefix, id)")
    protected String idMagic = null;
    @Option(name =  "--expr-prefix", usage = "Preprend the contents of the given file to any " +
            "possible expression given to this command. Useful for defining a include path")
    protected File expressionPrefixFile = null;

    protected String expressionPrefix = null;

    private String findUpwardsAndRead(String name) {
        File parent = new File("").getAbsoluteFile();
        while (parent != null) {
            File file = new File(parent, name);
            if (file.exists()) {
                try {
                    return IOUtils.toString(new FileReader(file)).trim();
                } catch (IOException e) {
                    System.err.printf("Failed to read " + file + ", will ignore.\n");
                }
            }
            parent = parent.getParentFile();
        }
        return null;
    }

    public void setupIdMagic() {
        if (idMagic == null)
            idMagic = findUpwardsAndRead(".slrpk.id");
        if (idMagic == null)
            idMagic = "W";
        Id.setIdMagic(idMagic);
    }

    private void setupExpressionPrefix() throws IOException {
        if (expressionPrefixFile != null)
            expressionPrefix = IOUtils.toString(new FileReader(expressionPrefixFile));
        else
            expressionPrefix = findUpwardsAndRead(".slrpk-expr-prefix");
    }

    public void run() throws Exception {
        setupIdMagic();
        setupExpressionPrefix();
        runCommand();
    }

    protected abstract void runCommand() throws Exception;
}
