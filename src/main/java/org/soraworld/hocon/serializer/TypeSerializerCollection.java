package org.soraworld.hocon.serializer;

import org.soraworld.hocon.reflect.Bounds;
import org.soraworld.hocon.reflect.Primitives;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class TypeSerializerCollection {
    private final TypeSerializerCollection parent;
    private final SerializerList serializers = new SerializerList();
    private final ConcurrentHashMap<Type, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    TypeSerializerCollection(TypeSerializerCollection parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public TypeSerializer get(@Nonnull Type type) {
        if (type instanceof Class) {
            type = Primitives.wrap((Class) type);
        }
        TypeSerializer<?> serial = typeMatches.computeIfAbsent(type, serializers);
        if (serial == null && parent != null) {
            serial = parent.get(type);
        }
        return serial;
    }

    public <T> TypeSerializerCollection registerType(@Nonnull Type type, @Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(new RegisteredSerializer(type, serializer));
        typeMatches.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeSerializerCollection registerPredicate(@Nonnull Predicate<Type> test, @Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(new RegisteredSerializer(test, serializer));
        typeMatches.clear();
        return this;
    }

    public TypeSerializerCollection newChild() {
        return new TypeSerializerCollection(this);
    }

    private static final class RegisteredSerializer {
        private final Predicate<Type> predicate;
        private final TypeSerializer<?> serializer;

        private RegisteredSerializer(Predicate<Type> predicate, TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        private RegisteredSerializer(Type type, TypeSerializer<?> serializer) {
            this(new SuperTypePredicate(type), serializer);
        }
    }

    private static final class SuperTypePredicate implements Predicate<Type> {

        private final Type type;

        SuperTypePredicate(Type type) {
            this.type = type;
        }

        @Override
        public boolean test(Type type) {
            try {
                return Bounds.isSuperOf(this.type, type);// type.isSuperTypeOf(type);
                //SUPERTYPE_TEST.invoke(type, t);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static final class SerializerList extends CopyOnWriteArrayList<RegisteredSerializer> implements Function<Type, TypeSerializer<?>> {

        @Override
        public TypeSerializer<?> apply(Type type) {
            for (RegisteredSerializer serial : this) {
                if (serial.predicate.test(type)) {
                    return serial.serializer;
                }
            }
            return null;
        }
    }

}
