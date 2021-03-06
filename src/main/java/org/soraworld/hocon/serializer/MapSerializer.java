package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 映射类型序列化器.
 *
 * @author Himmelt
 */
final class MapSerializer extends TypeSerializer<Map<?, ?>, NodeMap> {
    @Override
    public @NotNull Map<?, ?> deserialize(@NotNull Type fieldType, @NotNull NodeMap node) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Map.class, fieldType);
        if (arguments != null && arguments.length == 2) {
            Options options = node.options();
            final TypeSerializer<Object, Node> keySerial = options.getSerializer(arguments[0]);
            final TypeSerializer<Object, Node> valSerial = options.getSerializer(arguments[1]);
            Map<Object, Object> map;
            try {
                map = (Map) getTypeInstance(fieldType);
            } catch (Throwable e) {
                System.out.println("Failed to construct instance for " + fieldType.getTypeName() + " , will fall back to LinkedHashMap.");
                if (options.isDebug()) {
                    e.printStackTrace();
                }
                map = new LinkedHashMap<>();
            }
            for (String path : node.keys()) {
                Object key = keySerial.deserialize(arguments[0], new NodeBase(options, path));
                Object val = valSerial.deserialize(arguments[1], node.get(path));
                map.put(key, val);
            }
            return map;
        } else {
            throw new NotMatchException(getType(), fieldType);
        }
    }

    @Override
    public @NotNull NodeMap serialize(@NotNull Type fieldType, @NotNull Map<?, ?> value, @NotNull Options options) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Map.class, fieldType);
        if (arguments != null && arguments.length == 2) {
            NodeMap map = new NodeMap(options);
            if (!value.isEmpty()) {
                final TypeSerializer<Object, Node> keyS = options.getSerializer(arguments[0]);
                final TypeSerializer<Object, Node> valS = options.getSerializer(arguments[1]);
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    final TypeSerializer<Object, Node> keySerial = keyS != null ? keyS : options.getSerializer(key.getClass());
                    final TypeSerializer<Object, Node> valSerial = valS != null ? valS : options.getSerializer(obj.getClass());
                    if (keySerial != null && valSerial != null) {
                        if (keySerial.keyAble() && key != null && obj != null) {
                            NodeBase base = (NodeBase) keySerial.serialize(keyS != null ? arguments[0] : key.getClass(), key, options);
                            Node valNode = valSerial.serialize(valS != null ? arguments[1] : obj.getClass(), obj, options);
                            if (!map.put(base.getString(), valNode)) {
                                throw new SerializerException("Node for key <" + base.getString() + "> put failed !");
                            }
                        }
                    } else {
                        throw new HoconException("KEY type " + key.getClass() + " has no serializer !!!");
                    }
                }
            }
            return map;
        } else {
            throw new NotMatchException(getType(), fieldType);
        }
    }
}
