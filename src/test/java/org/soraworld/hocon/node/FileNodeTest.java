package org.soraworld.hocon.node;

import org.junit.Test;
import org.soraworld.hocon.reflect.SubEnum;

import java.io.File;
import java.util.*;

public class FileNodeTest {

    private Settings settings = new Settings();
    private Options options = Options.build();
    private FileNode fileNode = new FileNode(new File("build/test.conf"), options);

    {
        settings.abc = 123;
        settings.string = "Str#ing & \" :Test";
        settings.maps = new HashMap<>();
        settings.maps.put("ke:y&1", 234);
        settings.maps.put("ke\"y2", 7899);
        settings.maps.put("key3 ", 88970);
        settings.maps.put(" key4", 88970);
        settings.set = new LinkedHashSet<>();
        settings.set.add(Arrays.asList(1, 2, 3, 4, 5, 6));
        settings.set.add(new ArrayList<>(Arrays.asList(4, 5, 6, 7, 9)));
        settings.set.add(new LinkedList<>());
        settings.subEnum = SubEnum.SUB_ENUM_2;
        options.setIndent(0);
        options.setTranslator(s -> s.replace('.', '-'));
    }

    @Test
    public void save() {
        fileNode.extract(settings);
        try {
            fileNode.clearHeads();
            fileNode.addHead("             Head Test Line 1");
            fileNode.addHead("             Head Test Line 2");
            fileNode.addHead("             Head Test Line 3");
            fileNode.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadSave() {
        try {
            fileNode.load(false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fileNode.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void load() {
        try {
            fileNode.set("xxx", 123);
            fileNode.load(true);
        } catch (Exception e) {
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
