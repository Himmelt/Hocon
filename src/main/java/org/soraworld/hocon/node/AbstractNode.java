package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 抽象结点类.
 *
 * @param <T> 封装内容的参数类型
 * @author Himmelt
 */
abstract class AbstractNode<T> implements Node {

    /**
     * 封装的值.
     */
    protected final @NotNull T value;
    /**
     * 多行注释.
     */
    protected List<String> comments;
    /**
     * 配置选项.
     */
    protected final @NotNull Options options;
    /**
     * 非法字符的正则表达式，匹配该正则时需要对字符串加双引号.
     */
    protected static final Pattern ILLEGAL = Pattern.compile(".*[\":=,+?`!@#$^&*{}\\[\\]\\\\].*");
    protected static final byte TYPE_BASE = 0, TYPE_LIST = 1, TYPE_MAP = 2;

    /**
     * 初始化一个新结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     */
    protected AbstractNode(@NotNull Options options, @NotNull T value) {
        this.options = options;
        this.value = value;
    }

    /**
     * 初始化一个新结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     * @param comment 注释
     */
    protected AbstractNode(@NotNull Options options, @NotNull T value, @NotNull String comment) {
        this.options = options;
        this.value = value;
        addComment(comment);
    }

    protected AbstractNode(@NotNull Options options, @NotNull T value, @NotNull List<String> comments) {
        this.options = options;
        this.value = value;
        setComments(comments);
    }

    @Override
    public List<String> getComments() {
        return comments;
    }

    @Override
    public final void addComment(@NotNull String comment) {
        if (!comment.isEmpty()) {
            if (comments == null) {
                comments = new ArrayList<>();
            }
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    @Override
    public void setComment(@NotNull String comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        } else {
            comments.clear();
        }
        comments.add(comment);
    }

    @Override
    public final void setComments(List<String> comments) {
        if (comments != null && !comments.isEmpty()) {
            this.comments = new ArrayList<>();
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        } else {
            this.comments = null;
        }
    }

    @Override
    public final void writeComment(int indent, BufferedWriter writer) throws IOException {
        if (comments != null && !comments.isEmpty()) {
            for (String comment : comments) {
                writeIndent(indent, writer);
                writer.write("# " + comment);
                writer.newLine();
            }
        }
    }

    @Override
    public final @NotNull Options options() {
        return options;
    }

    /**
     * 尝试给字符串加双引号.<br>
     * 如果 封装内容 是字符串"null"，或以空格开头或以空格结尾，或含有非法字符,<br>
     * 则在写入文本文件时会在两端添加双引号.<br>
     * 同时对转义字符进行转义.
     *
     * @param text 文本内容
     * @return 处理后的文本
     */
    public static String quotation(@NotNull String text) {
        if (text.startsWith(" ") || text.endsWith(" ") || ILLEGAL.matcher(text).matches()) {
            String target = text
                    .replace("\\", "\\\\")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\"", "\\\"");
            return '"' + target + '"';
        }
        if (text.isEmpty()) {
            return "\"\"";
        }
        return text;
    }

    /**
     * 给字符串去双引号.<br>
     * 并反向操作转义字符.
     *
     * @param text 文本内容
     * @return 处理后的文本
     */
    public static String unquotation(@NotNull String text) {
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        return text
                .replace("\\b", "\b")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    @Override
    public void writeIndent(int indent, BufferedWriter writer) throws IOException {
        indent *= options.getIndent();
        while (indent-- > 0) {
            writer.write(' ');
        }
    }

    @Override
    public abstract AbstractNode<T> copy();
}
