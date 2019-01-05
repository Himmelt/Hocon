package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.TypeResolver;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 数值类型序列化器.
 */
public class NumberSerializer extends TypeSerializer<Number, NodeBase> {
    @Nonnull
    public Number deserialize(@Nonnull Type type, @Nonnull NodeBase node) throws HoconException {
        String number = node.getString();
        if (type instanceof Class) {
            Class clazz = TypeResolver.wrapPrimitives((Class<?>) type);
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
                } else throw new NotMatchException(getType(), type);
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        }
        throw new NotMatchException(getType(), type);
    }

    @Nonnull
    public NodeBase serialize(@Nonnull Type type, @Nonnull Number value, @Nonnull Options options) {
        return new NodeBase(options, value);
    }
}
