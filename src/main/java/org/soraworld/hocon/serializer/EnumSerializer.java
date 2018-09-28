package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 枚举类型序列化器.
 */
public class EnumSerializer implements TypeSerializer<Enum<?>> {
    public Enum<?> deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeBase) {
            String name = ((NodeBase) node).getString();
            if (name == null) return null;
            try {
                Class<?> rawType = Reflects.getRawType(type);
                Enum<?> value = Reflects.getEnum(rawType.asSubclass(Enum.class), name);
                if (value != null) return value;
                else throw new SerializerException("Non Enum Value " + name + " for " + rawType.getName());
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        } else throw new NotMatchException("Enum<?> type need NodeBase");
    }

    public Node serialize(Type type, Enum<?> value, Options options) {
        return new NodeBase(options, value == null ? null : value.name(), false);
    }

    public Type getRegType() {
        return new TypeToken<Enum<?>>() {
        }.getType();
    }
}
