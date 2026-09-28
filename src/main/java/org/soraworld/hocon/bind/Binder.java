package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.doc.DocArray;
import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.doc.DocObject;
import org.soraworld.hocon.doc.DocPath;
import org.soraworld.hocon.doc.Trivia;
import org.soraworld.hocon.error.BindException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 绑定器：文档 ⇄ 对象.
 *
 * <p>两个方向的语义刻意不对称：</p>
 * <ul>
 *   <li><strong>读</strong>是"填"：按结构把值填进对象；缺失、多余、类型不符都进
 *       {@link BindReport}，缺什么、多了什么一目了然；</li>
 *   <li><strong>写</strong>是"合并"（默认 {@link BindingPolicy.SaveMode#MERGE}）：在已有文档上按路径原地更新，
 *       已有条目连注释、顺序、紧凑度一起保留，新条目追加到末尾.
 *       写路径里<strong>没有"清空重建"这一步</strong>，所以"改了值、注释没了"在结构上不会发生.</li>
 * </ul>
 *
 * @author Himmelt
 */
public final class Binder {

    private Binder() {
    }

    /**
     * 从文档读取到对象.
     *
     * @param doc      文档
     * @param schema   配置结构
     * @param target   目标对象
     * @param adapters 适配器注册表
     * @param policy   绑定策略
     * @param report   诊断报告
     * @throws BindException 绑定异常（仅在策略要求严格时报出）
     */
    public static void read(Doc doc, Schema schema, Object target, Adapters adapters,
                            BindingPolicy policy, BindReport report) throws BindException {
        BindContext context = new BindContext(adapters, DocPath.root(), schema.type(), report, policy);
        bind(doc.root(), target, schema, context);
    }

    /**
     * 把某个对象结点读到目标对象.
     *
     * @param node    对象结点
     * @param target  目标对象
     * @param schema  配置结构
     * @param context 上下文
     * @throws BindException 绑定异常
     */
    public static void bind(DocObject node, Object target, Schema schema, BindContext context) throws BindException {
        Set<String> known = new HashSet<>();
        for (Schema.FieldSpec spec : schema.fields()) {
            known.add(spec.path().first());
        }
        for (Schema.FieldSpec spec : schema.fields()) {
            if (!spec.writable()) {
                context.report(BindReport.Code.NOT_WRITABLE,
                        "字段 " + spec.name() + "（" + spec.type().getTypeName() + "）是 final 或不可访问，已跳过");
                continue;
            }
            TypeAdapter<?> adapter = context.adapters().resolve(spec.type());
            if (adapter == null) {
                String message = "字段 " + spec.name() + " 的类型 " + spec.type().getTypeName()
                        + " 没有可用适配器，请注册后重试";
                if (context.policy().mismatch() == BindingPolicy.Mismatch.ERROR) {
                    throw new BindException(message, spec.path().toString());
                }
                context.report(BindReport.Code.TYPE_MISMATCH, message + "（已保留默认值）");
                continue;
            }
            DocNode value = spec.path().get(node);
            if (value == null) {
                context.report(BindReport.Code.MISSING, "文档中没有该项，已使用字段默认值");
                continue;
            }
            try {
                Object bound = adapter.read(value, context.at(spec.path(), spec.type()));
                if (bound != null) {
                    spec.field().set(target, bound);
                } else if (!spec.field().field().getType().isPrimitive()) {
                    spec.field().set(target, null);
                } else {
                    context.report(BindReport.Code.TYPE_MISMATCH, "配置项为空值而字段是基本类型，已保留默认值");
                }
            } catch (BindException e) {
                if (context.policy().mismatch() == BindingPolicy.Mismatch.ERROR) {
                    throw new BindException(e.getMessage(), spec.path().toString(), e);
                }
                context.report(BindReport.Code.TYPE_MISMATCH, e.getMessage() + "（已保留默认值）");
            } catch (IllegalAccessException e) {
                context.report(BindReport.Code.NOT_WRITABLE, "字段不可写：" + e.getMessage());
            }
        }
        reportUnknown(node, known, context);
    }

    /**
     * 把对象写成文档，并合并到基准文档上.
     *
     * @param source   源对象
     * @param schema   配置结构
     * @param base     基准文档（合并目标），可为 null
     * @param adapters 适配器注册表
     * @param policy   绑定策略
     * @param report   诊断报告
     * @return 结果文档根对象（{@code MERGE} 时就是传入的 base）
     * @throws BindException 绑定异常
     */
    public static DocObject write(Object source, Schema schema, DocObject base, Adapters adapters,
                                  BindingPolicy policy, BindReport report) throws BindException {
        boolean replace = policy.saveMode() == BindingPolicy.SaveMode.REPLACE;
        DocObject target;
        if (base == null || replace) {
            target = new DocObject();
            if (base != null) {
                target.setLayout(base.layout());
                target.setBefore(base.before());
            }
        } else {
            target = base;
        }
        BindContext context = new BindContext(adapters, DocPath.root(), schema.type(), report, policy);
        writeInto(source, schema, target, context);
        return target;
    }

    /**
     * 把一个对象写成新对象结点（供嵌套字段使用）.
     *
     * @param source  源对象
     * @param context 上下文
     * @return 对象结点
     * @throws BindException 绑定异常
     */
    public static DocObject writeObject(Object source, BindContext context) throws BindException {
        Schema schema = Schema.of(source.getClass());
        if (schema.isEmpty()) {
            throw new BindException("类 " + source.getClass().getName() + " 没有任何 @Setting 字段，无法写出",
                    context.path().toString());
        }
        DocObject target = new DocObject();
        writeInto(source, schema, target, context);
        return target;
    }

    private static void writeInto(Object source, Schema schema, DocObject target, BindContext context)
            throws BindException {
        Set<String> known = new HashSet<>();
        for (Schema.FieldSpec spec : schema.fields()) {
            known.add(spec.path().first());
            if (!spec.writable()) {
                context.report(BindReport.Code.NOT_WRITABLE, "字段 " + spec.name() + " 是 final 或不可访问，未写出");
                continue;
            }
            Object value;
            try {
                value = spec.field().get(source);
            } catch (IllegalAccessException e) {
                context.report(BindReport.Code.NOT_WRITABLE, "字段不可读：" + e.getMessage());
                continue;
            }
            if (value == null) {
                // 空值不写出：文档里原有的内容保持不动
                continue;
            }
            TypeAdapter<?> adapter = context.adapters().resolve(spec.type());
            if (adapter == null) {
                throw new BindException("字段 " + spec.name() + " 的类型 " + spec.type().getTypeName()
                        + " 没有可用适配器，请注册后重试", spec.path().toString());
            }
            DocNode fresh = writeValue(adapter, value, context.at(spec.path(), spec.type()));
            if (fresh == null) {
                continue;
            }
            DocNode existing = spec.path().get(target);
            if (existing != null) {
                absorb(existing, fresh);
            }
            applyComment(fresh, spec, context);
            spec.path().set(target, fresh);
        }
        if (context.policy().unknown() == BindingPolicy.Unknown.REMOVE) {
            List<String> extras = new ArrayList<>();
            for (String key : target.keys()) {
                if (!known.contains(key)) {
                    extras.add(key);
                }
            }
            for (String key : extras) {
                target.remove(key);
                context.child(key, null).report(BindReport.Code.NOTE, "按策略移除了不存在的配置项");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static DocNode writeValue(TypeAdapter<?> adapter, Object value, BindContext context) throws BindException {
        return ((TypeAdapter<Object>) adapter).write(value, context);
    }

    /**
     * 记录文档里有、但结构里没有的键.
     */
    private static void reportUnknown(DocObject node, Set<String> known, BindContext context) throws BindException {
        List<String> unknown = new ArrayList<>();
        for (String key : node.keys()) {
            if (!known.contains(key)) {
                unknown.add(key);
            }
        }
        if (unknown.isEmpty()) {
            return;
        }
        for (String key : unknown) {
            context.child(key, null).report(BindReport.Code.UNKNOWN, "不属于任何已知配置项（可能是拼写错误）");
        }
        if (context.policy().unknown() == BindingPolicy.Unknown.ERROR) {
            throw new BindException("存在未知配置项 " + unknown, context.path().toString());
        }
        if (context.policy().unknown() == BindingPolicy.Unknown.REMOVE) {
            for (String key : unknown) {
                node.remove(key);
            }
        }
    }

    /**
     * 把旧结点上的"人写的东西"吸收到新结点上：前置注释、空行、来源位置、书写紧凑度，
     * 并递归处理子结点. 这是"保存后注释还在"的具体实现.
     *
     * @param existing 旧结点
     * @param fresh    新结点（会被就地补充）
     */
    private static void absorb(DocNode existing, DocNode fresh) {
        if (existing == null || fresh == null) {
            return;
        }
        if (fresh.before() == null) {
            fresh.setBefore(existing.before());
        }
        if (!fresh.hasAfter() && existing.hasAfter()) {
            fresh.setAfter(existing.after());
        }
        if (existing.position() != null && !existing.position().isUnknown()) {
            fresh.setPosition(existing.position());
        }
        if (existing.kind() != fresh.kind()) {
            return;
        }
        if (existing instanceof DocObject && fresh instanceof DocObject) {
            DocObject oldObject = (DocObject) existing;
            DocObject newObject = (DocObject) fresh;
            newObject.setLayout(oldObject.layout());
            for (String key : newObject.keys()) {
                absorb(oldObject.get(key), newObject.get(key));
            }
        } else if (existing instanceof DocArray && fresh instanceof DocArray) {
            DocArray oldArray = (DocArray) existing;
            DocArray newArray = (DocArray) fresh;
            newArray.setLayout(oldArray.layout());
            int size = Math.min(oldArray.size(), newArray.size());
            for (int i = 0; i < size; i++) {
                absorb(oldArray.get(i), newArray.get(i));
            }
        }
    }

    /**
     * 把 {@link Setting#comment()} 写进结点，但<strong>只在结点还没有注释时</strong>——
     * 用户手改过的注释永不被程序覆盖.
     */
    private static void applyComment(DocNode node, Schema.FieldSpec spec, BindContext context) {
        if (spec.comment().isEmpty() || (node.before() != null && !node.before().isEmpty())) {
            return;
        }
        String text = spec.comment();
        if (spec.translate()) {
            Function<String, String> translator = context.adapters().commentTranslator();
            if (translator != null) {
                String translated = translator.apply(text);
                if (translated != null) {
                    text = translated;
                }
            }
        }
        if (!text.isEmpty()) {
            node.addBefore(Trivia.comment(text));
        }
    }
}
