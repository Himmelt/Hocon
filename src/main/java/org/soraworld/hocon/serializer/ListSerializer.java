package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.node.NodeList;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

/**
 * 集合类型序列化器.
 * 目前支持 {@link java.util.List} {@link java.util.Set} {@link java.util.Queue}
 *
 * @author Himmelt
 */
final class ListSerializer extends TypeSerializer<Collection<?>, NodeList> {
    @Override
    public @NotNull Collection<?> deserialize(@NotNull Type fieldType, @NotNull NodeList node) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Collection.class, fieldType);
        if (arguments != null && arguments.length == 1) {
            Options options = node.options();
            TypeSerializer KEY = options.getSerializer(arguments[0]);
            Collection list;
            try {
                list = (Collection) getTypeInstance(fieldType);
            } catch (Throwable e) {
                System.out.println("Failed to construct instance for " + fieldType.getTypeName() + " , will fall back to LinkedList.");
                if (options.isDebug()) {
                    e.printStackTrace();
                }
                list = new LinkedList<>();
            }
            int size = node.size();
            for (int i = 0; i < size; i++) {
                list.add(KEY.deserialize(arguments[0], node.get(i)));
            }
            return list;
        } else {
            throw new NotMatchException(getType(), fieldType);
        }
    }

    @Override
    public @NotNull NodeList serialize(@NotNull Type fieldType, @NotNull Collection<?> value, @NotNull Options options) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Collection.class, fieldType);
        if (arguments != null && arguments.length == 1) {
            NodeList nodeList = new NodeList(options);
            TypeSerializer ELEMENT = options.getSerializer(arguments[0]);
            for (Object obj : value) {
                nodeList.add(ELEMENT.serialize(arguments[0], obj, options));
            }
            return nodeList;
        } else {
            throw new NotMatchException(getType(), fieldType);
        }
    }
}
