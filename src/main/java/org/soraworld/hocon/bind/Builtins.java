package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.DocArray;
import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.doc.DocObject;
import org.soraworld.hocon.doc.DocScalar;
import org.soraworld.hocon.error.BindException;
import org.soraworld.hocon.mirror.Mirror;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 内置适配器.
 *
 * <p>每个适配器都是无状态的，因此可以安全共享. 失败时一律给出"期望什么、实际是什么、在哪个配置项"，
 * 不再让 {@code NullPointerException} 冒到业务层.</p>
 *
 * @author Himmelt
 */
final class Builtins {

    static final TypeAdapter<String> STRING = new StringAdapter();
    static final TypeAdapter<Character> CHARACTER = new CharacterAdapter();
    static final TypeAdapter<Boolean> BOOLEAN = new BooleanAdapter();
    static final TypeAdapter<Number> NUMBER = new NumberAdapter();
    static final TypeAdapter<Enum<?>> ENUM = new EnumAdapter();
    static final TypeAdapter<Map<?, ?>> MAP = new MapAdapter();
    static final TypeAdapter<Collection<?>> COLLECTION = new CollectionAdapter();
    static final TypeAdapter<DocNode> NODE = new NodeAdapter();
    static final TypeAdapter<Object> OBJECT = new ObjectAdapter();

    private Builtins() {
    }

    private static BindException mismatch(DocNode node, BindContext context, String expect) {
        return new BindException("期望 " + expect + "，实际是 " + node.typeName(), context.path().toString());
    }

    private static DocScalar scalar(DocNode node, BindContext context, String expect) throws BindException {
        if (!(node instanceof DocScalar)) {
            throw mismatch(node, context, expect);
        }
        return (DocScalar) node;
    }

    private static String nameOf(Type type) {
        return type == null ? "<未指定>" : type.getTypeName();
    }

    // ------------------------------------------------------------ 标量

    static final class StringAdapter implements TypeAdapter<String> {

        @Override
        public String read(DocNode node, BindContext context) throws BindException {
            DocScalar scalar = scalar(node, context, "字符串");
            return scalar.isNull() ? null : scalar.lexeme();
        }

        @Override
        public DocNode write(String value, BindContext context) {
            return DocScalar.of(value);
        }
    }

    static final class CharacterAdapter implements TypeAdapter<Character> {

        @Override
        public Character read(DocNode node, BindContext context) throws BindException {
            DocScalar scalar = scalar(node, context, "单字符");
            if (scalar.isNull()) {
                return null;
            }
            if (scalar.lexeme().length() != 1) {
                throw new BindException("的值 '" + scalar.lexeme() + "' 不是单个字符", context.path().toString());
            }
            return scalar.lexeme().charAt(0);
        }

        @Override
        public DocNode write(Character value, BindContext context) {
            return DocScalar.of(String.valueOf(value));
        }
    }

    static final class BooleanAdapter implements TypeAdapter<Boolean> {

        @Override
        public Boolean read(DocNode node, BindContext context) throws BindException {
            DocScalar scalar = scalar(node, context, "逻辑值");
            return scalar.isNull() ? null : scalar.asBoolean();
        }

        @Override
        public DocNode write(Boolean value, BindContext context) {
            return DocScalar.of(value.booleanValue());
        }
    }

    static final class NumberAdapter implements TypeAdapter<Number> {

        @Override
        public Number read(DocNode node, BindContext context) throws BindException {
            DocScalar scalar = scalar(node, context, "数值");
            if (scalar.isNull()) {
                return null;
            }
            if (scalar.scalarKind() != DocScalar.ScalarKind.NUMBER) {
                throw new BindException("的值 '" + scalar.lexeme() + "' 不是数值", context.path().toString());
            }
            return parse(scalar.lexeme().trim(), Adapters.wrap(Adapters.rawOf(context.type())), context.path().toString());
        }

        @Override
        public DocNode write(Number value, BindContext context) {
            return DocScalar.of(value);
        }

        private static Number parse(String lexeme, Class<?> target, String path) throws BindException {
            try {
                if (Integer.class.equals(target)) {
                    return Integer.valueOf(lexeme);
                }
                if (Long.class.equals(target)) {
                    return Long.valueOf(lexeme);
                }
                if (Short.class.equals(target)) {
                    return Short.valueOf(lexeme);
                }
                if (Byte.class.equals(target)) {
                    return Byte.valueOf(lexeme);
                }
                if (Double.class.equals(target)) {
                    return Double.valueOf(lexeme);
                }
                if (Float.class.equals(target)) {
                    return Float.valueOf(lexeme);
                }
                if (BigInteger.class.equals(target)) {
                    return new BigInteger(lexeme);
                }
                if (BigDecimal.class.equals(target) || target == null || Number.class.equals(target)) {
                    return new BigDecimal(lexeme);
                }
            } catch (NumberFormatException e) {
                throw new BindException("的值 '" + lexeme + "' 不是 " + target.getSimpleName(), path, e);
            }
            throw new BindException("不支持把数值绑定到类型 " + target.getName(), path);
        }
    }

