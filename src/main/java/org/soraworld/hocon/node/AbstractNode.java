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
 */
abstract class AbstractNode<T> implements Node {

    /**
     * 封装的值.
     */
    @NotNull
    protected final T value;
    /**
     * 多行注释.
     */
    protected List<String> comments;
    /**
     * 配置选项.
     */
    @NotNull
    protected final Options options;
    /**
     * 非法字符的正则表达式，匹配该正则时需要对字符串加双引号.
     */
    protected static final Pattern ILLEGAL = Pattern.compile(".*[\":=,+?`!@#$^&*{}\\[\\]\\\\].*");

    protected static final Pattern CLZ_COMMENT = Pattern.compile("<class>.+</class>");

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

    /**
     * 检查循环引用.
     *
     * @param node 被检查 node
     * @return 如果不存在循环引用则返回 true，否则返回 false
     */
    protected boolean checkCycle(Node node) {
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

    public List<String> getComments() {
        return comments;
    }

    public final void addComment(@NotNull String comment) {
        if (!comment.isEmpty()) {
            if (comments == null) comments = new ArrayList<>();
            comments.addAll(Arrays.asList(comment.split("[\n\r]")));
            comments.removeIf(String::isEmpty);
        }
    }

    public void setComment(@NotNull String comment) {
        comments.clear();
        comments.add(comment);
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

    public void setTypeToComment(@NotNull Class<?> clazz) {
        if (comments == null) comments = new ArrayList<>();
        comments.removeIf(text -> CLZ_COMMENT.matcher(text).matches());
        comments.add("<class>" + clazz.getName() + "</class>");
    }

    public Class<?> getTypeFromComment() {
        if (comments != null) {
            String comment = comments.stream().filter(text -> CLZ_COMMENT.matcher(text).matches()).findAny().orElse("");
            try {
                return Class.forName(comment.replaceAll("<class>", "").replace("</class>", ""));
            } catch (ClassNotFoundException e) {
                if (options.isDebug()) e.printStackTrace();
            }
        }
        return null;
    }

    @NotNull
    public final Options options() {
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
        if (text.isEmpty()) return "\"\"";
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
