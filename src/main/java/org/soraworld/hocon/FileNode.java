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
        //write(writer);
        writeValue(4, writer);
        writer.flush();
        writer.close();
    }

    public void load() throws IOException {
        Reader reader = new FileReader(file);
        reader.read();
    }

}
