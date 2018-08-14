package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class NodeBase implements Node {

    private String value;
    // TODO set to null if empty
    private final List<String> comments = new ArrayList<>();
    private final NodeOptions options;

    public NodeBase(NodeOptions options, Object value, boolean parse) {
        this.options = options != null ? options : NodeOptions.defaults();
        if (value != null) {
            this.value = (parse && value instanceof String) ? parse((String) value) : value.toString();
        }
    }

    public NodeBase(NodeOptions options, Object value, boolean parse, String comment) {
        this.options = options != null ? options : NodeOptions.defaults();
        if (value != null) {
            this.value = (parse && value instanceof String) ? parse((String) value) : value.toString();
        }
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public boolean notEmpty() {
        return value != null;
    }

    @Override
    public void setValue(Object value) {
        this.value = value == null ? null : value.toString();
    }

    @Override
    public void readValue(BufferedReader reader) {
    }

    private String parse(String text) {
        if (text.equals("null")) return null;
        if (text.startsWith("\"")) text = text.substring(1);
        if (text.endsWith("\"")) text = text.substring(0, text.length() - 2);
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws IOException {
        if (value == null) writer.write("null");
        else {
            Matcher matcher = options.ILLEGAL.matcher(value);
            if (matcher.matches() || value.equals("null") || value.endsWith(" ")) {
                String target = value.replace("\\", "\\\\").replace("\"", "\\\"");
                writer.write('"' + target + '"');
            } else writer.write(value);
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

    public String getString() {
        return value;
    }

    public int getInt() {
        return Integer.valueOf(value);
    }

    public long getLong() {
        return Long.valueOf(value);
    }

    public float getFloat() {
        return Float.valueOf(value);
    }

    public double getDouble() {
        return Double.valueOf(value);
    }

    public boolean getBoolean() {
        // TODO true/yes/1/t/y
        return Boolean.valueOf(value);
    }

}
