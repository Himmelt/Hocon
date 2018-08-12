package org.soraworld.hocon.token;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;

public class Bounds {

    private final Type upper;
    private final Type lower;

    private static final HashMap<Type, Bounds> cache = new HashMap<>();

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
        } else {
            this.upper = type;
            this.lower = type;
        }
    }

    public boolean isSuperOf(Bounds bounds) {
        if (lower != null) {
            if (bounds.lower != null) {
                return TypeToken.of(upper).isSuperTypeOf(TypeToken.of(bounds.upper))
                        && TypeToken.of(bounds.lower).isSuperTypeOf(TypeToken.of(lower));
            } else return false;
        }
        return TypeToken.of(upper).isSuperTypeOf(TypeToken.of(bounds.upper));
    }

    public static Bounds getBounds(@Nonnull Type type) {
        return cache.computeIfAbsent(type, Bounds::new);
    }
}
