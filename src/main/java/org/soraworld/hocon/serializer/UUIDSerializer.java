package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDSerializer implements TypeSerializer<UUID> {
    public UUID deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException {
        if (node instanceof NodeBase) {
            String uuid = ((NodeBase) node).getString();
            if (uuid == null) throw new NullValueException(getRegType());
            try {
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, UUID uuid, @Nonnull Options options) {
        return new NodeBase(options, uuid, false);
    }

    @Nonnull
    public Type getRegType() {
        return UUID.class;
    }
}
