package org.soraworld.hocon;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileNode extends NodeMap {

    private File file;
    private final List<String> heads = new ArrayList<>();

    private static final String LINE = "-------------------------------------";

    public FileNode(File file) {
        super();
        this.file = file;
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

        if (!heads.isEmpty()) {
            writer.write("# " + LINE + NEW_LINE);
            for (String head : heads) writer.write("# " + head + NEW_LINE);
            writer.write("# " + LINE + NEW_LINE);
            if (notEmpty()) writer.write(NEW_LINE);
        }

        writeValue(0, writer);
        writer.flush();
        writer.close();
    }

    public void load() throws IOException {
        Reader reader = new FileReader(file);
        reader.read();
    }

    public void setHeads(List<String> heads) {
        this.heads.clear();
        if (heads != null) {
            heads.forEach(s -> this.heads.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.heads.removeIf(String::isEmpty);
        }
    }

    public void addHead(String head) {
        if (head != null && !head.isEmpty()) {
            heads.addAll(Arrays.asList(head.split("[\n\r]")));
            heads.removeIf(String::isEmpty);
        }
    }

    public void clearHeads() {
        this.heads.clear();
    }

    public void setIndent(int size) {
        INDENT_SIZE = size;
    }

}
