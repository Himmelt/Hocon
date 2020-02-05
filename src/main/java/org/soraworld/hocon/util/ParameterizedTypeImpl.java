package org.soraworld.hocon.util;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 参数类型实现.
 *
 * @author Himmelt
 */
final class ParameterizedTypeImpl implements ParameterizedType {

    private final Type[] arguments;
    private final Class<?> rawType;
    private final Type ownerType;

    ParameterizedTypeImpl(Class<?> rawType, Type[] arguments, Type ownerType) {
        this.arguments = arguments;
        this.rawType = rawType;
        this.ownerType = ownerType != null ? ownerType : rawType.getDeclaringClass();
        validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable[] params = rawType.getTypeParameters();
        if (params.length != arguments.length) {
            throw new MalformedParameterizedTypeException();
        }
    }

    @Override
    public Type[] getActualTypeArguments() {
        return arguments.clone();
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) obj;
            if (this != type) {
                Type ownerType = type.getOwnerType();
                Type rawType = type.getRawType();
                return Objects.equals(this.ownerType, ownerType) && Objects.equals(this.rawType, rawType) && Arrays.equals(this.arguments, type.getActualTypeArguments());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.arguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (ownerType != null) {
            if (ownerType instanceof Class) {
                builder.append(((Class) ownerType).getName());
            } else {
                builder.append(ownerType.toString());
            }

            builder.append("$");
            if (ownerType instanceof ParameterizedTypeImpl) {
                builder.append(rawType.getName().replace(((ParameterizedTypeImpl) ownerType).rawType.getName() + "$", ""));
            } else {
                builder.append(rawType.getSimpleName());
            }
        } else {
            builder.append(rawType.getName());
        }

        if (arguments != null && arguments.length > 0) {
            builder.append("<");
            boolean flag = true;
            for (Type type : this.arguments) {
                if (!flag) {
                    builder.append(", ");
                }
                builder.append(type.getTypeName());
                flag = false;
            }
            builder.append(">");
        }

        return builder.toString();
    }
}
