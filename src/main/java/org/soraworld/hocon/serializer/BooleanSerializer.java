package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 逻辑类型序列化器.
 */
public class BooleanSerializer implements TypeSerializer<Boolean> {
    public Boolean deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException {
        if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, Boolean value, @Nonnull Options options) {
        return new NodeBase(options, value, false);
    }

    @Nonnull
    public Type getRegType() {
        return Boolean.class;
    }
}
