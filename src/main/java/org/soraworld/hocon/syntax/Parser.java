package org.soraworld.hocon.syntax;

import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.doc.DocArray;
import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.doc.DocObject;
import org.soraworld.hocon.doc.DocScalar;
import org.soraworld.hocon.doc.Trivia;
import org.soraworld.hocon.error.ParseException;
import org.soraworld.hocon.text.Newline;
import org.soraworld.hocon.text.Position;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器.
 *
 * <p>递归下降，直接构建 {@link Doc}，不额外产生中间 AST——{@code Doc} 本身就是抽象语法树，
 * 多一层拷贝只会增加分配与遍历成本.</p>
 *
 * <pre>
 * doc    ::= (trivia | entry)* EOF
 * entry  ::= key ( '=' value | object )          (* key {...} 与 key = {...} 等价 *)
 * key    ::= STRING | BARE                       (* 一律按字面键处理，不做任何路径切分 *)
 * value  ::= object | array | scalar
 * object ::= '{' (trivia | entry | ',')* '}'
 * array  ::= '[' (trivia | value | ',')* ']'
 * scalar ::= STRING | BARE
 * </pre>
 *
 * <p>注释与空行<strong>永远</strong>被解析进文档，解析器没有"丢弃注释"的模式；
 * 是否写回由 {@code PrintOptions} 在打印期决定.</p>
 *
 * @author Himmelt
 */
public final class Parser {

    /**
     * 单个位置连续空行的保留上限，避免畸形文件把内存撑爆.
     */
    private static final int MAX_BLANKS = 8;

    private final Lexer lexer;
    private final Doc doc;
    private Token token;
    private Token lastConsumed;

    private Parser(String source, String sourceName, Doc doc) throws ParseException {
        this.lexer = new Lexer(source, sourceName);
        this.doc = doc;
        this.token = lexer.next();
        this.lastConsumed = token;
    }

    /**
     * 解析配置文本.
     *
     * @param source     源文本
     * @param sourceName 来源名（通常是文件路径），用于报错定位，可为 null
     * @return 文档
     * @throws ParseException 解析异常
     */
    public static Doc parse(String source, String sourceName) throws ParseException {
        Doc doc = new Doc();
        doc.setNewline(Newline.detect(source));
        doc.setIndentUnit(detectIndent(source));
        Parser parser = new Parser(source, sourceName, doc);
        parser.objectBody(doc.root(), false, true);
        return doc;
    }

    /**
     * 从字符流解析配置.
     *
     * @param reader     字符流
     * @param sourceName 来源名，可为 null
     * @return 文档
     * @throws IOException    读取异常
     * @throws ParseException 解析异常
     */
    public static Doc parse(Reader reader, String sourceName) throws IOException, ParseException {
        return parse(Lexer.readAll(reader), sourceName);
    }

    /**
     * 推断缩进宽度：取第一条有缩进的非空行的前导空格数.
     *
     * @param source 源文本
     * @return 缩进宽度，取不到时返回 2
     */
    static int detectIndent(String source) {
        int start = 0;
        boolean firstLine = true;
        while (start < source.length()) {
            int end = source.indexOf('\n', start);
            if (end < 0) {
                end = source.length();
            }
            if (!firstLine) {
                int i = start;
                while (i < end && source.charAt(i) == ' ') {
                    i++;
                }
                if (i > start && i < end) {
                    int width = i - start;
                    return width <= 16 ? width : 2;
                }
            }
            firstLine = false;
            start = end + 1;
        }
        return 2;
    }

    // ------------------------------------------------------------ 对象

    private void objectBody(DocObject object, boolean braced, boolean root) throws ParseException {
        List<Trivia> pending = new ArrayList<>();
        while (true) {
            switch (token.kind()) {
                case EOF:
                    if (braced) {
                        throw error("缺少配对的 '}'");
                    }
                    object.setAfter(pending);
                    return;
                case RBRACE:
                    if (!braced) {
                        throw error("多余的 '}'");
                    }
                    object.setAfter(pending);
                    advance();
                    return;
                case NEWLINE:
                    collectBlankLines(pending);
                    break;
                case COMMA:
                    advance();
                    break;
                case COMMENT:
                    pending.add(Trivia.comment(token.text(), token.style()));
                    advance();
                    break;
                case HEAD:
                    if (root && object.isEmpty()) {
                        doc.head().addAll(pending);
                        pending.clear();
                        doc.head().add(Trivia.comment(token.text(), Trivia.Style.HEAD));
                    } else {
                        pending.add(Trivia.comment(token.text(), Trivia.Style.HEAD));
                    }
                    advance();
                    break;
                default:
                    entry(object, pending);
                    pending = new ArrayList<>();
                    break;
            }
        }
    }

