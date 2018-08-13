package org.soraworld.hocon.node;

public final class Paths {

    private final String[] paths;
    private final int length;
    private int current;

    public Paths(String... paths) {
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                paths[i] = paths[i].replace('.', '_');
            }
        }
        this.paths = paths;
        this.length = this.paths == null ? 0 : this.paths.length;
        this.current = 0;
    }

    public Paths(String path) {
        this(path == null ? null : path.split("."));
    }

    public boolean empty() {
        return current >= length;
    }

    public boolean notEmpty() {
        return current < length;
    }

    public Paths next() {
        if (current < length) current++;
        return this;
    }

    public Paths revert() {
        if (current > 0) current--;
        return this;
    }

    public int size() {
        return length - current;
    }

    public String first() {
        if (current >= length) return "";
        return paths[current];
    }

    public String get(int index) {
        if (current + index >= length) return "";
        return paths[current + index];
    }

}
