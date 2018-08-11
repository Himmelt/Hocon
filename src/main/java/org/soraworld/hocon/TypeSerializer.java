package org.soraworld.hocon;

import org.soraworld.hocon.token.TypeToken;

import javax.annotation.Nonnull;

public interface TypeSerializer<T> {
    T deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception;

    Node serialize(@Nonnull TypeToken<?> type, T value, @Nonnull NodeOptions options) throws ObjectMappingException;
}
