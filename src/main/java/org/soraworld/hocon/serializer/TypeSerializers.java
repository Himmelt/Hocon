package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.util.Reflects;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 序列化器集合类.
 */
public final class TypeSerializers {

    private final CopyOnWriteArrayList<TypeSerializer> serializers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Type, TypeSerializer> typeMatches = new ConcurrentHashMap<>();
    /**
     * 默认顶级序列化器集合，不可修改.<br>
     * 包含 {@link Number},{@link String},{@link Boolean},{@link Map},{@link Enum}<br>
     * {@link java.util.Collection},{@link org.soraworld.hocon.node.Node},{@link java.io.Serializable}
     */
    public static final Map<Type, TypeSerializer> DEFAULT_SERIALIZERS;

    static {
        // 默认顶级序列化器
        Map<Type, TypeSerializer> map = new LinkedHashMap<>();
        TypeSerializer serializer;
        try {
            serializer = new NumberSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new StringSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new BooleanSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new MapSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new ListSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new EnumSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new NodeSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        try {
            serializer = new SerializableSerializer();
            map.put(serializer.getType(), serializer);
        } catch (SerializerException e) {
            e.printStackTrace();
        }
        DEFAULT_SERIALIZERS = Collections.unmodifiableMap(map);
    }

    /**
     * 获取类型对应的序列化器.
     *
     * @param type 类型
     * @return 序列化器 type serializer
     */
    public TypeSerializer get(@NotNull Type type) {
        if (type instanceof Class<?>) type = Reflects.wrap((Class<?>) type);
        TypeSerializer serializer = typeMatches.computeIfAbsent(type, actual -> {
            TreeSet<TypeSerializer> set = new TreeSet<>();
            for (TypeSerializer serial : serializers) {
                if (Reflects.isAssignableFrom(serial.getType(), actual)) set.add(serial);
            }
            return set.isEmpty() ? null : set.first();
        });
        if (serializer == null) {
            // 默认序列化器集合里全是顶级序列化器，不存在 "以下序上" 的情况
            for (TypeSerializer serial : DEFAULT_SERIALIZERS.values()) {
                if (Reflects.isAssignableFrom(serial.getType(), type)) {
                    serializer = serial;
                    break;
                }
            }
            if (serializer != null) typeMatches.put(type, serializer);
        }
        return serializer;
    }

    /**
     * 注册序列化器.<br>
     * 允许注册已存在的 父/子 序列化器，但会输出提示.<br>
     * 不允许注册已存在的 同类 序列化器.
     *
     * @param serializer 序列化器
     * @throws SerializerException 序列化异常
     */
    public void registerType(@NotNull TypeSerializer serializer) throws SerializerException {
        Type type = serializer.getType();
        TypeSerializer serial = get(type);
        if (serial != null) {
            if (DEFAULT_SERIALIZERS.containsKey(type)) {
                throw new SerializerException("Top Serializer of " + type.getTypeName() + " CAN NOT be overridden !!");
            } else if (type.equals(serial.getType())) {
                throw new SerializerException("Serializer of the same type " + type.getTypeName() + " is already exist !!");
            }
            if (!serial.getType().equals(Serializable.class)) {
                System.out.println("WARNING Serializer of " + type.getTypeName() + " has been registered with related type " + serial.getType().getTypeName());
            }
        }
        serializers.add(serializer);
        typeMatches.clear();
    }
}
