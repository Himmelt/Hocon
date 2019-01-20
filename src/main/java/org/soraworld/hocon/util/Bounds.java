package org.soraworld.hocon.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 泛型类型边界.
 */
final class Bounds {

    private final Type upper;
    private final Type lower;
    private static final ConcurrentHashMap<Type, Bounds> BOUNDS_CACHE = new ConcurrentHashMap<>();

    private Bounds(@NotNull Type type) {
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

    /**
     * 是否是其他边界的超集.
     *
     * @param bounds 其他边界
     * @return 是否超集
     */
    boolean isSuperOf(@NotNull Bounds bounds) {
        if (lower != null) {
            return bounds.lower != null && Reflects.isAssignableFrom(upper, bounds.upper) && Reflects.isAssignableFrom(bounds.lower, lower);
        }
        return Reflects.isAssignableFrom(upper, bounds.upper);
    }

    /**
     * 获取类型对应边界.
     *
     * @param type 类型
     * @return 边界
     */
    @NotNull
    static Bounds getBounds(@NotNull Type type) {
        return BOUNDS_CACHE.computeIfAbsent(type, Bounds::new);
    }
}
