package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 结点类型序列化器.
 *
 * @author Himmelt
 */
final class NodeSerializer extends TypeSerializer<Node, Node> {
    @Override
    public @NotNull Node deserialize(@NotNull Type fieldType, @NotNull Node node) {
        return node.copy();
    }

    @Override
    public @NotNull Node serialize(@NotNull Type fieldType, @NotNull Node value, @NotNull Options options) {
        return value.copy();
    }
}
