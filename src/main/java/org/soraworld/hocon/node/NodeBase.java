package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.List;

/**
 * 基础结点类.
 * 此类会把对象以字符串的形式存储.
 *
 * @author Himmelt
 */
public class NodeBase extends AbstractNode<String> implements Node, Serializable, Comparable<NodeBase>, CharSequence {

    private static final long serialVersionUID = 511187959363727820L;

    public NodeBase(@NotNull Object obj) {
        super(Options.defaults(), String.valueOf(obj));
    }

    /**
     * 实例化一个新的基础结点.
     *
     * @param options 配置选项
     * @param obj     封装对象
     */
    public NodeBase(@NotNull Options options, @NotNull Object obj) {
        super(options, obj.toString());
    }

    /**
     * 实例化一个新的基础结点.
     *
     * @param options 配置选项
     * @param obj     封装对象
     * @param comment 注释
     */
    public NodeBase(@NotNull Options options, @NotNull Object obj, String comment) {
        super(options, obj.toString(), comment);
    }

    public NodeBase(@NotNull Options options, @NotNull Object obj, List<String> comments) {
        super(options, obj.toString(), comments);
    }

    @Override
    public boolean notEmpty() {
        return true;
    }

    @Override
    public void readValue(BufferedReader reader, boolean keepComments) {
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws Exception {
        writer.write(quotation(value));
    }

    @Override
    public @NotNull NodeBase translate(byte cfg) {
        return new NodeBase(options, options.translate(cfg, value), comments);
    }

    /**
     * 获取封装对象的字符串形式.
     *
     * @return 字符串
     */
    @NotNull
    public String getString() {
        return value;
    }

    /**
     * 获取封装对象的整数形式.
     *
     * @return 整数
     */
    public int getInt() {
        return Integer.parseInt(value);
    }

    /**
     * 获取封装对象的长整数形式.
     *
     * @return 长整数
     */
    public long getLong() {
        return Long.parseLong(value);
    }

    /**
     * 获取封装对象的浮点数形式.
     *
     * @return 浮点数
     */
    public float getFloat() {
        return Float.parseFloat(value);
    }

    /**
     * 获取封装对象的双精度小数形式.
     *
     * @return 双精度小数
     */
    public double getDouble() {
        return Double.parseDouble(value);
    }

    /**
     * 获取封装对象的逻辑值形式.
     * true yes 1 t y
     *
     * @return 逻辑值
     */
    public Boolean getBoolean() {
        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("t")
                || value.equalsIgnoreCase("y");
    }

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

    @Override
    public int compareTo(@NotNull NodeBase o) {
        return value.compareTo(o.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NodeBase && value.equals(((NodeBase) obj).value);
    }

    @Override
    @NotNull
    public String toString() {
        return value;
    }
}
