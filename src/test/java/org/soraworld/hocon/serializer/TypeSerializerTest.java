package org.soraworld.hocon.serializer;

import org.junit.Test;
import org.soraworld.hocon.exception.SerializerException;

import java.util.TreeSet;

public class TypeSerializerTest {

    @Test
    public void compareTo() {
        TreeSet<TypeSerializer> serializers = new TreeSet<>();
        try {
            serializers.add(new TestClazz.SC());
            serializers.add(new TestClazz.SB());
            serializers.add(new TestClazz.SD());
            serializers.add(new TestClazz.SA());
            System.out.println(serializers);
            System.out.println(serializers.first());
        } catch (SerializerException e) {
            e.printStackTrace();
        }
    }

}