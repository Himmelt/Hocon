package org.soraworld.hocon;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fields {

    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        Class<?> parent = clazz.getSuperclass();
        if (parent != null) fields.addAll(0, getFields(parent));
        return fields;
    }

}
