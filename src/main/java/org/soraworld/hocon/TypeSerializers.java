package org.soraworld.hocon;

import org.soraworld.hocon.token.TypeToken;

import javax.annotation.Nonnull;
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
        //DEFAULT_SERIALIZERS.registerPredicate(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Number.class), new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(String.class), new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Boolean.class), new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Map<Object, ?>>() {
        }, new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<List<?>>() {
        }, new ListSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Enum<?>>() {
        }, new EnumSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Pattern.class), new PatternSerializer());
    }

    private static class URISerializer implements TypeSerializer<URI> {
        @Override
        public URI deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                String plain = ((NodeBase) node).getString();
                if (plain == null) {
                    throw new ObjectMappingException("No value present in node " + node);
                }
                URI uri;
                try {
                    uri = new URI(plain);
                } catch (URISyntaxException e) {
                    // TODO
                    throw new ObjectMappingException("Invalid URI string provided for key???" + ": got " + plain);
                }
                return uri;
            }
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, URI uri, @Nonnull NodeOptions options) {
            return new NodeBase(uri, options);
        }
    }

    private static class URLSerializer implements TypeSerializer<URL> {
        @Override
        public URL deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                String plain = ((NodeBase) node).getString();
                if (plain == null) {
                    throw new ObjectMappingException("No value present in node " + node);
                }

                URL url;
                try {
                    url = new URL(plain);
                } catch (MalformedURLException e) {
                    // TODO
                    throw new ObjectMappingException("Invalid URL string provided for key??" + ": got " + plain);
                }
                return url;
            }
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, URL url, @Nonnull NodeOptions options) {
            return new NodeBase(url, options);
        }
    }

    private static class UUIDSerializer implements TypeSerializer<UUID> {
        @Override
        public UUID deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                try {
                    return UUID.fromString(((NodeBase) node).getString());
                } catch (IllegalArgumentException ex) {
                    throw new ObjectMappingException("Value not a UUID");
                }
            }
            throw new Exception();
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, UUID uuid, @Nonnull NodeOptions options) {
            return new NodeBase(uuid, options);
        }
    }

    private static class NumberSerializer implements TypeSerializer<Number> {
        @Override
        public Number deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) {
            if (node instanceof NodeBase) {
                type = type.wrap();
                Class<?> clazz = type.getRawType();
                if (Integer.class.equals(clazz)) {
                    return ((NodeBase) node).getInt();
                } else if (Long.class.equals(clazz)) {
                    return ((NodeBase) node).getLong();
                } else if (Short.class.equals(clazz)) {
                    return (short) ((NodeBase) node).getInt();
                } else if (Byte.class.equals(clazz)) {
                    return (byte) ((NodeBase) node).getInt();
                } else if (Float.class.equals(clazz)) {
                    return ((NodeBase) node).getFloat();
                } else if (Double.class.equals(clazz)) {
                    return ((NodeBase) node).getDouble();
                }
            }
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, Number value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class StringSerializer implements TypeSerializer<String> {
        @Override
        public String deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getString();
            else return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, String value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class BooleanSerializer implements TypeSerializer<Boolean> {
        @Override
        public Boolean deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, Boolean value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class PatternSerializer implements TypeSerializer<Pattern> {
        @Override
        public Pattern deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                try {
                    return Pattern.compile(((NodeBase) node).getString());
                } catch (PatternSyntaxException ex) {
                    throw new ObjectMappingException(ex.getMessage());
                }
            }
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, Pattern pattern, @Nonnull NodeOptions options) {
            return new NodeBase(pattern.pattern(), options);
        }
    }

    private static class MapSerializer implements TypeSerializer<Map<Object, ?>> {
        public Map<Object, ?> deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeMap) {
                if (!(type.getType() instanceof ParameterizedType)) {
                    throw new ObjectMappingException("Raw types are not supported for collections");
                }
                TypeToken<?> keyToken = type.getKeyType();
                TypeToken<?> valToken = type.getValType();
                TypeSerializer<?> keySerial = node.getOptions().getSerializers().get(keyToken);
                TypeSerializer<?> valSerial = node.getOptions().getSerializers().get(valToken);

                if (valSerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + valToken);
                }

                Map<Object, Object> returnVal = new LinkedHashMap<>();

                for (Map.Entry<String, Node> entry : ((NodeMap) node).getValue().entrySet()) {
                    Object key = keySerial.deserialize(keyToken, new NodeBase(entry.getKey(), node.getOptions()));
                    Object val = valSerial.deserialize(valToken, entry.getValue());
                    if (key == null || val == null) continue;
                    returnVal.put(key, val);
                }
                return returnVal;
            }
            return null;
        }

        public Node serialize(@Nonnull TypeToken<?> type, Map<Object, ?> value, @Nonnull NodeOptions options) throws ObjectMappingException {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> keyToken = type.getKeyType();
            TypeToken<?> valToken = type.getValType();
            TypeSerializer keySerial = options.getSerializers().get(keyToken);
            TypeSerializer valSerial = options.getSerializers().get(valToken);

            if (keySerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + keyToken);
            }

            if (valSerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + valToken);
            }

            NodeMap node = new NodeMap(options);

            for (Map.Entry<Object, ?> entry : value.entrySet()) {
                Object key = entry.getKey();
                Object obj = entry.getValue();
                if (key != null && obj != null) {
                    Node keyNode = keySerial.serialize(keyToken, key, options);
                    if (keyNode instanceof NodeBase) {
                        node.setNode(((NodeBase) keyNode).getString(), valSerial.serialize(valToken, obj, options));
                    }
                }
            }
            return node;
        }
    }

    private static class ListSerializer implements TypeSerializer<List<?>> {
        public List<?> deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeList) {
                if (!(type.getType() instanceof ParameterizedType)) {
                    throw new ObjectMappingException("Raw types are not supported for collections");
                }
                TypeToken<?> entryToken = type.getKeyType();
                TypeSerializer entrySerial = node.getOptions().getSerializers().get(entryToken);
                if (entrySerial == null) {
                    throw new ObjectMappingException("No applicable type serializer for type " + entryToken);
                }
                ArrayList<Object> list = new ArrayList<>();
                for (Node element : ((NodeList) node).getValue()) {
                    list.add(entrySerial.deserialize(entryToken, element));
                }
                return list;
            }
            return new ArrayList<>();
        }

        public Node serialize(@Nonnull TypeToken<?> type, List<?> value, @Nonnull NodeOptions options) throws ObjectMappingException {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> entryToken = type.getKeyType();
            TypeSerializer entrySerial = options.getSerializers().get(entryToken);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryToken);
            }
            NodeList node = new NodeList(options);
            for (Object obj : value) {
                node.add(entrySerial.serialize(entryToken, obj, options));
            }
            return node;
        }
    }

    private static class EnumSerializer implements TypeSerializer<Enum<?>> {
        @Override
        public Enum<?> deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                String name = ((NodeBase) node).getString();
                if (name == null || name.trim().isEmpty()) {
                    throw new ObjectMappingException("No value present in node " + node);
                }
                Class<?> rawType = type.getRawType();
                if (rawType != null) {
                    Enum<?> value = Fields.getEnums(rawType.asSubclass(Enum.class), name);
                    if (value != null) return value;
                }
                throw new Exception();
            }
            return null;
        }

        public Node serialize(@Nonnull TypeToken<?> type, Enum<?> value, @Nonnull NodeOptions options) {
            return new NodeBase(value.name(), options);
        }
    }
}
