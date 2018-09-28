package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 结点类型序列化器.
 */
public class NodeSerializer implements TypeSerializer<Node> {
    public Node deserialize(Type type, Node node) throws NullNodeException {
        if (node == null) throw new NullNodeException();
        return node;
    }

    public Node serialize(Type type, Node value, Options options) {
        return value == null ? new NodeBase(options, null, false) : value;
    }

    public Type getRegType() {
        return Node.class;
    }
}
