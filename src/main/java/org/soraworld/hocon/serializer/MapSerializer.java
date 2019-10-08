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
 */
final class MapSerializer extends TypeSerializer<Map<?, ?>, NodeMap> {
    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException the serializer exception
     */
    MapSerializer() throws SerializerException {
    }

    @Override
    public @NotNull Map<?, ?> deserialize(@NotNull Type fieldType, @NotNull NodeMap node) throws HoconException {
        Type[] arguments = Reflects.getActualTypes(Map.class, fieldType);
        if (arguments != null && arguments.length == 2) {
            Options options = node.options();
            TypeSerializer KEY = options.getSerializer(arguments[0]);
            TypeSerializer VAL = options.getSerializer(arguments[1]);
            Map map;
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
                Object key = KEY.deserialize(arguments[0], new NodeBase(options, path));
                Object val = VAL.deserialize(arguments[1], node.get(path));
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
            NodeMap node = new NodeMap(options);
            if (!value.isEmpty()) {
                final TypeSerializer KEY_S = options.getSerializer(arguments[0]);
                final TypeSerializer VAL_S = options.getSerializer(arguments[1]);
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    TypeSerializer KEY = KEY_S != null ? KEY_S : options.getSerializer(key.getClass());
                    TypeSerializer VAL = VAL_S != null ? VAL_S : options.getSerializer(obj.getClass());
                    if (KEY != null && VAL != null) {
                        if (KEY.keyAble() && key != null && obj != null) {
                            NodeBase base = (NodeBase) KEY.serialize(KEY_S != null ? arguments[0] : key.getClass(), key, options);
                            Node valNode = VAL.serialize(VAL_S != null ? arguments[1] : obj.getClass(), obj, options);
                            if (!node.add(base.getString(), valNode)) {
                                throw new SerializerException("Node for key <" + base.getString() + "> already exist !");
                            }
                        }
                    } else {
                        throw new HoconException("KEY type " + key.getClass() + " has no serializer !!!");
                    }
                }
            }
            return node;
        } else {
            throw new NotMatchException(getType(), fieldType);
        }
    }
}
