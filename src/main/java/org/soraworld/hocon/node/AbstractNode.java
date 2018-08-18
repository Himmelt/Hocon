package org.soraworld.hocon.node;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractNode<T> implements Node {

    protected final T value;
    protected List<String> comments;
    protected final Options options;
    protected static final Pattern ILLEGAL = Pattern.compile(".*[\":=,+?`!@#$^&*{}\\[\\]\\\\].*");

    protected AbstractNode(Options options, T value) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
    }

    protected AbstractNode(Options options, T value, String comment) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
        addComment(comment);
    }

    public boolean checkCycle(Node node) {
        if (this.equals(node)) return false;
        if (node instanceof NodeMap) {
            for (Node sub : ((NodeMap) node).value.values()) {
                if (!checkCycle(sub)) return false;
            }
            return true;
        } else if (node instanceof NodeList) {
            for (Node sub : ((NodeList) node).value) {
                if (!checkCycle(sub)) return false;
            }
            return true;
        } else return true;
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

    public static String quotation(@Nonnull String text) {
        if (text.equals("null") || text.startsWith(" ") || text.endsWith(" ") || ILLEGAL.matcher(text).matches()) {
            String target = text.replace("\\", "\\\\").replace("\"", "\\\"");
            return '"' + target + '"';
        }
        return text;
    }

    public static String unquotation(@Nonnull String text) {
        if (text.startsWith("\"")) text = text.substring(1);
        if (text.endsWith("\"")) text = text.substring(0, text.length() - 1);
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
