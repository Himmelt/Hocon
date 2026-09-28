package org.soraworld.hocon.doc;

import org.soraworld.hocon.text.Newline;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文档.
 *
 * <p>文档是这个库真正在管的东西，也是"对象的投影"之外的那个<strong>唯一真相源</strong>：
 * 手动编辑留下的注释、空行、键顺序、缩进与换行风格都记在这里，程序改值只动值、不动这些.</p>
 *
 * @author Himmelt
 */
public final class Doc {

    private DocObject root;
    private final List<Trivia> head = new ArrayList<>();
    private Newline newline = Newline.LF;
    private int indentUnit = 2;

    /**
     * 实例化一个空文档.
     */
    public Doc() {
        this(new DocObject());
    }

    /**
     * 实例化文档.
     *
     * @param root 根对象
     */
    public Doc(DocObject root) {
        this.root = root == null ? new DocObject() : root;
    }

    /**
     * 构造一个空文档.
     *
     * @return 空文档
     */
    public static Doc empty() {
        return new Doc();
    }

    /**
     * 获取根对象.
     *
     * @return 根对象
     */
    public DocObject root() {
        return root;
    }

    /**
     * 设置根对象.
     *
     * @param root 根对象
     */
    public void setRoot(DocObject root) {
        this.root = root == null ? new DocObject() : root;
    }

    /**
     * 获取文件头内容（{@code #!} 行）.
     *
     * @return 文件头内容列表
     */
    public List<Trivia> head() {
        return head;
    }

    /**
     * 添加一条文件头注释.
     *
     * @param text 注释正文
     */
    public void addHead(String text) {
        head.add(Trivia.comment(text, Trivia.Style.HEAD));
    }

    /**
     * 获取换行风格.
     *
     * @return 换行风格
     */
    public Newline newline() {
        return newline;
    }

    /**
     * 设置换行风格.
     *
     * @param newline 换行风格
     */
    public void setNewline(Newline newline) {
        this.newline = newline == null ? Newline.LF : newline;
    }

    /**
     * 获取缩进宽度（空格数）.
     *
     * @return 缩进宽度
     */
    public int indentUnit() {
        return indentUnit;
    }

    /**
     * 设置缩进宽度.
     *
     * @param indentUnit 缩进宽度，小于 0 视为 0
     */
    public void setIndentUnit(int indentUnit) {
        this.indentUnit = Math.max(0, indentUnit);
    }

    /**
     * 按显式路径取值.
     *
     * @param path 路径
     * @return 对应结点，不存在返回 null
     */
    public DocNode get(DocPath path) {
        return path == null ? null : path.get(root);
    }

    /**
     * 按显式路径放值（中间对象不存在时自动创建）.
     *
     * @param path  路径
     * @param node  结点
     * @return 被替换掉的旧结点
     */
    public DocNode set(DocPath path, DocNode node) {
        return path == null ? null : path.set(root, node);
    }

    /**
     * 深拷贝本文档.
     *
     * @return 拷贝
     */
    public Doc copy() {
        Doc copy = new Doc((DocObject) root.copy());
        copy.head.addAll(head);
        copy.newline = newline;
        copy.indentUnit = indentUnit;
        return copy;
    }
}
