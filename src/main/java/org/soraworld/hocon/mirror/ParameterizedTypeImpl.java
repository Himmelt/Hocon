package org.soraworld.hocon.mirror;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 参数化类型的值对象实现.
 * 反射每次取到的 {@code ParameterizedType} 实例并不相等，这里提供值语义实现便于比较与缓存.
 *
 * @author Himmelt
 */
final class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type[] arguments;
    private final Type ownerType;

    ParameterizedTypeImpl(Class<?> rawType, Type[] arguments, Type ownerType) {
        this.rawType = rawType;
        this.arguments = arguments == null ? new Type[0] : arguments.clone();
        this.ownerType = ownerType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return arguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType other = (ParameterizedType) obj;
        return rawType.equals(other.getRawType())
                && Arrays.equals(arguments, other.getActualTypeArguments())
                && (ownerType == null ? other.getOwnerType() == null : ownerType.equals(other.getOwnerType()));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arguments) ^ rawType.hashCode() ^ (ownerType == null ? 0 : ownerType.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(rawType.getName());
        if (arguments.length > 0) {
            builder.append('<');
            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(arguments[i].getTypeName());
            }
            builder.append('>');
        }
        return builder.toString();
    }
}
