package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;

/**
 * 枚举类型序列化器.
 */
final class EnumSerializer extends TypeSerializer<Enum<?>, NodeBase> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    EnumSerializer() throws SerializerException {
    }

    @NotNull
    public Enum<?> deserialize(@NotNull Type actualType, @NotNull NodeBase node) throws HoconException {
        String name = node.getString();
        try {
            Class<?> rawType = Reflects.getRawType(actualType);
            Enum<?> value = Reflects.getEnum(rawType.asSubclass(Enum.class), name);
            if (value != null) return value;
            else throw new SerializerException("No Enum Value " + name + " for " + rawType.getName());
        } catch (Throwable e) {
            throw new SerializerException(e);
        }
    }

    @NotNull
    public NodeBase serialize(@NotNull Type actualType, @NotNull Enum<?> value, @NotNull Options options) {
        return new NodeBase(options, value.name());
    }
}
