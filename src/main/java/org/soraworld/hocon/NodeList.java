package org.soraworld.hocon;

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

    @Override
    public void readValue(BufferedReader reader) {

    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws IOException {
        if (notEmpty()) {
            Iterator<Node> it = value.iterator();
            while (it.hasNext()) {
                writeIndent(indent, writer);
                it.next().writeValue(indent + 1, writer);
                if (it.hasNext()) {
                    writer.write(',');
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

}
