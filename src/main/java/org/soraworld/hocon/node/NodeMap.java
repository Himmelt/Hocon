package org.soraworld.hocon.node;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.NonRawTypeException;
import org.soraworld.hocon.exception.NotParamListException;
import org.soraworld.hocon.exception.NotParamMapException;
import org.soraworld.hocon.reflect.Primitives;
import org.soraworld.hocon.reflect.Reflects;
import org.soraworld.hocon.serializer.TypeSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 映射结点类.
 */
public class NodeMap extends AbstractNode<LinkedHashMap<String, Node>> implements Node {

    private NodeMap(NodeMap source) {
        super(source.options, source.value);
    }

    /**
     * 实例化一个新的映射结点.
     *
     * @param options 配置选项
     */
    public NodeMap(Options options) {
        super(options, new LinkedHashMap<>());
    }

    /**
     * 实例化一个新的映射结点.
     *
     * @param options 配置选项
     * @param comment 注释
     */
    public NodeMap(Options options, String comment) {
        super(options, new LinkedHashMap<>(), comment);
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
     * <br>
     * 另外, {@link Setting#nullable()}为 true 的非原生类型字段, <br>
     * 在对应结点反序列化结果是 null 时, 会被覆盖成 null
     *
     * @param target 修改对象
     */
    public void modify(Object target) {
        if (target == null) return;
        List<Field> fields = Reflects.getFields(target.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                Type fieldType = field.getGenericType();
                TypeSerializer serializer = options.getSerializer(fieldType);
                if (serializer == null && fieldType instanceof Class && ((Class) fieldType).isAnnotationPresent(Serializable.class)) {
                    serializer = options.getSerializer(Serializable.class);
                }
                if (serializer != null) {
                    Paths paths = new Paths(setting.path().isEmpty() ? field.getName() : setting.path());
                    Node node = get(paths);
                    if (node != null) {
                        try {
                            Object current = field.get(target);
                            Class<?> clzType = current == null ? field.getType() : current.getClass();
                            Object value = serializer.deserialize(fieldType, node);
                            if (value != null) {
                                if (Map.class.isAssignableFrom(clzType) && value instanceof Map) {
                                    try {
                                        if (current == null || current instanceof Map) {
                                            value = transMap((Map) value, (Map) current, fieldType);
                                        }
                                    } catch (Throwable e) {
                                        if (options.isDebug()) e.printStackTrace();
                                    }
                                    field.set(target, value);
                                    continue;
                                }
                                if (Collection.class.isAssignableFrom(clzType) && value instanceof Collection) {
                                    if (current == null || current instanceof Collection) {
                                        value = transCollection((Collection) value, (Collection) current, fieldType);
                                    }
                                    field.set(target, value);
                                    continue;
                                }
                                field.set(target, value);
                            } else if (setting.nullable() && !Primitives.isNative(clzType)) {
                                field.set(target, null);
                            }
                        } catch (Throwable e) {
                            if (options.isDebug()) e.printStackTrace();
                        }
                    }
                } else if (options.isDebug()) System.out.println("No TypeSerializer for the type of field "
                        + field.getDeclaringClass().getTypeName() + "." + field.getName()
                        + " with @Setting.");
            }
        }
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * 保留旧结点的注释, 清除所有旧结点内容.
     *
     * @param source 源对象
     */
    public void extract(Object source) {
        extract(source, true, true, true);
    }

    /**
     * 提取对象{@link Setting} 修饰的字段的值到map对应的结点.<br>
     * 清除所有旧结点内容.
     *
     * @param source      源对象
     * @param keepComment 是否保留旧结点注释
     */
    public void extract(Object source, boolean keepComment) {
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
    public void extract(Object source, boolean keepComment, boolean clearOld) {
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
    public void extract(Object source, boolean keepComment, boolean clearOld, boolean overwrite) {
        NodeMap oldNode = new NodeMap(this);
        if (clearOld) value.clear();
        List<Field> fields = Reflects.getFields(source.getClass());
        for (Field field : fields) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting != null) {
                try {
                    Paths paths = new Paths(setting.path().isEmpty() ? field.getName() : setting.path());
                    String comment = options.getTranslator().apply(setting.comment());
                    Node old = oldNode.get(paths.clone());
                    List<String> list = old != null ? old.getComments() : null;
                    Type fieldType = field.getGenericType();
                    TypeSerializer serializer = options.getSerializer(fieldType);
                    if (serializer == null && fieldType instanceof Class && ((Class) fieldType).isAnnotationPresent(Serializable.class)) {
                        serializer = options.getSerializer(Serializable.class);
                    }
                    if (serializer != null) {
                        Node node = serializer.serialize(fieldType, field.get(source), options);
                        if (overwrite) {
                            if (set(paths, node, comment)) {
                                if (comment.isEmpty() && keepComment) node.setComments(list);
                            } else if (options.isDebug()) System.out.println("NodeMap set failed, paths is empty or not map path !!");
                        } else if (put(paths, node, comment)) {
                            if (comment.isEmpty() && keepComment) node.setComments(list);
                        } else if (options.isDebug()) System.out.println("NodeMap put failed, node not match or already exist !!");
                    } else if (options.isDebug()) System.out.println("No TypeSerializer for the type of field "
                            + field.getDeclaringClass().getTypeName() + "." + field.getName()
                            + " with @Setting.");
                } catch (HoconException | IllegalAccessException e) {
                    if (options.isDebug()) e.printStackTrace();
                }
            }
        }
    }

    /**
     * map 的键集合.
     *
     * @return 键集合
     */
    public Set<String> keys() {
        return value.keySet();
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

    /**
     * 添加一个新的结点映射.
     * 如果对应路径树上已有非空结点，则失败.
     *
     * @param paths   路径树
     * @param obj     对象
     * @param comment 注释
     * @return 是否成功
     */
    public boolean put(Paths paths, Object obj, String comment) {
        if (paths.empty()) return false;
        if (paths.hasNext()) {
            Node parent = get(paths.first());
            if (parent == null) {
                parent = new NodeMap(options);
                set(paths.first(), parent);
            }
            if (parent instanceof NodeMap) return ((NodeMap) parent).put(paths.next(), obj, comment);
            return false;
        }
        return put(paths.first(), obj, comment);
    }

    /**
     * 添加一个新的结点映射.
     * 如果对应路径上已有非空结点，则失败.
     *
     * @param path 路径
     * @param obj  对象
     * @return 是否成功
     */
    public boolean put(String path, Object obj) {
        if (value.get(path) != null) return false;
        return set(path, obj);
    }

    /**
     * 添加一个新的结点映射.
     * 如果对应路径上已有非空结点，则失败.
     *
     * @param path    路径
     * @param obj     对象
     * @param comment 注释
     * @return 是否成功
     */
    public boolean put(String path, Object obj, String comment) {
        if (value.get(path) != null) return false;
        return set(path, obj, comment);
    }

    /**
     * 获取路径对应的结点.
     *
     * @param path 路径
     * @return 对应结点
     */
    public Node get(String path) {
        return value.get(path);
    }

    /**
     * 获取路径树对应的结点.
     *
     * @param paths 路径树
     * @return 对应结点
     */
    public Node get(Paths paths) {
        if (paths.hasNext()) {
            Node node = get(paths.first());
            if (node instanceof NodeMap) return ((NodeMap) node).get(paths.next());
            else return null;
        }
        return get(paths.first());
    }

    /**
     * 添加一个新的结点映射.<br>
     * 如果对应路径树上已有非空结点，则覆盖.<br>
     * 如果路径中间存在 非空非Map 结点则失败.
     *
     * @param paths   路径树
     * @param obj     对象
     * @param comment 注释
     * @return 是否成功
     */
    public boolean set(Paths paths, Object obj, String comment) {
        if (paths.empty()) return false;
        if (paths.hasNext()) {
            Node parent = get(paths.first());
            if (parent == null) {
                parent = new NodeMap(options);
                set(paths.first(), parent);
            }
            if (parent instanceof NodeMap) return ((NodeMap) parent).set(paths.next(), obj, comment);
            return false;
        }
        return set(paths.first(), obj, comment);
    }

    /**
     * 强制设置(覆盖)路径对应结点.
     * 如果存在循环引用则失败.
     *
     * @param path 路径
     * @param obj  对象
     * @return 是否成功
     */
    public boolean set(String path, Object obj) {
        if (obj instanceof Node) {
            if (checkCycle((Node) obj)) {
                value.put(path, (Node) obj);
                return true;
            }
            if (options.isDebug()) System.out.println("NodeMap Cycle Reference !!");
            return false;
        }
        value.put(path, new NodeBase(options, obj, false));
        return true;
    }

    /**
     * 强制设置(覆盖)路径对应结点.
     * 如果存在循环引用则失败.
     *
     * @param path    路径
     * @param obj     对象
     * @param comment 注释
     * @return 是否成功
     */
    public boolean set(String path, Object obj, String comment) {
        if (obj instanceof Node) {
            if (checkCycle((Node) obj)) {
                ((Node) obj).addComment(comment);
                value.put(path, (Node) obj);
                return true;
            }
            if (options.isDebug()) System.out.println("NodeMap Cycle Reference !!");
            return false;
        }
        value.put(path, new NodeBase(options, obj, false, comment));
        return true;
    }

    /**
     * 移除路径对应结点.
     *
     * @param path 路径
     */
    public void remove(String path) {
        value.remove(path);
    }

    /**
     * 移除路径和结点.
     *
     * @param path 路径
     * @param node 结点
     */
    public void remove(String path, Node node) {
        value.remove(path, node);
    }

    /**
     * 为对应路径的结点添加注释.
     *
     * @param path    路径
     * @param comment 注释
     */
    public void addComment(String path, String comment) {
        Node node = value.get(path);
        if (node != null) node.addComment(comment);
    }

    /**
     * 为对应路径的结点设置多行注释.
     *
     * @param path     路径
     * @param comments 多行注释
     */
    public void setComments(String path, List<String> comments) {
        Node node = value.get(path);
        if (node != null) node.setComments(comments);
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

    public boolean notEmpty() {
        return value != null && !value.isEmpty();
    }

    public void readValue(BufferedReader reader) throws Exception {
        value.clear();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("}") || line.startsWith("]")) return;
            if (line.isEmpty() || line.startsWith("#")) continue;
            // text maybe contains { [ ] } ...
            if (line.endsWith("{") || (line.contains("{") && line.endsWith("}"))) {
                NodeMap node = new NodeMap(options);
                String path = line.substring(0, line.indexOf('{') - 1).trim();
                value.put(unquotation(path), node);
                if (!line.endsWith("}")) node.readValue(reader);
            } else if (line.contains("=") && (line.endsWith("[") || (line.contains("[") && line.endsWith("]")))) {
                NodeList list = new NodeList(options);
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                value.put(unquotation(path), list);
                if (!line.endsWith("]")) list.readValue(reader);
            } else if (line.contains("=")) {
                String path = line.substring(0, line.indexOf('=') - 1).trim();
                String text = line.substring(line.indexOf('=') + 1).trim();
                value.put(unquotation(path), new NodeBase(options, text, true));
            }
        }
    }

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
                    if (it.hasNext()) writer.newLine();
                }
            }
        }
    }

    /* source 不得为 null */
    private static Map<?, ?> transMap(Map<?, ?> source, Map target, Type targetType) throws HoconException, NotParamMapException, NonRawTypeException, NotParamListException {
        if (target == null) {
            Class<?> rawType = Reflects.getRawType(targetType);
            if (rawType.equals(source.getClass()) || rawType.equals(Map.class)) return source;
            if (rawType.equals(HashMap.class) || rawType.equals(LinkedHashMap.class)) {
                LinkedHashMap map = new LinkedHashMap();
                map.putAll(source);
                return map;
            }
            if (rawType.equals(ConcurrentMap.class) || rawType.equals(ConcurrentHashMap.class)) {
                ConcurrentMap map = new ConcurrentHashMap();
                map.putAll(source);
                return map;
            }
            if (rawType.equals(TreeMap.class) || rawType.equals(SortedMap.class) || rawType.equals(NavigableMap.class)) {
                TreeMap map = new TreeMap();
                map.putAll(source);
                return map;
            }
            try {
                target = (Map<?, ?>) rawType.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new HoconException("Class " + rawType.getName() + " must have public non-parameter constructor !!");
            }
        } else target.clear();

        if (targetType instanceof ParameterizedType) {
            Type[] params = Reflects.getMapParameter((ParameterizedType) targetType);
            Type keyType = params[0];
            Type valType = params[1];
            if (keyType instanceof Class<?>) {
                Class<?> keyClazz = (Class<?>) keyType;
                if (String.class.isAssignableFrom(keyClazz) || NodeBase.class.isAssignableFrom(keyClazz)) {
                    if (valType instanceof ParameterizedType) {
                        Class<?> rawValClazz = (Class<?>) ((ParameterizedType) valType).getRawType();
                        if (Map.class.isAssignableFrom(rawValClazz)) {
                            for (Map.Entry<?, ?> entry : source.entrySet()) {
                                Object objKey = entry.getKey();
                                Object objVal = entry.getValue();
                                if (objKey instanceof String || objKey instanceof NodeBase) {
                                    if (objVal instanceof Map<?, ?>) {
                                        Map<?, ?> value = transMap((Map<?, ?>) objVal, null, valType);
                                        target.put(objKey, value);
                                    }
                                }
                            }
                        } else if (Collection.class.isAssignableFrom(rawValClazz)) {
                            for (Map.Entry<?, ?> entry : source.entrySet()) {
                                Object objKey = entry.getKey();
                                Object objVal = entry.getValue();
                                if (objKey instanceof String || objKey instanceof NodeBase) {
                                    if (objVal instanceof Collection<?>) {
                                        Collection<?> value = transCollection((Collection<?>) objVal, null, valType);
                                        target.put(objKey, value);
                                    }
                                }
                            }
                        } else transfer(source, target, rawValClazz);
                    } else if (valType instanceof Class<?>) transfer(source, target, (Class<?>) valType);
                    else throw new HoconException("Unexpected Map value type : " + valType.getTypeName());
                } else throw new HoconException("Key Type for Map must be String or NodeBase !!!");
            } else throw new HoconException("Key Type for Map must be String or NodeBase !!!");
        } else if (targetType instanceof Class<?>) {
            target.putAll(source);
        }
        return target;
    }

    /* source 不得为 null */
    private static void transfer(Map<?, ?> source, Map target, Class<?> targetClazz) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Object objKey = entry.getKey();
            Object objVal = entry.getValue();
            if (objKey instanceof String || objKey instanceof NodeBase) {
                if (targetClazz.isAssignableFrom(objVal.getClass())) {
                    target.put(objKey, objVal);
                }
            }
        }
    }

    /* source 不得为 null */
    private static Collection<?> transCollection(Collection<?> source, Collection target, Type targetType) throws HoconException, NotParamListException, NotParamMapException, NonRawTypeException {
        if (target == null) {
            Class<?> rawType = Reflects.getRawType(targetType);
            if (rawType.equals(source.getClass()) || rawType.equals(Collection.class)) return source;
            if (rawType.equals(List.class) || rawType.equals(Queue.class) || rawType.equals(Deque.class)) {
                LinkedList list = new LinkedList();
                list.addAll(source);
                return list;
            }
            if (rawType.equals(ArrayList.class)) {
                ArrayList list = new ArrayList();
                list.addAll(source);
                return list;
            }
            if (rawType.equals(Set.class) || rawType.equals(HashSet.class) || rawType.equals(LinkedHashSet.class)) {
                LinkedHashSet set = new LinkedHashSet();
                set.addAll(source);
                return set;
            }
            if (rawType.equals(TreeSet.class) || rawType.equals(NavigableSet.class) || rawType.equals(SortedSet.class)) {
                TreeSet set = new TreeSet();
                set.addAll(source);
                return set;
            }
            try {
                target = (Collection<?>) rawType.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new HoconException("Class " + rawType.getName() + " must have public non-parameter constructor !!");
            }
        } else target.clear();

        if (targetType instanceof ParameterizedType) {
            Type elementType = Reflects.getListParameter((ParameterizedType) targetType);
            if (elementType instanceof ParameterizedType) {
                Class<?> elementRawClazz = (Class<?>) ((ParameterizedType) elementType).getRawType();
                if (Map.class.isAssignableFrom(elementRawClazz)) {
                    for (Object element : source) {
                        if (element instanceof Map<?, ?>) {
                            Map<?, ?> value = transMap((Map<?, ?>) element, null, elementType);
                            target.add(value);
                        }
                    }
                } else if (Collection.class.isAssignableFrom(elementRawClazz)) {
                    for (Object element : source) {
                        if (element instanceof Collection<?>) {
                            Collection<?> value = transCollection((Collection<?>) element, null, elementType);
                            target.add(value);
                        }
                    }
                } else {
                    for (Object value : source) {
                        if (elementRawClazz.isAssignableFrom(value.getClass())) {
                            target.add(value);
                        }
                    }
                }
            } else if (elementType instanceof Class<?>) {
                Class<?> elementClazz = (Class<?>) elementType;
                for (Object object : source) {
                    if (elementClazz.isAssignableFrom(object.getClass())) {
                        target.add(object);
                    }
                }
            } else throw new HoconException("Unexpected element type : " + elementType.getTypeName());
        } else if (targetType instanceof Class<?>) {
            target.addAll(source);
        }
        return target;
    }
}
