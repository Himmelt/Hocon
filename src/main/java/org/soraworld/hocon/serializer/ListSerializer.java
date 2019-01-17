package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeList;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

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
    public Collection<?> deserialize(@NotNull Type actualType, @NotNull NodeList node) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Collection.class, actualType);
        if (arguments != null && arguments.length == 1) {
            Collection<Object> collection;
            if (Reflects.isAssignableFrom(actualType, ArrayList.class)) collection = new ArrayList<>();
            else if (Reflects.isAssignableFrom(actualType, HashSet.class)) collection = new HashSet<>();
            else collection = new LinkedList<>();
            TypeSerializer KEY = node.options().getSerializer(arguments[0]);
            int size = node.size();
            for (int i = 0; i < size; i++) {
                collection.add(KEY.deserialize(arguments[0], node.get(i)));
            }
            return collection;
        } else throw new NotMatchException(getType(), actualType);
    }

    @NotNull
    public NodeList serialize(@NotNull Type actualType, @NotNull Collection<?> value, @NotNull Options options) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Collection.class, actualType);
        if (arguments != null && arguments.length == 1) {
            NodeList nodeList = new NodeList(options);
            TypeSerializer ELEMENT = options.getSerializer(arguments[0]);
            for (Object obj : value) nodeList.add(ELEMENT.serialize(arguments[0], obj, options));
            return nodeList;
        } else throw new NotMatchException(getType(), actualType);
    }
}
