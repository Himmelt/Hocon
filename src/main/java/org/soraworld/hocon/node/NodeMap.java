package org.soraworld.hocon.node;

import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.serializer.TypeSerializer;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class NodeMap extends AbstractNode<LinkedHashMap<String, Node>> implements Node {

    public NodeMap(Options options) {
        super(options, new LinkedHashMap<>());
    }

    public NodeMap(Options options, String comment) {
        super(options, new LinkedHashMap<>(), comment);
    }

    public void clear() {
        value.clear();
    }

    public void setNode(String path, Object obj) {
        if (obj instanceof Node && checkCycle((Node) obj)) {
            value.put(path, (Node) obj);
        } else value.put(path, new NodeBase(options, obj, false));
    }

    public void setNode(String path, Object obj, String comment) {
        if (obj instanceof Node && checkCycle((Node) obj)) {
            ((Node) obj).addComment(comment);
            value.put(path, (Node) obj);
        } else value.put(path, new NodeBase(options, obj, false, comment));
    }

    public void modify(@Nonnull Object target) throws Exception {
        List<Field> fields = Reflects.getFields(target.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                Type fieldType = field.getGenericType();
                TypeSerializer serializer = options.getSerializers().get(fieldType);
                if (serializer != null) {
                    Node node = getNode(setting.path().isEmpty() ? field.getName() : setting.path());
                    Object value = serializer.deserialize(fieldType, node);
                    Object current = field.get(target);
                    Class<?> type = current == null ? field.getType() : current.getClass();
                    if (Map.class.isAssignableFrom(type) && value instanceof Map) {
                        if (current instanceof Map) {
                            ((Map) current).clear();
                            ((Map) current).putAll((Map) value);
                        } else {
                            try {
                                Object instance = type.getConstructor().newInstance();
                                if (instance instanceof Map) {
                                    ((Map) instance).putAll((Map) value);
                                    field.set(target, instance);
                                }
                            } catch (Throwable e) {
                                // TODO Interface or Abstract Class has no default non-arg constructor
                                // TODO Exception type cast exception
                                field.set(target, value);
                            }
                        }
                    } else if (Collection.class.isAssignableFrom(type) && value instanceof Collection) {
                        if (current instanceof Collection) {
                            // TODO Exception not support method
                            ((Collection) current).clear();
                            ((Collection) current).addAll((Collection) value);
                        } else {
                            try {
                                Object instance = type.getConstructor().newInstance();
                                if (instance instanceof Collection) {
                                    ((Collection) instance).addAll((Collection) value);
                                    field.set(target, instance);
                                }
                            } catch (Throwable e) {
                                // TODO Interface or Abstract Class has no default non-arg constructor
                                // TODO Exception type cast exception
                                field.set(target, value);
                            }
                        }
                    } else field.set(target, value);// TODO Exception type cast exception
                }
            }
        }
    }

    public void extract(@Nonnull Object source) throws IllegalAccessException {
        clear();
        List<Field> fields = Reflects.getFields(source.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                Object current = field.get(source);
                String path = setting.path().isEmpty() ? field.getName() : setting.path();
                String comment = options.getTranslator().apply(setting.comment());
                if (current == null) {
                    setNode(path, null, comment);
                } else {
                    Type fieldType = field.getGenericType();
                    TypeSerializer serializer = options.getSerializers().get(fieldType);
                    try {
                        setNode(path, serializer.serialize(fieldType, current, options), comment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Node getNode(String path) {
        return value.get(path);
    }

    public Node getNode(String... paths) {
        Node node = this;
        for (String path : paths) {
            if (node instanceof NodeMap) {
                node = ((NodeMap) node).getNode(path);
            } else return null;
        }
        return node;
    }

    public Set<String> getKeys() {
        return value.keySet();
    }

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
    }

    public HashMap<String, String> asStringMap() {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, Node> entry : value.entrySet()) {
            String key = entry.getKey();
            Node node = entry.getValue();
            if (node instanceof NodeBase) {
                map.put(key, ((NodeBase) node).getString());
            } else if (node instanceof NodeMap) {
                HashMap<String, String> sub = ((NodeMap) node).asStringMap();
                sub.forEach((subKey, value) -> map.put(key + '.' + subKey, value));
            }
        }
        return map;
    }

    public void readValue(BufferedReader reader) throws IOException {
        value.clear();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("}") || line.startsWith("]")) return;
            if (line.isEmpty() || line.startsWith("#")) continue;
            // text maybe contains { [ ] } ...
            if (line.endsWith("{") || (line.contains("{") && line.endsWith("}"))) {
                NodeMap node = new NodeMap(options);
                String path = line.substring(0, line.indexOf('{') - 1).trim();
                value.put(path, node);
                if (!line.endsWith("}")) node.readValue(reader);
            } else if (line.contains("=") && (line.endsWith("[") || (line.contains("[") && line.endsWith("]")))) {
                NodeList list = new NodeList(options);
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                value.put(path, list);
                if (!line.endsWith("]")) list.readValue(reader);
            } else if (line.contains("=")) {
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                String text = line.substring(line.indexOf('=') + 1).trim();
                value.put(path, new NodeBase(options, text, true));
            }
        }
    }

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
                    // TODO path with illegal characters
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
                        writer.write(" = [");
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write(']');
                    } else {
                        writer.write(" = ");
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
}
