package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeMap extends NodeBase<LinkedHashMap<String, NodeBase>> {

    public NodeMap() {
        super(new LinkedHashMap<>());
    }

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
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
    protected void writeValue(int indent, Writer writer) throws IOException {
        if (notEmpty()) {
            Iterator<Map.Entry<String, NodeBase>> it = value.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, NodeBase> entry = it.next();
                String path = entry.getKey();
                NodeBase node = entry.getValue();
                if (path != null && !path.isEmpty() && node != null) {
                    node.writeComment(indent, writer);
                    writeIndent(indent, writer);
                    writer.write(path);
                    if (node instanceof NodeMap) {
                        writer.write(" {" + NEW_LINE);
                        node.writeValue(indent + 1, writer);
                        writer.write(NEW_LINE);
                        writeIndent(indent, writer);
                        writer.write('}');
                    } else if (node instanceof NodeList) {
                        writer.write(Constant.EQUAL_LIST + NEW_LINE);
                        node.writeValue(indent + 1, writer);
                        writer.write(NEW_LINE);
                        writeIndent(indent, writer);
                        writer.write(']');
                    } else {
                        writer.write(Constant.EQUAL + node);
                    }
                    if (it.hasNext()) writer.write(NEW_LINE);
                }
            }
        }
    }

}
