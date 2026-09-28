package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.DocPath;
import org.soraworld.hocon.mirror.Mirror;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置结构（Schema）.
 *
 * <p>Schema 是把"对象"和"文档"连起来的<strong>显式契约</strong>：从类的 {@link Setting} 字段反射一次得出，
 * 之后加载、保存、诊断、本地化都基于它，而不是每次重新猜. 反射结果按类缓存.</p>
 *
 * @author Himmelt
 */
public final class Schema {

    private static final ConcurrentHashMap<String, Schema> CACHE = new ConcurrentHashMap<>();

    /**
     * 一个配置项的描述.
     */
    public static final class FieldSpec {

        private final Mirror.FieldInfo field;
        private final DocPath path;
        private final String comment;
        private final boolean translate;

        FieldSpec(Mirror.FieldInfo field, DocPath path, String comment, boolean translate) {
            this.field = field;
            this.path = path;
            this.comment = comment;
            this.translate = translate;
        }

        /**
         * 获取字段信息.
         *
         * @return 字段信息
         */
        public Mirror.FieldInfo field() {
            return field;
        }

        /**
         * 获取字段名.
         *
         * @return 字段名
         */
        public String name() {
            return field.name();
        }

        /**
         * 获取配置路径.
         *
         * @return 配置路径
         */
        public DocPath path() {
            return path;
        }

        /**
         * 获取字段的泛型类型.
         *
         * @return 泛型类型
         */
        public Type type() {
            return field.type();
        }

        /**
         * 获取注释键或注释文本.
         *
         * @return 注释
         */
        public String comment() {
            return comment;
        }

        /**
         * 注释是否需要翻译（本地化）.
         *
         * @return 需要翻译返回 true
         */
        public boolean translate() {
            return translate;
        }

        /**
         * 字段是否可写.
         *
         * @return 可写返回 true
         */
        public boolean writable() {
            return field.writable();
        }

        @Override
        public String toString() {
            return path + " <- " + field.name() + "(" + type().getTypeName() + ")";
        }
    }

    private final Class<?> type;
    private final boolean statics;
    private final List<FieldSpec> fields;

    private Schema(Class<?> type, boolean statics, List<FieldSpec> fields) {
        this.type = type;
        this.statics = statics;
        this.fields = Collections.unmodifiableList(fields);
    }

    /**
     * 获取某个类的配置结构（实例字段）.
     *
     * @param type 类型
     * @return 配置结构
     */
    public static Schema of(Class<?> type) {
        return of(type, false);
    }

    /**
     * 获取某个类的配置结构.
     *
     * @param type    类型
     * @param statics 是否使用静态字段
     * @return 配置结构
     */
    public static Schema of(Class<?> type, boolean statics) {
        String key = type.getName() + '#' + statics;
        Schema cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<FieldSpec> fields = new ArrayList<>();
        for (Mirror.FieldInfo field : statics ? Mirror.staticFields(type) : Mirror.instanceFields(type)) {
            Setting setting = field.field().getAnnotation(Setting.class);
            if (setting == null) {
                continue;
            }
            DocPath path = setting.path().isEmpty() ? DocPath.of(field.name()) : DocPath.parse(setting.path());
            fields.add(new FieldSpec(field, path, setting.comment(), setting.translate()));
        }
        Schema schema = new Schema(type, statics, fields);
        CACHE.put(key, schema);
        return schema;
    }

    /**
     * 判断某个类是否含 {@link Setting} 字段.
     *
     * @param type 类型
     * @return 含注解字段返回 true
     */
    public static boolean annotated(Class<?> type) {
        return !of(type).fields.isEmpty();
    }

    /**
     * 获取被描述的类.
     *
     * @return 类型
     */
    public Class<?> type() {
        return type;
    }

    /**
     * 是否使用静态字段.
     *
     * @return 静态字段模式返回 true
     */
    public boolean statics() {
        return statics;
    }

    /**
     * 获取全部配置项.
     *
     * @return 配置项列表
     */
    public List<FieldSpec> fields() {
        return fields;
    }

    /**
     * 是否没有任何配置项.
     *
     * @return 没有配置项返回 true
     */
    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
