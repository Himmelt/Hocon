package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NotMatchException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Serializable 序列化器.
 *
 * @author Himmelt
 */
final class SerializableSerializer extends TypeSerializer<Serializable, NodeMap> {
    @Override
    public @NotNull Serializable deserialize(@NotNull Type fieldType, @NotNull NodeMap node) throws HoconException {
        if (fieldType instanceof Class) {
            try {
                Serializable object = (Serializable) ((Class<?>) fieldType).newInstance();
                node.modify(object);
                return object;
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new SerializerException("Class " + fieldType + " which implements Serializable must have public non-parameter constructor !!!");
            } catch (Throwable e) {
                throw new SerializerException(e);
            }
        }
        throw new NotMatchException("Annotation type must be Class");
    }

    @Override
    public @NotNull NodeMap serialize(@NotNull Type fieldType, @NotNull Serializable value, @NotNull Options options) {
        NodeMap node = new NodeMap(options);
        node.extract(value);
        return node;
    }
}
