package org.soraworld.hocon.syntax;

/**
 * 词法单元类型.
 *
 * @author Himmelt
 */
public enum TokenKind {

    /**
     * 带引号的字符串，{@code Token.text()} 已经是解码后的内容.
     */
    STRING,
    /**
     * 不带引号的裸文本（键与值都用它承载）.
     */
    BARE,
    /**
     * {@code &#123;}.
     */
    LBRACE,
    /**
     * {@code &#125;}.
     */
    RBRACE,
    /**
     * {@code [}.
     */
    LBRACKET,
    /**
     * {@code ]}.
     */
    RBRACKET,
    /**
     * {@code =}.
     */
    EQUALS,
    /**
     * {@code ,}.
     */
    COMMA,
    /**
     * 换行.
     */
    NEWLINE,
    /**
     * 注释行.
     */
    COMMENT,
    /**
     * 文件头 {@code #!} 行.
     */
    HEAD,
    /**
     * 输入结束.
     */
    EOF
}
