package org.soraworld.hocon.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 反射相关工具.
 *
 * @author Himmelt
 */
public final class Reflects {

    private static final Double JAVA_VERSION;
    private static final Map<String, Method> OBJECT_METHODS;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE;
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> CLAZZ_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Field>> STATIC_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, ConcurrentHashMap<String, Enum<?>>> ENUM_FIELDS = new ConcurrentHashMap<>();
    private static boolean RESOLVES_LAMBDAS;
    private static Method GET_CONSTANT_POOL;
    private static Method GET_CONSTANT_POOL_SIZE;
    private static Method GET_CONSTANT_POOL_METHOD_AT;

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
        Map<String, Method> map3 = new HashMap<>();
        for (Method method : Object.class.getDeclaredMethods()) {
            map3.put(method.getName(), method);
        }
        OBJECT_METHODS = Collections.unmodifiableMap(map3);
        JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));
        try {
            GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
            String constantPoolName = JAVA_VERSION < 9 ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
            Class<?> constantPoolClass = Class.forName(constantPoolName);
            GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize");
            GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", int.class);

            GET_CONSTANT_POOL.setAccessible(true);
            GET_CONSTANT_POOL_SIZE.setAccessible(true);
            GET_CONSTANT_POOL_METHOD_AT.setAccessible(true);

            Object constantPool = GET_CONSTANT_POOL.invoke(Object.class);
            GET_CONSTANT_POOL_SIZE.invoke(constantPool);
            RESOLVES_LAMBDAS = true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        if (CLAZZ_FIELDS.containsKey(clazz)) {
            return CLAZZ_FIELDS.get(clazz);
        }
        CopyOnWriteArrayList<Field> fields = new CopyOnWriteArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        Class<?> supClz = clazz.getSuperclass();
        if (supClz != null && supClz != Object.class) {
            fields.addAll(0, getFields(supClz));
        }
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
        if (STATIC_FIELDS.containsKey(clazz)) {
            return STATIC_FIELDS.get(clazz);
        }
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
            if (enums != null) {
                for (Enum field : enums) {
                    fields.put(field.name(), field);
                }
            }
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
            if (paramType != null) {
                return isAssignableFrom((ParameterizedType) upper, paramType);
            }
            return false;
        }
        if (upper instanceof WildcardType || upper instanceof TypeVariable) {
            return Bounds.getBounds(upper).isSuperOf(Bounds.getBounds(lower));
        }
        return false;
    }

    public static boolean isAssignableFrom(@NotNull Class<?> upper, @NotNull Class<?> lower) {
        if (upper.isAssignableFrom(lower)) {
            return true;
        }
        if (upper.isPrimitive()) {
            return upper == unwrap(lower);
        } else {
            return upper.isAssignableFrom(wrap(lower));
        }
    }

    public static boolean isAssignableFrom(@NotNull ParameterizedType upper, @NotNull ParameterizedType lower) {
        if (isAssignableFrom((Class<?>) upper.getRawType(), (Class<?>) lower.getRawType())) {
            Type[] upArgs = upper.getActualTypeArguments();
            Type[] lowArgs = lower.getActualTypeArguments();
            if (upArgs.length == lowArgs.length) {
                for (int i = 0; i < upArgs.length; i++) {
                    if (!isAssignableFrom(upArgs[i], lowArgs[i])) {
                        return false;
                    }
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
        } else {
            return null;
        }

        if (ancestor.equals(rawType)) {
            return paramType;
        }
        ParameterizedType result = null;

        if (ancestor.isInterface()) {
            for (Type parent : rawType.getGenericInterfaces()) {
                if (parent instanceof ParameterizedType) {
                    if (isAssignableFrom(ancestor, (Class<?>) ((ParameterizedType) parent).getRawType())) {
                        if (paramType != null) {
                            TypeVariable[] variables = rawType.getTypeParameters();
                            Type[] arguments = paramType.getActualTypeArguments();
                            if (variables.length == arguments.length) {
                                HashMap<TypeVariable<?>, Type> map = new HashMap<>();
                                for (int i = 0; i < variables.length; i++) {
                                    map.put(variables[i], arguments[i]);
                                }
                                result = getGenericType(ancestor, fillParameter((ParameterizedType) parent, map));
                            }
                        } else {
                            result = getGenericType(ancestor, parent);
                        }
                    }
                } else if (parent instanceof Class<?> && isAssignableFrom(ancestor, (Class<?>) parent)) {
                    if (RESOLVES_LAMBDAS && rawType.isSynthetic()) {
                        Map<TypeVariable<?>, Type> map = getLambdaArgs(ancestor, rawType);
                        TypeVariable<?>[] variables = ((Class<?>) parent).getTypeParameters();
                        if (variables != null) {
                            ParameterizedType type = new ParameterizedTypeImpl((Class<?>) parent, variables, ((Class<?>) parent).getDeclaringClass());
                            parent = fillParameter(type, map);
                        }
                    }
                    result = getGenericType(ancestor, parent);
                }
                if (result != null) {
                    return result;
                }
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
                            HashMap<TypeVariable<?>, Type> map = new HashMap<>();
                            for (int i = 0; i < variables.length; i++) {
                                map.put(variables[i], arguments[i]);
                            }
                            result = getGenericType(ancestor, fillParameter((ParameterizedType) parent, map));
                        }
                    } else {
                        result = getGenericType(ancestor, parent);
                    }
                }
            } else if (parent instanceof Class<?> && isAssignableFrom(ancestor, (Class<?>) parent)) {
                result = getGenericType(ancestor, parent);
            }
        }
        return result;
    }

    @NotNull
    private static ParameterizedType fillParameter(@NotNull ParameterizedType type, @NotNull Map<TypeVariable<?>, Type> map) {
        Class<?> rawClass = (Class<?>) type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        Type[] params = new Type[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof TypeVariable<?>) {
                TypeVariable<?> variable = (TypeVariable<?>) arguments[i];
                params[i] = map.getOrDefault(variable, variable);
            } else if (arguments[i] instanceof ParameterizedType) {
                params[i] = fillParameter((ParameterizedType) arguments[i], map);
            } else {
                params[i] = arguments[i];
            }
        }
        return new ParameterizedTypeImpl(rawClass, params, type.getOwnerType());
    }

    @NotNull
    private static HashMap<TypeVariable<?>, Type> getLambdaArgs(@NotNull Class<?> ancestor, @NotNull final Class<?> child) {
        HashMap<TypeVariable<?>, Type> map = new HashMap<>();
        if (RESOLVES_LAMBDAS) {
            for (Method m : ancestor.getMethods()) {
                if (!isDefaultMethod(m) && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
                    // Skip methods that override Object.class
                    Method objectMethod = OBJECT_METHODS.get(m.getName());
                    if (objectMethod != null && Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) {
                        continue;
                    }

                    // Get functional interface's type params
                    Type returnTypeVar = m.getGenericReturnType();
                    Type[] paramTypeVars = m.getGenericParameterTypes();

                    Member member = getMemberRef(child);
                    if (member == null) {
                        return map;
                    }

                    // Populate return type argument
                    if (returnTypeVar instanceof TypeVariable) {
                        Class<?> returnType = member instanceof Method ? ((Method) member).getReturnType()
                                : ((Constructor<?>) member).getDeclaringClass();
                        returnType = wrap(returnType);
                        if (!returnType.equals(Void.class)) {
                            map.put((TypeVariable<?>) returnTypeVar, returnType);
                        }
                    }

                    Class<?>[] arguments = member instanceof Method ? ((Method) member).getParameterTypes()
                            : ((Constructor<?>) member).getParameterTypes();

                    // Populate object type from arbitrary object method reference
                    int paramOffset = 0;
                    if (paramTypeVars.length > 0 && paramTypeVars[0] instanceof TypeVariable
                            && paramTypeVars.length == arguments.length + 1) {
                        Class<?> instanceType = member.getDeclaringClass();
                        map.put((TypeVariable<?>) paramTypeVars[0], instanceType);
                        paramOffset = 1;
                    }

                    // Handle additional arguments that are captured from the lambda's enclosing scope
                    int argOffset = 0;
                    if (paramTypeVars.length < arguments.length) {
                        argOffset = arguments.length - paramTypeVars.length;
                    }

                    // Populate type arguments
                    for (int i = 0; i + argOffset < arguments.length; i++) {
                        if (paramTypeVars[i] instanceof TypeVariable) {
                            map.put((TypeVariable<?>) paramTypeVars[i + paramOffset], wrap(arguments[i + argOffset]));
                        }
                    }
                    return map;
                }
            }
        }
        return map;
    }

    @Nullable
    private static Member getMemberRef(@NotNull Class<?> type) {
        Object constantPool;
        try {
            constantPool = GET_CONSTANT_POOL.invoke(type);
        } catch (Exception ignore) {
            return null;
        }

        Member result = null;
        for (int i = getConstantPoolSize(constantPool) - 1; i >= 0; i--) {
            Member member = getConstantPoolMethodAt(constantPool, i);
            // Skip SerializedLambda constructors and members of the "type" class
            if (member == null
                    || (member instanceof Constructor
                    && member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda"))
                    || member.getDeclaringClass().isAssignableFrom(type)) {
                continue;
            }

            result = member;

            // Return if not valueOf method
            if (!(member instanceof Method) || !isAutoBoxingMethod((Method) member)) {
                break;
            }
        }

        return result;
    }

    private static boolean isAutoBoxingMethod(@NotNull Method method) {
        Class<?>[] params = method.getParameterTypes();
        return method.getName().equals("valueOf") && params.length == 1
                && params[0].isPrimitive() && wrap(params[0]).equals(method.getDeclaringClass());
    }

    private static int getConstantPoolSize(@NotNull Object constantPool) {
        try {
            return (Integer) GET_CONSTANT_POOL_SIZE.invoke(constantPool);
        } catch (Exception ignore) {
            return 0;
        }
    }

    @Nullable
    private static Member getConstantPoolMethodAt(@NotNull Object constantPool, int i) {
        try {
            return (Member) GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, i);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static boolean isDefaultMethod(@NotNull Method method) {
        return JAVA_VERSION >= 1.8 && method.isDefault();
    }
}
