package org.soraworld.hocon.text;

import java.io.Serializable;

/**
 * 文本中的位置.
 * 不可变，用于把错误定位到"哪个文件的第几行第几列".
 *
 * @author Himmelt
 */
public final class Position implements Serializable {

    private static final long serialVersionUID = 6415288731204578213L;

    private static final Position UNKNOWN = new Position(null, 0, 0);

    private final String source;
    private final int line;
    private final int column;

    private Position(String source, int line, int column) {
        this.source = source;
        this.line = line;
        this.column = column;
    }

    /**
     * 获取未知位置.
     *
     * @return 未知位置
     */
    public static Position unknown() {
        return UNKNOWN;
    }

    /**
     * 构造一个位置.
     *
     * @param source 来源名（通常是文件路径），可为 null
     * @param line   行号，从 1 开始
     * @param column 列号，从 1 开始
     * @return 位置
     */
    public static Position at(String source, int line, int column) {
        return new Position(source, line, column);
    }

    /**
     * 获取来源名.
     *
     * @return 来源名，可能为 null
     */
    public String source() {
        return source;
    }

    /**
     * 获取行号.
     *
     * @return 行号，从 1 开始；未知时为 0
     */
    public int line() {
        return line;
    }

    /**
     * 获取列号.
     *
     * @return 列号，从 1 开始；未知时为 0
     */
    public int column() {
        return column;
    }

    /**
     * 是否未知位置.
     *
     * @return 未知返回 true
     */
    public boolean isUnknown() {
        return line <= 0;
    }

    /**
     * 输出便于人读的位置描述.
     *
     * @return 形如 {@code conf/a.conf:12:5}、{@code 12:5} 或 {@code <unknown>}
     */
    public String describe() {
        if (isUnknown()) {
            return "<unknown>";
        }
        return (source == null || source.isEmpty() ? "" : source + ":") + line + ":" + column;
    }

    @Override
    public String toString() {
        return describe();
    }
}
