package org.soraworld.hocon.reflect;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 泛型类型边界.
 */
public final class Bounds {

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

    /**
     * 是否是其他边界的超集.
     *
     * @param bounds 其他边界
     * @return 是否超集
     */
    public boolean isSuperOf(Bounds bounds) {
        if (lower != null) {
            return bounds.lower != null && Reflects.isSuperOf(upper, bounds.upper) && Reflects.isSuperOf(bounds.lower, lower);
        }
        return Reflects.isSuperOf(upper, bounds.upper);
    }

    /**
     * 获取类型对应边界.
     *
     * @param type 类型
     * @return 边界
     */
    public static Bounds getBounds(@Nonnull Type type) {
        return cache.computeIfAbsent(type, Bounds::new);
    }
}
