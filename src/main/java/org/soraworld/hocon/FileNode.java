package org.soraworld.hocon;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileNode extends NodeMap {

    private File file;
    private final List<String> heads = new ArrayList<>();

    public FileNode(File file) {
        super();
        this.file = file;
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writeValue(0, writer);
        writer.flush();
        writer.close();
    }

    public void load() throws IOException {
        Reader reader = new FileReader(file);
        reader.read();
    }

    public void setIndent(int size) {
        INDENT_SIZE = size;
    }

}
