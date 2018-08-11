package org.soraworld.hocon.token;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

public abstract class TypeToken<T> {

    private Type keyType;
    private Type valType;
    private Boolean isMap;
    private Boolean isList;
    private Class<?> rawType;
    private final Type runtimeType;

    protected TypeToken() {
        this.runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalStateException("Cannot construct a TypeToken for a type variable.\n"
                    + "You probably meant to call new TypeToken<"
                    + String.valueOf(runtimeType)
                    + ">(getClass()) "
                    + "that can resolve the type variable for you.\n"
                    + "If you do need to create a TypeToken of a type variable, "
                    + "please use TypeToken.of() instead.");
        }
    }

    private TypeToken(@Nonnull Type type) {
        this.runtimeType = type;
    }

    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(String.valueOf(superclass) + " isn't parameterized.");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public static <T> TypeToken<T> of(@Nonnull Class<T> type) {
        return new SimpleTypeToken<T>(type);
    }

    public static TypeToken<?> of(@Nonnull Type type) {
        return new SimpleTypeToken<>(type);
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
        if (runtimeType instanceof Class<?> && type.runtimeType instanceof Class<?>) {
            return ((Class<?>) runtimeType).isAssignableFrom((Class<?>) type.runtimeType);
        }
        if (isMap() && type.isMap()) {
            return getKeyType().isSuperTypeOf(type.getKeyType()) && getValType().isSuperTypeOf(type.getValType());
        }
        if (isList() && type.isList()) {
            return getKeyType().isSuperTypeOf(type.getKeyType());
        }
        return runtimeType instanceof WildcardType;
    }

    public boolean isMap() {
        if (isMap == null) {
            if (runtimeType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) runtimeType).getActualTypeArguments();
                if (getRawType().equals(Map.class) && types != null && types.length == 2) {
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
                if (getRawType().equals(List.class) && types != null && types.length == 1) {
                    keyType = types[0];
                    isList = true;
                } else isList = false;
            } else isList = false;
        }
        return isList;
    }

    /* Map Key Type && List Type*/
    public TypeToken<?> getKeyType() {
        return isMap() || isList() ? of(keyType) : null;
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
