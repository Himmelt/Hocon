package org.soraworld.hocon.serializer;

public class TypeSerializers {

    private static final SerializerCollection DEFAULT_SERIALIZERS = new SerializerCollection(null);

    static {
        DEFAULT_SERIALIZERS.registerType(new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new ListSerializer());
    }

    public static SerializerCollection defaults() {
        return DEFAULT_SERIALIZERS;
    }

    public static SerializerCollection build() {
        return DEFAULT_SERIALIZERS.newChild();
    }
}
