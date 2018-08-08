package org.soraworld.hocon;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TypeSerializers {

    private static final TypeSerializerCollection DEFAULT_SERIALIZERS = new TypeSerializerCollection(null);

    public static TypeSerializerCollection getDefaultSerializers() {
        return DEFAULT_SERIALIZERS;
    }

    public static TypeSerializerCollection newCollection() {
        return DEFAULT_SERIALIZERS.newChild();
    }

    static {
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(URI.class), new URISerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(URL.class), new URLSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(UUID.class), new UUIDSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Number.class), new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(String.class), new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Boolean.class), new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Pattern.class), new PatternSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Map<?, ?>>() {
        }, new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<List<?>>() {
        }, new ListSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Enum<?>>() {
        }, new EnumSerializer());
    }

    private static class URISerializer implements TypeSerializer<URI, NodeBase> {
        @Override
        public URI deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            String plain = node.getString();
            if (plain == null) {
                throw new ObjectMappingException("No value present in node " + node);
            }
            URI uri;
            try {
                uri = new URI(plain);
            } catch (URISyntaxException e) {
                throw new ObjectMappingException("Invalid URI string provided for " + node.getKey() + ": got " + plain);
            }
            return uri;
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, URI uri, @Nonnull NodeBase node) {
            node.setValue(uri);
        }
    }

    private static class URLSerializer implements TypeSerializer<URL, NodeBase> {
        @Override
        public URL deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            String plain = node.getString();
            if (plain == null) {
                throw new ObjectMappingException("No value present in node " + node);
            }

            URL url;
            try {
                url = new URL(plain);
            } catch (MalformedURLException e) {
                throw new ObjectMappingException("Invalid URL string provided for " + node.getKey() + ": got " + plain);
            }
            return url;
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, URL url, @Nonnull NodeBase node) {
            node.setValue(url);
        }
    }

    private static class UUIDSerializer implements TypeSerializer<UUID, NodeBase> {
        @Override
        public UUID deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            try {
                return UUID.fromString(node.getString());
            } catch (IllegalArgumentException ex) {
                throw new ObjectMappingException("Value not a UUID", ex);
            }
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, UUID uuid, @Nonnull NodeBase node) {
            node.setValue(uuid);
        }
    }

    private static class NumberSerializer implements TypeSerializer<Number, NodeBase> {
        @Override
        public Number deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            type = type.wrap();
            Class<?> clazz = type.getRawType();
            if (Integer.class.equals(clazz)) {
                return node.getInt();
            } else if (Long.class.equals(clazz)) {
                return node.getLong();
            } else if (Short.class.equals(clazz)) {
                return (short) node.getInt();
            } else if (Byte.class.equals(clazz)) {
                return (byte) node.getInt();
            } else if (Float.class.equals(clazz)) {
                return node.getFloat();
            } else if (Double.class.equals(clazz)) {
                return node.getDouble();
            }
            return null;
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, Number value, @Nonnull NodeBase node) {
            node.setValue(value);
        }
    }

    private static class StringSerializer implements TypeSerializer<String, NodeBase> {
        @Override
        public String deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            return node.getString();
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, String value, @Nonnull NodeBase node) {
            node.setValue(value);
        }
    }

    private static class BooleanSerializer implements TypeSerializer<Boolean, NodeBase> {
        @Override
        public Boolean deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            return node.getBoolean();
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, Boolean value, @Nonnull NodeBase node) {
            node.setValue(value);
        }
    }

    private static class PatternSerializer implements TypeSerializer<Pattern, NodeBase> {
        @Override
        public Pattern deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            try {
                return Pattern.compile(node.getString());
            } catch (PatternSyntaxException ex) {
                throw new ObjectMappingException(ex);
            }
        }

        @Override
        public void serialize(@Nonnull TypeToken<?> type, Pattern pattern, @Nonnull NodeBase node) {
            node.setValue(pattern.pattern());
        }
    }

    private static class MapSerializer implements TypeSerializer<Map<?, ?>, NodeMap> {
        @Override
        public Map<?, ?> deserialize(TypeToken<?> type, Node node) {
            Map<Object, Object> ret = new LinkedHashMap<>();
            if (node.hasMapChildren()) {
                if (!(type.getType() instanceof ParameterizedType)) {
                    throw new ObjectMappingException("Raw types are not supported for collections");
                }
                TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
                TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
                TypeSerializer<?> keySerial = node.getOptions().getSerializers().get(key);
                TypeSerializer<?> valueSerial = node.getOptions().getSerializers().get(value);

                if (keySerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + key);
                }

                if (valueSerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + value);
                }

                for (Map.Entry<Object, ? extends Node> ent : node.getChildrenMap().entrySet()) {
                    Object keyValue = keySerial.deserialize(key, SimpleNode.root().setValue(ent.getKey()));
                    Object valueValue = valueSerial.deserialize(value, ent.getValue());
                    if (keyValue == null || valueValue == null) {
                        continue;
                    }

                    ret.put(keyValue, valueValue);
                }
            }
            return ret;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void serialize(TypeToken<?> type, Map<?, ?> obj, Node node) {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
            TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
            TypeSerializer keySerial = node.getOptions().getSerializers().get(key);
            TypeSerializer valueSerial = node.getOptions().getSerializers().get(value);

            if (keySerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + key);
            }

            if (valueSerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + value);
            }

            node.setValue(ImmutableMap.of());
            for (Map.Entry<?, ?> ent : obj.entrySet()) {
                SimpleNode keyNode = SimpleNode.root();
                keySerial.serialize(key, ent.getKey(), keyNode);
                valueSerial.serialize(value, ent.getValue(), node.getNode(keyNode.getValue()));
            }
        }

        public Map<?, ?> deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeMap node) throws Exception {
            return null;
        }

        public void serialize(@Nonnull TypeToken<?> type, Map<?, ?> value, @Nonnull NodeMap node) {

        }
    }

    private static class ListSerializer implements TypeSerializer<List<?>, NodeList> {
        @Override
        public List<?> deserialize(TypeToken<?> type, Node node) {
            if (node instanceof NodeList)
                if (!(type.getType() instanceof ParameterizedType)) {
                    throw new ObjectMappingException("Raw types are not supported for collections");
                }
            TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
            TypeSerializer entrySerial = node.getOptions().getSerializers().get(entryType);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryType);
            }

            if (node.hasListChildren()) {
                List<? extends Node> values = node.getChildrenList();
                List<Object> ret = new ArrayList<>(values.size());
                for (Node ent : values) {
                    ret.add(entrySerial.deserialize(entryType, ent));
                }
                return ret;
            } else {
                Object unwrappedVal = node.getValue();
                if (unwrappedVal != null) {
                    return Lists.newArrayList(entrySerial.deserialize(entryType, node));
                }
            }
            return new ArrayList<>();
        }

        @Override
        public void serialize(TypeToken<?> type, List<?> obj, Node value) {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
            TypeSerializer entrySerial = value.getOptions().getSerializers().get(entryType);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryType);
            }
            value.setValue(ImmutableList.of());
            for (Object ent : obj) {
                entrySerial.serialize(entryType, ent, value.getAppendedNode());
            }
        }

        public List<?> deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeList node) throws Exception {
            return null;
        }

        public void serialize(@Nonnull TypeToken<?> type, List<?> value, @Nonnull NodeList node) {

        }
    }

    private static class EnumSerializer implements TypeSerializer<Enum<?>, NodeBase> {
        @Override
        public Enum<?> deserialize(@Nonnull TypeToken<?> type, @Nonnull NodeBase node) throws Exception {
            String name = node.getString();
            if (name == null || name.trim().isEmpty()) {
                throw new ObjectMappingException("No value present in node " + node);
            }
            Enum<?> value = Fields.getEnums(type.getRawType().asSubclass(Enum.class), name);
            if (value != null) return value;
            else throw new Exception();
        }

        public void serialize(@Nonnull TypeToken<?> type, Enum<?> value, @Nonnull NodeBase node) {
            node.setValue(value.name());
        }
    }

    private static class AnnotatedSerializer implements TypeSerializer<Object, NodeMap> {
        @Override
        public Object deserialize(TypeToken<?> type, Node value) {
            Class<?> clazz = getInstantiableType(type, value.getNode("__class__").getString());
            return value.getOptions().getObjectMapperFactory().getMapper(clazz).bindToNew().populate(value);
        }

        private Class<?> getInstantiableType(TypeToken<?> type, String configuredName) throws ObjectMappingException {
            Class<?> retClass;
            if (type.getRawType().isInterface() || Modifier.isAbstract(type.getRawType().getModifiers())) {
                if (configuredName == null) {
                    throw new ObjectMappingException("No available configured type for instances of " + type);
                } else {
                    try {
                        retClass = Class.forName(configuredName);
                    } catch (ClassNotFoundException e) {
                        throw new ObjectMappingException("Unknown class of object " + configuredName, e);
                    }
                    if (!type.getRawType().isAssignableFrom(retClass)) {
                        throw new ObjectMappingException("Configured type " + configuredName + " does not extend "
                                + type.getRawType().getCanonicalName());
                    }
                }
            } else {
                retClass = type.getRawType();
            }
            return retClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void serialize(TypeToken<?> type, Object obj, Node value) {
            if (type.getRawType().isInterface() || Modifier.isAbstract(type.getRawType().getModifiers())) {
                // serialize obj's concrete type rather than the interface/abstract class
                value.getNode("__class__").setValue(obj.getClass().getName());
            }
            ((ObjectMapper<Object>) value.getOptions().getObjectMapperFactory().getMapper(obj.getClass())).bind(obj).serialize(value);
        }
    }

}
