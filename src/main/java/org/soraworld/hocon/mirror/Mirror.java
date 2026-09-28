package org.soraworld.hocon.mirror;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射隔离层：整个库中<strong>唯一</strong>允许反射的地方.
 *
 * <p>能力边界（刻意的，写在这里而不是散落各处）：</p>
 * <ul>
 *   <li>只使用公开契约：{@code Class}/{@code Field}/{@code Modifier}。不使用 {@code Field.modifiers}、
 *       常量池、lambda 类型解析等 JDK 内部手段——那些在 JDK 16+ 已被默认拒绝，且在 JDK 8 / 17 / 21 上行为不一致；</li>
 *   <li>因此 <strong>{@code final} 字段（含 {@code static final}）只读</strong>。这是 JVM 的既定行为，不是本库的缺陷；
 *       这类字段会被标记为 {@link FieldInfo#writable()} 为 {@code false}，由上层记录诊断而不是静默失败；</li>
 *   <li>字段打不开访问权限时跳过，绝不向控制台打印堆栈.</li>
 * </ul>
 *
 * @author Himmelt
 */
public final class Mirror {

    private static final ConcurrentHashMap<Class<?>, List<FieldInfo>> INSTANCE_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, List<FieldInfo>> STATIC_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, Map<String, Enum<?>>> ENUM_VALUES = new ConcurrentHashMap<>();

    private Mirror() {
    }

    /**
     * 字段信息.
     */
    public static final class FieldInfo {

        private final Field field;
        private final String name;
        private final boolean writable;

        private FieldInfo(Field field, String name, boolean writable) {
            this.field = field;
            this.name = name;
            this.writable = writable;
        }

        /**
         * 获取底层字段.
         *
         * @return 字段
         */
        public Field field() {
            return field;
        }

        /**
         * 获取字段名.
         *
         * @return 字段名
         */
        public String name() {
            return name;
        }

        /**
         * 获取字段的泛型类型.
         *
         * @return 泛型类型
         */
        public Type type() {
            return field.getGenericType();
        }

        /**
         * 是否可写.
         * {@code final} 字段或访问权限受限的字段为 {@code false}.
         *
         * @return 可写返回 true
         */
        public boolean writable() {
            return writable;
        }

        /**
         * 读取字段值.
         *
         * @param target 目标对象，静态字段可传 null
         * @return 字段值
         * @throws IllegalAccessException 访问异常
         */
        public Object get(Object target) throws IllegalAccessException {
            return field.get(target);
        }

        /**
         * 写入字段值.
         *
         * @param target 目标对象，静态字段可传 null
         * @param value  值
         * @throws IllegalAccessException 访问异常
         */
        public void set(Object target, Object value) throws IllegalAccessException {
            field.set(target, value);
        }

        @Override
        public String toString() {
            return field.getDeclaringClass().getSimpleName() + "." + name;
        }
    }

    /**
     * 获取本类及父类的非静态字段；父类字段排在前面.
     *
     * @param type 类型
     * @return 字段列表（不可变）
     */
    public static List<FieldInfo> instanceFields(Class<?> type) {
        List<FieldInfo> cached = INSTANCE_FIELDS.get(type);
        if (cached != null) {
            return cached;
        }
        List<FieldInfo> fields = new ArrayList<>();
        Class<?> superType = type.getSuperclass();
        if (superType != null && superType != Object.class) {
            fields.addAll(instanceFields(superType));
        }
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || field.isSynthetic()) {
                continue;
            }
            fields.add(scan(field));
        }
        List<FieldInfo> result = Collections.unmodifiableList(fields);
        INSTANCE_FIELDS.put(type, result);
        return result;
    }

    /**
     * 获取本类的静态字段.
     *
     * @param type 类型
     * @return 字段列表（不可变）
     */
    public static List<FieldInfo> staticFields(Class<?> type) {
        List<FieldInfo> cached = STATIC_FIELDS.get(type);
        if (cached != null) {
            return cached;
        }
        List<FieldInfo> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            fields.add(scan(field));
        }
        List<FieldInfo> result = Collections.unmodifiableList(fields);
        STATIC_FIELDS.put(type, result);
        return result;
    }

    private static FieldInfo scan(Field field) {
        boolean accessible = false;
        try {
            field.setAccessible(true);
            accessible = true;
        } catch (RuntimeException ignored) {
            // 模块限制或安全管理器拒绝，标记为不可写，由上层记录诊断
        }
        boolean writable = accessible && !Modifier.isFinal(field.getModifiers());
        return new FieldInfo(field, field.getName(), writable);
    }

    /**
     * 按名字取枚举值.
     *
     * @param <T>  枚举类型
     * @param type 枚举类
     * @param name 名字
     * @return 枚举值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T enumByName(Class<T> type, String name) {
        Map<String, Enum<?>> values = ENUM_VALUES.get(type);
        if (values == null) {
            Map<String, Enum<?>> created = new HashMap<>();
            T[] constants = type.getEnumConstants();
            if (constants != null) {
                for (T constant : constants) {
                    created.put(constant.name(), constant);
                }
            }
            ENUM_VALUES.put(type, created);
            values = created;
        }
        return (T) values.get(name);
    }

    /**
     * 按名字取枚举值（不要求调用方给出带泛型的类型）.
     *
     * @param type 枚举类
     * @param name 名字
     * @return 枚举值，不存在返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Enum<?> enumOf(Class<?> type, String name) {
        return enumByName((Class) type, name);
    }

    /**
     * 构造一个实例（要求有无参构造器，允许非 public）.
     *
     * @param type 类型
     * @return 实例
     * @throws ReflectiveOperationException 构造失败
     */
    public static Object newInstance(Class<?> type) throws ReflectiveOperationException {
        java.lang.reflect.Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * 求 {@code child} 相对祖先 {@code ancestor} 的泛型实参.
     *
     * @param ancestor 祖先类型，例如 {@code java.util.Collection}
     * @param child    子类型，例如 {@code ArrayList&lt;String&gt;}
     * @return 实参数组，无法确定返回 null
     */
    public static Type[] actualTypes(Class<?> ancestor, Type child) {
        ParameterizedType type = genericType(ancestor, child);
        return type == null ? null : type.getActualTypeArguments();
    }

    /**
     * 求 {@code child} 相对祖先 {@code ancestor} 的实参化类型.
     *
     * @param ancestor 祖先类型
     * @param child    子类型
     * @return 实参化类型，无法确定返回 null
     */
    public static ParameterizedType genericType(Class<?> ancestor, Type child) {
        Class<?> raw;
        ParameterizedType declared;
        if (child instanceof Class<?>) {
            raw = (Class<?>) child;
            declared = null;
        } else if (child instanceof ParameterizedType) {
            raw = (Class<?>) ((ParameterizedType) child).getRawType();
            declared = (ParameterizedType) child;
        } else {
            return null;
        }
        if (ancestor.equals(raw)) {
            return declared;
        }
        ParameterizedType result = null;
        for (Type parent : raw.getGenericInterfaces()) {
            Class<?> parentRaw = rawOf(parent);
            if (parentRaw == null || !ancestor.isAssignableFrom(parentRaw)) {
                continue;
            }
            result = genericType(ancestor, parent instanceof ParameterizedType
                    ? substitute((ParameterizedType) parent, raw, declared) : parent);
            if (result != null) {
                return result;
            }
        }
        Type parent = raw.getGenericSuperclass();
        Class<?> parentRaw = rawOf(parent);
        if (parentRaw != null && ancestor.isAssignableFrom(parentRaw)) {
            result = genericType(ancestor, parent instanceof ParameterizedType
                    ? substitute((ParameterizedType) parent, raw, declared) : parent);
        }
        return result;
    }

    private static Class<?> rawOf(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return raw instanceof Class<?> ? (Class<?>) raw : null;
        }
        return null;
    }

    /**
     * 用子类型实参替换父类型声明里的类型变量.
     */
    private static ParameterizedType substitute(ParameterizedType type, Class<?> raw, ParameterizedType actual) {
        if (actual == null) {
            return type;
        }
        TypeVariable<?>[] variables = raw.getTypeParameters();
        Type[] arguments = actual.getActualTypeArguments();
        if (variables.length != arguments.length) {
            return type;
        }
        Map<TypeVariable<?>, Type> bindings = new HashMap<>();
        for (int i = 0; i < variables.length; i++) {
            bindings.put(variables[i], arguments[i]);
        }
        return fill(type, bindings);
    }

    private static ParameterizedType fill(ParameterizedType type, Map<TypeVariable<?>, Type> bindings) {
        Class<?> raw = (Class<?>) type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        Type[] resolved = new Type[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            resolved[i] = resolve(arguments[i], bindings);
        }
        return new ParameterizedTypeImpl(raw, resolved, type.getOwnerType());
    }

    private static Type resolve(Type type, Map<TypeVariable<?>, Type> bindings) {
        if (type instanceof TypeVariable<?>) {
            return bindings.getOrDefault(type, type);
        }
        if (type instanceof ParameterizedType) {
            return fill((ParameterizedType) type, bindings);
        }
        return type;
    }
}
