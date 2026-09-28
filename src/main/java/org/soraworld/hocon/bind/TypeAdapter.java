package org.soraworld.hocon.bind;

import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.error.BindException;

/**
 * 类型适配器：在 Java 对象与文档结点之间双向转换.
 *
 * <p>实现应当是无状态的，从而可以被缓存与共享；需要知道"当前在绑哪个字段"时，
 * 从 {@link BindContext} 里取路径与期望类型.</p>
 *
 * @param <T> 目标 Java 类型
 * @author Himmelt
 */
public interface TypeAdapter<T> {

    /**
     * 把结点读成对象.
     *
     * @param node    结点
     * @param context 绑定上下文（含期望类型、配置路径、诊断报告）
     * @return 对象；结点为空值且目标类型无法表达时返回 null，由上层决定用默认值还是置空
     * @throws BindException 绑定异常
     */
    T read(DocNode node, BindContext context) throws BindException;

    /**
     * 把对象写成结点.
     *
     * @param value   对象
     * @param context 绑定上下文
     * @return 结点
     * @throws BindException 绑定异常
     */
    DocNode write(T value, BindContext context) throws BindException;
}
