package org.soraworld.hocon.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 反射相关工具.
 */
public final class Reflects {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE;
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> CLAZZ_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> STATIC_FIELDS = new ConcurrentHashMap<>();
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

    private Reflects() {
    }

    @NotNull
    public static Class<?> wrap(@NotNull Class<?> clazz) {
        return clazz.isPrimitive() ? PRIMITIVE_WRAPPER.get(clazz) : clazz;
    }

    @NotNull
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
     * 获取类的静态字段.
     *
     * @param clazz 类
     * @return 字段列表
     */
    @NotNull
    public static List<Field> getStaticFields(@NotNull Class<?> clazz) {
        if (STATIC_FIELDS.containsKey(clazz)) return STATIC_FIELDS.get(clazz);
        CopyOnWriteArrayList<Field> fields = new CopyOnWriteArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> !Modifier.isStatic(field.getModifiers()));
        fields.forEach(field -> field.setAccessible(true));
        STATIC_FIELDS.put(clazz, fields);
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
            if (enums != null) for (Enum field : enums) fields.put(field.name(), field);
            ENUM_FIELDS.put(clazz, fields);
        }
        return (T) fields.get(name);
    }

    public static boolean isAssignableFrom(@NotNull Type upper, @NotNull Type lower) {
        if (upper instanceof Class<?>) {
            if (lower instanceof Class<?>) {
                return isAssignableFrom((Class<?>) upper, (Class<?>) lower);
            } else if (lower instanceof ParameterizedType) {
                Class<?> rawType = (Class<?>) ((ParameterizedType) lower).getRawType();
                return isAssignableFrom((Class<?>) upper, rawType);
            }
            return false;
        }
        if (upper instanceof ParameterizedType) {
            Class<?> upperRaw = (Class<?>) ((ParameterizedType) upper).getRawType();
            ParameterizedType paramType = getGenericType(upperRaw, lower);
            if (paramType != null) return isAssignableFrom((ParameterizedType) upper, paramType);
            return false;
        }
        if (upper instanceof WildcardType || upper instanceof TypeVariable) {
            return Bounds.getBounds(upper).isSuperOf(Bounds.getBounds(lower));
        }
        return false;
    }

    public static boolean isAssignableFrom(@NotNull Class<?> upper, @NotNull Class<?> lower) {
        if (upper.isAssignableFrom(lower)) return true;
        if (upper.isPrimitive()) return upper == unwrap(lower);
        else return upper.isAssignableFrom(wrap(lower));
    }

    public static boolean isAssignableFrom(@NotNull ParameterizedType upper, @NotNull ParameterizedType lower) {
        if (isAssignableFrom((Class<?>) upper.getRawType(), (Class<?>) lower.getRawType())) {
            Type[] upArgs = upper.getActualTypeArguments();
            Type[] lowArgs = lower.getActualTypeArguments();
            if (upArgs.length == lowArgs.length) {
                for (int i = 0; i < upArgs.length; i++) {
                    if (!isAssignableFrom(upArgs[i], lowArgs[i])) return false;
                }
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Type[] getActualTypes(@NotNull Class<?> topClass, @NotNull Type actualType) {
        ParameterizedType type = getGenericType(topClass, actualType);
        return type == null ? null : type.getActualTypeArguments();
    }

    @Nullable
    public static ParameterizedType getGenericType(@NotNull Class<?> ancestor, @NotNull Type child) {
        Class<?> rawType;
        ParameterizedType paramType;
        if (child instanceof Class<?>) {
            rawType = (Class<?>) child;
            paramType = null;
        } else if (child instanceof ParameterizedType) {
            rawType = (Class<?>) ((ParameterizedType) child).getRawType();
            paramType = (ParameterizedType) child;
        } else return null;
        if (ancestor.equals(rawType)) return paramType;
        ParameterizedType result = null;
        if (ancestor.isInterface()) {
            for (Type parent : rawType.getGenericInterfaces()) {
                if (parent instanceof ParameterizedType) {
                    if (isAssignableFrom(ancestor, (Class<?>) ((ParameterizedType) parent).getRawType())) {
                        if (paramType != null) {
                            TypeVariable[] variables = rawType.getTypeParameters();
                            Type[] arguments = paramType.getActualTypeArguments();
                            if (variables.length == arguments.length) {
                                HashMap<TypeVariable, Type> map = new HashMap<>();
                                for (int i = 0; i < variables.length; i++) {
                                    map.put(variables[i], arguments[i]);
                                }
                                result = getGenericType(ancestor, fillParameter((ParameterizedType) parent, map));
                            }
                        } else result = getGenericType(ancestor, parent);
                    }
                } else if (parent instanceof Class<?> && isAssignableFrom(ancestor, (Class<?>) parent)) {
                    result = getGenericType(ancestor, parent);
                }
                if (result != null) return result;
            }
        }
        Type parent = rawType.getGenericSuperclass();
        if (parent != null && !parent.equals(Object.class)) {
            if (parent instanceof ParameterizedType) {
                if (isAssignableFrom(ancestor, (Class<?>) ((ParameterizedType) parent).getRawType())) {
                    if (paramType != null) {
                        TypeVariable[] variables = rawType.getTypeParameters();
                        Type[] arguments = paramType.getActualTypeArguments();
                        if (variables.length == arguments.length) {
                            HashMap<TypeVariable, Type> map = new HashMap<>();
                            for (int i = 0; i < variables.length; i++) {
                                map.put(variables[i], arguments[i]);
                            }
                            result = getGenericType(ancestor, fillParameter((ParameterizedType) parent, map));
                        }
                    } else result = getGenericType(ancestor, parent);
                }
            } else if (parent instanceof Class<?> && isAssignableFrom(ancestor, (Class<?>) parent)) {
                result = getGenericType(ancestor, parent);
            }
        }
        return result;
    }

    @NotNull
    private static ParameterizedType fillParameter(@NotNull ParameterizedType type, @NotNull Map<TypeVariable, Type> map) {
        Class<?> rawClass = (Class<?>) type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof TypeVariable) {
                TypeVariable variable = (TypeVariable) arguments[i];
                arguments[i] = map.getOrDefault(variable, variable);
            } else if (arguments[i] instanceof ParameterizedType) {
                arguments[i] = fillParameter((ParameterizedType) arguments[i], map);
            }
        }
        return new ParameterizedTypeImpl(rawClass, arguments, type.getOwnerType());
    }
}
