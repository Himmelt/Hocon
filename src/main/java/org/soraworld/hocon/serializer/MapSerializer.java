package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 映射类型序列化器.
 */
final class MapSerializer extends TypeSerializer<Map<?, ?>, NodeMap> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    MapSerializer() throws SerializerException {
    }

    @NotNull
    public Map<?, ?> deserialize(@NotNull Type actualType, @NotNull NodeMap node) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Map.class, actualType);
        if (arguments != null && arguments.length == 2) {
            Options options = node.options();
            TypeSerializer KEY = options.getSerializer(arguments[0]);
            TypeSerializer VAL = options.getSerializer(arguments[1]);
            Map<Object, Object> map = new LinkedHashMap<>();
            for (String path : node.keys()) {
                Object key = KEY.deserialize(arguments[0], new NodeBase(node.options(), path));
                Object val = VAL.deserialize(arguments[1], node.get(path));
                map.put(key, val);
            }
            return map;
        } else throw new NotMatchException(getType(), actualType);
    }

    @NotNull
    public NodeMap serialize(@NotNull Type actualType, @NotNull Map<?, ?> value, @NotNull Options options) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Map.class, actualType);
        if (arguments != null && arguments.length == 2) {
            NodeMap nodeMap = new NodeMap(options);
            TypeSerializer KEY = options.getSerializer(arguments[0]);
            TypeSerializer VAL = options.getSerializer(arguments[1]);
            if (!value.isEmpty() && KEY.keyAble()) {
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    if (key != null && obj != null) {
                        NodeBase node = (NodeBase) KEY.serialize(arguments[0], key, options);
                        if (!nodeMap.add(node.getString(), VAL.serialize(arguments[1], obj, options))) {
                            throw new SerializerException("Node for key <" + node.getString() + "> already exist !");
                        }
                    }
                }
            }
            return nodeMap;
        } else throw new NotMatchException(getType(), actualType);
    }
}
