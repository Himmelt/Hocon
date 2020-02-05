package org.soraworld.hocon.node;

import org.junit.Test;

public class PathsTest {

    @Test
    public void testToString() {
        Paths paths = new Paths("abc", "def", "ghi", "jkl");
        System.out.println(paths);
        paths = new Paths("abc.234.567.hjk");
        System.out.println(paths);
    }
}