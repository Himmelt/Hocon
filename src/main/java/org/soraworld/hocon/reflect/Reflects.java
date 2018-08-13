package org.soraworld.hocon.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Reflects {

    private static final HashMap<Class<?>, List<Field>> CLAZZ_FIELDS = new HashMap<>();
    private static final HashMap<Class<? extends Enum<?>>, HashMap<String, Enum<?>>> ENUM_FIELDS = new HashMap<>();

    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
        if (CLAZZ_FIELDS.containsKey(clazz)) return CLAZZ_FIELDS.get(clazz);
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        fields.addAll(0, getFields(clazz.getSuperclass()));
        fields.forEach(field -> field.setAccessible(true));
        CLAZZ_FIELDS.put(clazz, fields);
        return fields;
    }

    public static <T extends Enum<T>> T getEnums(Class<T> clazz, String name) {
        if (clazz == null) return null;
        HashMap<String, Enum<?>> fields = ENUM_FIELDS.get(clazz);
        if (fields == null) {
            fields = new HashMap<>();
            Enum[] vals = clazz.getEnumConstants();
            if (vals != null) {
                for (Enum field : vals) {
                    fields.put(field.name(), field);
                }
            }
            ENUM_FIELDS.put(clazz, fields);
        }
        return (T) fields.get(name);
    }

    public static Class<?> getRawType(Type type) throws NonRawTypeException {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) return (Class<?>) ((ParameterizedType) type).getRawType();
        else throw new NonRawTypeException(type);
    }

    public static Type[] getMapParameter(ParameterizedType type) throws NonMapParamException {
        if (Map.class.isAssignableFrom((Class<?>) type.getRawType())) {
            Type[] types = type.getActualTypeArguments();
            if (types.length == 2) return types;
        }
        throw new NonMapParamException();
    }

    public static Type getListParameter(ParameterizedType type) throws NonListParamException {
        if (Collection.class.isAssignableFrom((Class<?>) type.getRawType())) {
            Type[] types = type.getActualTypeArguments();
            if (types.length == 1) return types[0];
        }
        throw new NonListParamException();
    }

}
