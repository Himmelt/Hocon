package org.soraworld.hocon.serializer;

import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 逻辑类型序列化器.
 */
public class BooleanSerializer extends TypeSerializer<Boolean, NodeBase> {
    @Nonnull
    public Boolean deserialize(@Nonnull Type type, @Nonnull NodeBase node) {
        return node.getBoolean();
    }

    @Nonnull
    public NodeBase serialize(@Nonnull Type type, @Nonnull Boolean value, @Nonnull Options options) {
        return new NodeBase(options, value, false);
    }
}
