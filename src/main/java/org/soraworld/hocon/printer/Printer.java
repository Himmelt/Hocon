package org.soraworld.hocon.printer;

import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.doc.DocArray;
import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.doc.DocObject;
import org.soraworld.hocon.doc.DocScalar;
import org.soraworld.hocon.doc.Trivia;
import org.soraworld.hocon.text.Escaper;
import org.soraworld.hocon.text.Newline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档写出（格式化）器.
 *
 * <p>写出是独立于文档模型的一层：{@code Doc} 是纯数据，怎么排版由这里决定，
 * 于是同一个文档能打印成"宽松"或"紧凑"两种风格，也能被独立单测（不需要临时文件）.</p>
 *
 * <p>保真范围：换行风格、缩进宽度、注释与空行的位置、键顺序、数组/对象的紧凑程度.
 * 不保留：精确列对齐、注释内部排版、文件末尾的多余空行.</p>
 *
 * @author Himmelt
 */
public final class Printer {

    private static final String SEPARATOR = " = ";

    private final PrintOptions options;
    private final String newline;
    private final int indentUnit;
    private final List<String> lines = new ArrayList<>();

    private Printer(PrintOptions options, Doc doc) {
        this.options = options == null ? PrintOptions.defaults() : options;
        Newline style = this.options.newline() != null ? this.options.newline() : doc.newline();
        this.newline = (style == null ? Newline.LF : style).text();
        this.indentUnit = this.options.indent() > 0 ? this.options.indent()
                : (doc.indentUnit() > 0 ? doc.indentUnit() : 2);
    }

    /**
     * 用默认策略打印.
     *
     * @param doc 文档
     * @return 文本
     */
    public static String print(Doc doc) {
        return print(doc, PrintOptions.defaults());
    }

    /**
     * 打印文档.
     *
     * @param doc     文档
     * @param options 打印策略
     * @return 文本
     */
    public static String print(Doc doc, PrintOptions options) {
        Printer printer = new Printer(options, doc);
        printer.document(doc);
        return printer.build();
    }

    /**
     * 打印文档到输出流.
     *
     * @param doc     文档
     * @param options 打印策略
     * @param out     输出
     * @throws IOException 写出异常
     */
    public static void print(Doc doc, PrintOptions options, Appendable out) throws IOException {
        out.append(print(doc, options));
    }

    private void document(Doc doc) {
        for (Trivia trivia : doc.head()) {
            trivia(trivia, 0);
        }
        if (!doc.head().isEmpty() && !doc.root().isEmpty() && !startsWithBlank(doc.root())) {
            blank();
        }
        entries(doc.root(), 0);
        trivia(doc.root().after(), 0);
    }

    /**
     * 判断第一个条目的前置内容里是否已经有空行；已经有的话就不再额外补，避免出现两个空行.
     */
    private boolean startsWithBlank(DocObject object) {
        for (String key : object.keys()) {
            DocNode first = object.get(key);
            if (first == null) {
                continue;
            }
            List<Trivia> before = first.before();
            return before != null && !before.isEmpty() && before.get(0).isBlank();
        }
        return false;
    }

    private void entries(DocObject object, int indent) {
        for (String key : object.keys()) {
            DocNode node = object.get(key);
            if (node == null) {
                continue;
            }
            trivia(node.before(), indent);
            entry(key, node, indent);
        }
    }

    private void entry(String key, DocNode node, int indent) {
        String prefix = indent(indent) + Escaper.quoteKey(key);
        if (node instanceof DocObject) {
            DocObject object = (DocObject) node;
            if (inline(object, indent + key.length())) {
                add(prefix + SEPARATOR + inlineObject(object));
            } else {
                add(prefix + " {");
                entries(object, indent + 1);
                trivia(object.after(), indent + 1);
                add(indent(indent) + "}");
            }
        } else if (node instanceof DocArray) {
            DocArray array = (DocArray) node;
            if (inline(array, indent + key.length())) {
                add(prefix + SEPARATOR + inlineArray(array));
            } else {
                add(prefix + SEPARATOR + "[");
                elements(array, indent + 1);
                trivia(array.after(), indent + 1);
                add(indent(indent) + "]");
            }
        } else {
            add(prefix + SEPARATOR + scalar((DocScalar) node));
        }
    }

    private void elements(DocArray array, int indent) {
        for (DocNode element : array.elements()) {
            trivia(element.before(), indent);
            element(element, indent);
        }
    }

