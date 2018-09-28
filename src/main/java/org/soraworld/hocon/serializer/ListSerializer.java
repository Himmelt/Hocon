package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeList;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

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
public class ListSerializer implements TypeSerializer<Collection<?>> {
    public Collection<?> deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeList) {
            if (type instanceof ParameterizedType) {
                try {
                    Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                    Type paramType = Reflects.getListParameter((ParameterizedType) type);
                    TypeSerializer KEY = node.options().getSerializer(paramType);
                    Collection<Object> collection;
                    if (rawType.isAssignableFrom(ArrayList.class)) collection = new ArrayList<>();
                    else if (rawType.isAssignableFrom(HashSet.class)) collection = new HashSet<>();
                    else collection = new LinkedList<>();
                    int size = ((NodeList) node).size();
                    for (int i = 0; i < size; i++) {
                        collection.add(KEY.deserialize(paramType, ((NodeList) node).get(i)));
                    }
                    return collection;
                } catch (Throwable e) {
                    throw new SerializerException(e);
                }
            } else throw new NotMatchException(getRegType(), type);
        } else throw new NotMatchException("Collection<?> type need NodeList");
    }

    public Node serialize(Type type, Collection<?> value, Options options) throws HoconException {
        if (value == null || value.isEmpty()) return new NodeList(options);
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
        } else throw new NotMatchException(getRegType(), type);
    }

    public Type getRegType() {
        return new TypeToken<Collection<?>>() {
        }.getType();
    }
}
