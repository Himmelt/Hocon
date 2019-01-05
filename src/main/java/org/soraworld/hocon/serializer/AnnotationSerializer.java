package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class AnnotationSerializer extends TypeSerializer<Object, NodeMap> {
    @Nonnull
    public Object deserialize(@Nonnull Type type, @Nonnull NodeMap node) throws HoconException {
        if (type instanceof Class) {
            try {
                Object object = ((Class<?>) type).getConstructor().newInstance();
                node.modify(object);
                return object;
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new SerializerException("Class " + type + " annotated with @Serializable must have public non-parameter constructor !!");
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        }
        throw new NotMatchException("Annotation type must be Class");
    }

    @Nonnull
    public NodeMap serialize(@Nonnull Type type, @Nonnull Object value, @Nonnull Options options) {
        NodeMap node = new NodeMap(options);
        node.extract(value);
        return node;
    }
}
