package org.soraworld.hocon.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.hocon.exception.NonRawTypeException;
import org.soraworld.hocon.exception.NotParamListException;
import org.soraworld.hocon.exception.NotParamMapException;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 反射相关工具.
 */
public abstract class Reflects {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE;
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> CLAZZ_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, ConcurrentHashMap<String, Enum<?>>> ENUM_FIELDS = new ConcurrentHashMap<>();

    static {
        Map<Class<?>, Class<?>> map1 = new HashMap<>();
        Map<Class<?>, Class<?>> map2 = new HashMap<>();
        map1.put(boolean.class, Boolean.class);
        map2.put(Boolean.class, boolean.class);
        map1.put(byte.class, Byte.class);
        map2.put(Byte.class, byte.class);
        map1.put(char.class, Character.class);
        map2.put(Character.class, char.class);
        map1.put(double.class, Double.class);
        map2.put(Double.class, double.class);
        map1.put(float.class, Float.class);
        map2.put(Float.class, float.class);
        map1.put(int.class, Integer.class);
        map2.put(Integer.class, int.class);
        map1.put(long.class, Long.class);
        map2.put(Long.class, long.class);
        map1.put(short.class, Short.class);
        map2.put(Short.class, short.class);
        map1.put(void.class, Void.class);
        map2.put(Void.class, void.class);
        PRIMITIVE_WRAPPER = Collections.unmodifiableMap(map1);
        WRAPPER_PRIMITIVE = Collections.unmodifiableMap(map2);
    }

    public static Class<?> wrap(@NotNull Class<?> clazz) {
        return clazz.isPrimitive() ? PRIMITIVE_WRAPPER.get(clazz) : clazz;
    }

    public static Class<?> unwrap(@NotNull Class<?> clazz) {
        return WRAPPER_PRIMITIVE.getOrDefault(clazz, clazz);
    }

    /**
     * 获取类的非静态字段.
     *
     * @param clazz 类
     * @return 字段列表
     */
    @NotNull
    public static List<Field> getFields(@NotNull Class<?> clazz) {
        if (CLAZZ_FIELDS.containsKey(clazz)) return CLAZZ_FIELDS.get(clazz);
        CopyOnWriteArrayList<Field> fields = new CopyOnWriteArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        fields.addAll(0, getFields(clazz.getSuperclass()));
        fields.forEach(field -> field.setAccessible(true));
        CLAZZ_FIELDS.put(clazz, fields);
        return fields;
    }

    /**
     * 按名字获取枚举类型的实例.
     *
     * @param <T>   枚举类型
     * @param clazz 实例类
     * @param name  名字
     * @return 枚举实例
     */
    public static <T extends Enum<T>> T getEnum(@NotNull Class<T> clazz, @NotNull String name) {
        ConcurrentHashMap<String, Enum<?>> fields = ENUM_FIELDS.get(clazz);
        if (fields == null) {
            fields = new ConcurrentHashMap<>();
            Enum[] enums = clazz.getEnumConstants();
            if (enums != null) {
                for (Enum field : enums) {
                    fields.put(field.name(), field);
                }
            }
            ENUM_FIELDS.put(clazz, fields);
        }
        return (T) fields.get(name);
    }

