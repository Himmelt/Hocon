package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class NodeBase<T> {

    protected T value;
    protected List<String> comments = new ArrayList<>();

    public static final String NEW_LINE = System.lineSeparator();

    public NodeBase(T value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void writeValue(int indent, Writer writer) throws IOException {
        if (value instanceof NodeBase) {
            ((NodeBase) value).writeValue(indent, writer);
        } else if (value != null) {
//            writeIndent(indent, writer);
            writer.write(" = " + value + NEW_LINE);
        }
    }

    public void writeComment(int indent, Writer writer) {

    }

    public static void writeIndent(int indent, Writer writer) throws IOException {
        while (indent-- > 0) writer.write(' ');
    }

}
