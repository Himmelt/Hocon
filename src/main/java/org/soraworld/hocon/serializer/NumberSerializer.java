package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;

/**
 * 数值类型序列化器.
 */
final class NumberSerializer extends TypeSerializer<Number, NodeBase> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    NumberSerializer() throws SerializerException {
    }

    @Override

    public @NotNull Number deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
        String number = node.getString();
        if (fieldType instanceof Class) {
            Class clazz = Reflects.wrap((Class<?>) fieldType);
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
                } else throw new NotMatchException(getType(), fieldType);
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        }
        throw new NotMatchException(getType(), fieldType);
    }

    @Override
    public @NotNull NodeBase serialize(@NotNull Type fieldType, @NotNull Number value, @NotNull Options options) {
        return new NodeBase(options, value);
    }
}
