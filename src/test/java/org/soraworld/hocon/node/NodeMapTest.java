package org.soraworld.hocon.node;

import org.junit.Test;

import java.io.File;

public class NodeMapTest {
    private Settings settings = new Settings();
    private Options options = Options.build();
    private FileNode fileNode = new FileNode(new File("build/test.conf"), options);

    @Test
    public void modify() {
        options.setDebug(true);
        try {
            fileNode.load(true);
            fileNode.modify(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
