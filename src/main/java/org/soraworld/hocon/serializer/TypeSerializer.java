package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.node.Serializable;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 序列化器接口.
 *
 * @param <T> 序列化类型参数
 */
public abstract class TypeSerializer<T, N extends Node> {

    protected Type[] types = new Type[2];

    /**
     * 实例化,并计算类型标记.
     */
    public TypeSerializer() {
        // 必须获取两个有效类型，否则抛出异常
        types[0] = capture();
    }

    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(superclass + " isn't parameterized.");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * 反序列化.
     * 此方法第一行应该检查 node 是否为空
     * {@code if (node == null) throw new NullNodeException();}
     *
     * @param type 实例类型
     * @param node 结点
     * @return 反序列化后的对象
     * @throws HoconException Hocon操作异常
     */
    @Nonnull
    abstract T deserialize(@Nonnull Type type, @Nonnull N node) throws HoconException;

    /**
     * 序列化.
     * 此方法可以抛出异常，但不应该返回空值.
     *
     * @param type    实例类型
     * @param value   序列化对象
     * @param options 配置选项
     * @return 序列化后的结点
     * @throws HoconException Hocon操作异常
     */
    @Nonnull
    abstract N serialize(@Nonnull Type type, @Nonnull T value, @Nonnull Options options) throws HoconException;

    public final boolean keyAble() {
        return types[1] == NodeBase.class;
    }

    /**
     * 获取注册类型.
     *
     * @return 注册类型
     */
    @Nonnull
    public final Type getType() {
        if (this instanceof AnnotationSerializer) return Serializable.class;
        return types[0];
    }
}
