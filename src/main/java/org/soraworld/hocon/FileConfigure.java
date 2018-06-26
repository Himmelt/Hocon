package org.soraworld.hocon;

import java.io.*;
import java.util.ArrayList;

public class FileConfigure {

    private File file;
    private String head;
    private ArrayList<Node> nodes;

    public void save() throws IOException {
        Writer writer = new FileWriter(file);
        writer.write(head);
        writer.write("\n");
        for (Node node : nodes) {
            node.write(writer);
        }
    }

    public void load() throws IOException {
        Reader reader = new FileReader(file);
        reader.read();
    }

}
