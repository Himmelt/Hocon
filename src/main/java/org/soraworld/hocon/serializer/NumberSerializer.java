package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Primitives;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class NumberSerializer implements TypeSerializer<Number> {
    public Number deserialize(@Nonnull Type type, @Nonnull Node node) throws DeserializeException, NullValueException, NotMatchException {
        if (node instanceof NodeBase && type instanceof Class) {
            String number = ((NodeBase) node).getString();
            if (number == null) throw new NullValueException(getRegType());
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
                throw new DeserializeException(e);
            }
        }
        throw new NotMatchException(getRegType(), type);
    }

    public Node serialize(@Nonnull Type type, Number value, @Nonnull Options options) {
        return new NodeBase(options, value, false);
    }

    @Nonnull
    public Type getRegType() {
        return Number.class;
    }
}
