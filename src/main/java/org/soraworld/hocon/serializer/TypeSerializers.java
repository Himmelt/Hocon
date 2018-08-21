package org.soraworld.hocon.serializer;

import org.soraworld.hocon.reflect.Primitives;
import org.soraworld.hocon.reflect.Reflects;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 序列化器集合类.
 */
public class TypeSerializers {

    private final TypeSerializers parent;
    private final CopyOnWriteArrayList<TypeSerializer<?>> serializers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Type, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();
    private static final TypeSerializers SERIALIZERS = new TypeSerializers(null);

    static {
        SERIALIZERS.registerType(new NumberSerializer());
        SERIALIZERS.registerType(new StringSerializer());
        SERIALIZERS.registerType(new BooleanSerializer());
        SERIALIZERS.registerType(new MapSerializer());
        SERIALIZERS.registerType(new ListSerializer());
        SERIALIZERS.registerType(new NodeSerializer());
        SERIALIZERS.registerType(new EnumSerializer());
    }

    private TypeSerializers(TypeSerializers parent) {
        this.parent = checkCycle(parent) ? parent : null;
    }

    private boolean checkCycle(TypeSerializers another) {
        if (another == null) return true;
        return this != another && checkCycle(another.parent);
    }

    /**
     * 获取类型对应的序列化器.
     *
     * @param type 类型
     * @return 序列化器
     */
    public TypeSerializer get(@Nonnull Type type) {
        if (type instanceof Class) {
            type = Primitives.wrap((Class<?>) type);
        }
        TypeSerializer<?> serializer = typeMatches.computeIfAbsent(type, typ -> {
            for (TypeSerializer<?> serial : serializers) {
                if (Reflects.isSuperOf(serial.getRegType(), typ)) return serial;
            }
            return null;
        });
        if (serializer == null && parent != null) {
            serializer = parent.get(type);
        }
        return serializer;
    }

    /**
     * 注册序列化器.
     *
     * @param <T>        序列化器类型参数
     * @param serializer 序列化器
     */
    public <T> void registerType(@Nonnull TypeSerializer<? super T> serializer) {
        serializers.add(serializer);
        typeMatches.clear();
    }

    /**
     * 创建新的子序列化器集合.
     *
     * @return 子序列化器集合
     */
    public TypeSerializers newChild() {
        return new TypeSerializers(this);
    }

    /**
     * 获取根(默认)序列化器集合.
     *
     * @return 根序列化器集合
     */
    public static TypeSerializers defaults() {
        return SERIALIZERS;
    }

    /**
     * 创建新的序列化器集合.
     * 以根集合为父集合
     *
     * @return 序列化器集合
     */
    public static TypeSerializers build() {
        return SERIALIZERS.newChild();
    }
}
