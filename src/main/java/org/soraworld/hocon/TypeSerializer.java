package org.soraworld.hocon;

import javax.annotation.Nonnull;

public interface TypeSerializer<T,N extends Node> {

    T deserialize(@Nonnull TypeToken<?> type, @Nonnull N node) throws Exception;

    void serialize(@Nonnull TypeToken<?> type, T value, @Nonnull N node);

}
