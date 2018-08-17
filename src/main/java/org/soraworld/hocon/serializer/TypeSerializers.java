package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.*;
import org.soraworld.hocon.node.*;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class TypeSerializers {

    private static final TypeSerializerCollection DEFAULT_SERIALIZERS = new TypeSerializerCollection(null);

    public static TypeSerializerCollection getDefaultSerializers() {
        return DEFAULT_SERIALIZERS;
    }

    public static TypeSerializerCollection newCollection() {
        return DEFAULT_SERIALIZERS.newChild();
    }

    static {
        DEFAULT_SERIALIZERS.registerType(new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new ListSerializer());
    }

}