    private void element(DocNode node, int indent) {
        String prefix = indent(indent);
        if (node instanceof DocObject) {
            DocObject object = (DocObject) node;
            if (inline(object, indent)) {
                add(prefix + inlineObject(object));
            } else {
                add(prefix + "{");
                entries(object, indent + 1);
                trivia(object.after(), indent + 1);
                add(prefix + "}");
            }
        } else if (node instanceof DocArray) {
            DocArray array = (DocArray) node;
            if (inline(array, indent)) {
                add(prefix + inlineArray(array));
            } else {
                add(prefix + "[");
                elements(array, indent + 1);
                trivia(array.after(), indent + 1);
                add(prefix + "]");
            }
        } else {
            add(prefix + scalar((DocScalar) node));
        }
    }

    // ------------------------------------------------------------ 压行判定

    private boolean inline(DocNode node, int prefixWidth) {
        DocNode.Layout layout = node instanceof DocObject ? ((DocObject) node).layout() : ((DocArray) node).layout();
        if (layout == DocNode.Layout.INLINE) {
            return inlineLength(node) + prefixWidth <= options.inlineWidth();
        }
        if (!options.inlineShortArrays()) {
            return false;
        }
        return inlineLength(node) + prefixWidth <= options.inlineWidth();
    }

    private int inlineLength(DocNode node) {
        if (node instanceof DocScalar) {
            return scalar((DocScalar) node).length();
        }
        if (node instanceof DocObject) {
            DocObject object = (DocObject) node;
            int length = 2;
            for (String key : object.keys()) {
                DocNode child = object.get(key);
                if (child == null) {
                    continue;
                }
                length += Escaper.quoteKey(key).length() + SEPARATOR.length() + inlineLength(child) + 2;
            }
            return length;
        }
        DocArray array = (DocArray) node;
        int length = 2;
        for (DocNode child : array.elements()) {
            length += inlineLength(child) + 2;
        }
        return length;
    }

    private String inlineObject(DocObject object) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (String key : object.keys()) {
            DocNode child = object.get(key);
            if (child == null) {
                continue;
            }
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(Escaper.quoteKey(key)).append(SEPARATOR).append(inlineNode(child));
        }
        return builder.append('}').toString();
    }

    private String inlineArray(DocArray array) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (DocNode child : array.elements()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(inlineNode(child));
        }
        return builder.append(']').toString();
    }

    private String inlineNode(DocNode node) {
        if (node instanceof DocObject) {
            return inlineObject((DocObject) node);
        }
        if (node instanceof DocArray) {
            return inlineArray((DocArray) node);
        }
        return scalar((DocScalar) node);
    }

    // ------------------------------------------------------------ 标量

    private String scalar(DocScalar node) {
        switch (node.scalarKind()) {
            case NULL:
                return "null";
            case BOOLEAN:
                return node.asBoolean() ? "true" : "false";
            case NUMBER:
                return DocScalar.infer(node.lexeme()) == DocScalar.ScalarKind.NUMBER
                        ? node.lexeme() : Escaper.quote(node.lexeme());
            default:
                return Escaper.quote(node.lexeme());
        }
    }

    // ------------------------------------------------------------ 行管理

    private void trivia(List<Trivia> list, int indent) {
        if (list == null) {
            return;
        }
        for (Trivia item : list) {
            trivia(item, indent);
        }
    }

    private void trivia(Trivia item, int indent) {
        if (item.isBlank()) {
            blank();
            return;
        }
        if (!options.emitComments()) {
            return;
        }
        String marker;
        switch (item.style()) {
            case SLASH:
                marker = "// ";
                break;
            case HEAD:
                marker = "#! ";
                break;
            default:
                marker = "# ";
                break;
        }
        add(indent(indent) + marker + item.text());
    }

    private void blank() {
        if (options.emitBlankLines()) {
            add("");
        }
    }

    private void add(String line) {
        lines.add(line);
    }

    private String indent(int level) {
        if (level <= 0 || indentUnit <= 0) {
            return "";
        }
        int width = level * indentUnit;
        StringBuilder builder = new StringBuilder(width);
        for (int i = 0; i < width; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    private String build() {
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        if (lines.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append(newline);
            }
            builder.append(lines.get(i));
        }
        return builder.append(newline).toString();
    }
}
