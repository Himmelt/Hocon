package org.soraworld.hocon.syntax;

import org.soraworld.hocon.doc.Trivia;
import org.soraworld.hocon.error.ParseException;
import org.soraworld.hocon.text.Escaper;
import org.soraworld.hocon.text.Position;

import java.io.IOException;
import java.io.Reader;

/**
 * 词法分析器.
 *
 * <p>单遍扫描字符流产出 {@link Token} 序列. 这一层只回答"下一个记号是什么、在哪个位置"，
 * 不做任何结构判断，因此读取逻辑不再依赖"整行 trim 后看前缀后缀"这种脆弱的字符串猜测.</p>
 *
 * <ul>
 *   <li>结构字符 {@code &#123; &#125; [ ] = ,} 各自成记号；</li>
 *   <li>空白跳过，换行产出 {@link TokenKind#NEWLINE}；</li>
 *   <li>{@code #} 与 {@code //} 到行尾为注释，{@code #!} 识别为文件头；</li>
 *   <li>引号文本内部支持转义，解码交给 {@link Escaper#unquote}；</li>
 *   <li>裸文本直到下一个边界字符为止，不做任何转义解释.</li>
 * </ul>
 *
 * @author Himmelt
 */
public final class Lexer {

    private static final String BOUNDS = " \t\f\r\n\"{}[]=,#";

    private final String source;
    private final String sourceName;
    private int index;
    private int line = 1;
    private int column = 1;

    /**
     * 实例化词法分析器.
     *
     * @param source     源文本
     * @param sourceName 来源名（通常是文件路径），可为 null
     */
    public Lexer(String source, String sourceName) {
        this.source = source == null ? "" : source;
        this.sourceName = sourceName;
    }

    /**
     * 实例化词法分析器并把 reader 一次性读入.
     *
     * @param reader     字符流
     * @param sourceName 来源名，可为 null
     * @throws IOException 读取异常
     */
    public Lexer(Reader reader, String sourceName) throws IOException {
        this(readAll(reader), sourceName);
    }

    /**
     * 把 reader 内容一次性读成字符串.
     *
     * @param reader 字符流
     * @return 文本
     * @throws IOException 读取异常
     */
    public static String readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder(1 << 12);
        char[] buffer = new char[1 << 12];
        int count;
        while ((count = reader.read(buffer)) >= 0) {
            builder.append(buffer, 0, count);
        }
        return builder.toString();
    }

    /**
     * 产出下一个词法单元.
     * 到达末尾后反复返回 {@link TokenKind#EOF}.
     *
     * @return 词法单元
     * @throws ParseException 词法错误（例如字符串引号未闭合）
     */
    public Token next() throws ParseException {
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '\r' || c == '\n') {
                Token token = new Token(TokenKind.NEWLINE, "", Position.at(sourceName, line, column), null);
                advance();
                return token;
            }
            if (c == ' ' || c == '\t' || c == '\f') {
                advance();
                continue;
            }
            if (c == '#') {
                return comment();
            }
            if (c == '/' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                return comment();
            }
            switch (c) {
                case '{':
                    return single(TokenKind.LBRACE);
                case '}':
                    return single(TokenKind.RBRACE);
                case '[':
                    return single(TokenKind.LBRACKET);
                case ']':
                    return single(TokenKind.RBRACKET);
                case '=':
                    return single(TokenKind.EQUALS);
                case ',':
                    return single(TokenKind.COMMA);
                case '"':
                    return string();
                default:
                    return bare();
            }
        }
        return new Token(TokenKind.EOF, "", Position.at(sourceName, line, column), null);
    }

    private Token single(TokenKind kind) {
        Token token = new Token(kind, String.valueOf(source.charAt(index)), Position.at(sourceName, line, column), null);
        advance();
        return token;
    }

    private Token string() throws ParseException {
        Position position = Position.at(sourceName, line, column);
        int open = index;
        int close = Escaper.closingQuote(source, open);
        if (close < 0) {
            throw new ParseException("字符串缺少闭合的双引号", position);
        }
        while (index <= close) {
            advance();
        }
        return new Token(TokenKind.STRING, Escaper.unquote(source.substring(open, close + 1)), position, null);
    }

    private Token bare() {
        Position position = Position.at(sourceName, line, column);
        int start = index;
        while (index < source.length() && BOUNDS.indexOf(source.charAt(index)) < 0) {
            advance();
        }
        return new Token(TokenKind.BARE, source.substring(start, index), position, null);
    }

    private Token comment() {
        Position position = Position.at(sourceName, line, column);
        char marker = source.charAt(index);
        boolean head = false;
        if (marker == '#') {
            advance();
            if (index < source.length() && source.charAt(index) == '!') {
                advance();
                head = true;
            }
        } else {
            advance();
            advance();
        }
        if (index < source.length() && source.charAt(index) == ' ') {
            advance();
        }
        int start = index;
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '\r' || c == '\n') {
                break;
            }
            advance();
        }
        String text = source.substring(start, index);
        if (head) {
            return new Token(TokenKind.HEAD, text, position, Trivia.Style.HEAD);
        }
        return new Token(TokenKind.COMMENT, text, position, marker == '#' ? Trivia.Style.HASH : Trivia.Style.SLASH);
    }

    /**
     * 前进一个字符并维护行列号；{@code \r\n} 作为一个换行整体吃掉.
     */
    private void advance() {
        char c = source.charAt(index++);
        if (c == '\n') {
            line++;
            column = 1;
        } else if (c == '\r') {
            if (index < source.length() && source.charAt(index) == '\n') {
                index++;
            }
            line++;
            column = 1;
        } else {
            column++;
        }
    }
}
