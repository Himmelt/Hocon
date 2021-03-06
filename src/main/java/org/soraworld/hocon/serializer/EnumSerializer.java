package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 枚举类型序列化器.
 *
 * @author Himmelt
 */
final class EnumSerializer extends TypeSerializer<Enum<?>, NodeBase> {
    @Override
    public @NotNull Enum<?> deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
        String name = node.getString();
        try {
            Class<?> rawType;
            if (fieldType instanceof Class<?>) {
                rawType = (Class<?>) fieldType;
            } else if (fieldType instanceof ParameterizedType) {
                rawType = (Class<?>) ((ParameterizedType) fieldType).getRawType();
            } else {
                throw new SerializerException("Failed to get raw class type for " + fieldType.getTypeName());
            }
            Enum<?> value = Reflects.getEnum(rawType.asSubclass(Enum.class), name);
            if (value != null) {
                return value;
            } else {
                throw new SerializerException("No Enum Value " + name + " for " + rawType.getName());
            }
        } catch (Throwable e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public @NotNull NodeBase serialize(@NotNull Type fieldType, @NotNull Enum<?> value, @NotNull Options options) {
        return new NodeBase(options, value.name());
    }
}
