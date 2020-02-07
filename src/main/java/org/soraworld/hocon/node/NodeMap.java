package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.util.Reflects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static org.soraworld.hocon.node.Options.READ;
import static org.soraworld.hocon.node.Options.WRITE;

/**
 * 映射结点类.
 *
 * @author Himmelt
 */
public class NodeMap extends AbstractNode<LinkedHashMap<String, Node>> implements Node {

    public NodeMap(@NotNull NodeMap origin) {
        super(origin.options, new LinkedHashMap<>(), origin.comments);
        origin.value.forEach((key, val) -> this.value.put(key, val.copy()));
    }

    /**
     * 实例化一个新的映射结点.
     *
     * @param options 配置选项
     */
    public NodeMap(@NotNull Options options) {
        super(options, new LinkedHashMap<>());
    }

    /**
     * 实例化一个新的映射结点.
     *
     * @param options 配置选项
     * @param comment 注释
     */
    public NodeMap(@NotNull Options options, String comment) {
        super(options, new LinkedHashMap<>(), comment);
    }

    public NodeMap(@NotNull Options options, List<String> comments) {
        super(options, new LinkedHashMap<>(), comments);
    }

    /**
     * 用 {@link NodeMap} 里结点的值修改对象 {@link Setting} 修饰的字段.<br>
     * !!! 特别注意<br>
     * 此方法不会修改 对应结点不存在，或 对应结点反序列化失败 的字段<br>
     * 特别建议: 如果需要设置默认值 或 非首次执行，建议在调用此方法之前，<br>
     * 对目标对象的{@link Setting}字段进行 初始化 或 设置默认值 或 !!清空之前的内容!!<br>
     * 尤其是集合和映射，有可能在执行此方法之前就有内容，可根据需求选择是否清空当前内容 !!!<br>
     * <br>
     * 如果 集合或映射 的字段当前值为空，反序列化之后会对字段进行初始化，<br>
     * 因此要求对应 集合或映射 的类要有无参构造函数,<br>
     * 比如 {@link HashMap},{@link LinkedHashMap},{@link ArrayList},<br>
     * {@link LinkedList},{@link HashSet},{@link TreeMap} 等等,<br>
     * 因此建议在字段声明时使用准确的类型而不是 {@link Map},{@link Set},{@link List} 这样的类型<br>
     *
     * @param target 修改对象
     */
    public void modify(@NotNull Object target) {
        List<Field> fields = target instanceof Class<?> ? Reflects.getStaticFields((Class<?>) target) : Reflects.getFields(target.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                Type fieldType = field.getGenericType();
                TypeSerializer<Object, Node> serializer = options.getSerializer(fieldType);
                if (serializer != null) {
                    Paths paths = new Paths(setting.path().isEmpty() ? field.getName() : setting.path());
                    Node node = get(paths);
                    if (node != null) {
                        try {
                            if ((setting.trans() & 0b1010) != 0) {
                                node.translate(READ);
                            }
                            field.set(target, serializer.deserialize(fieldType, node));
                        } catch (Throwable e) {
                            if (options.isDebug()) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (options.isDebug()) {
                    System.out.println("No TypeSerializer for the type of field "
                            + field.getDeclaringClass().getTypeName() + "." + field.getName()
                            + " with @Setting.");
                }
            }
        }
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * 保留旧结点的注释, 清除所有旧结点内容.
     *
     * @param source 源对象
     */
    public void extract(@NotNull Object source) {
        extract(source, true, true, true);
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * 清除所有旧结点内容.
     *
     * @param source      源对象
     * @param keepComment 是否保留旧结点注释
     */
    public void extract(@NotNull Object source, boolean keepComment) {
        extract(source, keepComment, true, true);
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * 覆盖旧结点内容.<br>
     *
     * @param source      源对象
     * @param keepComment 是否保留旧结点注释
     * @param clearOld    是否清除所有旧结点
     */
    public void extract(@NotNull Object source, boolean keepComment, boolean clearOld) {
        extract(source, keepComment, clearOld, true);
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * !! 注意:<br>
     * 如果存在两个 {@link Setting#path()} 相同的字段, 靠后的字段将无法提取<br>
     * 在调试模式下会输出相关警告信息.
     *
     * @param source      源对象
     * @param keepComment 是否保留旧结点注释
     * @param clearOld    是否清除所有旧结点
     * @param overwrite   是否覆盖旧结点内容
     */
    public void extract(@NotNull Object source, boolean keepComment, boolean clearOld, boolean overwrite) {
        NodeMap oldNode = new NodeMap(this);
        if (clearOld) {
            value.clear();
        }
        List<Field> fields = source instanceof Class<?> ? Reflects.getStaticFields((Class<?>) source) : Reflects.getFields(source.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                try {
                    Paths paths = new Paths(setting.path().isEmpty() ? field.getName() : setting.path());
                    String comment = (setting.trans() & 0b1001) == 0 ? setting.comment() : options.translateComment(setting.comment(), paths);
                    Node old = oldNode.get(paths.copy());
                    List<String> list = old != null ? old.getComments() : null;
                    Type fieldType = field.getGenericType();
                    TypeSerializer<Object, Node> serializer = options.getSerializer(fieldType);
                    if (serializer == null && fieldType instanceof Class<?> && Serializable.class.isAssignableFrom((Class<?>) fieldType)) {
                        serializer = options.getSerializer(Serializable.class);
                    }
                    if (serializer != null) {
                        Node node = serializer.serialize(fieldType, field.get(source), options);
                        if ((setting.trans() & 0b1100) != 0) {
                            node.translate(WRITE);
                        }
                        if (overwrite) {
                            if (set(paths, node, comment)) {
                                if (comment.isEmpty() && keepComment) {
                                    node.setComments(list);
                                }
                            } else if (options.isDebug()) {
                                System.out.println("NodeMap set failed, paths is empty or not-map path !!");
                            }
                        } else if (put(paths, node, comment)) {
                            if (comment.isEmpty() && keepComment) {
                                node.setComments(list);
                            }
                        } else if (options.isDebug()) {
                            System.out.println("NodeMap put failed, node type not match !!");
                        }
                    } else if (options.isDebug()) {
                        System.out.println("No TypeSerializer for the type of field "
                                + field.getDeclaringClass().getTypeName() + "." + field.getName()
                                + " with @Setting.");
                    }
                } catch (HoconException | IllegalAccessException e) {
                    if (options.isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * map 的不可变键集合.
     *
     * @return 不可变键集合
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(value.keySet());
    }

    /**
     * 清空 map.
     */
    public void clear() {
        value.clear();
    }

    /**
     * 获取 map 大小.
     *
     * @return size
     */
    public int size() {
        return value.size();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public boolean containsKey(String key) {
        return value.containsKey(key);
    }

    public boolean containsValue(Node node) {
        return value.containsValue(node);
    }

    public boolean put(@NotNull String paths, @NotNull Object obj) {
        return put(new Paths(paths), obj, "");
    }

    public boolean put(@NotNull String paths, @NotNull Object obj, String comment) {
        return put(new Paths(paths), obj, comment);
    }

    public boolean put(@NotNull Paths paths, @NotNull Object obj) {
        return put(paths, obj, "");
    }

    public boolean put(@NotNull Paths paths, @NotNull Object obj, String comment) {
        if (paths.empty()) {
            return false;
        }
        if (paths.hasNext()) {
            Node parent = value.get(paths.first());
            if (parent == null) {
                // 父结点为空，满足条件，直接 set
                NodeMap map = new NodeMap(options);
                value.put(paths.first(), map);
                return map.set(paths.next(), obj, comment);
            }
            if (parent instanceof NodeMap) {
                return ((NodeMap) parent).put(paths.next(), obj, comment);
            }
            // 中间结点不是 NodeMap 无法添加子结点
            return false;
        }
        // 最后结点
        Node old = value.get(paths.first());
        Node node = options.serialize(obj);
        if (node == null) {
            node = new NodeBase(options, String.valueOf(obj));
        }
        node.addComment(comment);
        // 最后结点为空或可接受类型
        if (old == null || old.getType() == node.getType()) {
            value.put(paths.first(), node);
            return true;
        }
        return false;
    }

    public boolean set(String paths, @NotNull Object obj) {
        return set(new Paths(paths), obj);
    }

    public boolean set(String paths, @NotNull Object obj, String comment) {
        return set(new Paths(paths), obj, comment);
    }

    public boolean set(@NotNull Paths paths, @NotNull Object obj) {
        return set(paths, obj, "");
    }

    public boolean set(@NotNull Paths paths, @NotNull Object obj, String comment) {
        if (paths.empty()) {
            return false;
        }
        if (paths.hasNext()) {
            Node parent = value.computeIfAbsent(paths.first(), key -> new NodeMap(options));
            if (parent instanceof NodeMap) {
                return ((NodeMap) parent).set(paths.next(), obj, comment);
            }
            return false;
        }
        // 最后结点
        Node node = options.serialize(obj);
        if (node == null) {
            node = new NodeBase(options, String.valueOf(obj));
        }
        node.addComment(comment);
        value.put(paths.first(), node);
        return true;
    }

    public Node get(String paths) {
        return get(new Paths(paths));
    }

    public Node get(@NotNull Paths paths) {
        if (paths.hasNext()) {
            Node node = value.get(paths.first());
            if (node instanceof NodeMap) {
                return ((NodeMap) node).get(paths.next());
            } else {
                return null;
            }
        }
        return value.get(paths.first());
    }

    public NodeBase getBase(String paths) {
        Node node = get(new Paths(paths));
        return node instanceof NodeBase ? (NodeBase) node : null;
    }

    public NodeList getList(String paths) {
        Node node = get(new Paths(paths));
        return node instanceof NodeList ? (NodeList) node : null;
    }

    public NodeMap getMap(String paths) {
        Node node = get(new Paths(paths));
        return node instanceof NodeMap ? (NodeMap) node : null;
    }

    /**
     * 移除路径对应结点.
     *
     * @param paths 路径
     * @return 移除的结点
     */
    public Node remove(String paths) {
        return remove(new Paths(paths));
    }

    /**
     * 移除路径对应结点.
     *
     * @param paths 路径
     * @return 移除的结点
     */
    public Node remove(@NotNull Paths paths) {
        if (paths.hasNext()) {
            Node node = value.get(paths.first());
            if (node instanceof NodeMap) {
                return ((NodeMap) node).remove(paths.next());
            } else {
                return null;
            }
        }
        return value.remove(paths.first());
    }

    /**
     * 为对应路径的结点添加注释.
     *
     * @param paths   路径
     * @param comment 注释
     */
    public void addComment(String paths, String comment) {
        addComment(new Paths(paths), comment);
    }

    public void addComment(@NotNull Paths paths, String comment) {
        Node node = get(paths);
        if (node != null) {
            node.addComment(comment);
        }
    }

    /**
     * 为对应路径的结点设置多行注释.
     *
     * @param paths    路径
     * @param comments 多行注释
     */
    public void setComments(String paths, List<String> comments) {
        setComments(new Paths(paths), comments);
    }

    public void setComments(@NotNull Paths paths, List<String> comments) {
        Node node = get(paths);
        if (node != null) {
            node.setComments(comments);
        }
    }

    /**
     * 获取全部基础结点的字符串映射集合.
     * 路径树用'.'连接作为键.
     * 例: {@code path1.path2.path3 -> node3.toString() }
     *
     * @return 字符串映射集合
     */
    public HashMap<String, String> asStringMap() {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, Node> entry : value.entrySet()) {
            String key = entry.getKey();
            Node node = entry.getValue();
            if (node instanceof NodeBase) {
                map.put(key, ((NodeBase) node).getString());
            } else if (node instanceof NodeMap) {
                HashMap<String, String> sub = ((NodeMap) node).asStringMap();
                sub.forEach((subKey, value) -> map.put(key + '.' + subKey, value));
            }
        }
        return map;
    }

    @Override
    public boolean notEmpty() {
        return !value.isEmpty();
    }

    @Override
    public void readValue(BufferedReader reader, boolean keepComments) throws Exception {
        value.clear();
        String line;
        ArrayList<String> commentTemp = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("}") || line.startsWith("]")) {
                return;
            }
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                if (keepComments) {
                    int index = line.startsWith("#! ") ? 3 : line.startsWith("#!") || line.startsWith("# ") ? 2 : 1;
                    String text = line.substring(index);
                    if (line.startsWith("#!") && this instanceof FileNode) {
                        ((FileNode) this).addHead(text);
                    } else {
                        commentTemp.add(text);
                    }
                } else {
                    continue;
                }
            }
            // text maybe contains { [ ] } ...
            if (line.endsWith("{") || (line.contains("{") && line.endsWith("}"))) {
                NodeMap node = new NodeMap(options);
                if (keepComments) {
                    node.setComments(commentTemp);
                    commentTemp = new ArrayList<>();
                }
                String path = line.substring(0, line.indexOf('{') - 1).trim();
                value.put(unquotation(path), node);
                if (!line.endsWith("}")) {
                    node.readValue(reader, keepComments);
                }
            } else {
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                if (line.contains("=") && (line.endsWith("[") || (line.contains("[") && line.endsWith("]")))) {
                    NodeList list = new NodeList(options);
                    if (keepComments) {
                        list.setComments(commentTemp);
                        commentTemp = new ArrayList<>();
                    }
                    value.put(unquotation(path), list);
                    if (!line.endsWith("]")) {
                        list.readValue(reader, keepComments);
                    }
                } else if (line.contains("=")) {
                    String text = line.substring(line.indexOf('=') + 1).trim();
                    NodeBase base = new NodeBase(options, unquotation(text));
                    if (keepComments) {
                        base.setComments(commentTemp);
                        commentTemp = new ArrayList<>();
                    }
                    value.put(unquotation(path), base);
                }
            }
        }
    }

    @Override
    public void writeValue(int indent, BufferedWriter writer) throws Exception {
        if (notEmpty()) {
            Iterator<Map.Entry<String, Node>> it = value.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Node> entry = it.next();
                String path = entry.getKey();
                Node node = entry.getValue();
                if (path != null && !path.isEmpty() && node != null) {
                    node.writeComment(indent, writer);
                    writeIndent(indent, writer);
                    writer.write(quotation(path));
                    if (node instanceof NodeMap) {
                        writer.write(" {");
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write('}');
                    } else if (node instanceof NodeList) {
                        writer.write(" = [");
                        if (node.notEmpty()) {
                            writer.newLine();
                            node.writeValue(indent + 1, writer);
                            writer.newLine();
                            writeIndent(indent, writer);
                        }
                        writer.write(']');
                    } else {
                        writer.write(" = ");
                        node.writeValue(indent + 1, writer);
                    }
                    if (it.hasNext()) {
                        writer.newLine();
                    }
                }
            }
        }
    }

    @Override
    public void translate(byte cfg) {
        value.values().forEach(val -> val.translate(cfg));
    }

    @Override
    public NodeMap copy() {
        return new NodeMap(this);
    }

    @Override
    public final byte getType() {
        return TYPE_MAP;
    }
}
