package org.soraworld.hocon.node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileNode extends NodeMap {

    private File file;
    private final List<String> heads = new ArrayList<>();

    public FileNode(File file) {
        super(NodeOptions.defaults());
        this.file = file;
    }

    public FileNode(File file, NodeOptions options) {
        super(options);
        this.file = file;
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

        if (!heads.isEmpty()) {
            writer.write("# " + getOptions().LINE);
            writer.newLine();
            for (String head : heads) {
                writer.write("# " + head);
                writer.newLine();
            }
            writer.write("# " + getOptions().LINE);
            writer.newLine();
            if (notEmpty()) writer.newLine();
        }

        writeValue(0, writer);
        writer.flush();
        writer.close();
    }

    public void load() throws IOException {
        // TODO backup copy
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        clear();
        readValue(reader);
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

}
