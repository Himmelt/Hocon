package org.soraworld.hocon.syntax;

import org.soraworld.hocon.doc.Trivia;
import org.soraworld.hocon.text.Position;

/**
 * 词法单元.
 * 不可变，携带文本与位置.
 *
 * @author Himmelt
 */
public final class Token {

    private final TokenKind kind;
    private final String text;
    private final Position position;
    private final Trivia.Style style;

    /**
     * 实例化词法单元.
     *
     * @param kind     类型
     * @param text     文本
     * @param position 位置
     * @param style    注释风格，非注释类型为 null
     */
    public Token(TokenKind kind, String text, Position position, Trivia.Style style) {
        this.kind = kind;
        this.text = text == null ? "" : text;
        this.position = position == null ? Position.unknown() : position;
        this.style = style;
    }

    /**
     * 获取类型.
     *
     * @return 类型
     */
    public TokenKind kind() {
        return kind;
    }

    /**
     * 获取文本.
     *
     * @return 文本；字符串类型已是解码后的内容
     */
    public String text() {
        return text;
    }

    /**
     * 获取位置.
     *
     * @return 位置
     */
    public Position position() {
        return position;
    }

    /**
     * 获取注释风格.
     *
     * @return 注释风格，非注释类型为 null
     */
    public Trivia.Style style() {
        return style;
    }

    @Override
    public String toString() {
        return kind + "(" + text + ")@" + position;
    }
}
