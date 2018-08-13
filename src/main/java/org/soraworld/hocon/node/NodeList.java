package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class NodeList implements Node {

    private final ArrayList<Node> value = new ArrayList<>();
    private final List<String> comments = new ArrayList<>();

    private final NodeOptions options;

    public NodeList(NodeOptions options) {
        this.options = options != null ? options : NodeOptions.defaults();
    }

    public void clear() {
        value.clear();
    }

    public void add(Node node) {
        value.add(node);
    }

    @Override
    public boolean notEmpty() {
        return !value.isEmpty();
    }

    public void setValue(Object value) {

    }

    @Override
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
                // TODO ","
                NodeBase node = new NodeBase(options);
                node.readValue(line);
                value.add(node);
            }
        }
    }

    @Override
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
                if (it.hasNext()) {
                    //writer.write(',');
                    writer.newLine();
                }
            }
        }
    }

    @Override
    public void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    @Override
    public void setComments(List<String> comments) {
        this.comments.clear();
        if (comments != null) {
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        }
    }

    @Override
    public void writeComment(int indent, BufferedWriter writer) throws IOException {
        for (String comment : comments) {
            writeIndent(indent, writer);
            writer.write("# " + comment);
            writer.newLine();
        }
    }

    @Override
    public void clearComments() {
        this.comments.clear();
    }

    @Override
    public NodeOptions getOptions() {
        return options;
    }

    public List<Node> getValue() {
        return value;
    }

}
