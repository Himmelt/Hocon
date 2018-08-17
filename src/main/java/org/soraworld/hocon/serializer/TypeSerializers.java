package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.*;
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
        DEFAULT_SERIALIZERS.registerType(new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(new UUIDSerializer());
        DEFAULT_SERIALIZERS.registerType(new PatternSerializer());
        DEFAULT_SERIALIZERS.registerType(new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new ListSerializer());
        DEFAULT_SERIALIZERS.registerType(new EnumSerializer());
        DEFAULT_SERIALIZERS.registerType(new URISerializer());
        DEFAULT_SERIALIZERS.registerType(new URLSerializer());
        //DEFAULT_SERIALIZERS.registerPredicate(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer());
    }

    private static class URISerializer implements TypeSerializer<URI> {
        public URI deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException {
            if (node instanceof NodeBase) {
                String plain = ((NodeBase) node).getString();
                if (plain == null) throw new NullValueException(getRegType());
                try {
                    return new URI(plain);
                } catch (URISyntaxException e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, URI uri, @Nonnull Options options) {
            return new NodeBase(options, uri, false);
        }

        @Nonnull
        public Type getRegType() {
            return URI.class;
        }
    }

    private static class URLSerializer implements TypeSerializer<URL> {
        public URL deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException {
            if (node instanceof NodeBase) {
                String plain = ((NodeBase) node).getString();
                if (plain == null) throw new NullValueException(getRegType());
                try {
                    return new URL(plain);
                } catch (MalformedURLException e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, URL url, @Nonnull Options options) {
            return new NodeBase(options, url, false);
        }

        @Nonnull
        public Type getRegType() {
            return URL.class;
        }
    }

    private static class UUIDSerializer implements TypeSerializer<UUID> {
        public UUID deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException {
            if (node instanceof NodeBase) {
                String uuid = ((NodeBase) node).getString();
                if (uuid == null) throw new NullValueException(getRegType());
                try {
                    return UUID.fromString(uuid);
                } catch (IllegalArgumentException e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, UUID uuid, @Nonnull Options options) {
            return new NodeBase(options, uuid, false);
        }

        @Nonnull
        public Type getRegType() {
            return UUID.class;
        }
    }

    private static class NumberSerializer implements TypeSerializer<Number> {
        public Number deserialize(@Nonnull Type type, @Nonnull Node node) throws DeserializeException, NullValueException, NotMatchException {
            if (node instanceof NodeBase && type instanceof Class) {
                String number = ((NodeBase) node).getString();
                if (number == null) throw new NullValueException(getRegType());
                Class clazz = Primitives.wrap((Class<?>) type);
                try {
                    if (Integer.class.equals(clazz)) {
                        return Integer.valueOf(number);
                    } else if (Long.class.equals(clazz)) {
                        return Long.valueOf(number);
                    } else if (Short.class.equals(clazz)) {
                        return Short.valueOf(number);
                    } else if (Byte.class.equals(clazz)) {
                        return Byte.valueOf(number);
                    } else if (Float.class.equals(clazz)) {
                        return Float.valueOf(number);
                    } else if (Double.class.equals(clazz)) {
                        return Double.valueOf(number);
                    } else throw new NotMatchException(getRegType(), type);
                } catch (Throwable e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotMatchException(getRegType(), type);
        }

        public Node serialize(@Nonnull Type type, Number value, @Nonnull Options options) {
            return new NodeBase(options, value, false);
        }

        @Nonnull
        public Type getRegType() {
            return Number.class;
        }
    }

    private static class StringSerializer implements TypeSerializer<String> {
        public String deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, NotBaseException {
            if (node instanceof NodeBase) {
                String string = ((NodeBase) node).getString();
                if (string == null) throw new NullValueException(getRegType());
                else return string;
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, String value, @Nonnull Options options) {
            return new NodeBase(options, value, false);
        }

        @Nonnull
        public Type getRegType() {
            return String.class;
        }
    }

    private static class BooleanSerializer implements TypeSerializer<Boolean> {
        public Boolean deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException {
            if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, Boolean value, @Nonnull Options options) {
            return new NodeBase(options, value, false);
        }

        @Nonnull
        public Type getRegType() {
            return Boolean.class;
        }
    }

    private static class PatternSerializer implements TypeSerializer<Pattern> {
        public Pattern deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException, NullValueException, DeserializeException {
            if (node instanceof NodeBase) {
                String string = ((NodeBase) node).getString();
                if (string == null) throw new NullValueException(getRegType());
                try {
                    return Pattern.compile(string);
                } catch (PatternSyntaxException e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, Pattern pattern, @Nonnull Options options) {
            return new NodeBase(options, pattern.pattern(), false);
        }

        @Nonnull
        public Type getRegType() {
            return Pattern.class;
        }
    }

    private static class MapSerializer implements TypeSerializer<Map<?, ?>> {
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
                            } else {
                                // TODO console message Non NodeBase Key
                            }
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

    private static class ListSerializer implements TypeSerializer<Collection<?>> {
        public Collection<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws NotMatchException, DeserializeException {
            if (node instanceof NodeList && type instanceof ParameterizedType) {
                try {
                    Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                    Type paramType = Reflects.getListParameter((ParameterizedType) type);
                    TypeSerializer keySerial = node.options().getSerializers().get(paramType);
                    // TODO Collection > List/Set/Queue...
                    Collection<Object> collection;
                    if (rawType.equals(List.class)) collection = new ArrayList<>();
                    else if (rawType.equals(Set.class)) collection = new HashSet<>();
                    else collection = new LinkedList<>();
                    int size = ((NodeList) node).size();
                    for (int i = 0; i < size; i++) {
                        collection.add(keySerial.deserialize(paramType, ((NodeList) node).getNode(i)));
                    }
                    return collection;
                } catch (Throwable e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotMatchException(getRegType(), type);
        }

        public Node serialize(@Nonnull Type type, Collection<?> value, @Nonnull Options options) throws NotMatchException, SerializeException {
            if (type instanceof ParameterizedType) {
                try {
                    Type keyType = Reflects.getListParameter((ParameterizedType) type);
                    TypeSerializer keySerial = options.getSerializers().get(keyType);
                    NodeList node = new NodeList(options);
                    for (Object obj : value) {
                        node.add(keySerial.serialize(keyType, obj, options));
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
            return new TypeToken<Collection<?>>() {
            }.getType();
        }
    }

    private static class EnumSerializer implements TypeSerializer<Enum<?>> {
        public Enum<?> deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException, NullValueException, DeserializeException {
            if (node instanceof NodeBase) {
                String name = ((NodeBase) node).getString();
                if (name == null) throw new NullValueException(getRegType());
                try {
                    Class<?> rawType = Reflects.getRawType(type);
                    Enum<?> value = Reflects.getEnums(rawType.asSubclass(Enum.class), name);
                    if (value != null) return value;
                    else throw new DeserializeException("Non Enum Value " + name + " for " + rawType.getName());
                } catch (Throwable e) {
                    throw new DeserializeException(e);
                }
            }
            throw new NotBaseException(getRegType());
        }

        public Node serialize(@Nonnull Type type, Enum<?> value, @Nonnull Options options) {
            return new NodeBase(options, value.name(), false);
        }

        @Nonnull
        public Type getRegType() {
            return new TypeToken<Enum<?>>() {
            }.getType();
        }
    }
}
