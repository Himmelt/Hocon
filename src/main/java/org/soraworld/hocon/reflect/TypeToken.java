package org.soraworld.hocon.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 类型标记类.
 *
 * @param <T> 参数类型
 */
public abstract class TypeToken<T> {

    private final Type runtimeType;

    /**
     * 实例化一个类型标记.
     */
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

    /**
     * 获取类型.
     *
     * @return 类型
     */
    public final Type getType() {
        return runtimeType;
    }

    /**
     * 是否是超类型.
     *
     * @param type 类型标记
     * @return 是否超类型
     */
    public final boolean isSuperTypeOf(TypeToken<?> type) {
        return Reflects.isSuperOf(runtimeType, type.runtimeType);
    }
}
