package org.soraworld.hocon;

import com.google.common.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class NodeMap implements Node {

    private final LinkedHashMap<String, Node> value = new LinkedHashMap<>();
    private final List<String> comments = new ArrayList<>();


    public void clear() {
        value.clear();
    }

    public void setNode(String path, Object node) {
        // TODO cycle reference
        if (node instanceof Node) value.put(path, (Node) node);
        else value.put(path, new NodeBase(node));
    }

    public void setNode(String path, Object node, String comment) {
        // TODO cycle reference
        if (node instanceof Node) {
            ((Node) node).addComment(comment);
            value.put(path, (Node) node);
        } else value.put(path, new NodeBase(String.valueOf(node), comment));
    }

    public void modify(Object object) throws Exception {
        List<Field> fields = Fields.getFields(object.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                String path = setting.value().isEmpty() ? field.getName() : setting.value();
                Node node = getNode(path);
                Class<?> type = field.getType();
                AnnotatedType annotatedType = field.getAnnotatedType();
                Type ttt = field.getGenericType();
                TypeToken token = TypeToken.of(ttt);
                TypeSerializer serializer = TypeSerializers.getDefaultSerializers().get(token);
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
                    } else field.set(object, serializer.deserialize(token, node));
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
                NodeMap node = new NodeMap();
                String path = line.substring(0, line.indexOf('{') - 1).trim();
                value.put(path, node);
                if (!line.endsWith("}")) node.readValue(reader);
            } else if (line.contains("[")) {
                NodeList list = new NodeList();
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                value.put(path, list);
                if (!line.endsWith("]")) list.readValue(reader);
            } else if (line.contains("=")) {
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                String base = line.substring(line.indexOf('=') + 1).trim();
                NodeBase node = new NodeBase();
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
                        writer.write(Global.EQUAL_LIST);
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write(']');
                    } else {
                        writer.write(Global.EQUAL);
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

}
