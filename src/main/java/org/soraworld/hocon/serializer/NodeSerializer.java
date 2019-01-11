package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 结点类型序列化器.
 */
public final class NodeSerializer extends TypeSerializer<Node, Node> {
    @NotNull
    public Node deserialize(@NotNull Type type, @NotNull Node node) {
        return node;
    }

    @NotNull
    public Node serialize(@NotNull Type type, @NotNull Node value, @NotNull Options options) {
        return value;
    }
}
