package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class NodeBase<T> {

    protected T value;
    protected List<String> comments = new ArrayList<>();

    public static final String NEW_LINE = System.lineSeparator();

    static int INDENT_SIZE = 2;

    public NodeBase(T value) {
        this.value = value;
    }

    public boolean notEmpty() {
        return value != null;
    }

    protected void writeValue(int indent, Writer writer) throws IOException {
        writer.write(toString());
    }

    public void writeComment(int indent, Writer writer) {

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
