package org.soraworld.hocon.reflect;

import org.soraworld.hocon.exception.NonRawTypeException;
import org.soraworld.hocon.exception.NotParamListException;
import org.soraworld.hocon.exception.NotParamMapException;

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具.
 */
public final class Reflects {

    private static final ConcurrentHashMap<Class<?>, List<Field>> CLAZZ_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, HashMap<String, Enum<?>>> ENUM_FIELDS = new ConcurrentHashMap<>();

    /**
     * 获取类的非静态字段.
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
        if (CLAZZ_FIELDS.containsKey(clazz)) return CLAZZ_FIELDS.get(clazz);
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
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
    public static <T extends Enum<T>> T getEnum(Class<T> clazz, String name) {
        if (clazz == null) return null;
        HashMap<String, Enum<?>> fields = ENUM_FIELDS.get(clazz);
        if (fields == null) {
            fields = new HashMap<>();
            Enum[] vals = clazz.getEnumConstants();
            if (vals != null) {
                for (Enum field : vals) {
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

    /**
     * 是否是超类型.
     *
     * @param upper 预计超类型
     * @param lower 预计子类型
     * @return 是否超类型
     */
    public static boolean isSuperOf(@Nonnull Type upper, @Nonnull Type lower) {
        if (upper.equals(lower)) return true;
        if (upper instanceof Class) {
            if (lower instanceof Class) return ((Class<?>) upper).isAssignableFrom((Class<?>) lower);
            return lower instanceof ParameterizedType && ((Class<?>) upper).isAssignableFrom((Class<?>) ((ParameterizedType) lower).getRawType());
        }
        if (upper instanceof ParameterizedType) {
            Class<?> upperRaw = (Class) ((ParameterizedType) upper).getRawType();
            Type[] upperTypes = ((ParameterizedType) upper).getActualTypeArguments();
            if (lower instanceof ParameterizedType) {
                Class<?> lowerRaw = (Class) ((ParameterizedType) lower).getRawType();
                if (upperRaw.isAssignableFrom(lowerRaw)) {
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
            if (lower instanceof Class && upperRaw.isAssignableFrom((Class<?>) lower)) {
                if (upperTypes.length > 0) {
                    for (Type upperType : upperTypes) {
                        if (!isSuperOf(upperType, Enum.class.isAssignableFrom((Class) lower) ? lower : Object.class)) return false;
                    }
                }
                return true;
            }
            return false;
        }
        if (upper instanceof WildcardType || upper instanceof TypeVariable) {
            return Bounds.getBounds(upper).isSuperOf(Bounds.getBounds(lower));
        }
        return false;
    }
}
