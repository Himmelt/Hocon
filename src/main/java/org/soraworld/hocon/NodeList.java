package org.soraworld.hocon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

public class NodeList extends NodeBase<ArrayList<NodeBase>> {

    public NodeList(ArrayList<NodeBase> value) {
        super(value);
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public void clear() {
        value.clear();
    }

    @Override
    public void writeValue(int indent, Writer writer) throws IOException {
        writer.write(" = [");
        if (!isEmpty()) {
            writer.write(NEW_LINE);
            writeIndent(indent, writer);
            Iterator<NodeBase> it = value.iterator();
            while (it.hasNext()) {
                it.next().writeValue(indent + 4, writer);
                if (it.hasNext()) writer.write(',');
            }
            writeIndent(indent - 4, writer);
        }
        writer.write(']');
        writer.write(NEW_LINE);
    }

}
