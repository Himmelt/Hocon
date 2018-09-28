package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 逻辑类型序列化器.
 */
public class BooleanSerializer implements TypeSerializer<Boolean> {
    public Boolean deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
        throw new NotMatchException("Boolean type need NodeBase");
    }

    public Node serialize(Type type, Boolean value, Options options) {
        return new NodeBase(options, value, false);
    }

    public Type getRegType() {
        return Boolean.class;
    }
}
