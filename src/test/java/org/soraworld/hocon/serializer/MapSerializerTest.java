package org.soraworld.hocon.serializer;

import org.junit.Test;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.node.NodeMap;
import org.soraworld.hocon.node.Options;

import java.util.HashMap;

public class MapSerializerTest {

    @Test
    public void serialize() {
        try {
            MapSerializer serializer = new MapSerializer();
            StringIntHashMap map = new StringIntHashMap();
            map.put("123", 123);
            map.put("456", 456);
            NodeMap node = serializer.serialize(StringIntHashMap.class, map, Options.build());
            System.out.println(node);
        } catch (HoconException e) {
            e.printStackTrace();
        }
    }

    class StringIntHashMap extends HashMap<String, Integer> {

    }
}