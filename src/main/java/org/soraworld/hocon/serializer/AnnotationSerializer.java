package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.NullNodeException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.node.Serializable;

import java.lang.reflect.Type;

public class AnnotationSerializer implements TypeSerializer<Object> {
    public Object deserialize(Type type, Node node) throws HoconException {
        if (node == null) throw new NullNodeException();
        if (node instanceof NodeMap) {
            if (type instanceof Class) {
                try {
                    Object object = ((Class<?>) type).getConstructor().newInstance();
                    ((NodeMap) node).modify(object);
                    return object;
                } catch (Throwable e) {
                    throw new SerializerException(e);
                }
            }
            throw new NotMatchException("Annotation type must be Class");
        }
        throw new NotMatchException("Annotation Object need NodeMap");
    }

    public Node serialize(Type type, Object value, Options options) throws HoconException {
        if (value == null) return new NodeMap(options);
        if (type instanceof Class) {
            NodeMap node = new NodeMap(options);
            node.extract(value);
            return node;
        }
        throw new NotMatchException("Annotation type must be Class");
    }

    public Type getRegType() {
        return Serializable.class;
    }
}
