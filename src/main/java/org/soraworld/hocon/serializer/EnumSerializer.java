package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * 枚举类型序列化器.
 */
public class EnumSerializer extends TypeSerializer<Enum<?>, NodeBase> {
    @Nonnull
    public Enum<?> deserialize(@Nonnull Type type, @Nonnull NodeBase node) throws HoconException {
        String name = node.getString();
        try {
            Class<?> rawType = Reflects.getRawType(type);
            Enum<?> value = Reflects.getEnum(rawType.asSubclass(Enum.class), name);
            if (value != null) return value;
            else throw new SerializerException("No Enum Value " + name + " for " + rawType.getName());
        } catch (Throwable e) {
            throw new SerializerException(e);
        }
    }

    @Nonnull
    public NodeBase serialize(@Nonnull Type type, @Nonnull Enum<?> value, @Nonnull Options options) {
        return new NodeBase(options, value.name(), false);
    }
}
