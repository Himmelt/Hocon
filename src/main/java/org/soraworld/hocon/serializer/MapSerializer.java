package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 映射类型序列化器.
 */
public final class MapSerializer extends TypeSerializer<Map<?, ?>, NodeMap> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    public MapSerializer() throws SerializerException {
    }

    @NotNull
    public Map<?, ?> deserialize(@NotNull Type type, @NotNull NodeMap node) throws HoconException {
        if (type instanceof ParameterizedType) {
            try {
                Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                TypeSerializer KEY = node.options().getSerializer(params[0]);
                TypeSerializer VAL = node.options().getSerializer(params[1]);
                Map<Object, Object> map = new LinkedHashMap<>();
                for (String path : node.keys()) {
                    Object key = KEY.deserialize(params[0], new NodeBase(node.options(), path, false));
                    Object val = VAL.deserialize(params[1], node.get(path));
                    map.put(key, val);
                }
                return map;
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        } else throw new NotMatchException(getType(), type);
    }

    @NotNull
    public NodeMap serialize(@NotNull Type actualType, @NotNull Map<?, ?> value, @NotNull Options options) throws HoconException {
        if (value.isEmpty()) return new NodeMap(options);
        Type[] arguments = null;
        if (actualType instanceof ParameterizedType) {
            arguments = Reflects.getActualArguments(Map.class, (ParameterizedType) actualType);
        } else if (actualType instanceof Class) arguments = Reflects.getActualArguments(Map.class, (Class) actualType);
        if (arguments != null && arguments.length == 2) {
            TypeSerializer KEY = options.getSerializer(arguments[0]);
            TypeSerializer VAL = options.getSerializer(arguments[1]);
            NodeMap nodeMap = new NodeMap(options);
            if (KEY.keyAble()) {
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
