package org.soraworld.hocon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private static final String COMMENT_HEAD = "# ";
    private static final String TAB_SPACE = "    ";
    private static final String EQUAL = " = ";
    private static final String EQUAL_NODE = " = {";
    private static final String EQUAL_LIST = " = [";
    private static final char END_NODE = '}';
    private static final char END_LIST = ']';

    private List<String> heads = new ArrayList<>();
    private LinkedHashMap<String, CommentValue> values = new LinkedHashMap<>();

    public void write(BufferedWriter writer) throws IOException {
        if (heads != null) {
            for (String head : heads) {
                writer.write(COMMENT_HEAD + head);
                writer.newLine();
            }
            if (values != null && values.size() > 0) writer.newLine();
        }
        if (values != null) {
            for (Map.Entry<String, CommentValue> entry : values.entrySet()) {
                CommentValue value = entry.getValue();
                if (value != null) {
                    value.writeComments(writer);
                    writer.write(entry.getKey());
                    if (value.isNode()) {
                        writer.write(EQUAL_NODE);
                        if (value.isEmpty()) {
                            writer.write(END_NODE);
                            writer.newLine();
                        } else {
                            writer.newLine();
                            value.getNode().write(writer);
                        }
                    } else if (value.isList()) {
                        writer.write(EQUAL_LIST);
                        if (value.isEmpty()) {
                            writer.write(END_LIST);
                            writer.newLine();
                        } else {
                            writer.newLine();
                            //value.getList().write(writer);
                        }
                    } else {
                        writer.write(EQUAL);
                        writer.write(value.toString());
                        writer.newLine();
                    }
                }
            }
        }
    }

    public boolean empty() {
        return values == null || values.isEmpty();
    }

    public void read(BufferedReader reader) {

    }

    public void modify(Object obj) {

    }

    public void readFrom(Object obj) throws IllegalAccessException {
        values = new LinkedHashMap<>();
        appendFrom(obj);
    }

    public void appendFrom(Object obj) throws IllegalAccessException {
        if (obj == null) return;
        List<Field> fields = Fields.getFields(obj.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            Setting set = field.getAnnotation(Setting.class);
            if (set != null) {
                values.put(field.getName(), new CommentValue(field.get(obj)));
            }
        }
    }

    public void addHead(String head) {
        heads.add(head);
    }

    public void put(String path, Object value) {
        values.put(path, new CommentValue(value));
    }

    public CommentValue getValue(String path) {
        return getValue(new Paths(path));
    }

    public CommentValue getValue(String path, Object def) {
        return getValue(new Paths(path), def);
    }

    public CommentValue getValue(String... paths) {
        return getValue(new Paths(paths));
    }

    public CommentValue getValue(Paths paths) {
        return getValue(paths, null);
    }

    public CommentValue getValue(Paths paths, Object def) {
        CommentValue value = values.get(paths.first());
        if (value != null) {
            return value.isNode() ? value.getNode().getValue(paths.next(), def) : value;
        }
        return def == null ? null : new CommentValue(def);
    }

    public Node getNode(String path) {
        return getNode(new Paths(path));
    }

    public Node getNode(String... paths) {
        return getNode(new Paths(paths));
    }

    public Node getNode(Paths paths) {
        CommentValue value = getValue(paths);
        return value != null && value.isNode() ? value.getNode() : null;
    }

}
