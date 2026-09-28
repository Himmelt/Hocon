package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.mirror.Mirror;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * 类型适配器注册表.
 *
 * <p>解析规则（<strong>不依赖注册顺序</strong>）：</p>
 * <ol>
 *   <li>精确类型匹配；</li>
 *   <li>沿继承链就近匹配（{@code LinkedHashSet} 命中 {@code Set} 而不是 {@code Collection}）；</li>
 *   <li>标了 {@link Setting} 字段的普通类 → 对象适配器；</li>
 *   <li>可选兜底：{@code Serializable}（默认<strong>关闭</strong>，因为"什么都能塞进去"意味着
 *       "什么都静默变成空对象"）；</li>
 *   <li>仍无匹配 → 返回 null，由调用方给出"字段 X 的类型 Y 没有适配器"的明确异常，而不是 NPE.</li>
 * </ol>
 *
 * @author Himmelt
 */
public final class Adapters {

    private static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<>();

    static {
        WRAPPERS.put(boolean.class, Boolean.class);
        WRAPPERS.put(byte.class, Byte.class);
        WRAPPERS.put(char.class, Character.class);
        WRAPPERS.put(short.class, Short.class);
        WRAPPERS.put(int.class, Integer.class);
        WRAPPERS.put(long.class, Long.class);
        WRAPPERS.put(float.class, Float.class);
        WRAPPERS.put(double.class, Double.class);
        WRAPPERS.put(void.class, Void.class);
    }

    private static final class Entry {

        private final Type type;
        private final TypeAdapter<?> adapter;

        private Entry(Type type, TypeAdapter<?> adapter) {
            this.type = type;
            this.adapter = adapter;
        }
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();
    private final Map<Type, Optional<TypeAdapter<?>>> cache = new ConcurrentHashMap<>();
    private final Function<String, String> commentTranslator;
    private final boolean serializableFallback;

    /**
     * 实例化注册表，并注册全部内置适配器.
     */
    public Adapters() {
        this(null, false);
    }

    /**
     * 实例化注册表，并注册全部内置适配器.
     *
     * @param commentTranslator   注释翻译器（本地化），可为 null
     * @param serializableFallback 是否启用 {@code Serializable} 兜底
     */
    public Adapters(Function<String, String> commentTranslator, boolean serializableFallback) {
        this.commentTranslator = commentTranslator;
        this.serializableFallback = serializableFallback;
        register(String.class, Builtins.STRING);
        register(Character.class, Builtins.CHARACTER);
        register(Boolean.class, Builtins.BOOLEAN);
        register(Number.class, Builtins.NUMBER);
        register(Enum.class, Builtins.ENUM);
        register(Map.class, Builtins.MAP);
        register(Collection.class, Builtins.COLLECTION);
        register(DocNode.class, Builtins.NODE);
    }

    /**
     * 获取注释翻译器.
     *
     * @return 翻译器，可能为 null
     */
    public Function<String, String> commentTranslator() {
        return commentTranslator;
    }

    /**
     * 是否启用 {@code Serializable} 兜底.
     *
     * @return 启用返回 true
     */
    public boolean serializableFallback() {
        return serializableFallback;
    }

    /**
     * 注册适配器，类型由泛型参数推断.
     *
     * @param adapter 适配器
     * @return 本注册表
     */
    public Adapters register(TypeAdapter<?> adapter) {
        if (adapter == null) {
            return this;
        }
        ParameterizedType type = Mirror.genericType(TypeAdapter.class, adapter.getClass());
        if (type == null) {
            throw new IllegalArgumentException(adapter.getClass().getName()
                    + " 必须通过 TypeAdapter<X> 明确指定目标类型，请改用 register(Type, TypeAdapter)");
        }
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length != 1) {
            throw new IllegalArgumentException(adapter.getClass().getName() + " 的 TypeAdapter 泛型参数个数不是 1");
        }
        return register(arguments[0], adapter);
    }

    /**
     * 注册适配器.
     *
     * @param type    目标类型
     * @param adapter 适配器
     * @return 本注册表
     */
    public Adapters register(Type type, TypeAdapter<?> adapter) {
        if (type == null || adapter == null) {
            return this;
        }
        entries.add(new Entry(type, adapter));
        cache.clear();
        return this;
    }

    /**
     * 获取类型对应的适配器.
     *
     * @param type 类型
     * @return 适配器，没有匹配时返回 null
     */
    public TypeAdapter<?> resolve(Type type) {
        if (type == null) {
            return null;
        }
        Type key = normalize(type);
        Optional<TypeAdapter<?>> cached = cache.get(key);
        if (cached != null) {
            return cached.orElse(null);
        }
        TypeAdapter<?> selected = select(key);
        cache.put(key, Optional.ofNullable(selected));
        return selected;
    }

