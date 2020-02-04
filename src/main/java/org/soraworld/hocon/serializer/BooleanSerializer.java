package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 逻辑类型序列化器.
 *
 * @author Himmelt
 */
final class BooleanSerializer extends TypeSerializer<Boolean, NodeBase> {
    /**
     * Instantiates a new Boolean serializer.
     *
     * @throws SerializerException the serializer exception
     */
    BooleanSerializer() throws SerializerException {
    }

    @Override
    @NotNull
    public Boolean deserialize(@NotNull Type fieldType, @NotNull NodeBase node) {
        return node.getBoolean();
    }

    @Override
    @NotNull
    public NodeBase serialize(@NotNull Type fieldType, @NotNull Boolean value, @NotNull Options options) {
        return new NodeBase(options, value);
    }
}
