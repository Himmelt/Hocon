package org.soraworld.hocon.node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractNode<T> implements Node {

    protected final T value;
    protected List<String> comments;
    protected final Options options;

    protected AbstractNode(Options options, T value) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
    }

    protected AbstractNode(Options options, T value, String comment) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
        addComment(comment);
    }

    public final T getValue() {
        return value;
    }

    public final void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            if (comments == null) comments = new ArrayList<>();
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public final void setComments(List<String> comments) {
        if (comments != null && !comments.isEmpty()) {
            this.comments = new ArrayList<>();
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        } else this.comments = null;
    }

    public final void writeComment(int indent, BufferedWriter writer) throws IOException {
        if (comments != null && !comments.isEmpty()) {
            for (String comment : comments) {
                writeIndent(indent, writer);
                writer.write("# " + comment);
                writer.newLine();
            }
        }
    }

    public final Options options() {
        return options;
    }
}
