package org.soraworld.hocon.node;

import org.junit.Test;

import java.io.File;

public class NodeTest {

    private Options options = Options.build();
    private FileNode fileNode = new FileNode(new File("build/test.conf"), options);

    @Test
    public void readValue() {

        try {
            fileNode.load(false, true);
            System.out.println(fileNode.heads);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void toType() {
    }

    @Test
    public void toType1() {
    }
}
