package org.soraworld.hocon.node;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * 基础结点类.
 * 此类会把对象以字符串的形式存储.
 */
public class NodeBase extends AbstractNode<String> implements Node, java.io.Serializable, Comparable<NodeBase>, CharSequence {

    private static final long serialVersionUID = 511187959363727820L;

    public NodeBase(@Nonnull Object obj) {
        super(Options.defaults(), String.valueOf(obj));
    }

    /**
     * 实例化一个新的基础结点.
     * 如果 parse 为真，则会按照文本文件内容解析，即把双引号去掉，用在从文本读取解析时.
     * 如果 parse 为假，则会直接存储 obj 的字符串形式，用在创建新实例时.
     *
     * @param options 配置选项
     * @param obj     封装对象
     */
    public NodeBase(@Nonnull Options options, @Nonnull Object obj) {
        super(options, obj.toString());
    }

    /**
     * 实例化一个新的基础结点.
     * 如果 parse 为真，则会按照文本文件内容解析，即把双引号去掉，用在从文本读取解析时.
     * 如果 parse 为假，则会直接存储 obj 的字符串形式，用在创建新实例时.
     *
     * @param options 配置选项
     * @param obj     封装对象
     * @param parse   是否解析
     */
    public NodeBase(@Nonnull Options options, @Nonnull Object obj, boolean parse) {
        super(options, parse && obj instanceof String ? unquotation((String) obj) : obj.toString());
    }

    /**
     * 实例化一个新的基础结点.
     *
     * @param options 配置选项
     * @param obj     封装对象
     * @param parse   是否解析
     * @param comment 注释
     */
    public NodeBase(@Nonnull Options options, @Nonnull Object obj, boolean parse, String comment) {
        super(options, obj == null ? null : parse && obj instanceof String ? parse((String) obj) : obj.toString(), comment);
    }

    public boolean notEmpty() {
        return value != null;
    }

    public void readValue(BufferedReader reader, boolean keepComments) {
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws Exception {
        if (value == null) writer.write("null");
        else writer.write(quotation(value));
    }

    /**
     * 获取封装对象的字符串形式.
     *
     * @return 字符串
     */
    @Nonnull
    public String getString() {
        return value;
    }

    /**
     * 获取封装对象的整数形式.
     *
     * @return 整数
     */
    public int getInt() {
        return Integer.valueOf(value);
    }

    /**
     * 获取封装对象的长整数形式.
     *
     * @return 长整数
     */
    public long getLong() {
        return Long.valueOf(value);
    }

    /**
     * 获取封装对象的浮点数形式.
     *
     * @return 浮点数
     */
    public float getFloat() {
        return Float.valueOf(value);
    }

    /**
     * 获取封装对象的双精度小数形式.
     *
     * @return 双精度小数
     */
    public double getDouble() {
        return Double.valueOf(value);
    }

    /**
     * 获取封装对象的逻辑值形式.
     * true yes 1 t y
     *
     * @return 逻辑值
     */
    public Boolean getBoolean() {
        if (value != null) {
            return value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("yes")
                    || value.equalsIgnoreCase("1")
                    || value.equalsIgnoreCase("t")
                    || value.equalsIgnoreCase("y");
        } else return null;
    }

    private static String parse(String text) {
        if (text.equals("null")) return null;
        return unquotation(text);
    }

    public int length() {
        return value == null ? 0 : value.length();
    }

    public char charAt(int index) {
        return value == null ? 0 : value.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return value == null ? "" : value.subSequence(start, end);
    }

    public int compareTo(NodeBase o) {
        return value == null || o == null || o.value == null ? 0 : value.compareTo(o.value);
    }

    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof NodeBase) {
            if (value == null && ((NodeBase) obj).value == null) return true;
            if (value != null && ((NodeBase) obj).value != null) return value.equals(((NodeBase) obj).value);
        }
        return false;
    }

    public String toString() {
        return value == null ? "[null]" : value;
    }
}
