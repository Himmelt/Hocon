package org.soraworld.hocon.node;

import org.junit.Test;

public class NodeMapTest {

    public static class TestClazz {
        @Setting
        private static String shiki = "Shiki Test !";
        @Setting
        private static int inttttt = 56785;
    }

    @Test
    public void modify() {
        NodeMap map = new NodeMap(Options.defaults());
        map.extract(TestClazz.class);
        System.out.println(map.asStringMap());
        map.set("shiki", "This is after modify !");
        map.set("inttttt", 677778);
        map.modify(TestClazz.class);
        System.out.println("shiki:" + TestClazz.shiki);
        System.out.println("inttttt:" + TestClazz.inttttt);
    }

    @Test
    public void extract() {
    }
}