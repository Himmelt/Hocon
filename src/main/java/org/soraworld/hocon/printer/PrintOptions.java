package org.soraworld.hocon.printer;

import org.soraworld.hocon.text.Newline;

/**
 * 打印策略.
 *
 * <p>"要不要把注释写出来"是<strong>打印期</strong>的决定，解析期没有这个开关——
 * 注释永远在文档里，只是可以选择不输出. 这样解析只有一条代码路径.</p>
 *
 * @author Himmelt
 */
public final class PrintOptions {

    private int indent = 0;
    private Newline newline;
    private boolean emitComments = true;
    private boolean emitBlankLines = true;
    private boolean inlineShortArrays = false;
    private int inlineWidth = 80;

    /**
     * 获取默认策略.
     *
     * @return 默认策略（缩进 2、沿用文档换行风格、输出注释与空行）
     */
    public static PrintOptions defaults() {
        return new PrintOptions();
    }

    /**
     * 获取缩进宽度.
     *
     * @return 缩进宽度
     */
    public int indent() {
        return indent;
    }

    /**
     * 设置缩进宽度（0 表示沿用文档里读到的宽度）.
     *
     * @param indent 缩进宽度
     * @return 本策略
     */
    public PrintOptions indent(int indent) {
        this.indent = Math.max(0, indent);
        return this;
    }

    /**
     * 获取换行风格.
     *
     * @return 换行风格，null 表示沿用文档的
     */
    public Newline newline() {
        return newline;
    }

    /**
     * 设置换行风格.
     *
     * @param newline 换行风格，null 表示沿用文档的
     * @return 本策略
     */
    public PrintOptions newline(Newline newline) {
        this.newline = newline;
        return this;
    }

    /**
     * 是否输出注释.
     *
     * @return 输出返回 true
     */
    public boolean emitComments() {
        return emitComments;
    }

    /**
     * 设置是否输出注释.
     *
     * @param emitComments 是否输出
     * @return 本策略
     */
    public PrintOptions emitComments(boolean emitComments) {
        this.emitComments = emitComments;
        return this;
    }

    /**
     * 是否输出空行.
     *
     * @return 输出返回 true
     */
    public boolean emitBlankLines() {
        return emitBlankLines;
    }

    /**
     * 设置是否输出空行.
     *
     * @param emitBlankLines 是否输出
     * @return 本策略
     */
    public PrintOptions emitBlankLines(boolean emitBlankLines) {
        this.emitBlankLines = emitBlankLines;
        return this;
    }

    /**
     * 是否把较短的数组/对象压成一行.
     * 无论此开关如何，"原本就写在一行"的内容始终按一行输出（保真优先）.
     *
     * @return 压行返回 true
     */
    public boolean inlineShortArrays() {
        return inlineShortArrays;
    }

    /**
     * 设置是否把较短的数组/对象压成一行.
     *
     * @param inlineShortArrays 是否压行
     * @return 本策略
     */
    public PrintOptions inlineShortArrays(boolean inlineShortArrays) {
        this.inlineShortArrays = inlineShortArrays;
        return this;
    }

    /**
     * 获取压行的宽度上限.
     *
     * @return 宽度上限
     */
    public int inlineWidth() {
        return inlineWidth;
    }

    /**
     * 设置压行的宽度上限.
     *
     * @param inlineWidth 宽度上限
     * @return 本策略
     */
    public PrintOptions inlineWidth(int inlineWidth) {
        this.inlineWidth = Math.max(8, inlineWidth);
        return this;
    }
}
