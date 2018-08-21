package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 字符串类型序列化器.
 */
public class StringSerializer implements TypeSerializer<String> {
    public String deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, NotBaseException {
        if (node instanceof NodeBase) {
            String string = ((NodeBase) node).getString();
            if (string == null) throw new NullValueException(getRegType());
            else return string;
        }
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, String value, @Nonnull Options options) {
        return new NodeBase(options, value, false);
    }

    @Nonnull
    public Type getRegType() {
        return String.class;
    }
}
