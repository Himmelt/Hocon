package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Primitives;

import java.lang.reflect.Type;

/**
 * 数值类型序列化器.
 */
public class NumberSerializer implements TypeSerializer<Number> {
    public Number deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeBase) {
            if (type instanceof Class) {
                String number = ((NodeBase) node).getString();
                if (number == null) return null;
                Class clazz = Primitives.wrap((Class<?>) type);
                try {
                    if (Integer.class.equals(clazz)) {
                        return Integer.valueOf(number);
                    } else if (Long.class.equals(clazz)) {
                        return Long.valueOf(number);
                    } else if (Short.class.equals(clazz)) {
                        return Short.valueOf(number);
                    } else if (Byte.class.equals(clazz)) {
                        return Byte.valueOf(number);
                    } else if (Float.class.equals(clazz)) {
                        return Float.valueOf(number);
                    } else if (Double.class.equals(clazz)) {
                        return Double.valueOf(number);
                    } else throw new NotMatchException(getRegType(), type);
                } catch (Throwable e) {
                    throw new SerializerException(e);
                }
            } else throw new NotMatchException(getRegType(), type);
        } else throw new NotMatchException("Number type need NodeBase");
    }

    public Node serialize(Type type, Number value, Options options) {
        return new NodeBase(options, value, false);
    }

    public Type getRegType() {
        return Number.class;
    }
}
