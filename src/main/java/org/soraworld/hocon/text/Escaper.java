package org.soraworld.hocon.text;

/**
 * 引号与转义工具.
 *
 * <p>这是整个库中<strong>唯一</strong>实现转义表的地方：写出侧（{@link #quote}）与读入侧（词法分析器）
 * 共用同一张表，因此 {@code unquote(quote(x)).equals(x)} 恒成立.</p>
 *
 * <p>键与值的判定口径刻意分开：值不需要为 {@code '.'} 加引号（保住 {@code port = 8080}、
 * {@code ratio = 1.5} 的可读性），键必须为 {@code '.'} 加引号（否则无法区分"层级"与"字面键"）.</p>
 *
 * @author Himmelt
 */
public final class Escaper {

    /**
     * 值上下文中需要加引号的字符集合.
     * 与 {@code Lexer} 的裸文本边界保持一致.
     */
    private static final String SPECIAL = "\":=,+?`!@#$^&*{}[]\\";

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private Escaper() {
    }

    /**
     * 判断值文本写出时是否需要加双引号.
     *
     * @param text 文本
     * @return 是否需要加引号
     */
    public static boolean needsQuote(String text) {
        if (text.isEmpty() || "null".equals(text)) {
            return true;
        }
        char first = text.charAt(0);
        if (first == ' ' || text.charAt(text.length() - 1) == ' ') {
            return true;
        }
        if (first == '/' && text.length() > 1 && text.charAt(1) == '/') {
            // 行首的 "//" 会被词法器识别为注释
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 空白（含空格）会截断裸文本，因此必须在引号内
            if (c <= 0x20 || SPECIAL.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断键文本写出时是否需要加双引号.
     * 键比值多一条限制：{@code '.'} 是路径分隔符，含点的键必须加引号才能作为字面键往返.
     *
     * @param key 键文本
     * @return 是否需要加引号
     */
    public static boolean needsQuoteKey(String key) {
        return key.indexOf('.') >= 0 || needsQuote(key);
    }

    /**
     * 给文本加双引号并转义；不需要加引号时原样返回.
     *
     * @param text 文本
     * @return 可直接写出的文本
     */
    public static String quote(String text) {
        if (!needsQuote(text)) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append("\\u");
                        for (int shift = 12; shift >= 0; shift -= 4) {
                            builder.append(HEX[(c >> shift) & 0xF]);
                        }
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        return builder.append('"').toString();
    }

    /**
     * 给键加双引号，规则同 {@link #quote}，额外把 {@code '.'} 视为特殊字符.
     *
     * @param key 键文本
     * @return 可直接写出的键
     */
    public static String quoteKey(String key) {
        if (!needsQuoteKey(key)) {
            return key;
        }
        if (key.indexOf('.') < 0) {
            return quote(key);
        }
        return needsQuote(key) ? quote(key) : '"' + key + '"';
    }

    /**
     * 找出从 {@code openIndex} 处开引号开始、对应的闭合引号位置.
     *
     * @param text      文本
     * @param openIndex 开引号位置，该位置必须是 {@code '"'}
     * @return 闭合引号位置，未闭合返回 -1
     */
    public static int closingQuote(String text, int openIndex) {
        for (int i = openIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 给文本去双引号并还原转义，是 {@link #quote} 的逆运算.
     * 单遍扫描，每个反斜杠只被消费一次；文本不以双引号开头时按字面返回.
     *
     * @param text 文本
     * @return 解码后的文本
     */
    public static String unquote(String text) {
        if (text.isEmpty() || text.charAt(0) != '"') {
            return text;
        }
        int end = closingQuote(text, 0);
        if (end < 0) {
            end = text.length();
        }
        StringBuilder builder = new StringBuilder(end);
        int i = 1;
        while (i < end) {
            char c = text.charAt(i++);
            if (c != '\\') {
                builder.append(c);
                continue;
            }
            if (i >= end) {
                builder.append('\\');
                break;
            }
            char escape = text.charAt(i++);
            switch (escape) {
                case '"':
                    builder.append('"');
                    break;
                case '\\':
                    builder.append('\\');
                    break;
                case '/':
                    builder.append('/');
                    break;
                case 'b':
                    builder.append('\b');
                    break;
                case 'f':
                    builder.append('\f');
                    break;
                case 'n':
                    builder.append('\n');
                    break;
                case 'r':
                    builder.append('\r');
                    break;
                case 't':
                    builder.append('\t');
                    break;
                case 'u':
                    if (i + 4 <= end) {
                        int code = -1;
                        try {
                            code = Integer.parseInt(text.substring(i, i + 4), 16);
                        } catch (NumberFormatException ignored) {
                        }
                        if (code >= 0) {
                            builder.append((char) code);
                            i += 4;
                            break;
                        }
                    }
                    builder.append("\\u");
                    break;
                default:
                    // 未知转义序列按字面保留，便于发现写坏的配置
                    builder.append('\\').append(escape);
                    break;
            }
        }
        return builder.toString();
    }
}
