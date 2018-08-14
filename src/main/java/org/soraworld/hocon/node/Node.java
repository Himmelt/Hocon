package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public interface Node {

    boolean notEmpty();

    void setValue(Object value);

    void readValue(BufferedReader reader) throws IOException;

    void writeValue(int indent, BufferedWriter writer) throws IOException;

    void addComment(String comment);

    void setComments(List<String> comments);

    void writeComment(int indent, BufferedWriter writer) throws IOException;

    void clearComments();

    NodeOptions getOptions();

    default void writeIndent(int indent, BufferedWriter writer) throws IOException {
        indent *= getOptions().INDENT_SIZE;
        while (indent-- > 0) writer.write(' ');
    }

}