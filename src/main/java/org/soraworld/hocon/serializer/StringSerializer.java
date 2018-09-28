package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 字符串类型序列化器.
 */
public class StringSerializer implements TypeSerializer<String> {
    public String deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeBase) {
            return ((NodeBase) node).getString();
        } else throw new NotMatchException("String type need NodeBase");
    }

    public Node serialize(Type type, String value, Options options) {
        return new NodeBase(options, value, false);
    }

    public Type getRegType() {
        return String.class;
    }
}
