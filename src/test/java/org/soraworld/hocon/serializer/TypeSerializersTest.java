package org.soraworld.hocon.serializer;

import org.junit.Test;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Options;

public class TypeSerializersTest {
    @Test
    public void registerType() {
        Options options = Options.build();
        try {
            options.registerType(new TestClazz.SA());
            options.registerType(new TestClazz.SB());
            options.registerType(new TestClazz.SC());
            options.registerType(new TestClazz.SD());
            System.out.println(options.getSerializer(TestClazz.D.class));
            System.out.println(options.getSerializer(TestClazz.C.class));
            System.out.println(options.getSerializer(TestClazz.B.class));
            System.out.println(options.getSerializer(TestClazz.A.class));
        } catch (SerializerException e) {
            e.printStackTrace();
        }
    }
}