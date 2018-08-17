package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializeException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapSerializer implements TypeSerializer<Map<?, ?>> {
    public Map<?, ?> deserialize(@Nonnull Type type, @Nonnull Node node) throws DeserializeException, NotMatchException {
        if (node instanceof NodeMap && type instanceof ParameterizedType) {
            try {
                Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                TypeSerializer<?> keySerial = node.options().getSerializers().get(params[0]);
                TypeSerializer<?> valSerial = node.options().getSerializers().get(params[1]);
                Map<Object, Object> returnVal = new LinkedHashMap<>();
                for (String path : ((NodeMap) node).getKeys()) {
                    Object key = keySerial.deserialize(params[0], new NodeBase(node.options(), path, false));
                    Object val = valSerial.deserialize(params[1], ((NodeMap) node).getNode(path));
                    if (key == null || val == null) continue;
                    returnVal.put(key, val);
                }
                return returnVal;
            } catch (Throwable e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotMatchException(getRegType(), type);
    }

    public Node serialize(@Nonnull Type type, Map<?, ?> value, @Nonnull Options options) throws NotMatchException, SerializeException {
        if (value == null || value.isEmpty()) return new NodeMap(options);
        if (type instanceof ParameterizedType) {
            try {
                Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                TypeSerializer keySerial = options.getSerializers().get(params[0]);
                TypeSerializer valSerial = options.getSerializers().get(params[1]);
                NodeMap node = new NodeMap(options);
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    if (key != null && obj != null) {
                        Node keyNode = keySerial.serialize(params[0], key, options);
                        if (keyNode instanceof NodeBase) {
                            node.setNode(((NodeBase) keyNode).getString(), valSerial.serialize(params[1], obj, options));
                        } else throw new NotBaseException(key.getClass());
                    }
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
        return new TypeToken<Map<?, ?>>() {
        }.getType();
    }
}