    /**
     * 是否已有该类型的适配器.
     *
     * @param type 类型
     * @return 有返回 true
     */
    public boolean supports(Type type) {
        return resolve(type) != null;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> resolve(Type type, Class<T> expect) {
        return (TypeAdapter<T>) resolve(type);
    }

    private TypeAdapter<?> select(Type type) {
        TypeAdapter<?> best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Entry entry : entries) {
            int distance = distance(type, entry.type);
            if (distance >= 0 && distance < bestDistance) {
                bestDistance = distance;
                best = entry.adapter;
            }
        }
        if (best != null) {
            return best;
        }
        Class<?> raw = rawOf(type);
        if (raw == null) {
            return null;
        }
        if (Schema.annotated(raw)) {
            return Builtins.OBJECT;
        }
        if (serializableFallback && Serializable.class.isAssignableFrom(raw)) {
            return Builtins.OBJECT;
        }
        return null;
    }

    /**
     * 计算类型到注册类型的"就近距离"，越小越具体.
     *
     * @param from 实际类型
     * @param to   注册类型
     * @return 距离，不可匹配返回 -1
     */
    private static int distance(Type from, Type to) {
        if (from.equals(to)) {
            return 0;
        }
        Class<?> fromRaw = rawOf(from);
        Class<?> toRaw = rawOf(to);
        if (fromRaw == null || toRaw == null || !toRaw.isAssignableFrom(fromRaw)) {
            return -1;
        }
        return hops(fromRaw, toRaw);
    }

    /**
     * 沿父类与接口做广度优先，求最少继承跳数.
     */
    private static int hops(Class<?> from, Class<?> to) {
        if (from.equals(to)) {
            return 0;
        }
        Set<Class<?>> seen = new HashSet<>();
        Set<Class<?>> level = new HashSet<>();
        level.add(from);
        seen.add(from);
        int depth = 0;
        while (!level.isEmpty() && depth < 32) {
            depth++;
            Set<Class<?>> next = new HashSet<>();
            for (Class<?> type : level) {
                Class<?> superType = type.getSuperclass();
                if (superType != null) {
                    if (superType.equals(to)) {
                        return depth;
                    }
                    if (seen.add(superType)) {
                        next.add(superType);
                    }
                }
                for (Class<?> itf : type.getInterfaces()) {
                    if (itf.equals(to)) {
                        return depth;
                    }
                    if (seen.add(itf)) {
                        next.add(itf);
                    }
                }
            }
            level = next;
        }
        return -1;
    }

    /**
     * 把基本类型换成对应的包装类型，其余原样返回.
     *
     * @param type 类型
     * @return 包装类型
     */
    public static Class<?> wrap(Class<?> type) {
        if (type == null || !type.isPrimitive()) {
            return type;
        }
        Class<?> wrapper = WRAPPERS.get(type);
        return wrapper == null ? type : wrapper;
    }

    private static Type normalize(Type type) {
        if (type instanceof Class<?>) {
            return wrap((Class<?>) type);
        }
        return type;
    }

    /**
     * 取类型的原始类.
     *
     * @param type 类型
     * @return 原始类，取不到返回 null
     */
    public static Class<?> rawOf(Type type) {
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
     * 取容器元素/键值类型.
     *
     * @param ancestor 祖先接口，例如 {@code Collection}、{@code Map}
     * @param type     实际类型
     * @return 实参数组，取不到返回 null
     */
    public static Type[] actualTypes(Class<?> ancestor, Type type) {
        return Mirror.actualTypes(ancestor, type);
    }

    /**
     * 便捷方法：取某容器类型的第 index 个实参.
     *
     * @param ancestor 祖先接口
     * @param type     实际类型
     * @param index    坐标
     * @return 实参类型，取不到返回 null
     */
    public static Type actualType(Class<?> ancestor, Type type, int index) {
        Type[] types = actualTypes(ancestor, type);
        return types != null && index >= 0 && index < types.length ? types[index] : null;
    }

    /**
     * 便捷方法：把实参数组转成列表用于日志.
     *
     * @param types 实参数组
     * @return 描述文本
     */
    static String describe(Type[] types) {
        if (types == null) {
            return "<none>";
        }
        List<String> names = new ArrayList<>();
        for (Type type : types) {
            names.add(type.getTypeName());
        }
        return names.toString();
    }
}
