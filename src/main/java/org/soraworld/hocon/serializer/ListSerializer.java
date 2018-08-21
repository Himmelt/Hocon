package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeList;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import javax.annotation.Nonnull;
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
    public Collection<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws NotMatchException, DeserializeException {
        if (node instanceof NodeList && type instanceof ParameterizedType) {
            try {
                Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                Type paramType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer keySerial = node.options().getSerializer(paramType);
                Collection<Object> collection;
                if (rawType.isAssignableFrom(ArrayList.class)) collection = new ArrayList<>();
                else if (rawType.isAssignableFrom(HashSet.class)) collection = new HashSet<>();
                else collection = new LinkedList<>();
                int size = ((NodeList) node).size();
                for (int i = 0; i < size; i++) {
                    collection.add(keySerial.deserialize(paramType, ((NodeList) node).get(i)));
                }
                return collection;
            } catch (Throwable e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotMatchException(getRegType(), type);
    }

    public Node serialize(@Nonnull Type type, Collection<?> value, @Nonnull Options options) throws NotMatchException, SerializeException {
        if (value == null || value.isEmpty()) return new NodeList(options);
        if (type instanceof ParameterizedType) {
            try {
                Type keyType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer keySerial = options.getSerializer(keyType);
                NodeList node = new NodeList(options);
                for (Object obj : value) {
                    node.add(keySerial.serialize(keyType, obj, options));
                }
                return node;
            } catch (Throwable e) {
                throw new SerializeException(e);
            }
        }
        throw new NotMatchException(getRegType(), type);
    }

    @Nonnull
    public Type getRegType() {
        return new TypeToken<Collection<?>>() {
        }.getType();
    }
}
