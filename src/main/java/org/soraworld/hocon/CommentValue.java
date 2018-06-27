package org.soraworld.hocon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class CommentValue {

    private static final String COMMENT_HEAD = "# ";

    private List<String> comments;
    private Object value;

    public CommentValue(Object value) {
        this.value = value;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public boolean isNode() {
        return value instanceof Node;
    }

    public Node getNode() {
        if (value instanceof Node) return (Node) value;
        return null;
    }

    public boolean hasComment() {
        return comments != null && comments.size() > 0;
    }

    public void writeComments(BufferedWriter writer) throws IOException {
        if (comments != null) {
            for (String comment : comments) {
                writer.write(COMMENT_HEAD + comment);
                writer.newLine();
            }
        }
    }

    public boolean isList() {
        return value instanceof Collection;
    }

    public Collection getList() {
        if (value instanceof Collection) return (Collection) value;
        return null;
    }

    public boolean isEmpty() {
        if (value instanceof Node) return ((Node) value).empty();
        if (value instanceof Collection) return ((Collection) value).isEmpty();
        return value == null;
    }

    public void writePrimitive(BufferedWriter writer) throws IOException {
        writer.write(value.toString());
    }

    public String toString() {
        return value == null ? null : value.toString();
    }

}
