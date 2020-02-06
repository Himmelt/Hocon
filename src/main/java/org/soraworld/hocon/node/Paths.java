package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

/**
 * 路径树, 以 . 分隔
 *
 * @author Himmelt
 */
public final class Paths {

    private int current;
    private final int length;
    private final String[] paths;

    private Paths(@NotNull Paths origin) {
        this.paths = origin.paths;
        this.length = origin.length;
        this.current = 0;
    }

    /**
     * 实例化路径树.
     * 以 . 分隔, 路径段内的 . 会被替换成下划线 '_'
     *
     * @param paths 路径树
     */
    public Paths(String... paths) {
        for (int i = 0; i < paths.length; i++) {
            paths[i] = paths[i].replace('.', '_');
        }
        this.paths = paths;
        this.length = this.paths.length;
        this.current = 0;
    }

    /**
     * 实例化路径树.
     * 以 . 分隔, 路径段内的 . 会被替换成下划线 '_'
     *
     * @param path 路径树字符串
     */
    public Paths(String path) {
        this(path == null ? new String[]{} : path.split("\\."));
    }

    /**
     * 当前位置是否为空.
     *
     * @return 是否为空
     */
    public boolean empty() {
        return current >= length;
    }

    /**
     * 当前位置是否非空.
     *
     * @return 是否非空
     */
    public boolean notEmpty() {
        return current < length;
    }

    /**
     * 是否还有下一个路径段.
     *
     * @return 是否还有下一个
     */
    public boolean hasNext() {
        return current < length - 1;
    }

    /**
     * 游标下移一个.
     *
     * @return 下移后的路径树
     */
    public Paths next() {
        if (current < length) {
            current++;
        }
        return this;
    }

    /**
     * 游标上移一个.
     *
     * @return 上移后的路径树
     */
    public Paths revert() {
        if (current > 0) {
            current--;
        }
        return this;
    }

    /**
     * 获取 当前位置 之后路径段的个数.
     *
     * @return 当前大小
     */
    public int size() {
        return length - current;
    }

    /**
     * 获取 当前位置 的路径段.
     *
     * @return 当前路径段
     */
    public String first() {
        if (current >= length) {
            return "";
        }
        return paths[current];
    }

    /**
     * 获取 当前位置的索引位置 的路径段.
     *
     * @param index 索引位置
     * @return 路径段
     */
    public String get(int index) {
        if (current + index >= length) {
            return "";
        }
        return paths[current + index];
    }

    /**
     * 设置 当前位置的索引位置 的路径段.
     * 索引位置必须在路径树内.
     * 以 . 分隔, . 会被替换成下划线 '_'
     *
     * @param index 索引位置
     * @param value 新值
     */
    public void set(int index, String value) {
        if (current + index < length) {
            paths[current + index] = value.replace('.', '_');
        }
    }

    public final Paths copy() {
        return new Paths(this);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(".");
        for (String path : paths) {
            joiner.add(path);
        }
        return joiner.toString();
    }
}
