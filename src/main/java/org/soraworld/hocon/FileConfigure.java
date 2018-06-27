package org.soraworld.hocon;

import java.io.*;

public class FileConfigure extends Node {

    private File file;

    public FileConfigure(File file) {
        this.file = file;
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        write(writer);
        writer.flush();
        writer.close();
    }

    public void load() throws IOException {
        Reader reader = new FileReader(file);
        reader.read();
    }

}
