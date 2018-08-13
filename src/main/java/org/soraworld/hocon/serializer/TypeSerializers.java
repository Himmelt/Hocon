package org.soraworld.hocon.serializer;

import org.soraworld.hocon.node.*;
import org.soraworld.hocon.reflect.Primitives;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        DEFAULT_SERIALIZERS.registerType(Number.class, new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(String.class, new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(Boolean.class, new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(UUID.class, new UUIDSerializer());
        DEFAULT_SERIALIZERS.registerType(Pattern.class, new PatternSerializer());
        //DEFAULT_SERIALIZERS.registerPredicate(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Map<Object, ?>>() {
        }.getType(), new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Collection<?>>() {
        }.getType(), new ListSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Enum<?>>() {
        }.getType(), new EnumSerializer());
        DEFAULT_SERIALIZERS.registerType(URI.class, new URISerializer());
        DEFAULT_SERIALIZERS.registerType(URL.class, new URLSerializer());
    }

    private static class URISerializer implements TypeSerializer<URI> {
        @Override
        public URI deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
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
        public Node serialize(@Nonnull Type type, URI uri, @Nonnull NodeOptions options) {
            return new NodeBase(uri, options);
        }
    }

    private static class URLSerializer implements TypeSerializer<URL> {
        @Override
        public URL deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
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
        public Node serialize(@Nonnull Type type, URL url, @Nonnull NodeOptions options) {
            return new NodeBase(url, options);
        }
    }

    private static class UUIDSerializer implements TypeSerializer<UUID> {
        @Override
        public UUID deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
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
        public Node serialize(@Nonnull Type type, UUID uuid, @Nonnull NodeOptions options) {
            return new NodeBase(uuid, options);
        }
    }

    private static class NumberSerializer implements TypeSerializer<Number> {
        @Override
        public Number deserialize(@Nonnull Type type, @Nonnull Node node) {
            if (node instanceof NodeBase && type instanceof Class) {
                Class clazz = Primitives.wrap((Class<?>) type);
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
        public Node serialize(@Nonnull Type type, Number value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class StringSerializer implements TypeSerializer<String> {
        @Override
        public String deserialize(@Nonnull Type type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getString();
            else return null;
        }

        @Override
        public Node serialize(@Nonnull Type type, String value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class BooleanSerializer implements TypeSerializer<Boolean> {
        @Override
        public Boolean deserialize(@Nonnull Type type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
            return null;
        }

        @Override
        public Node serialize(@Nonnull Type type, Boolean value, @Nonnull NodeOptions options) {
            return new NodeBase(value, options);
        }
    }

    private static class PatternSerializer implements TypeSerializer<Pattern> {
        @Override
        public Pattern deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
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
        public Node serialize(@Nonnull Type type, Pattern pattern, @Nonnull NodeOptions options) {
            return new NodeBase(pattern.pattern(), options);
        }
    }

    private static class MapSerializer implements TypeSerializer<Map<?, ?>> {
        public Map<?, ?> deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeMap) {
                if (type instanceof ParameterizedType) {
                    Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                    TypeSerializer<?> keySerial = node.getOptions().getSerializers().get(params[0]);
                    TypeSerializer<?> valSerial = node.getOptions().getSerializers().get(params[1]);

                    if (valSerial == null) {
                        throw new ObjectMappingException("No type serializer available for type " + params[1]);
                    }

                    Map<Object, Object> returnVal = new LinkedHashMap<>();

                    for (Map.Entry<String, Node> entry : ((NodeMap) node).getValue().entrySet()) {
                        Object key = keySerial.deserialize(params[0], new NodeBase(entry.getKey(), node.getOptions()));
                        Object val = valSerial.deserialize(params[1], entry.getValue());
                        if (key == null || val == null) continue;
                        returnVal.put(key, val);
                    }
                    return returnVal;
                } else throw new ObjectMappingException("Raw types are not supported for collections");
            }
            return null;
        }

        public Node serialize(@Nonnull Type type, Map<?, ?> value, @Nonnull NodeOptions options) throws Exception {
            if (type instanceof ParameterizedType) {
                Type[] params = Reflects.getMapParameter((ParameterizedType) type);
                TypeSerializer keySerial = options.getSerializers().get(params[0]);
                TypeSerializer valSerial = options.getSerializers().get(params[1]);

                if (keySerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + params[0]);
                }

                if (valSerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + params[1]);
                }

                NodeMap node = new NodeMap(options);

                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    Object key = entry.getKey();
                    Object obj = entry.getValue();
                    if (key != null && obj != null) {
                        Node keyNode = keySerial.serialize(params[0], key, options);
                        if (keyNode instanceof NodeBase) {
                            node.setNode(((NodeBase) keyNode).getString(), valSerial.serialize(params[1], obj, options));
                        } else {
                            // TODO console message Non NodeBase Key
                        }
                    }
                }
                return node;
            } else throw new ObjectMappingException("Raw types are not supported for collections");
        }
    }

    private static class ListSerializer implements TypeSerializer<Collection<?>> {
        public Collection<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeList && type instanceof ParameterizedType) {
                Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                Type paramType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer keySerial = node.getOptions().getSerializers().get(paramType);
                if (keySerial == null) {
                    throw new ObjectMappingException("No applicable type serializer for type " + paramType);
                }
                // TODO Collection > List/Set/Queue...
                Collection<Object> collection;
                if (rawType.equals(List.class)) collection = new ArrayList<>();
                else if (rawType.equals(Set.class)) collection = new HashSet<>();
                else collection = new LinkedList<>();
                for (Node element : ((NodeList) node).getValue()) {
                    collection.add(keySerial.deserialize(paramType, element));
                }
                return collection;
            }
            // TODO console error message
            // TODO ?? empty or null ??
            return null;
        }

        public Node serialize(@Nonnull Type type, Collection<?> value, @Nonnull NodeOptions options) throws Exception {
            if (type instanceof ParameterizedType) {
                Type keyType = Reflects.getListParameter((ParameterizedType) type);
                TypeSerializer keySerial = options.getSerializers().get(keyType);
                if (keySerial == null) {
                    throw new ObjectMappingException("No applicable type serializer for type " + keyType);
                }
                NodeList node = new NodeList(options);
                for (Object obj : value) {
                    node.add(keySerial.serialize(keyType, obj, options));
                }
                return node;
            } else throw new ObjectMappingException("Raw types are not supported for collections");
        }
    }

    private static class EnumSerializer implements TypeSerializer<Enum<?>> {
        @Override
        public Enum<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeBase) {
                String name = ((NodeBase) node).getString();
                if (name == null || name.trim().isEmpty()) {
                    throw new ObjectMappingException("No value present in node " + node);
                }
                Class<?> rawType = Reflects.getRawType(type);
                if (rawType != null) {
                    Enum<?> value = Reflects.getEnums(rawType.asSubclass(Enum.class), name);
                    if (value != null) return value;
                }
                throw new Exception();
            }
            return null;
        }

        public Node serialize(@Nonnull Type type, Enum<?> value, @Nonnull NodeOptions options) {
            return new NodeBase(value.name(), options);
        }
    }
}
