package org.soraworld.hocon.serializer;

import org.soraworld.hocon.reflect.Primitives;
import org.soraworld.hocon.reflect.Reflects;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SerializerCollection {

    private final SerializerCollection parent;
    private final CopyOnWriteArrayList<TypeSerializer<?>> serializers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Type, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    SerializerCollection(SerializerCollection parent) {
        this.parent = parent;
    }

    public TypeSerializer get(@Nonnull Type type) {
        if (type instanceof Class) {
            type = Primitives.wrap((Class<?>) type);
        }
        TypeSerializer<?> serializer = typeMatches.computeIfAbsent(type, typ -> {
            for (TypeSerializer<?> serial : serializers) {
                if (Reflects.isSuperOf(serial.getRegType(), typ)) return serial;
            }
            return null;
        });
        if (serializer == null && parent != null) {
            serializer = parent.get(type);
        }
        return serializer;
    }

    public <T> void registerType(@Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(serializer);
        typeMatches.clear();
    }

    public SerializerCollection newChild() {
        return new SerializerCollection(this);
    }
}
