package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 结点类型序列化器.
 */
final class NodeSerializer extends TypeSerializer<Node, Node> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    NodeSerializer() throws SerializerException {
    }

    @Override
    public @NotNull Node deserialize(@NotNull Type fieldType, @NotNull Node node) {
        return node;
    }

    @Override
    public @NotNull Node serialize(@NotNull Type fieldType, @NotNull Node value, @NotNull Options options) {
        return value;
    }
}
