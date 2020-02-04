package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 字符串类型序列化器.
 *
 * @author Himmelt
 */
final class StringSerializer extends TypeSerializer<String, NodeBase> {
    @Override
    public @NotNull String deserialize(@NotNull Type fieldType, @NotNull NodeBase node) {
        return node.getString();
    }

    @Override
    public @NotNull NodeBase serialize(@NotNull Type fieldType, @NotNull String value, @NotNull Options options) {
        return new NodeBase(options, value);
    }
}
