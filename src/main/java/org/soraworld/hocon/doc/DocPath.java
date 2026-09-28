package org.soraworld.hocon.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 显式配置路径.
 *
 * <p>路径是路径、键是键：只有这里是"分段"的，{@link DocObject} 的键永远是字面量.
 * 于是含 {@code '.'} 的键（域名、文件名、版本号）既不会被误拆成层级，也依然能按名字取到.</p>
 *
 * <ul>
 *   <li>{@link #parse(String)}：把 {@code "a.b.c"} 按 {@code '.'} 切成三段（空段被忽略）；</li>
 *   <li>{@link #of(String...)}：每个参数本身就是字面量段，含 {@code '.'} 也不用转义.</li>
 * </ul>
 *
 * @author Himmelt
 */
public final class DocPath {

    private static final DocPath ROOT = new DocPath(new String[0]);

    private final String[] segments;

    private DocPath(String[] segments) {
        this.segments = segments;
    }

    /**
     * 获取根路径.
     *
     * @return 根路径
     */
    public static DocPath root() {
        return ROOT;
    }

    /**
     * 解析点分路径.
     *
     * @param dotted 形如 {@code a.b.c} 的路径，为 null 或空串时返回根路径
     * @return 路径
     */
    public static DocPath parse(String dotted) {
        if (dotted == null || dotted.isEmpty()) {
            return ROOT;
        }
        List<String> parts = new ArrayList<>();
        for (String part : dotted.split("\\.", -1)) {
            if (!part.isEmpty()) {
                parts.add(part);
            }
        }
        return parts.isEmpty() ? ROOT : new DocPath(parts.toArray(new String[0]));
    }

    /**
     * 用字面量段构造路径.
     *
     * @param segments 各段，参数本身即字面量
     * @return 路径
     */
    public static DocPath of(String... segments) {
        if (segments == null || segments.length == 0) {
            return ROOT;
        }
        String[] copy = new String[segments.length];
        for (int i = 0; i < segments.length; i++) {
            copy[i] = segments[i] == null ? "" : segments[i];
        }
        return new DocPath(copy);
    }

    /**
     * 追加一段.
     *
     * @param segment 字面量段
     * @return 新路径
     */
    public DocPath child(String segment) {
        String[] next = Arrays.copyOf(segments, segments.length + 1);
        next[segments.length] = segment == null ? "" : segment;
        return new DocPath(next);
    }

    /**
     * 去掉最后一段.
     *
     * @return 新路径，已经是根路径时返回根路径
     */
    public DocPath parent() {
        return segments.length <= 1 ? ROOT : new DocPath(Arrays.copyOf(segments, segments.length - 1));
    }

    /**
     * 获取第一段.
     *
     * @return 段名，根路径时为空串
     */
    public String first() {
        return segments.length == 0 ? "" : segments[0];
    }

    /**
     * 获取最后一段.
     *
     * @return 段名，根路径时为空串
     */
    public String name() {
        return segments.length == 0 ? "" : segments[segments.length - 1];
    }

    /**
     * 获取段数.
     *
     * @return 段数
     */
    public int size() {
        return segments.length;
    }

    /**
     * 是否根路径.
     *
     * @return 根路径返回 true
     */
    public boolean isRoot() {
        return segments.length == 0;
    }

    /**
     * 获取段数组的副本.
     *
     * @return 段数组
     */
    public String[] segments() {
        return segments.clone();
    }

    /**
     * 取值.
     *
     * @param root 根对象
     * @return 对应结点，任一层缺失或类型不是对象时返回 null
     */
    public DocNode get(DocObject root) {
        if (root == null || segments.length == 0) {
            return root;
        }
        DocObject current = root;
        for (int i = 0; i < segments.length; i++) {
            DocNode child = current.get(segments[i]);
            if (child == null) {
                return null;
            }
            if (i == segments.length - 1) {
                return child;
            }
            if (!(child instanceof DocObject)) {
                return null;
            }
            current = (DocObject) child;
        }
        return null;
    }

    /**
     * 放值，中间缺失的对象会自动创建.
     *
     * @param root 根对象
     * @param node 结点
     * @return 被替换掉的旧结点
     */
    public DocNode set(DocObject root, DocNode node) {
        if (root == null || node == null || segments.length == 0) {
            return null;
        }
        DocObject current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            DocNode child = current.get(segments[i]);
            if (!(child instanceof DocObject)) {
                DocObject created = new DocObject();
                current.put(segments[i], created);
                child = created;
            }
            current = (DocObject) child;
        }
        return current.put(segments[segments.length - 1], node);
    }

    /**
     * 移除.
     *
     * @param root 根对象
     * @return 被移除的结点，不存在返回 null
     */
    public DocNode remove(DocObject root) {
        if (root == null || segments.length == 0) {
            return null;
        }
        DocObject current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            DocNode child = current.get(segments[i]);
            if (!(child instanceof DocObject)) {
                return null;
            }
            current = (DocObject) child;
        }
        return current.remove(segments[segments.length - 1]);
    }

    /**
     * 输出点分形式，仅用于展示与日志；段内含 {@code '.'} 时结果有歧义.
     *
     * @return 展示用字符串
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(segments[i]);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DocPath && Arrays.equals(segments, ((DocPath) obj).segments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(segments);
    }
}
