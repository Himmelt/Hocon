package org.soraworld.hocon;

import org.soraworld.hocon.token.TypeToken;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class TypeSerializerCollection {
    private final TypeSerializerCollection parent;
    private final SerializerList serializers = new SerializerList();
    private final Map<TypeToken<?>, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    TypeSerializerCollection(TypeSerializerCollection parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeSerializer<T> get(@Nonnull TypeToken<T> type) {
        type = type.wrap();
        TypeSerializer<?> serial = typeMatches.computeIfAbsent(type, serializers);
        if (serial == null && parent != null) {
            serial = parent.get(type);
        }
        return (TypeSerializer) serial;
    }

    public <T> TypeSerializerCollection registerType(@Nonnull TypeToken<T> type, @Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(new RegisteredSerializer(type, serializer));
        typeMatches.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeSerializerCollection registerPredicate(@Nonnull Predicate<TypeToken<T>> test, @Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(new RegisteredSerializer((Predicate) test, serializer));
        typeMatches.clear();
        return this;
    }

    public TypeSerializerCollection newChild() {
        return new TypeSerializerCollection(this);
    }

    private static final class RegisteredSerializer {
        private final Predicate<TypeToken<?>> predicate;
        private final TypeSerializer<?> serializer;

        private RegisteredSerializer(Predicate<TypeToken<?>> predicate, TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        private RegisteredSerializer(TypeToken<?> type, TypeSerializer<?> serializer) {
            this(new SuperTypePredicate(type), serializer);
        }
    }

    private static final class SuperTypePredicate implements Predicate<TypeToken<?>> {

        private final TypeToken<?> type;

        SuperTypePredicate(TypeToken<?> type) {
            this.type = type;
        }

        @Override
        public boolean test(TypeToken<?> token) {
            try {
                return type.isSuperTypeOf(token);
                //SUPERTYPE_TEST.invoke(type, t);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static final class SerializerList extends CopyOnWriteArrayList<RegisteredSerializer> implements Function<TypeToken<?>, TypeSerializer<?>> {

        @Override
        public TypeSerializer<?> apply(TypeToken<?> type) {
            for (RegisteredSerializer serial : this) {
                if (serial.predicate.test(type)) {
                    return serial.serializer;
                }
            }
            return null;
        }
    }

}
