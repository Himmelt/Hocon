package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 映射类型序列化器.
 */
public class MapSerializer implements TypeSerializer<Map<?, ?>> {
    public Map<?, ?> deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeMap) {
            if (type instanceof ParameterizedType) {
                try {
                    Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                    TypeSerializer<?> KEY = node.options().getSerializer(params[0]);
                    TypeSerializer<?> VAL = node.options().getSerializer(params[1]);
                    Map<Object, Object> map = new LinkedHashMap<>();
                    for (String path : ((NodeMap) node).keys()) {
                        Object key = KEY.deserialize(params[0], new NodeBase(node.options(), path, false));
                        Object val = VAL.deserialize(params[1], ((NodeMap) node).get(path));
                        if (key != null && val != null) map.put(key, val);
                    }
                    return map;
                } catch (Throwable e) {
                    throw new SerializerException(e);
                }
            } else throw new NotMatchException(getRegType(), type);
        } else throw new NotMatchException("Map<?,?> type need NodeMap");
    }

    public Node serialize(Type type, Map<?, ?> value, Options options) throws HoconException {
        if (value == null || value.isEmpty()) return new NodeMap(options);
        if (type instanceof ParameterizedType) {
            try {
                Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                TypeSerializer KEY = options.getSerializer(params[0]);
                TypeSerializer VAL = options.getSerializer(params[1]);
                NodeMap map = new NodeMap(options);
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    if (key != null && obj != null) {
                        Node node = KEY.serialize(params[0], key, options);
                        if (node instanceof NodeBase) {
                            if (!map.add(((NodeBase) node).getString(), VAL.serialize(params[1], obj, options))) {
                                throw new SerializerException("Node for key <" + ((NodeBase) node).getString() + "> already exist");
                            }
                        } else throw new NotMatchException("Key type must be NodeBase");
                    }
                }
                return map;
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        } else throw new NotMatchException(getRegType(), type);
    }

    public Type getRegType() {
        return new TypeToken<Map<?, ?>>() {
        }.getType();
    }
}
