package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 基础结点类.
 * 此类会把对象以字符串的形式存储.
 *
 * @author Himmelt
 */
public class NodeBase extends AbstractNode<String> implements Node, Serializable, Comparable<NodeBase>, CharSequence {

    private static final Field FIELD_VALUE;
    private static final long serialVersionUID = 511187959363727820L;

    static {
        Field field = null;
        try {
            field = AbstractNode.class.getDeclaredField("value");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        FIELD_VALUE = field;
    }

    public NodeBase(@NotNull NodeBase origin) {
        super(origin.options, origin.value, origin.comments);
    }

    /**
     * 实例化一个新的基础结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     */
    public NodeBase(@NotNull Options options, @NotNull String value) {
        super(options, value);
    }

    /**
     * 实例化一个新的基础结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     * @param comment 注释
     */
    public NodeBase(@NotNull Options options, @NotNull String value, String comment) {
        super(options, value, comment);
    }

    public NodeBase(@NotNull Options options, @NotNull String value, List<String> comments) {
        super(options, value, comments);
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
    public void translate(byte cfg) {
        try {
            FIELD_VALUE.set(this, options.translate(cfg, value));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取封装对象的字符串形式.
     *
     * @return 字符串
     */
    public @NotNull String getString() {
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
        return "true".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value)
                || "1".equalsIgnoreCase(value)
                || "t".equalsIgnoreCase(value)
                || "y".equalsIgnoreCase(value);
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
    public @NotNull String toString() {
        return value;
    }

    @Override
    public NodeBase copy() {
        return new NodeBase(this);
    }

    @Override
    public final byte getType() {
        return TYPE_BASE;
    }
}
