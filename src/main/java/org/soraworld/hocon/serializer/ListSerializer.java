package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeList;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * 集合类型序列化器.
 * 目前支持 {@link java.util.List} {@link java.util.Set} {@link java.util.Queue}
 */
public final class ListSerializer extends TypeSerializer<Collection<?>, NodeList> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    public ListSerializer() throws SerializerException {
    }

    @NotNull
    public Collection<?> deserialize(@NotNull Type type, @NotNull NodeList node) throws HoconException {
        if (type instanceof ParameterizedType) {
            try {
                Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                Type paramType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer KEY = node.options().getSerializer(paramType);
                Collection<Object> collection;
                if (rawType.isAssignableFrom(ArrayList.class)) collection = new ArrayList<>();
                else if (rawType.isAssignableFrom(HashSet.class)) collection = new HashSet<>();
                else collection = new LinkedList<>();
                int size = node.size();
                for (int i = 0; i < size; i++) {
                    collection.add(KEY.deserialize(paramType, node.get(i)));
                }
                return collection;
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        } else throw new NotMatchException(getType(), type);
    }

    @NotNull
    public NodeList serialize(@NotNull Type type, @NotNull Collection<?> value, @NotNull Options options) throws HoconException {
        if (value.isEmpty()) return new NodeList(options);
        if (type instanceof ParameterizedType) {
            try {
                Type keyType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer KEY = options.getSerializer(keyType);
                NodeList node = new NodeList(options);
                for (Object obj : value) node.add(KEY.serialize(keyType, obj, options));
                return node;
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        } else throw new NotMatchException(getType(), type);
    }
}
