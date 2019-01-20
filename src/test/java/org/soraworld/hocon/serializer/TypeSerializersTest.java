package org.soraworld.hocon.serializer;

import org.junit.Test;
import org.soraworld.hocon.TestClazz;
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
            System.out.println(options.getSerializer(TestClazz.CD.class));
            System.out.println(options.getSerializer(TestClazz.CC.class));
            System.out.println(options.getSerializer(TestClazz.IB.class));
            System.out.println(options.getSerializer(TestClazz.IA.class));
        } catch (SerializerException e) {
            e.printStackTrace();
        }
    }
}