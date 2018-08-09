package org.soraworld.hocon;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class NodeMap implements Node {

    NodeOptions options;
    private final List<String> comments = new ArrayList<>();
    private final LinkedHashMap<String, Node> value = new LinkedHashMap<>();

    public NodeMap(NodeOptions options) {
        this.options = options != null ? options : NodeOptions.defaults();
    }

    public void clear() {
        value.clear();
    }

    public void setNode(String path, Object node) {
        // TODO cycle reference
        if (node instanceof Node) value.put(path, (Node) node);
        else value.put(path, new NodeBase(node, options));
    }

    public void setNode(String path, Object node, String comment) {
        // TODO cycle reference
        if (node instanceof Node) {
            ((Node) node).addComment(comment);
            value.put(path, (Node) node);
        } else value.put(path, new NodeBase(node, comment, options));
    }

    public void modify(@Nonnull Object object) throws Exception {
        List<Field> fields = Fields.getFields(object.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                String path = setting.path().isEmpty() ? field.getName() : setting.path();
                Node node = getNode(path);
                Class<?> type = field.getType();
                TypeToken<?> token = TypeToken.of(field.getGenericType());
                TypeSerializer serializer = options.getSerializers().get(token);
                if (serializer != null) {
                    if (Map.class.isAssignableFrom(type)) {
                        try {
                            Constructor constructor = type.getConstructor();
                            Object instance = constructor.newInstance();
                            Object value = serializer.deserialize(token, node);
                            if (instance instanceof Map && value instanceof Map) {
                                ((Map) instance).putAll((Map) value);
                                field.set(object, instance);
                            }
                        } catch (Throwable e) {
                            field.set(object, serializer.deserialize(token, node));
                        }
                    } else if (List.class.isAssignableFrom(type)) {
                        try {
                            Constructor constructor = type.getConstructor();
                            Object instance = constructor.newInstance();
                            Object value = serializer.deserialize(token, node);
                            if (instance instanceof List && value instanceof List) {
                                ((List) instance).addAll((Collection) value);
                                field.set(object, instance);
                            }
                        } catch (Throwable e) {
                            field.set(object, serializer.deserialize(token, node));
                        }
                    } else field.set(object, serializer.deserialize(token, node));
                }
            }
        }
    }

    public void extract(@Nonnull Object object) throws IllegalAccessException {
        clear();
        List<Field> fields = Fields.getFields(object.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                Object value = field.get(object);
                String path = setting.path().isEmpty() ? field.getName() : setting.path();
                String comment = setting.comment().startsWith("comment.") ? options.getTranslator().apply(setting.comment()) : setting.comment();
                if (value == null) {
                    setNode(path, null, comment);
                } else {
                    TypeToken<?> token = TypeToken.of(field.getGenericType());
                    TypeSerializer serializer = options.getSerializers().get(token);
                    try {
                        setNode(path, serializer.serialize(token, value, options), comment);
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Node getNode(String path) {
        return value.get(path);
    }

    public boolean notEmpty() {
        return !value.isEmpty();
    }

    public LinkedHashMap<String, Node> getValue() {
        return value;
    }

    public void setValue(Object value) {

    }

    @Override
    public void readValue(BufferedReader reader) throws IOException {
        value.clear();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("}") || line.startsWith("]")) return;
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.contains("{")) {
                NodeMap node = new NodeMap(options);
                String path = line.substring(0, line.indexOf('{') - 1).trim();
                value.put(path, node);
                if (!line.endsWith("}")) node.readValue(reader);
            } else if (line.contains("[")) {
                NodeList list = new NodeList(options);
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                value.put(path, list);
                if (!line.endsWith("]")) list.readValue(reader);
            } else if (line.contains("=")) {
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                String base = line.substring(line.indexOf('=') + 1).trim();
                NodeBase node = new NodeBase(options);
                node.readValue(base);
                value.put(path, node);
            }
        }
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws IOException {
        if (notEmpty()) {
            Iterator<Map.Entry<String, Node>> it = value.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Node> entry = it.next();
                String path = entry.getKey();
                Node node = entry.getValue();
                if (path != null && !path.isEmpty() && node != null) {
                    node.writeComment(indent, writer);
                    writeIndent(indent, writer);
                    writer.write(path);
                    if (node instanceof NodeMap) {
                        writer.write(" {");
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write('}');
                    } else if (node instanceof NodeList) {
                        writer.write(options.EQUAL_LIST);
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write(']');
                    } else {
                        writer.write(options.EQUAL);
                        node.writeValue(indent + 1, writer);
                    }
                    if (it.hasNext()) writer.newLine();
                }
            }
        }
    }

    public void addComment(String path, String comment) {
        Node node = value.get(path);
        if (node != null) node.addComment(comment);
    }

    public void setComments(String path, List<String> comments) {
        Node node = value.get(path);
        if (node != null) node.setComments(comments);
    }

    @Override
    public void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    @Override
    public void setComments(List<String> comments) {
        this.comments.clear();
        if (comments != null) {
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        }
    }

    @Override
    public void writeComment(int indent, BufferedWriter writer) throws IOException {
        for (String comment : comments) {
            writeIndent(indent, writer);
            writer.write("# " + comment);
            writer.newLine();
        }
    }

    @Override
    public void clearComments() {
        this.comments.clear();
    }

    @Override
    public NodeOptions getOptions() {
        return options;
    }

}
