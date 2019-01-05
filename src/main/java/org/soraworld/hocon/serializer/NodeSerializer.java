package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 结点类型序列化器.
 */
public class NodeSerializer extends TypeSerializer<Node, Node> {
    @Nonnull
    public Node deserialize(@Nonnull Type type, @Nonnull Node node) {
        return node;
    }

    @Nonnull
    public Node serialize(@Nonnull Type type, @Nonnull Node value, @Nonnull Options options) {
        return value;
    }
}
