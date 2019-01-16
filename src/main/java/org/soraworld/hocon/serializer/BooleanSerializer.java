package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 逻辑类型序列化器.
 */
public final class BooleanSerializer extends TypeSerializer<Boolean, NodeBase> {
    /**
     * Instantiates a new Boolean serializer.
     *
     * @throws SerializerException the serializer exception
     */
    public BooleanSerializer() throws SerializerException {
    }

    @NotNull
    public Boolean deserialize(@NotNull Type type, @NotNull NodeBase node) {
        return node.getBoolean();
    }

    @NotNull
    public NodeBase serialize(@NotNull Type type, @NotNull Boolean value, @NotNull Options options) {
        return new NodeBase(options, value, false);
    }
}
