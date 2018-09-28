package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

/**
 * 序列化器接口.
 *
 * @param <T> 序列化类型参数
 */
public interface TypeSerializer<T> {
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
    T deserialize(Type type, Node node) throws HoconException;

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
    Node serialize(Type type, T value, Options options) throws HoconException;

    /**
     * 获取注册类型.
     *
     * @return 注册类型
     */
    Type getRegType();
}
