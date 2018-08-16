package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class NodeList extends AbstractNode<ArrayList<Node>> implements Node {

    public NodeList(Options options) {
        super(options, new ArrayList<>());
    }

    public NodeList(Options options, String comment) {
        super(options, new ArrayList<>(), comment);
    }

    public void clear() {
        value.clear();
    }

    public void add(Node node) {
        value.add(node);
    }

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
    }

    public void readValue(BufferedReader reader) throws IOException {
        value.clear();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("]")) return;
            if (line.startsWith("#")) continue;
            if (line.startsWith("{")) {
                NodeMap node = new NodeMap(options);
                value.add(node);
                if (!line.endsWith("}")) node.readValue(reader);
            } else if (line.startsWith("[")) {
                NodeList list = new NodeList(options);
                value.add(list);
                if (!line.endsWith("]")) list.readValue(reader);
            } else {
                value.add(new NodeBase(options, line, true));
            }
        }
    }

    public void writeValue(int indent, BufferedWriter writer) throws IOException {
        if (notEmpty()) {
            Iterator<Node> it = value.iterator();
            while (it.hasNext()) {
                writeIndent(indent, writer);
                Node node = it.next();
                if (node instanceof NodeMap) {
                    writer.write("{");
                    if (node.notEmpty()) {
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                    }
                    writer.write("}");
                } else if (node instanceof NodeList) {
                    writer.write("[");
                    if (node.notEmpty()) {
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                    }
                    writer.write("]");
                } else node.writeValue(indent + 1, writer);
                if (it.hasNext()) writer.newLine();
            }
        }
    }
}
