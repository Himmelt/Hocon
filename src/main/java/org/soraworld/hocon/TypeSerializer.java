package org.soraworld.hocon;

public interface TypeSerializer<T> {

    T deserialize(Class<?> type, Node node);

    void serialize(Class<?> type, T obj, Node node);

}
