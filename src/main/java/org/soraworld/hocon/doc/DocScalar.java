package org.soraworld.hocon.doc;

import org.soraworld.hocon.error.BindException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 标量结点（叶子）.
 *
 * <p>同时保存<strong>原始字面量</strong>与<strong>解析出的类型</strong>，因此：</p>
 * <ul>
 *   <li>无损往返：{@code 1.50}、{@code 1e3}、{@code 007} 都能原样写回；</li>
 *   <li>{@code null} 与字符串 {@code "null"} 是两个不同的东西（{@link ScalarKind#NULL} 与
 *       {@link ScalarKind#STRING}），不再靠文档解释；</li>
 *   <li>类型转换失败时给出可读消息，而不是裸的 {@code NumberFormatException}.</li>
 * </ul>
 *
 * @author Himmelt
 */
public final class DocScalar extends DocNode {

    /**
     * 标量类型.
     */
    public enum ScalarKind {
        /**
         * 空值 {@code null}.
         */
        NULL,
        /**
         * 逻辑值 {@code true} / {@code false}.
         */
        BOOLEAN,
        /**
         * 数值.
         */
        NUMBER,
        /**
         * 字符串.
         */
        STRING
    }

    private String lexeme;
    private ScalarKind scalarKind;

    /**
     * 实例化标量结点.
     *
     * @param lexeme     字面量（字符串类型时为解码后的内容）
     * @param scalarKind 类型
     */
    public DocScalar(String lexeme, ScalarKind scalarKind) {
        this.lexeme = lexeme == null ? "" : lexeme;
        this.scalarKind = scalarKind == null ? ScalarKind.STRING : scalarKind;
    }

    /**
     * 构造空值结点.
     *
     * @return {@code null} 结点
     */
    public static DocScalar ofNull() {
        return new DocScalar("null", ScalarKind.NULL);
    }

    /**
     * 构造字符串结点.
     *
     * @param text 字符串内容
     * @return 标量结点
     */
    public static DocScalar of(String text) {
        return new DocScalar(text, ScalarKind.STRING);
    }

    /**
     * 构造逻辑值结点.
     *
     * @param value 逻辑值
     * @return 标量结点
     */
    public static DocScalar of(boolean value) {
        return new DocScalar(value ? "true" : "false", ScalarKind.BOOLEAN);
    }

    /**
     * 构造数值结点.
     *
     * @param value 数值
     * @return 标量结点
     */
    public static DocScalar of(Number value) {
        return new DocScalar(String.valueOf(value), ScalarKind.NUMBER);
    }

    /**
     * 用裸文本构造结点，类型由文本推断.
     *
     * @param lexeme 裸文本
     * @return 标量结点
     */
    public static DocScalar bare(String lexeme) {
        return new DocScalar(lexeme, infer(lexeme));
    }

    /**
     * 从裸文本推断标量类型.
     * {@code null} / {@code true} / {@code false} / 数值 字面量之外一律视为字符串.
     *
     * @param lexeme 裸文本
     * @return 推断出的类型
     */
    public static ScalarKind infer(String lexeme) {
        if (lexeme == null || lexeme.isEmpty()) {
            return ScalarKind.STRING;
        }
        if ("null".equals(lexeme)) {
            return ScalarKind.NULL;
        }
        if ("true".equals(lexeme) || "false".equals(lexeme)) {
            return ScalarKind.BOOLEAN;
        }
        return isNumber(lexeme) ? ScalarKind.NUMBER : ScalarKind.STRING;
    }

    private static boolean isNumber(String text) {
        int i = 0;
        int length = text.length();
        if (length == 0) {
            return false;
        }
        char first = text.charAt(0);
        if (first == '+' || first == '-') {
            i++;
        }
        int digits = 0;
        while (i < length && isDigit(text.charAt(i))) {
            i++;
            digits++;
        }
        if (i < length && text.charAt(i) == '.') {
            i++;
            while (i < length && isDigit(text.charAt(i))) {
                i++;
                digits++;
            }
        }
        if (digits == 0) {
            return false;
        }
        if (i < length && (text.charAt(i) == 'e' || text.charAt(i) == 'E')) {
            i++;
            if (i < length && (text.charAt(i) == '+' || text.charAt(i) == '-')) {
                i++;
            }
            int exponent = 0;
            while (i < length && isDigit(text.charAt(i))) {
                i++;
                exponent++;
            }
            if (exponent == 0) {
                return false;
            }
        }
        return i == length;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 获取字面量.
     *
     * @return 字面量；字符串类型时是解码后的内容
     */
    public String lexeme() {
        return lexeme;
    }

    /**
     * 设置字面量，同时按文本重新推断类型.
     *
     * @param lexeme 新的字面量
     */
    public void setLexeme(String lexeme) {
        this.lexeme = lexeme == null ? "" : lexeme;
        this.scalarKind = infer(this.lexeme);
    }

    /**
     * 获取类型.
     *
     * @return 类型
     */
    public ScalarKind scalarKind() {
        return scalarKind;
    }

    /**
     * 设置类型（只改类型，不改字面量）.
     *
     * @param scalarKind 类型
     */
    public void setScalarKind(ScalarKind scalarKind) {
        this.scalarKind = scalarKind == null ? ScalarKind.STRING : scalarKind;
    }

    /**
     * 是否为空值.
     *
     * @return 空值返回 true
     */
    public boolean isNull() {
        return scalarKind == ScalarKind.NULL;
    }

    /**
     * 获取字符串形式.
     *
     * @return 字面量
     */
    public String asString() {
        return lexeme;
    }

    /**
     * 转换为整数.
     *
     * @return 整数
     * @throws BindException 不是可解析的整数
     */
    public int asInt() throws BindException {
        try {
            return Integer.parseInt(lexeme.trim());
        } catch (NumberFormatException e) {
            throw new BindException("的值 '" + lexeme + "' 不是整数", null, e);
        }
    }

    /**
     * 转换为长整数.
     *
     * @return 长整数
     * @throws BindException 不是可解析的长整数
     */
    public long asLong() throws BindException {
        try {
            return Long.parseLong(lexeme.trim());
        } catch (NumberFormatException e) {
            throw new BindException("的值 '" + lexeme + "' 不是长整数", null, e);
        }
    }

    /**
     * 转换为双精度小数.
     *
     * @return 双精度小数
     * @throws BindException 不是可解析的小数
     */
    public double asDouble() throws BindException {
        try {
            return Double.parseDouble(lexeme.trim());
        } catch (NumberFormatException e) {
            throw new BindException("的值 '" + lexeme + "' 不是小数", null, e);
        }
    }

    /**
     * 转换为高精度小数.
     *
     * @return 高精度小数
     * @throws BindException 不是可解析的小数
     */
    public BigDecimal asDecimal() throws BindException {
        try {
            return new BigDecimal(lexeme.trim());
        } catch (NumberFormatException e) {
            throw new BindException("的值 '" + lexeme + "' 不是小数", null, e);
        }
    }

    /**
     * 转换为大整数.
     *
     * @return 大整数
     * @throws BindException 不是可解析的整数
     */
    public BigInteger asInteger() throws BindException {
        try {
            return new BigInteger(lexeme.trim());
        } catch (NumberFormatException e) {
            throw new BindException("的值 '" + lexeme + "' 不是整数", null, e);
        }
    }

    /**
     * 转换为逻辑值.
     * 接受 {@code true}/{@code yes}/{@code 1}/{@code t}/{@code y}（忽略大小写）为真，其余为假.
     *
     * @return 逻辑值
     */
    public boolean asBoolean() {
        return "true".equalsIgnoreCase(lexeme)
                || "yes".equalsIgnoreCase(lexeme)
                || "1".equals(lexeme)
                || "t".equalsIgnoreCase(lexeme)
                || "y".equalsIgnoreCase(lexeme);
    }

    @Override
    public Kind kind() {
        return Kind.SCALAR;
    }

    @Override
    public DocNode copy() {
        DocScalar copy = new DocScalar(lexeme, scalarKind);
        copyTriviaTo(copy);
        return copy;
    }

    @Override
    public String toString() {
        return lexeme;
    }
}
