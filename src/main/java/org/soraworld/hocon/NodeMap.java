package org.soraworld.hocon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
        else value.put(path, new NodeBase(String.valueOf(node)));
    }

    public void setNode(String path, Object node, String comment) {
        // TODO cycle reference
        if (node instanceof Node) {
            ((Node) node).addComment(comment);
            value.put(path, (Node) node);
        } else value.put(path, new NodeBase(String.valueOf(node), comment));
    }

    public boolean notEmpty() {
        return !value.isEmpty();
    }

    @Override
    public void readValue(BufferedReader reader) throws IOException {
        value.clear();
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("}") || line.startsWith("]")) return;
            if (line.startsWith("#")) continue;
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
                String base = line.substring(line.indexOf('='));
                value.put(path, new NodeBase(base));
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
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                        writer.write('}');
                    } else if (node instanceof NodeList) {
                        writer.write(Global.EQUAL_LIST);
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                        writer.write(']');
                    } else {
                        writer.write(Global.EQUAL + node);
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
