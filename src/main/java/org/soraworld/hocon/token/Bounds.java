package org.soraworld.hocon.token;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.concurrent.ConcurrentHashMap;

public class Bounds {

    private final Type upper;
    private final Type lower;

    private static final ConcurrentHashMap<Type, Bounds> cache = new ConcurrentHashMap<>();

    private Bounds(@Nonnull Type type) {
        if (type instanceof WildcardType) {
            Type[] upper = ((WildcardType) type).getUpperBounds();
            Type[] lower = ((WildcardType) type).getLowerBounds();
            if (upper.length == 0) this.upper = Object.class;
            else this.upper = upper[0];
            if (lower.length == 0) this.lower = null;
            else this.lower = lower[0];
        } else if (type instanceof TypeVariable) {
            Type[] upper = ((TypeVariable) type).getBounds();
            if (upper.length == 0) this.upper = Object.class;
            else this.upper = upper[0];
            lower = null;
        } else if (type instanceof ParameterizedType) {
            this.upper = ((ParameterizedType) type).getRawType();
            this.lower = ((ParameterizedType) type).getRawType();
        } else {
            this.upper = type;
            this.lower = type;
        }
    }

    public boolean isSuperOf(Bounds bounds) {
        if (lower != null) {
            return bounds.lower != null && isSuperOf(upper, bounds.upper) && isSuperOf(bounds.lower, lower);
        }
        return isSuperOf(upper, bounds.upper);
    }

    public static Bounds getBounds(@Nonnull Type type) {
        return cache.computeIfAbsent(type, Bounds::new);
    }

    public static boolean isSuperOf(@Nonnull Type upper, @Nonnull Type lower) {
        if (upper.equals(lower)) return true;
        if (upper instanceof Class && lower instanceof Class) {
            return ((Class<?>) upper).isAssignableFrom((Class<?>) lower);
        }
        if (upper instanceof ParameterizedType && lower instanceof ParameterizedType) {
            // raw type isSuper than compare parameter
            Class<?> upperRaw = (Class) ((ParameterizedType) upper).getRawType();
            Class<?> lowerRaw = (Class) ((ParameterizedType) lower).getRawType();
            if (upperRaw.isAssignableFrom(lowerRaw)) {
                /*if (Map.class.isAssignableFrom(upperRaw) && Map.class.isAssignableFrom(lowerRaw)) {
                    //compare parameter type
                    Type[] upperTypes = ((ParameterizedType) upper).getActualTypeArguments();
                    Type[] lowerTypes = ((ParameterizedType) lower).getActualTypeArguments();
                    if (upperTypes.length == 2 && lowerTypes.length == 2) {
                        return isSuperOf(upperTypes[0], lowerTypes[0]) && isSuperOf(upperTypes[1], lowerTypes[1]);
                    }
                } else if (Collection.class.isAssignableFrom(upperRaw) && Collection.class.isAssignableFrom(lowerRaw)) {
                    //compare parameter type
                    Type[] upperTypes = ((ParameterizedType) upper).getActualTypeArguments();
                    Type[] lowerTypes = ((ParameterizedType) lower).getActualTypeArguments();
                    if (upperTypes.length == 1 && lowerTypes.length == 1) {
                        return isSuperOf(upperTypes[0], lowerTypes[0]);
                    }
                }*/
                Type[] upperTypes = ((ParameterizedType) upper).getActualTypeArguments();
                Type[] lowerTypes = ((ParameterizedType) lower).getActualTypeArguments();
                if (upperTypes.length == lowerTypes.length) {
                    if (upperTypes.length > 0) {
                        for (int i = 0; i < upperTypes.length; i++) {
                            if (!isSuperOf(upperTypes[i], lowerTypes[i])) return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        if (upper instanceof WildcardType || upper instanceof TypeVariable) {
            return getBounds(upper).isSuperOf(getBounds(lower));
        }
        return false;
    }
}
