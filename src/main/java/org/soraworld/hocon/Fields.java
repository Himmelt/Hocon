package org.soraworld.hocon;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Fields {

    private static final HashMap<Class<?>, List<Field>> caches = new HashMap<>();

    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
        if (caches.containsKey(clazz)) return caches.get(clazz);
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        fields.addAll(0, getFields(clazz.getSuperclass()));
        fields.forEach(field -> field.setAccessible(true));
        caches.put(clazz, fields);
        return fields;
    }

}