    /**
     * 获取原始类型.
     *
     * @param type 类型
     * @return 原始类型
     * @throws NonRawTypeException 无原始类型异常
     */
    public static Class<?> getRawType(Type type) throws NonRawTypeException {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) return (Class<?>) ((ParameterizedType) type).getRawType();
        else throw new NonRawTypeException(type);
    }

    /**
     * 获取映射类型的类型参数数组.
     *
     * @param type 参数化类型
     * @return 类型参数
     * @throws NotParamMapException 非映射参数化类型异常
     */
    public static Type[] getMapParameter(ParameterizedType type) throws NotParamMapException {
        if (Map.class.isAssignableFrom((Class<?>) type.getRawType())) {
            Type[] types = type.getActualTypeArguments();
            if (types.length == 2) return types;
        }
        throw new NotParamMapException(type);
    }

    /**
     * 获取集合类型的类型参数数组.
     *
     * @param type 参数化类型
     * @return 类型参数
     * @throws NotParamListException 非集合参数化类型异常
     */
    public static Type getListParameter(ParameterizedType type) throws NotParamListException {
        if (Collection.class.isAssignableFrom((Class<?>) type.getRawType())) {
            Type[] types = type.getActualTypeArguments();
            if (types.length == 1) return types[0];
        }
        throw new NotParamListException(type);
    }

    public static boolean isAssignableFrom(@NotNull Type ancestor, @NotNull Type child) {
        if (ancestor.equals(child) || Object.class == ancestor) return true;

        if (ancestor instanceof Class<?>) {
            Class<?> parent = (Class<?>) ancestor;
            if (child instanceof Class<?>) {
                return isAssignableFrom(parent, (Class<?>) child);
            }
            if (child instanceof ParameterizedType) {
                Type childRaw = ((ParameterizedType) child).getRawType();
                if (childRaw instanceof Class) {
                    return isAssignableFrom(parent, (Class<?>) childRaw);
                }
            } else if (parent.isArray() && child instanceof GenericArrayType) {
                Type childComponent = ((GenericArrayType) child).getGenericComponentType();
                return isAssignableFrom(parent.getComponentType(), childComponent);
            }
        }

        if (ancestor instanceof ParameterizedType) {
            if (child instanceof Class<?>) {
                Type parent = ((ParameterizedType) ancestor).getRawType();
                if (parent instanceof Class<?>) {
                    return isAssignableFrom((Class<?>) parent, (Class<?>) child);
                }
            } else if (child instanceof ParameterizedType) {
                return isAssignableFrom((ParameterizedType) ancestor, (ParameterizedType) child);
            }
        }

        if (ancestor instanceof GenericArrayType) {
            Type parentComponent = ((GenericArrayType) ancestor).getGenericComponentType();
            if (child instanceof Class<?>) {
                Class<?> childClazz = (Class<?>) child;
                if (childClazz.isArray()) {
                    return isAssignableFrom(parentComponent, childClazz.getComponentType());
                }
            } else if (child instanceof GenericArrayType) {
                Type childComponent = ((GenericArrayType) child).getGenericComponentType();
                return isAssignableFrom(parentComponent, childComponent);
            }
        }

        if (ancestor instanceof WildcardType) {
            return isAssignableFrom((WildcardType) ancestor, child);
        }

        return false;
    }

    public static boolean isAssignableFrom(@NotNull Class<?> ancestor, @NotNull Class<?> child) {
        if (ancestor.isAssignableFrom(child)) return true;
        if (ancestor.isPrimitive()) {
            Class<?> primitive = WRAPPER_PRIMITIVE.get(child);
            return ancestor == primitive;
        } else {
            Class<?> wrapper = PRIMITIVE_WRAPPER.get(child);
            return wrapper != null && ancestor.isAssignableFrom(wrapper);
        }
    }

    private static boolean isAssignableFrom(ParameterizedType ancestor, ParameterizedType child) {
        if (ancestor.equals(child)) return true;

        Type[] ancestorArgs = ancestor.getActualTypeArguments();
        Type[] childArgs = child.getActualTypeArguments();

        if (ancestorArgs.length != childArgs.length) return false;

        for (int i = 0; i < ancestorArgs.length; ++i) {
            Type lhsArg = ancestorArgs[i];
            Type rhsArg = childArgs[i];

            if (!lhsArg.equals(rhsArg) && !(lhsArg instanceof WildcardType && isAssignableFrom((WildcardType) lhsArg, rhsArg))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAssignableFrom(WildcardType lhsType, Type rhsType) {
        Type[] lUpperBounds = lhsType.getUpperBounds();

        // supply the implicit upper bound if none are specified
        if (lUpperBounds.length == 0) {
            lUpperBounds = new Type[]{Object.class};
        }

        Type[] lLowerBounds = lhsType.getLowerBounds();

        // supply the implicit lower bound if none are specified
        if (lLowerBounds.length == 0) {
            lLowerBounds = new Type[]{null};
        }

        if (rhsType instanceof WildcardType) {
            // both the upper and lower bounds of the right-hand side must be
            // completely enclosed in the upper and lower bounds of the left-
            // hand side.
            WildcardType rhsWcType = (WildcardType) rhsType;
            Type[] rUpperBounds = rhsWcType.getUpperBounds();

            if (rUpperBounds.length == 0) {
                rUpperBounds = new Type[]{Object.class};
            }

            Type[] rLowerBounds = rhsWcType.getLowerBounds();

            if (rLowerBounds.length == 0) {
                rLowerBounds = new Type[]{null};
            }

            for (Type lBound : lUpperBounds) {
                for (Type rBound : rUpperBounds) {
                    if (!isAssignableBound(lBound, rBound)) {
                        return false;
                    }
                }

                for (Type rBound : rLowerBounds) {
                    if (!isAssignableBound(lBound, rBound)) {
                        return false;
                    }
                }
            }

            for (Type lBound : lLowerBounds) {
                for (Type rBound : rUpperBounds) {
                    if (!isAssignableBound(rBound, lBound)) {
                        return false;
                    }
                }

                for (Type rBound : rLowerBounds) {
                    if (!isAssignableBound(rBound, lBound)) {
                        return false;
                    }
                }
            }
        } else {
            for (Type lBound : lUpperBounds) {
                if (!isAssignableBound(lBound, rhsType)) {
                    return false;
                }
            }

            for (Type lBound : lLowerBounds) {
                if (!isAssignableBound(rhsType, lBound)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isAssignableBound(@Nullable Type lhsType, @Nullable Type rhsType) {
        if (rhsType == null) {
            return true;
        }
        if (lhsType == null) {
            return false;
        }
        return isAssignableFrom(lhsType, rhsType);
    }

    public static <T, S extends T> Type[] getActualArguments(Class<T> ancestor, Class<S> child) {
        return getActualArguments(getGenericType(ancestor, child), child);
    }

    public static Type[] getActualArguments(Type ancestor, Class<?> child) {
        Type[] result = null;
        if (ancestor instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) ancestor;
            Type[] arguments = paramType.getActualTypeArguments();
            result = new Type[arguments.length];
            System.arraycopy(arguments, 0, result, 0, arguments.length);
        } else if (ancestor instanceof TypeVariable) {
            result = new Type[1];
            result[0] = ancestor;
        } else if (ancestor instanceof Class) {
            TypeVariable<?>[] typeParams = ((Class<?>) ancestor).getTypeParameters();
            result = new Type[typeParams.length];
            System.arraycopy(typeParams, 0, result, 0, typeParams.length);
        }
        return result;
    }

    public static Type getGenericType(@NotNull Class<?> ancestor, @NotNull Type child) {
        Class<?> rawType;
        if (child instanceof ParameterizedType) {
            rawType = (Class<?>) ((ParameterizedType) child).getRawType();
        } else rawType = (Class<?>) child;

        if (ancestor.equals(rawType)) return child;

        Type result;
        if (ancestor.isInterface()) {
            for (Type parent : rawType.getGenericInterfaces()) {
                if (parent != null && !parent.equals(Object.class)) {
                    if (child instanceof ParameterizedType && parent instanceof ParameterizedType) {
                        TypeVariable[] variables = rawType.getTypeParameters();
                        Type[] arguments = ((ParameterizedType) child).getActualTypeArguments();
                        if (variables.length == arguments.length) {
                            HashMap<TypeVariable, Type> map = new HashMap<>();
                            for (int i = 0; i < variables.length; i++) {
                                map.put(variables[i], arguments[i]);
                            }
                            parent = fillParameter((ParameterizedType) parent, map);
                        }
                    }
                    result = getGenericType(ancestor, parent);
                    if (result != null) return result;
                }
            }
        }

        Type superClass = rawType.getGenericSuperclass();
        if (superClass != null && !superClass.equals(Object.class))
            if ((result = getGenericType(ancestor, superClass)) != null)
                return result;

        return null;
    }

    private static ParameterizedType fillParameter(@NotNull ParameterizedType type, @NotNull Map<TypeVariable, Type> map) {
        Class<?> rawClass = (Class<?>) type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        boolean changed = false;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof TypeVariable) {
                TypeVariable variable = (TypeVariable) arguments[i];
                arguments[i] = map.getOrDefault(variable, variable);
                changed |= arguments[i] != variable;
            } else if (arguments[i] instanceof ParameterizedType) {
                arguments[i] = fillParameter((ParameterizedType) arguments[i], map);
            }
        }
        if (changed) {
            return ParameterizedTypeImpl.make(rawClass, arguments, type.getOwnerType());
        } else return type;
    }
}
