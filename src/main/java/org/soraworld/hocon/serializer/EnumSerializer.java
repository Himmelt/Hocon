package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 枚举类型序列化器.
 */
public class EnumSerializer implements TypeSerializer<Enum<?>> {
    public Enum<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException, NullValueException, DeserializeException {
        if (node instanceof NodeBase) {
            String name = ((NodeBase) node).getString();
            if (name == null) throw new NullValueException(getRegType());
            try {
                Class<?> rawType = Reflects.getRawType(type);
                Enum<?> value = Reflects.getEnum(rawType.asSubclass(Enum.class), name);
                if (value != null) return value;
                else throw new DeserializeException("Non Enum Value " + name + " for " + rawType.getName());
            } catch (Throwable e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, Enum<?> value, @Nonnull Options options) {
        return new NodeBase(options, value == null ? null : value.name(), false);
    }

    @Nonnull
    public Type getRegType() {
        return new TypeToken<Enum<?>>() {
        }.getType();
    }
}
