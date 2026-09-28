package org.soraworld.hocon.doc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 对象结点（映射）.
 *
 * <p>键是<strong>纯字符串、无语义</strong>：{@code put("example.com", ...)} 写的就是字面键
 * {@code example.com}，绝不会被拆成两层. 需要按层级访问请显式构造 {@link DocPath}.</p>
 *
 * <p>{@link #put(String, DocNode)} 对已存在的键是"原地替换值并保留该键原有注释"，因此
 * "改了值、注释还在"是结构保证，不需要任何启发式.</p>
 *
 * @author Himmelt
 */
public final class DocObject extends DocNode {

    private final LinkedHashMap<String, DocNode> entries = new LinkedHashMap<>();
    private Layout layout = Layout.EXPANDED;

    /**
     * 实例化空对象.
     */
    public DocObject() {
    }

    /**
     * 获取键集合（保持插入顺序）.
     *
     * @return 键集合
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    /**
     * 按字面键取值.
     *
     * @param literalKey 字面键，不做路径切分
     * @return 对应结点，不存在返回 null
     */
    public DocNode get(String literalKey) {
        return literalKey == null ? null : entries.get(literalKey);
    }

    /**
     * 按字面键放入结点.
     * 键已存在时替换其值，但<strong>保留该键原有的前置注释与来源位置</strong>；新键追加到末尾.
     *
     * @param literalKey 字面键
     * @param node       结点
     * @return 被替换掉的旧结点，原键不存在时返回 null
     */
    public DocNode put(String literalKey, DocNode node) {
        if (literalKey == null || node == null) {
            return null;
        }
        DocNode old = entries.put(literalKey, node);
        if (old != null) {
            if (node.before() == null) {
                node.setBefore(old.before());
            }
            node.setPosition(old.position());
        }
        return old;
    }

    /**
     * 按字面键移除.
     *
     * @param literalKey 字面键
     * @return 被移除的结点，不存在返回 null
     */
    public DocNode remove(String literalKey) {
        return literalKey == null ? null : entries.remove(literalKey);
    }

    /**
     * 是否包含键.
     *
     * @param literalKey 字面键
     * @return 包含返回 true
     */
    public boolean containsKey(String literalKey) {
        return literalKey != null && entries.containsKey(literalKey);
    }

    /**
     * 获取条目数.
     *
     * @return 条目数
     */
    public int size() {
        return entries.size();
    }

    /**
     * 是否为空.
     *
     * @return 空返回 true
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * 清空.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * 获取条目的不可变视图.
     *
     * @return 条目
     */
    public Map<String, DocNode> entries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * 获取书写布局.
     *
     * @return 布局
     */
    public Layout layout() {
        return layout;
    }

    /**
     * 设置书写布局.
     *
     * @param layout 布局
     */
    public void setLayout(Layout layout) {
        this.layout = layout == null ? Layout.EXPANDED : layout;
    }

    @Override
    public Kind kind() {
        return Kind.OBJECT;
    }

    @Override
    public DocNode copy() {
        DocObject copy = new DocObject();
        for (Map.Entry<String, DocNode> entry : entries.entrySet()) {
            copy.entries.put(entry.getKey(), entry.getValue().copy());
        }
        copy.layout = layout;
        copyTriviaTo(copy);
        return copy;
    }

    @Override
    public String toString() {
        return "{" + entries.size() + " 项}";
    }
}
