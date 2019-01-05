package org.soraworld.hocon.serializer;

import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 字符串类型序列化器.
 */
public class StringSerializer extends TypeSerializer<String, NodeBase> {
    @Nonnull
    String deserialize(@Nonnull Type type, @Nonnull NodeBase node) {
        return node.getString();
    }

    @Nonnull
    public NodeBase serialize(@Nonnull Type type, @Nonnull String value, @Nonnull Options options) {
        return new NodeBase(options, value);
    }
}
