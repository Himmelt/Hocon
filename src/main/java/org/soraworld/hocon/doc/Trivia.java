package org.soraworld.hocon.doc;

/**
 * 文档中的"非配置内容"：注释与空行.
 *
 * <p>注释与空行<strong>永远</strong>被解析进文档——不存在"丢弃注释的解析模式".
 * 是否把它们写回文件由打印期策略（{@code PrintOptions}）决定.
 * 这样解析只有一条代码路径，也让"改了值、注释还在"成为结构保证.</p>
 *
 * @author Himmelt
 */
public final class Trivia {

    /**
     * 类型.
     */
    public enum Kind {
        /**
         * 注释行.
         */
        COMMENT,
        /**
         * 空行.
         */
        BLANK
    }

    /**
     * 注释风格.
     */
    public enum Style {
        /**
         * {@code # 注释}.
         */
        HASH,
        /**
         * {@code // 注释}.
         */
        SLASH,
        /**
         * {@code #! 文件头}.
         */
        HEAD
    }

    private static final Trivia BLANK = new Trivia(Kind.BLANK, Style.HASH, "");

    private final Kind kind;
    private final Style style;
    private final String text;

    private Trivia(Kind kind, Style style, String text) {
        this.kind = kind;
        this.style = style;
        this.text = text;
    }

    /**
     * 构造一条 {@code #} 注释.
     *
     * @param text 注释正文（不含标记）
     * @return 注释
     */
    public static Trivia comment(String text) {
        return comment(text, Style.HASH);
    }

    /**
     * 构造一条注释.
     *
     * @param text  注释正文（不含标记）
     * @param style 风格
     * @return 注释
     */
    public static Trivia comment(String text, Style style) {
        return new Trivia(Kind.COMMENT, style == null ? Style.HASH : style, text == null ? "" : text);
    }

    /**
     * 获取空行.
     *
     * @return 空行
     */
    public static Trivia blank() {
        return BLANK;
    }

    /**
     * 获取类型.
     *
     * @return 类型
     */
    public Kind kind() {
        return kind;
    }

    /**
     * 获取注释风格.
     *
     * @return 注释风格
     */
    public Style style() {
        return style;
    }

    /**
     * 获取注释正文.
     *
     * @return 注释正文，空行时为空串
     */
    public String text() {
        return text;
    }

    /**
     * 是否为空行.
     *
     * @return 空行返回 true
     */
    public boolean isBlank() {
        return kind == Kind.BLANK;
    }

    /**
     * 是否为注释.
     *
     * @return 注释返回 true
     */
    public boolean isComment() {
        return kind == Kind.COMMENT;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Trivia)) {
            return false;
        }
        Trivia other = (Trivia) obj;
        return kind == other.kind && style == other.style && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return (kind.hashCode() * 31 + style.hashCode()) * 31 + text.hashCode();
    }

    @Override
    public String toString() {
        return isBlank() ? "<blank>" : style + ":" + text;
    }
}
