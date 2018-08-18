package org.soraworld.hocon.serializer;

import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class NodeSerializer implements TypeSerializer<Node> {
    public Node deserialize(@Nonnull Type type, @Nonnull Node node) {
        return node;
    }

    public Node serialize(@Nonnull Type type, Node value, @Nonnull Options options) {
        return value;
    }

    @Nonnull
    public Type getRegType() {
        return Node.class;
    }
}