    static final class EnumAdapter implements TypeAdapter<Enum<?>> {

        @Override
        public Enum<?> read(DocNode node, BindContext context) throws BindException {
            DocScalar scalar = scalar(node, context, "枚举名");
            if (scalar.isNull()) {
                return null;
            }
            Class<?> raw = Adapters.rawOf(context.type());
            if (raw == null || !raw.isEnum()) {
                throw new BindException("的目标类型不是枚举", context.path().toString());
            }
            Enum<?> value = Mirror.enumOf(raw, scalar.lexeme());
            if (value == null) {
                throw new BindException("的值 '" + scalar.lexeme() + "' 不是合法枚举值，可选：" + names(raw),
                        context.path().toString());
            }
            return value;
        }

        @Override
        public DocNode write(Enum<?> value, BindContext context) {
            return DocScalar.of(value.name());
        }

        private static String names(Class<?> raw) {
            Object[] constants = raw.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return "<none>";
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < constants.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(((Enum<?>) constants[i]).name());
            }
            return builder.toString();
        }
    }

    // ------------------------------------------------------------ 容器

    static final class CollectionAdapter implements TypeAdapter<Collection<?>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<?> read(DocNode node, BindContext context) throws BindException {
            if (!(node instanceof DocArray)) {
                throw mismatch(node, context, "数组");
            }
            Type elementType = Adapters.actualType(Collection.class, context.type(), 0);
            if (elementType == null) {
                elementType = DocNode.class;
                context.report(BindReport.Code.NOTE, "元素类型未声明（裸集合类型），元素将按文档结点原样绑定");
            }
            TypeAdapter<Object> element = (TypeAdapter<Object>) context.adapters().resolve(elementType);
            if (element == null) {
                throw new BindException("元素类型 " + nameOf(elementType) + " 没有可用适配器",
                        context.path().toString());
            }
            DocArray array = (DocArray) node;
            Collection<Object> result = newCollection(context.type());
            for (int i = 0; i < array.size(); i++) {
                result.add(element.read(array.get(i), context.element(i, elementType)));
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DocNode write(Collection<?> value, BindContext context) throws BindException {
            Type elementType = Adapters.actualType(Collection.class, context.type(), 0);
            if (elementType == null) {
                elementType = DocNode.class;
            }
            TypeAdapter<Object> element = (TypeAdapter<Object>) context.adapters().resolve(elementType);
            if (element == null) {
                throw new BindException("元素类型 " + nameOf(elementType) + " 没有可用适配器",
                        context.path().toString());
            }
            DocArray array = new DocArray();
            int index = 0;
            for (Object item : value) {
                if (item == null) {
                    array.add(DocScalar.ofNull());
                } else {
                    array.add(element.write(item, context.element(index, elementType)));
                }
                index++;
            }
            return array;
        }

        @SuppressWarnings("unchecked")
        private static Collection<Object> newCollection(Type type) {
            Class<?> raw = Adapters.rawOf(type);
            if (raw == null || raw.isInterface()) {
                return defaultCollection(raw);
            }
            try {
                return (Collection<Object>) Mirror.newInstance(raw);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return defaultCollection(raw);
            }
        }

        private static Collection<Object> defaultCollection(Class<?> raw) {
            if (SortedSet.class.isAssignableFrom(raw == null ? Object.class : raw)) {
                return new TreeSet<>();
            }
            if (Set.class.isAssignableFrom(raw == null ? Object.class : raw)) {
                return new LinkedHashSet<>();
            }
            if (Deque.class.isAssignableFrom(raw == null ? Object.class : raw)) {
                return new ArrayDeque<>();
            }
            if (Queue.class.isAssignableFrom(raw == null ? Object.class : raw)) {
                return new LinkedList<>();
            }
            return new ArrayList<>();
        }
    }

    static final class MapAdapter implements TypeAdapter<Map<?, ?>> {

        @Override
        @SuppressWarnings("unchecked")
        public Map<?, ?> read(DocNode node, BindContext context) throws BindException {
            if (!(node instanceof DocObject)) {
                throw mismatch(node, context, "对象（映射）");
            }
            Type keyType = Adapters.actualType(Map.class, context.type(), 0);
            Type valueType = Adapters.actualType(Map.class, context.type(), 1);
            TypeAdapter<Object> keyAdapter = (TypeAdapter<Object>) context.adapters().resolve(
                    keyType == null ? String.class : keyType);
            TypeAdapter<Object> valueAdapter = (TypeAdapter<Object>) context.adapters().resolve(
                    valueType == null ? DocNode.class : valueType);
            if (keyAdapter == null) {
                throw new BindException("键类型 " + nameOf(keyType) + " 没有可用适配器", context.path().toString());
            }
            if (valueAdapter == null) {
                throw new BindException("值类型 " + nameOf(valueType) + " 没有可用适配器", context.path().toString());
            }
            DocObject object = (DocObject) node;
            Map<Object, Object> result = newMap(context.type());
            for (String key : object.keys()) {
                // 键在文档里就是字面字符串，用它构造标量交给键适配器；
                // 用 bare() 让 "8080" 这类键能被推断成数值，从而支持 Integer 键
                Object resolvedKey = keyAdapter.read(DocScalar.bare(key),
                        context.child(key, keyType == null ? String.class : keyType));
                Object resolvedValue = valueAdapter.read(object.get(key),
                        context.child(key, valueType == null ? DocNode.class : valueType));
                result.put(resolvedKey, resolvedValue);
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DocNode write(Map<?, ?> value, BindContext context) throws BindException {
            Type keyType = Adapters.actualType(Map.class, context.type(), 0);
            Type valueType = Adapters.actualType(Map.class, context.type(), 1);
            TypeAdapter<Object> keyAdapter = (TypeAdapter<Object>) context.adapters().resolve(
                    keyType == null ? String.class : keyType);
            TypeAdapter<Object> valueAdapter = (TypeAdapter<Object>) context.adapters().resolve(
                    valueType == null ? DocNode.class : valueType);
            if (keyAdapter == null || valueAdapter == null) {
                throw new BindException("键或值类型没有可用适配器（" + nameOf(keyType) + " / " + nameOf(valueType) + "）",
                        context.path().toString());
            }
            DocObject object = new DocObject();
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                DocNode keyNode = keyAdapter.write(entry.getKey(), context);
                if (!(keyNode instanceof DocScalar)) {
                    throw new BindException("键类型 " + nameOf(keyType) + " 必须序列化成标量",
                            context.path().toString());
                }
                // 键按字面写入，含 '.' 也不会被拆成层级（写出时会自动加引号）
                String literal = ((DocScalar) keyNode).lexeme();
                object.put(literal, valueAdapter.write(entry.getValue(), context.child(literal, valueType)));
            }
            return object;
        }

        @SuppressWarnings("unchecked")
        private static Map<Object, Object> newMap(Type type) {
            Class<?> raw = Adapters.rawOf(type);
            if (raw == null || raw.isInterface()) {
                return new LinkedHashMap<>();
            }
            try {
                return (Map<Object, Object>) Mirror.newInstance(raw);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return new LinkedHashMap<>();
            }
        }
    }

    // ------------------------------------------------------------ 结点与对象

    static final class NodeAdapter implements TypeAdapter<DocNode> {

        @Override
        public DocNode read(DocNode node, BindContext context) {
            return node.copy();
        }

        @Override
        public DocNode write(DocNode value, BindContext context) {
            return value.copy();
        }
    }

    static final class ObjectAdapter implements TypeAdapter<Object> {

        @Override
        public Object read(DocNode node, BindContext context) throws BindException {
            if (!(node instanceof DocObject)) {
                throw mismatch(node, context, "对象");
            }
            Class<?> type = Adapters.rawOf(context.type());
            if (type == null) {
                throw new BindException("无法确定要绑定的类", context.path().toString());
            }
            Schema schema = Schema.of(type);
            if (schema.isEmpty()) {
                throw new BindException("类 " + type.getName()
                        + " 没有任何 @Setting 字段，无法绑定；请为它注册专用适配器", context.path().toString());
            }
            Object instance;
            try {
                instance = Mirror.newInstance(type);
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new BindException("类 " + type.getName()
                        + " 必须具有可访问的无参构造器", context.path().toString(), e);
            }
            Binder.bind((DocObject) node, instance, schema, context);
            return instance;
        }

        @Override
        public DocNode write(Object value, BindContext context) throws BindException {
            return Binder.writeObject(value, context);
        }
    }
}
