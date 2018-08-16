package org.soraworld.hocon.serializer;

import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public interface TypeSerializer<T> {
    T deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception;

    Node serialize(@Nonnull Type type, T value, @Nonnull Options options) throws Exception;

    @Nonnull
    Type getRegType();
}
