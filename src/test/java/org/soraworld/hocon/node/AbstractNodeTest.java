package org.soraworld.hocon.node;

import org.junit.Test;

public class AbstractNodeTest {

    @Test
    public void quotation() {
        System.out.println("&% - " + AbstractNode.quotation("789 23232 -[---123 3231"));
    }
}