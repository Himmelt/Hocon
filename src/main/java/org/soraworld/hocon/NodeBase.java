package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeBase<T> {

    final T value;
    private final List<String> comments = new ArrayList<>();

    static final String NEW_LINE = System.lineSeparator();

    static int INDENT_SIZE = 2;

    public NodeBase(T value) {
        this.value = value;
    }

    public NodeBase(T value, String comment) {
        this.value = value;
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public boolean notEmpty() {
        return value != null;
    }

    protected void writeValue(int indent, Writer writer) throws IOException {
        writer.write(toString());
    }

    public void setComments(List<String> comments) {
        this.comments.clear();
        if (comments != null) {
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        }
    }

    public void clearComments() {
        this.comments.clear();
    }

    public void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public void writeComment(int indent, Writer writer) throws IOException {
        for (String comment : comments) {
            writeIndent(indent, writer);
            writer.write("# " + comment + NEW_LINE);
        }
    }

    public static void writeIndent(int indent, Writer writer) throws IOException {
        indent *= INDENT_SIZE;
        while (indent-- > 0) writer.write(' ');
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
