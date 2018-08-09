package org.soraworld.hocon;

import com.google.common.reflect.TypeToken;

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

    public static final HashMap<TypeToken<?>, TypeSerializer<?>> DEFAULT_SERIALIZERS = new HashMap<>();

    static {
        DEFAULT_SERIALIZERS.put(TypeToken.of(URI.class), new URISerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(URL.class), new URLSerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(UUID.class), new UUIDSerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(Number.class), new NumberSerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(String.class), new StringSerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(Boolean.class), new BooleanSerializer());
        DEFAULT_SERIALIZERS.put(TypeToken.of(Pattern.class), new PatternSerializer());
        DEFAULT_SERIALIZERS.put(new TypeToken<Map<?, ?>>() {
        }, new MapSerializer());
        DEFAULT_SERIALIZERS.put(new TypeToken<List<?>>() {
        }, new ListSerializer());
        DEFAULT_SERIALIZERS.put(new TypeToken<Enum<?>>() {
        }, new EnumSerializer());
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
        public Node serialize(@Nonnull TypeToken<?> type, URI uri) {
            return new NodeBase(uri);
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
        public Node serialize(@Nonnull TypeToken<?> type, URL url) {
            return new NodeBase(url);
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
        public Node serialize(@Nonnull TypeToken<?> type, UUID uuid) {
            return new NodeBase(uuid);
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
        public Node serialize(@Nonnull TypeToken<?> type, Number value) {
            return new NodeBase(value);
        }
    }

    private static class StringSerializer implements TypeSerializer<String> {
        @Override
        public String deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getString();
            else return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, String value) {
            return new NodeBase(value);
        }
    }

    private static class BooleanSerializer implements TypeSerializer<Boolean> {
        @Override
        public Boolean deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) {
            if (node instanceof NodeBase) return ((NodeBase) node).getBoolean();
            return null;
        }

        @Override
        public Node serialize(@Nonnull TypeToken<?> type, Boolean value) {
            return new NodeBase(value);
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
        public Node serialize(@Nonnull TypeToken<?> type, Pattern pattern) {
            return new NodeBase(pattern.pattern());
        }
    }

    private static class MapSerializer implements TypeSerializer<Map<String, ?>> {
        public Map<String, ?> deserialize(@Nonnull TypeToken<?> type, @Nonnull Node node) throws Exception {
            if (node instanceof NodeMap) {
                Map<String, Object> returnVal = new LinkedHashMap<>();
                if (!(type.getType() instanceof ParameterizedType)) {
                    throw new ObjectMappingException("Raw types are not supported for collections");
                }
                TypeToken<?> keyToken = type.resolveType(Map.class.getTypeParameters()[0]);

                // TODO
                //if(!keyToken.getRawType().equals(String.class))return null;

                TypeToken<?> valToken = type.resolveType(Map.class.getTypeParameters()[1]);
                TypeSerializer<?> valSerial = DEFAULT_SERIALIZERS.get(valToken);

                if (valSerial == null) {
                    throw new ObjectMappingException("No type serializer available for type " + valToken);
                }

                for (Map.Entry<String, Node> entry : ((NodeMap) node).getValue().entrySet()) {
                    String key = entry.getKey();
                    Object val = valSerial.deserialize(valToken, entry.getValue());
                    if (key == null || key.isEmpty() || val == null) continue;
                    returnVal.put(key, val);
                }
                return returnVal;
            }
            return null;
        }

        public Node serialize(@Nonnull TypeToken<?> type, Map<String, ?> value) throws ObjectMappingException {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> keyToken = type.resolveType(Map.class.getTypeParameters()[0]);
            TypeToken<?> valToken = type.resolveType(Map.class.getTypeParameters()[1]);
            TypeSerializer keySerial = DEFAULT_SERIALIZERS.get(keyToken);
            TypeSerializer valSerial = DEFAULT_SERIALIZERS.get(valToken);

            if (keySerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + keyToken);
            }

            if (valSerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + valToken);
            }

            NodeMap node = new NodeMap();

            for (Map.Entry<String, ?> entry : value.entrySet()) {
                String key = entry.getKey();
                Object obj = entry.getValue();
                if (key != null && !key.isEmpty() && obj != null) {
                    node.setNode(key, valSerial.serialize(valToken, obj));
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
                TypeToken<?> entryToken = type.resolveType(List.class.getTypeParameters()[0]);
                TypeSerializer entrySerial = DEFAULT_SERIALIZERS.get(entryToken);
                if (entrySerial == null) {
                    throw new ObjectMappingException("No applicable type serializer for type " + entryToken);
                }

                ArrayList<Object> list = new ArrayList<>();

                for (Node element : ((NodeList) node).getValue()) {
                    list.add(entrySerial.deserialize(entryToken, element));
                }
                return list;

/*                if (value.hasListChildren()) {

                } else {
                    Object unwrappedVal = value.getValue();
                    if (unwrappedVal != null) {
                        return Lists.newArrayList(entrySerial.deserialize(entryType, value));
                    }
                }*/
            }

            return new ArrayList<>();
        }

        public Node serialize(@Nonnull TypeToken<?> type, List<?> value) throws ObjectMappingException {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> entryToken = type.resolveType(List.class.getTypeParameters()[0]);
            TypeSerializer entrySerial = DEFAULT_SERIALIZERS.get(entryToken);
            if (entrySerial == null) {
                throw new ObjectMappingException("No applicable type serializer for type " + entryToken);
            }
            NodeList node = new NodeList();
            for (Object obj : value) {
                node.add(entrySerial.serialize(entryToken, obj));
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
                Enum<?> value = Fields.getEnums(type.getRawType().asSubclass(Enum.class), name);
                if (value != null) return value;
                else throw new Exception();
            }
            return null;
        }

        public Node serialize(@Nonnull TypeToken<?> type, Enum<?> value) {
            return new NodeBase(value.name());
        }
    }

}
