package com.github.alexishuf.slrpk;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class Command {
    @Option(name = "--im", aliases = {"-i"}, usage = "Set the id magic prefix. Assigned ids " +
            "will have the form String.format(\"%s-%d\", prefix, id)")
    protected String idMagic = null;

    public void setupIdMagic() {
        File parent = new File("").getAbsoluteFile();
        while (parent != null && idMagic == null) {
            File file = new File(parent, ".slrpk.id");
            if (file.exists()) {
                try {
                    idMagic = IOUtils.toString(new FileReader(file)).trim();
                } catch (IOException e) {
                    System.err.printf("Failed to read " + file + ", will ignore.\n");
                }
            }
            parent = parent.getParentFile();
        }
        if (idMagic == null)
            idMagic = "W";
        Id.setIdMagic(idMagic);
    }

    public void run() throws Exception {
        setupIdMagic();
        runCommand();
    }

    protected abstract void runCommand() throws Exception;
}
