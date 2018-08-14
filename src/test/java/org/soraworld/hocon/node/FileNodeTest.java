package org.soraworld.hocon.node;

import org.junit.Test;
import org.soraworld.hocon.reflect.SubEnum;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileNodeTest {

    private Settings settings = new Settings();

    private FileNode fileNode = new FileNode(new File("build/test.conf"));

    {
        settings.abc = 123;
        settings.string = "String Test";
        settings.maps = new HashMap<>();
        settings.maps.put("key1", 234);
        settings.maps.put("key2", 7899);
        settings.maps.put("key3", 88970);
        settings.set = new LinkedHashSet<>();
        settings.set.add(Arrays.asList(1, 2, 3, 4, 5, 6));
        settings.set.add(new ArrayList<>(Arrays.asList(4, 5, 6, 7, 9)));
        settings.set.add(new LinkedList<>());
        settings.subEnum = SubEnum.SUB_ENUM_2;
    }

    @Test
    public void save() {
        try {
            fileNode.extract(settings);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            fileNode.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void load() {
        try {
            fileNode.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Settings settings = new Settings();

        settings.maps = new TreeMap<>();

        try {
            fileNode.modify(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fileNode.modify(this.settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}