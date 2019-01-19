package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 字符串类型序列化器.
 */
final class StringSerializer extends TypeSerializer<String, NodeBase> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    StringSerializer() throws SerializerException {
    }

    @NotNull
    public String deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
        return node.getString();
    }

    @NotNull
    public NodeBase serialize(@NotNull Type fieldType, @NotNull String value, @NotNull Options options) {
        return new NodeBase(options, value);
    }
}
