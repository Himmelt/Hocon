package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

public class NodeList extends NodeBase<ArrayList<NodeBase>> {

    public NodeList() {
        super(new ArrayList<>());
    }

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
    }

    public void clear() {
        value.clear();
    }

    public void add(NodeBase element) {
        value.add(element);
    }

    @Override
    public void writeValue(int indent, Writer writer) throws IOException {
        if (notEmpty()) {
            Iterator<NodeBase> it = value.iterator();
            while (it.hasNext()) {
                writeIndent(indent, writer);
                it.next().writeValue(indent + 1, writer);
                if (it.hasNext()) writer.write(',' + NEW_LINE);
            }
        }
    }

}
