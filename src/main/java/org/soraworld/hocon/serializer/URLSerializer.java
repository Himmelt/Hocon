package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

public class URLSerializer implements TypeSerializer<URL> {
    public URL deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException {
        if (node instanceof NodeBase) {
            String plain = ((NodeBase) node).getString();
            if (plain == null) throw new NullValueException(getRegType());
            try {
                return new URL(plain);
            } catch (MalformedURLException e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, URL url, @Nonnull Options options) {
        return new NodeBase(options, url, false);
    }

    @Nonnull
    public Type getRegType() {
        return URL.class;
    }
}
