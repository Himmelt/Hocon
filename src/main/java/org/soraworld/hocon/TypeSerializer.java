package org.soraworld.hocon;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;

public interface TypeSerializer<T> {

    T deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception;

    Node serialize(@Nonnull TypeToken<?> type, T value) throws ObjectMappingException;

}
