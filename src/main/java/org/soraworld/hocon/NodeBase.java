package org.soraworld.hocon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class NodeBase implements Node {

    private String value;
    private final List<String> comments = new ArrayList<>();

    public NodeBase() {
    }

    public NodeBase(Object value) {
        if (value != null) this.value = value.toString();
    }

    public NodeBase(String value, String comment) {
        this.value = value;
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public boolean notEmpty() {
        return value != null;
    }

    @Override
    public void readValue(BufferedReader reader) {

    }

    public void readValue(String text) {
        if (text != null) {
            if (text.equals("null")) {
                this.value = null;
                return;
            }
            if (text.startsWith("\"")) text = text.substring(1);
            if (text.endsWith("\"")) text = text.substring(0, text.length() - 2);
            text = text.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        this.value = text;
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws IOException {
        if (value == null) writer.write("null");
        else {
            Matcher matcher = Global.ILLEGAL.matcher(value);
            if (matcher.matches() || value.equals("null")) {
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

}
