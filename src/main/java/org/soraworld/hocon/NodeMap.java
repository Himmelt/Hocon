package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeMap extends NodeBase<LinkedHashMap<String, NodeBase>> {

    public NodeMap() {
        super(new LinkedHashMap<>());
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public void setBool(String path, boolean value) {
        this.value.put(path, new NodeBase<>(value));
    }

    public void setNum(String path, Number value) {
        this.value.put(path, new NodeBase<>(value));
    }

    public void setString(String path, String value) {
        this.value.put(path, new NodeBase<>(value));
    }

    public void setList(String path, NodeList list) {
        this.value.put(path, list);
    }

    public void setMap(String path, NodeMap map) {
        // TODO cycle reference
        this.value.put(path, map);
    }

    private boolean containsValue(NodeMap map) {
        return false;
    }

    public void clear() {
        value.clear();
    }

    @Override
    public void writeValue(int indent, Writer writer) throws IOException {
        //writer.write(" {");
        if (!isEmpty()) {
            //writer.write(NEW_LINE);
            //writeIndent(indent, writer);
            for (Map.Entry<String, NodeBase> entry : value.entrySet()) {
                NodeBase node = entry.getValue();
                node.writeComment(indent, writer);
                writer.write(entry.getKey());
                node.writeValue(indent + 4, writer);
            }
            writeIndent(indent - 4, writer);
        }
        //writer.write('}');
        //writer.write(NEW_LINE);
    }

}
