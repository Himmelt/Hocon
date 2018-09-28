package org.soraworld.hocon.node;

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
 */
public abstract class AbstractNode<T> implements Node {

    /**
     * 封装的值.
     */
    protected final T value;
    /**
     * 多行注释.
     */
    protected List<String> comments;
    /**
     * 配置选项.
     */
    protected final Options options;
    /**
     * 非法字符的正则表达式，匹配该正则时需要对字符串加双引号.
     */
    protected static final Pattern ILLEGAL = Pattern.compile(".*[\":=,+?`!@#$^&*{}\\[\\]\\\\].*");

    /**
     * 初始化一个新结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     */
    protected AbstractNode(Options options, T value) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
    }

    /**
     * 初始化一个新结点.
     *
     * @param options 配置选项
     * @param value   封装对象
     * @param comment 注释
     */
    protected AbstractNode(Options options, T value, String comment) {
        this.options = options != null ? options : Options.defaults();
        this.value = value;
        addComment(comment);
    }

    public boolean checkCycle(Node node) {
        if (this.equals(node)) return false;
        if (node instanceof NodeMap) {
            for (Node sub : ((NodeMap) node).value.values()) {
                if (!checkCycle(sub)) return false;
            }
            return true;
        } else if (node instanceof NodeList) {
            for (Node sub : ((NodeList) node).value) {
                if (!checkCycle(sub)) return false;
            }
            return true;
        } else return true;
    }

    public final void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            if (comments == null) comments = new ArrayList<>();
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public final void setComments(List<String> comments) {
        if (comments != null && !comments.isEmpty()) {
            this.comments = new ArrayList<>();
            comments.forEach(s -> this.comments.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.comments.removeIf(String::isEmpty);
        } else this.comments = null;
    }

    public final void writeComment(int indent, BufferedWriter writer) throws IOException {
        if (comments != null && !comments.isEmpty()) {
            for (String comment : comments) {
                writeIndent(indent, writer);
                writer.write("# " + comment);
                writer.newLine();
            }
        }
    }

    public final Options options() {
        return options;
    }

    /**
     * 尝试给字符串加双引号.
     * 如果 封装内容 是字符串"null"，或以空格开头或以空格结尾，或含有非法字符，
     * 则在写入文本文件时会在两端添加双引号.
     * 同时对转义字符 '\' 和 '"' 进行转义.
     *
     * @param text 文本内容
     * @return 处理后的文本
     */
    public static String quotation(String text) {
        if (text.equals("null") || text.startsWith(" ") || text.endsWith(" ") || ILLEGAL.matcher(text).matches()) {
            String target = text
                    .replace("\\", "\\\\")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\"", "\\\"");
            return '"' + target + '"';
        }
        return text;
    }

    /**
     * 给字符串去双引号.
     * 并反向操作转义字符.
     *
     * @param text 文本内容
     * @return 处理后的文本
     */
    public static String unquotation(String text) {
        if (text.startsWith("\"")) text = text.substring(1);
        if (text.endsWith("\"")) text = text.substring(0, text.length() - 1);
        return text
                .replace("\\b", "\b")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public void writeIndent(int indent, BufferedWriter writer) throws IOException {
        indent *= options.getIndent();
        while (indent-- > 0) writer.write(' ');
    }
}
