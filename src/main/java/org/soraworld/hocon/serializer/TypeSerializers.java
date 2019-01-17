package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 序列化器集合类.
 */
public final class TypeSerializers {

    private final TypeSerializers parent;
    private final CopyOnWriteArrayList<TypeSerializer> serializers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Type, TypeSerializer> typeMatches = new ConcurrentHashMap<>();
    private static final TypeSerializers DEFAULT_SERIALIZERS = new TypeSerializers(null);

    static {
        try {
            DEFAULT_SERIALIZERS.registerType(new NumberSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new StringSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new BooleanSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new MapSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new ListSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new SerializableSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new NodeSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            DEFAULT_SERIALIZERS.registerType(new EnumSerializer());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
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
    public TypeSerializer get(@NotNull Type type) {
        if (type instanceof Class) type = Reflects.wrap((Class<?>) type);
        TypeSerializer serializer = typeMatches.computeIfAbsent(type, typ -> {
            for (TypeSerializer serial : serializers) {
                if (Reflects.isAssignableFrom(serial.getType(), typ)) return serial;
            }
            return null;
        });
        if (serializer == null && parent != null) serializer = parent.get(type);
        return serializer;
    }

    /**
     * 注册序列化器.
     *
     * @param serializer 序列化器
     * @throws SerializerException 序列化异常
     */
    public void registerType(TypeSerializer serializer) throws SerializerException {
        TypeSerializer serial = get(serializer.getType());
        if (serial == null) {
            serializers.add(serializer);
            typeMatches.clear();
        } else {
            String message = "Serializer for type ["
                    + serializer.getType().getTypeName()
                    + "] has been registered with type ["
                    + serial.getType() + "].";
            throw new SerializerException(message);
        }
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
     * 创建新的序列化器集合.
     * 以根集合为父集合
     *
     * @return 序列化器集合
     */
    public static TypeSerializers build() {
        return DEFAULT_SERIALIZERS.newChild();
    }
}
