package org.soraworld.hocon.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数组结点.
 *
 * @author Himmelt
 */
public final class DocArray extends DocNode {

    private final List<DocNode> elements = new ArrayList<>();
    private Layout layout = Layout.EXPANDED;

    /**
     * 实例化空数组.
     */
    public DocArray() {
    }

    /**
     * 实例化数组.
     *
     * @param elements 元素
     */
    public DocArray(List<? extends DocNode> elements) {
        if (elements != null) {
            this.elements.addAll(elements);
        }
    }

    /**
     * 获取元素个数.
     *
     * @return 元素个数
     */
    public int size() {
        return elements.size();
    }

    /**
     * 是否为空.
     *
     * @return 空返回 true
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * 获取指定位置元素.
     *
     * @param index 索引
     * @return 元素，越界返回 null
     */
    public DocNode get(int index) {
        return index >= 0 && index < elements.size() ? elements.get(index) : null;
    }

    /**
     * 追加元素.
     *
     * @param node 元素
     * @return 本数组（便于链式调用）
     */
    public DocArray add(DocNode node) {
        if (node != null) {
            elements.add(node);
        }
        return this;
    }

    /**
     * 替换指定位置元素.
     *
     * @param index 索引
     * @param node  元素
     */
    public void set(int index, DocNode node) {
        if (index >= 0 && index < elements.size() && node != null) {
            elements.set(index, node);
        }
    }

    /**
     * 移除指定位置元素.
     *
     * @param index 索引
     * @return 被移除的元素，越界返回 null
     */
    public DocNode remove(int index) {
        return index >= 0 && index < elements.size() ? elements.remove(index) : null;
    }

    /**
     * 清空.
     */
    public void clear() {
        elements.clear();
    }

    /**
     * 获取元素列表的不可变视图.
     *
     * @return 元素列表
     */
    public List<DocNode> elements() {
        return Collections.unmodifiableList(elements);
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
        return Kind.ARRAY;
    }

    @Override
    public DocNode copy() {
        DocArray copy = new DocArray();
        for (DocNode element : elements) {
            copy.elements.add(element.copy());
        }
        copy.layout = layout;
        copyTriviaTo(copy);
        return copy;
    }

    @Override
    public String toString() {
        return "[" + elements.size() + " 项]";
    }
}