    private void entry(DocObject object, List<Trivia> pending) throws ParseException {
        Token key = token;
        if (key.kind() != TokenKind.STRING && key.kind() != TokenKind.BARE) {
            throw error("这里需要一个键，实际读到 " + describe(token));
        }
        advance();
        DocNode node;
        if (token.kind() == TokenKind.LBRACE) {
            node = bracedObject();
        } else if (token.kind() == TokenKind.EQUALS) {
            advance();
            node = value();
        } else {
            throw error("键 '" + key.text() + "' 之后缺少 '=' 或 '{'，实际读到 " + describe(token));
        }
        node.setBefore(pending);
        object.put(key.text(), node);
    }

    private DocObject bracedObject() throws ParseException {
        Position open = token.position();
        advance();
        DocObject child = new DocObject();
        objectBody(child, true, false);
        child.setLayout(sameLine(open) ? DocNode.Layout.INLINE : DocNode.Layout.EXPANDED);
        return child;
    }

    // ------------------------------------------------------------ 值

    private DocNode value() throws ParseException {
        Token current = token;
        switch (current.kind()) {
            case LBRACE:
                return bracedObject();
            case LBRACKET:
                return bracedArray();
            case STRING:
                advance();
                return DocScalar.of(current.text());
            case BARE:
                advance();
                return DocScalar.bare(current.text());
            default:
                throw error("这里需要一个值，实际读到 " + describe(current));
        }
    }

    private DocArray bracedArray() throws ParseException {
        Position open = token.position();
        advance();
        DocArray array = new DocArray();
        List<Trivia> pending = new ArrayList<>();
        while (true) {
            switch (token.kind()) {
                case EOF:
                    throw error("缺少配对的 ']'");
                case RBRACKET:
                    advance();
                    array.setLayout(sameLine(open) ? DocNode.Layout.INLINE : DocNode.Layout.EXPANDED);
                    array.setAfter(pending);
                    return array;
                case NEWLINE:
                    collectBlankLines(pending);
                    break;
                case COMMA:
                    advance();
                    break;
                case COMMENT:
                case HEAD:
                    pending.add(Trivia.comment(token.text(), token.style()));
                    advance();
                    break;
                default: {
                    DocNode element = value();
                    element.setBefore(pending);
                    pending = new ArrayList<>();
                    array.add(element);
                    break;
                }
            }
        }
    }

    // ------------------------------------------------------------ 工具

    /**
     * 吃掉一段连续换行：{@code run} 个换行对应 {@code run - 1} 个空行.
     */
    private void collectBlankLines(List<Trivia> pending) throws ParseException {
        int run = 1;
        advance();
        while (token.kind() == TokenKind.NEWLINE) {
            run++;
            advance();
        }
        int blanks = Math.min(run - 1, MAX_BLANKS);
        for (int i = 0; i < blanks; i++) {
            pending.add(Trivia.blank());
        }
    }

    private boolean sameLine(Position open) {
        return lastConsumed.position().line() == open.line();
    }

    private void advance() throws ParseException {
        lastConsumed = token;
        token = lexer.next();
    }

    private ParseException error(String message) {
        return new ParseException(message, token.position());
    }

    private static String describe(Token token) {
        switch (token.kind()) {
            case EOF:
                return "文件结尾";
            case NEWLINE:
                return "换行";
            case LBRACE:
                return "'{'";
            case RBRACE:
                return "'}'";
            case LBRACKET:
                return "'['";
            case RBRACKET:
                return "']'";
            case EQUALS:
                return "'='";
            case COMMA:
                return "','";
            case COMMENT:
            case HEAD:
                return "注释";
            default:
                return "文本 \"" + token.text() + "\"";
        }
    }
}
