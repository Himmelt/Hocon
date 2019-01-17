package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

public final class AnnotationSerializer extends TypeSerializer<Object, NodeMap> {

    /**
     * Instantiates a new Annotation serializer.
     *
     * @throws SerializerException the serializer exception
     */
    public AnnotationSerializer() throws SerializerException {
    }

    @NotNull
    public Object deserialize(@NotNull Type actualType, @NotNull NodeMap node) throws HoconException {
        if (actualType instanceof Class) {
            try {
                Object object = ((Class<?>) actualType).getConstructor().newInstance();
                node.modify(object);
                return object;
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new SerializerException("Class " + actualType + " annotated with @Serializable must have public non-parameter constructor !!");
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        }
        throw new NotMatchException("Annotation type must be Class");
    }

    @NotNull
    public NodeMap serialize(@NotNull Type actualType, @NotNull Object value, @NotNull Options options) {
        NodeMap node = new NodeMap(options);
        node.extract(value);
        return node;
    }
}
