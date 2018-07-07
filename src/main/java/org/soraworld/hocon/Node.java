package org.soraworld.hocon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import static org.soraworld.hocon.Global.INDENT_SIZE;

public interface Node {

    boolean notEmpty();

    void readValue(BufferedReader reader) throws IOException;

    void writeValue(int indent, BufferedWriter writer) throws IOException;

    void addComment(String comment);

    void setComments(List<String> comments);

    void writeComment(int indent, BufferedWriter writer) throws IOException;

    void clearComments();

    default void writeIndent(int indent, BufferedWriter writer) throws IOException {
        indent *= INDENT_SIZE;
        while (indent-- > 0) writer.write(' ');
    }

    default void setIndent(int indent) {
        INDENT_SIZE = indent;
    }

}
