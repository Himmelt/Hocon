package org.soraworld.hocon.reflect;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TypeToken<T> {

    private Type keyType;
    private Type valType;
    private Boolean isMap;
    private Boolean isList;
    private Boolean isEnum;
    private Class<?> rawType;
    private final Type runtimeType;

    private static final ConcurrentHashMap<Type, TypeToken> cache = new ConcurrentHashMap<>();

    protected TypeToken() {
        runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalStateException("Cannot construct a TypeToken for a type variable.\n"
                    + "You probably meant to call new TypeToken<"
                    + String.valueOf(runtimeType)
                    + ">(getClass()) "
                    + "that can resolve the type variable for you.\n"
                    + "If you do need to create a TypeToken of a type variable, "
                    + "please use TypeToken.of() instead.");
        }
        cache.put(runtimeType, this);
    }

    private TypeToken(@Nonnull Type type) {
        this.runtimeType = type;
        cache.put(type, this);
    }

    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(String.valueOf(superclass) + " isn't parameterized.");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public static <T> TypeToken<T> of(@Nonnull Class<T> type) {
        TypeToken token = cache.get(type);
        return token != null ? token : new SimpleTypeToken<T>(type);
    }

    public static TypeToken<?> of(@Nonnull Type type) {
        TypeToken token = cache.get(type);
        return token != null ? token : new SimpleTypeToken<>(type);
    }

    public final Type getType() {
        return runtimeType;
    }

    public final Class<?> getRawType() {
        if (rawType == null) {
            if (runtimeType instanceof ParameterizedType) {
                Type type = ((ParameterizedType) runtimeType).getRawType();
                if (type instanceof Class) rawType = (Class) type;
            } else if (runtimeType instanceof Class) {
                rawType = (Class) runtimeType;
            }
        }
        return rawType;
    }

    public final boolean isPrimitive() {
        return (runtimeType instanceof Class) && ((Class<?>) runtimeType).isPrimitive();
    }

    public final TypeToken<T> wrap() {
        if (isPrimitive()) {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) runtimeType;
            return of(Primitives.wrap(type));
        }
        return this;
    }

    public final boolean isSuperTypeOf(@Nonnull TypeToken<?> type) {
        return Reflects.isSuperOf(runtimeType, type.runtimeType);
    }

    public boolean isMap() {
        if (isMap == null) {
            if (runtimeType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) runtimeType).getActualTypeArguments();
                if (Map.class.isAssignableFrom(getRawType()) && types != null && types.length == 2) {
                    keyType = types[0];
                    valType = types[1];
                    isMap = true;
                } else isMap = false;
            } else isMap = false;
        }
        return isMap;
    }

    public boolean isList() {
        if (isList == null) {
            if (runtimeType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) runtimeType).getActualTypeArguments();
                if (Collection.class.isAssignableFrom(getRawType()) && types != null && types.length == 1) {
                    keyType = types[0];
                    isList = true;
                } else isList = false;
            } else isList = false;
        }
        return isList;
    }

    public boolean isEnum() {
        if (isEnum == null) {
            if (runtimeType instanceof Class) {
                isEnum = Enum.class.isAssignableFrom((Class<?>) runtimeType);
                if (isEnum) keyType = runtimeType;
            } else if (runtimeType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) runtimeType).getActualTypeArguments();
                if (Enum.class.isAssignableFrom(getRawType()) && types != null && types.length == 1) {
                    keyType = types[0];
                    isEnum = true;
                } else isEnum = false;
            } else isEnum = false;
        }
        return isEnum;
    }

    /* Map Key Type && List Type && Enum Type */
    public TypeToken<?> getKeyType() {
        return isMap() || isList() || isEnum() ? of(keyType) : null;
    }

    public TypeToken<?> getValType() {
        return isMap() ? of(valType) : null;
    }

    private static final class SimpleTypeToken<T> extends TypeToken<T> {
        SimpleTypeToken(Type type) {
            super(type);
        }
    }
}
