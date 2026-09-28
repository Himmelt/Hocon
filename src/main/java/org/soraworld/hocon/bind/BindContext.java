package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.DocPath;

import java.lang.reflect.Type;

/**
 * 绑定上下文.
 * 把"在绑哪个字段、期望什么类型、诊断往哪写"三件事一起传给适配器，从而让深层嵌套的
 * 绑定失败也能定位到具体配置项（而不是抛一个不知道出处的 NPE）.
 *
 * @author Himmelt
 */
public final class BindContext {

    private final Adapters adapters;
    private final DocPath path;
    private final Type type;
    private final BindReport report;
    private final BindingPolicy policy;

    BindContext(Adapters adapters, DocPath path, Type type, BindReport report, BindingPolicy policy) {
        this.adapters = adapters;
        this.path = path == null ? DocPath.root() : path;
        this.type = type;
        this.report = report;
        this.policy = policy;
    }

    /**
     * 获取适配器注册表.
     *
     * @return 注册表
     */
    public Adapters adapters() {
        return adapters;
    }

    /**
     * 获取当前配置路径.
     *
     * @return 配置路径
     */
    public DocPath path() {
        return path;
    }

    /**
     * 获取当前期望类型.
     *
     * @return 期望类型，可能为 null
     */
    public Type type() {
        return type;
    }

    /**
     * 获取诊断报告.
     *
     * @return 诊断报告
     */
    public BindReport report() {
        return report;
    }

    /**
     * 获取绑定策略.
     *
     * @return 绑定策略
     */
    public BindingPolicy policy() {
        return policy;
    }

    /**
     * 派生出子上下文.
     *
     * @param childPath 子路径，为 null 时沿用当前路径
     * @param childType 子期望类型，为 null 时沿用当前类型
     * @return 子上下文
     */
    public BindContext at(DocPath childPath, Type childType) {
        return new BindContext(adapters, childPath == null ? path : childPath,
                childType == null ? type : childType, report, policy);
    }

    /**
     * 派生到某个字段/键.
     *
     * @param segment 段名
     * @param childType 期望类型
     * @return 子上下文
     */
    public BindContext child(String segment, Type childType) {
        return at(path.child(segment), childType);
    }

    /**
     * 派生到数组元素.
     *
     * @param index     下标
     * @param childType 期望类型
     * @return 子上下文
     */
    public BindContext element(int index, Type childType) {
        return at(path.child("[" + index + "]"), childType);
    }

    /**
     * 记录一条诊断.
     *
     * @param code    诊断类型
     * @param message 消息
     */
    public void report(BindReport.Code code, String message) {
        report.add(code, path.toString(), null, message);
    }
}
