package org.soraworld.hocon.api;

import org.soraworld.hocon.bind.Adapters;
import org.soraworld.hocon.bind.BindingPolicy;
import org.soraworld.hocon.printer.PrintOptions;
import org.soraworld.hocon.text.Newline;

import java.util.function.Function;

/**
 * 配置选项：把打印策略、绑定策略、适配器注册表与本地化翻译器装配在一起.
 *
 * <p>注意：{@link #adapters()} 与 {@link #commentTranslator()} 相关的设置在<strong>首次使用之前</strong>配置，
 * 典型顺序是 {@code Options.build()} → 各项设置 → 交给 {@code Hocon.of(...)}.</p>
 *
 * @author Himmelt
 */
public final class Options {

    private int indent = 0;
    private Newline newline;
    private boolean emitComments = true;
    private boolean emitBlankLines = true;
    private boolean inlineShortArrays = false;
    private int inlineWidth = 80;
    private boolean serializableFallback = false;
    private Function<String, String> commentTranslator;
    private final BindingPolicy policy = BindingPolicy.defaults();

    private Adapters adapters;
    private boolean seal;

    private static final Options DEFAULTS = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
        if (seal) {
            this.adapters = new Adapters(null, false);
        }
    }

    /**
     * 获取默认选项（已封印，不可修改）.
     *
     * @return 默认选项
     */
    public static Options defaults() {
        return DEFAULTS;
    }

    /**
     * 获取新的可修改选项.
     *
     * @return 选项
     */
    public static Options build() {
        return new Options(false);
    }

    /**
     * 封印：之后所有设置方法都失效.
     */
    public void seal() {
        this.seal = true;
    }

    /**
     * 获取缩进宽度.
     *
     * @return 缩进宽度，0 表示沿用文档里读到的宽度
     */
    public int indent() {
        return indent;
    }

    /**
     * 设置缩进宽度.
     *
     * @param indent 缩进宽度
     * @return 本选项
     */
    public Options indent(int indent) {
        if (!seal) {
            this.indent = Math.max(0, indent);
        }
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
     * @param newline 换行风格
     * @return 本选项
     */
    public Options newline(Newline newline) {
        if (!seal) {
            this.newline = newline;
        }
        return this;
    }

    /**
     * 是否写出注释.
     *
     * @return 写出返回 true
     */
    public boolean emitComments() {
        return emitComments;
    }

    /**
     * 设置是否写出注释.
     *
     * @param emitComments 是否写出
     * @return 本选项
     */
    public Options emitComments(boolean emitComments) {
        if (!seal) {
            this.emitComments = emitComments;
        }
        return this;
    }

    /**
     * 是否写出空行.
     *
     * @return 写出返回 true
     */
    public boolean emitBlankLines() {
        return emitBlankLines;
    }

    /**
     * 设置是否写出空行.
     *
     * @param emitBlankLines 是否写出
     * @return 本选项
     */
    public Options emitBlankLines(boolean emitBlankLines) {
        if (!seal) {
            this.emitBlankLines = emitBlankLines;
        }
        return this;
    }

    /**
     * 是否把较短的集合压成一行.
     *
     * @return 压行返回 true
     */
    public boolean inlineShortArrays() {
        return inlineShortArrays;
    }

    /**
     * 设置是否把较短的集合压成一行.
     *
     * @param inlineShortArrays 是否压行
     * @return 本选项
     */
    public Options inlineShortArrays(boolean inlineShortArrays) {
        if (!seal) {
            this.inlineShortArrays = inlineShortArrays;
        }
        return this;
    }

    /**
     * 获取压行宽度上限.
     *
     * @return 宽度上限
     */
    public int inlineWidth() {
        return inlineWidth;
    }

    /**
     * 设置压行宽度上限.
     *
     * @param inlineWidth 宽度上限
     * @return 本选项
     */
    public Options inlineWidth(int inlineWidth) {
        if (!seal) {
            this.inlineWidth = Math.max(8, inlineWidth);
        }
        return this;
    }

    /**
     * 是否启用 {@code Serializable} 兜底适配器.
     * 默认关闭：开启后"任何实现了 Serializable 的类型"都会被当成可绑定的对象，
     * 容易把"没注册适配器"这种错误掩盖成"静默得到默认值".
     *
     * @return 启用返回 true
     */
    public boolean serializableFallback() {
        return serializableFallback;
    }

    /**
     * 设置是否启用 {@code Serializable} 兜底适配器（须在首次使用 {@link #adapters()} 之前）.
     *
     * @param serializableFallback 是否启用
     * @return 本选项
     */
    public Options serializableFallback(boolean serializableFallback) {
        if (!seal) {
            this.serializableFallback = serializableFallback;
        }
        return this;
    }

    /**
     * 获取注释翻译器.
     *
     * @return 翻译器，可能为 null
     */
    public Function<String, String> commentTranslator() {
        return commentTranslator;
    }

    /**
     * 设置注释翻译器，用于注释本地化（须在首次使用 {@link #adapters()} 之前）.
     *
     * @param commentTranslator 翻译器
     * @return 本选项
     */
    public Options commentTranslator(Function<String, String> commentTranslator) {
        if (!seal) {
            this.commentTranslator = commentTranslator;
        }
        return this;
    }

    /**
     * 获取绑定策略.
     *
     * @return 绑定策略
     */
    public BindingPolicy policy() {
        return policy;
    }

    /**
     * 获取适配器注册表（首次调用时创建）.
     *
     * @return 注册表
     */
    public Adapters adapters() {
        if (adapters == null) {
            adapters = new Adapters(commentTranslator, serializableFallback);
        }
        return adapters;
    }

    /**
     * 派生出对应的打印策略.
     *
     * @return 打印策略
     */
    public PrintOptions printOptions() {
        return PrintOptions.defaults()
                .indent(indent)
                .newline(newline)
                .emitComments(emitComments)
                .emitBlankLines(emitBlankLines)
                .inlineShortArrays(inlineShortArrays)
                .inlineWidth(inlineWidth);
    }
}
