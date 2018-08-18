package org.soraworld.hocon.reflect;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public abstract class TypeToken<T> {

    private final Type runtimeType;

    protected TypeToken() {
        runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalStateException("Cannot construct a TypeToken for a type variable.\n"
                    + "You probably meant to call new TypeToken<"
                    + String.valueOf(runtimeType)
                    + ">(getClass()) "
                    + "that can resolve the type variable for you.\n"
                    + "If you do need to create a TypeToken of a type variable.");
        }
    }

    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(String.valueOf(superclass) + " isn't parameterized.");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public final Type getType() {
        return runtimeType;
    }

    public final boolean isSuperTypeOf(@Nonnull TypeToken<?> type) {
        return Reflects.isSuperOf(runtimeType, type.runtimeType);
    }
}
