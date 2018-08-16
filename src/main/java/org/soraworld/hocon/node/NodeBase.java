package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;

public class NodeBase extends AbstractNode<String> implements Node {

    public NodeBase(Options options, Object obj, boolean parse) {
        super(options, obj == null ? null : parse && obj instanceof String ? parse((String) obj) : obj.toString());
    }

    public NodeBase(Options options, Object obj, boolean parse, String comment) {
        super(options, obj == null ? null : parse && obj instanceof String ? parse((String) obj) : obj.toString(), comment);
    }

    public boolean notEmpty() {
        return value != null;
    }

    @Override
    public void readValue(BufferedReader reader) {
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
        if (value != null) {
            return value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("yes")
                    || value.equalsIgnoreCase("1")
                    || value.equalsIgnoreCase("t")
                    || value.equalsIgnoreCase("y");
        } else return false;
    }

    private static String parse(String text) {
        if (text.equals("null")) return null;
        if (text.startsWith("\"")) text = text.substring(1);
        if (text.endsWith("\"")) text = text.substring(0, text.length() - 1);
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
