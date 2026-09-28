package org.soraworld.hocon.doc;

import org.soraworld.hocon.text.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档结点基类.
 *
 * <p>这是<strong>纯数据</strong>：不含任何 IO、格式化或对象绑定职责.
 * 读由 {@code Parser} 负责，写由 {@code Printer} 负责，与对象互转由 {@code bind} 负责.</p>
 *
 * @author Himmelt
 */
public abstract class DocNode {

    /**
     * 结点类型.
     */
    public enum Kind {
        /**
         * 标量（叶子）.
         */
        SCALAR,
        /**
         * 数组.
         */
        ARRAY,
        /**
         * 对象（映射）.
         */
        OBJECT
    }

    /**
     * 书写布局.
     * 记住原本是"一行写完"还是"展开多行"，保存时据此保持原样.
     */
    public enum Layout {
        /**
         * 一行写完，例如 {@code [1, 2, 3]}.
         */
        INLINE,
        /**
         * 展开多行.
         */
        EXPANDED
    }

    private List<Trivia> before;
    private List<Trivia> after;
    private Position position = Position.unknown();

    /**
     * 获取结点类型.
     *
     * @return 结点类型
     */
    public abstract Kind kind();

    /**
     * 深拷贝本结点.
     *
     * @return 拷贝
     */
    public abstract DocNode copy();

    /**
     * 获取前置注释与空行.
     *
     * @return 前置内容，没有时返回 null（调用方无需自行判空时可用 {@link #trivia()}）
     */
    public List<Trivia> before() {
        return before;
    }

    /**
     * 获取前置注释与空行，保证非 null.
     *
     * @return 前置内容列表
     */
    public List<Trivia> trivia() {
        if (before == null) {
            before = new ArrayList<>();
        }
        return before;
    }

    /**
     * 设置前置注释与空行.
     *
     * @param before 前置内容，可为 null
     */
    public void setBefore(List<Trivia> before) {
        this.before = before == null || before.isEmpty() ? null : new ArrayList<>(before);
    }

    /**
     * 追加一条前置注释或空行.
     *
     * @param trivia 注释或空行
     */
    public void addBefore(Trivia trivia) {
        if (trivia != null) {
            trivia().add(trivia);
        }
    }

    /**
     * 追加一行注释.
     *
     * @param text 注释正文
     */
    public void addComment(String text) {
        addBefore(Trivia.comment(text));
    }

    /**
     * 获取尾部内容：容器里最后一条之后的注释与空行（文件末尾的说明性注释就落在这里）.
     *
     * @return 尾部内容，没有时返回 null
     */
    public List<Trivia> after() {
        return after;
    }

    /**
     * 设置尾部内容.
     *
     * @param after 尾部内容，可为 null
     */
    public void setAfter(List<Trivia> after) {
        this.after = after == null || after.isEmpty() ? null : new ArrayList<>(after);
    }

    /**
     * 判断是否有尾部内容.
     *
     * @return 有返回 true
     */
    public boolean hasAfter() {
        return after != null && !after.isEmpty();
    }

    /**
     * 获取来源位置.
     *
     * @return 来源位置，程序构造时为未知位置
     */
    public Position position() {
        return position;
    }

    /**
     * 设置来源位置.
     *
     * @param position 来源位置
     */
    public void setPosition(Position position) {
        this.position = position == null ? Position.unknown() : position;
    }

    /**
     * 把本结点的前置内容与位置复制到目标结点.
     *
     * @param target 目标结点
     */
    protected void copyTriviaTo(DocNode target) {
        target.setBefore(before);
        target.setAfter(after);
        target.setPosition(position);
    }

    /**
     * 获取用于诊断的类型名.
     *
     * @return 类型名
     */
    public String typeName() {
        switch (kind()) {
            case SCALAR:
                return "标量";
            case ARRAY:
                return "数组";
            default:
                return "对象";
        }
    }
}
