package org.soraworld.hocon.reflect;

import org.soraworld.hocon.exception.NonRawTypeException;
import org.soraworld.hocon.exception.NotParamListException;
import org.soraworld.hocon.exception.NotParamMapException;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 反射工具.
 */
public final class Reflects {

    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> CLAZZ_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, ConcurrentHashMap<String, Enum<?>>> ENUM_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;

    static {
        Map<Class<?>, Class<?>> types = new HashMap<>();
        types.put(boolean.class, Boolean.class);
        types.put(byte.class, Byte.class);
        types.put(char.class, Character.class);
        types.put(double.class, Double.class);
        types.put(float.class, Float.class);
        types.put(int.class, Integer.class);
        types.put(long.class, Long.class);
        types.put(short.class, Short.class);
        types.put(void.class, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(types);
    }

    /**
     * 获取类的非静态字段.
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
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
    public static <T extends Enum<T>> T getEnum(Class<T> clazz, String name) {
        if (clazz == null) return null;
        ConcurrentHashMap<String, Enum<?>> fields = ENUM_FIELDS.get(clazz);
        if (fields == null) {
            fields = new ConcurrentHashMap<>();
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
     * 是否可以赋值.<br>
     * 判断继承关系, 是否超类型.
     *
     * @param parent 超类型
     * @param child  子类型
     * @return 是否超类型
     */
    // TODO 封装类型和原生类型的关系 ???
    public static boolean isAssignableFrom(Type parent, Type child) {
        if (parent.equals(child)) return true;
        if (parent instanceof Class) {
            if (child instanceof Class) return ((Class<?>) parent).isAssignableFrom((Class<?>) child);
            return child instanceof ParameterizedType && ((Class<?>) parent).isAssignableFrom((Class<?>) ((ParameterizedType) child).getRawType());
        }
        if (parent instanceof ParameterizedType) {
            Class<?> upperRaw = (Class) ((ParameterizedType) parent).getRawType();
            Type[] upperTypes = ((ParameterizedType) parent).getActualTypeArguments();
            if (child instanceof ParameterizedType) {
                Class<?> lowerRaw = (Class) ((ParameterizedType) child).getRawType();
                if (upperRaw.isAssignableFrom(lowerRaw)) {
                    Type[] lowerTypes = ((ParameterizedType) child).getActualTypeArguments();
                    if (upperTypes.length == lowerTypes.length) {
                        if (upperTypes.length > 0) {
                            for (int i = 0; i < upperTypes.length; i++) {
                                if (!isAssignableFrom(upperTypes[i], lowerTypes[i])) return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
            if (child instanceof Class && upperRaw.isAssignableFrom((Class<?>) child)) {
                if (upperTypes.length > 0) {
                    for (Type upperType : upperTypes) {
                        if (!isAssignableFrom(upperType, Enum.class.isAssignableFrom((Class) child) ? child : Object.class))
                            return false;
                    }
                }
                return true;
            }
            return false;
        }
        if (parent instanceof WildcardType || parent instanceof TypeVariable) {
            return Bounds.getBounds(parent).isSuperOf(Bounds.getBounds(child));
        }
        return false;
    }

    public static ArrayList<Type> getClassTree(ParameterizedType parent, Class<?> child) {
        ArrayList<Type> tree = new ArrayList<>();

        return tree;
    }

    public static <P, C extends P> Type getParentType(Class<P> ancestor, Class<C> child) {
        Type sup = child.getGenericSuperclass();
        if (sup instanceof Class<?> && ancestor.isAssignableFrom((Class<?>) sup)
                || sup instanceof ParameterizedType && ancestor.isAssignableFrom((Class<?>) ((ParameterizedType) sup).getRawType())) {
            return sup;
        }
        Type[] sups = child.getGenericInterfaces();
        for (int i = 0; sups != null && i < sups.length; i++) {
            sup = sups[i];
            if (sup instanceof Class<?> && ancestor.isAssignableFrom((Class<?>) sup)
                    || sup instanceof ParameterizedType && ancestor.isAssignableFrom((Class<?>) ((ParameterizedType) sup).getRawType())) {
                return sup;
            }
        }
        return null;
    }

    public static <P, C extends P> ArrayList<Type> getClassTree(Class<P> parent, Class<C> child) {
        ArrayList<Type> tree = new ArrayList<>();
        Type sup = child.getGenericSuperclass();
        if (sup instanceof Class<?> && parent.isAssignableFrom((Class<?>) sup)) {

        } else if (sup instanceof ParameterizedType && parent.isAssignableFrom((Class<?>) ((ParameterizedType) sup).getRawType())) {

        }
        return tree;
    }

    public static Type[] getRawParamType(Type origin, Type instance) {
        return null;
    }

    public static Type getRawParamTypeByName(Type origin, Type instance, String name) {
        return null;
    }

    public static Class<?> wrap(Class<?> clazz) {
        return clazz.isPrimitive() ? PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }
}
