package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 列表结点类.
 * 此类存储一个结点列表.
 */
public class NodeList extends AbstractNode<ArrayList<Node>> implements Node {

    /**
     * 实例化一个新的列表结点.
     *
     * @param options 配置选项
     */
    public NodeList(Options options) {
        super(options, new ArrayList<>());
    }

    /**
     * 实例化一个新的列表结点.
     *
     * @param options 配置选项
     * @param comment 注释
     */
    public NodeList(Options options, String comment) {
        super(options, new ArrayList<>(), comment);
    }

    /**
     * 清空结点列表.
     */
    public void clear() {
        value.clear();
    }

    /**
     * 获取列表大小.
     *
     * @return size
     */
    public int size() {
        return value.size();
    }

    /**
     * 添加一个结点到列表中.
     *
     * @param node 结点
     */
    public void add(Node node) {
        value.add(node);
    }

    /**
     * 获取索引位置结点.
     * 如果索引位置非法，则返回 null.
     *
     * @param index 索引位置
     * @return 对应结点
     */
    public Node get(int index) {
        if (index >= 0 && index < value.size()) return value.get(index);
        return null;
    }

    /**
     * 设置索引位置的结点.
     * 如果索引位置非法，则无效.
     *
     * @param index 索引位置
     * @param node  对应结点
     */
    public void set(int index, Node node) {
        if (index >= 0 && index < value.size()) value.set(index, node);
    }

    /**
     * 移除索引位置的结点.
     *
     * @param index 索引位置
     */
    public void remove(int index) {
        if (index >= 0 && index < value.size()) value.remove(index);
    }

    /**
     * 移除结点.
     *
     * @param node 待移除结点
     */
    public void remove(Node node) {
        value.remove(node);
    }

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
    }

    public void readValue(BufferedReader reader, boolean keepComments) throws Exception {
        value.clear();
        String line;
        ArrayList<String> commentTemp = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("]")) return;
            if (line.startsWith("#")) {
                if (keepComments) {
                    int index = line.startsWith("#! ") ? 3 : line.startsWith("#!") || line.startsWith("# ") ? 2 : 1;
                    commentTemp.add(line.substring(index));
                } else continue;
            }
            if (line.startsWith("{")) {
                NodeMap node = new NodeMap(options);
                if (keepComments) {
                    node.setComments(commentTemp);
                    commentTemp = new ArrayList<>();
                }
                value.add(node);
                if (!line.endsWith("}")) node.readValue(reader, keepComments);
            } else if (line.startsWith("[")) {
                NodeList list = new NodeList(options);
                if (keepComments) {
                    list.setComments(commentTemp);
                    commentTemp = new ArrayList<>();
                }
                value.add(list);
                if (!line.endsWith("]")) list.readValue(reader, keepComments);
            } else {
                NodeBase base = new NodeBase(options, line);
                if (keepComments) {
                    base.setComments(commentTemp);
                    commentTemp = new ArrayList<>();
                }
                value.add(base);
            }
        }
    }

    public void writeValue(int indent, BufferedWriter writer) throws Exception {
        if (notEmpty()) {
            Iterator<Node> it = value.iterator();
            while (it.hasNext()) {
                writeIndent(indent, writer);
                Node node = it.next();
                if (node instanceof NodeMap) {
                    writer.write("{");
                    if (node.notEmpty()) {
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                    }
                    writer.write("}");
                } else if (node instanceof NodeList) {
                    writer.write("[");
                    if (node.notEmpty()) {
                        writer.newLine();
                        node.writeValue(indent + 1, writer);
                        writer.newLine();
                        writeIndent(indent, writer);
                    }
                    writer.write("]");
                } else node.writeValue(indent + 1, writer);
                if (it.hasNext()) writer.newLine();
            }
        }
    }
}
