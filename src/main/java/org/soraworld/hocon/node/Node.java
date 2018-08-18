package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public interface Node {

    boolean notEmpty();

    boolean checkCycle(Node node);

    void readValue(BufferedReader reader) throws Exception;

    void writeValue(int indent, BufferedWriter writer) throws Exception;

    void addComment(String comment);

    void setComments(List<String> comments);

    void writeComment(int indent, BufferedWriter writer) throws IOException;

    Options options();

    default void writeIndent(int indent, BufferedWriter writer) throws IOException {
        indent *= options().getIndent();
        while (indent-- > 0) writer.write(' ');
    }
}
