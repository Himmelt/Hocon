package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.*;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * 序列化器接口.
 *
 * @param <T> 序列化类型参数
 */
public interface TypeSerializer<T> {
    /**
     * 反序列化.
     *
     * @param type 实例类型
     * @param node 结点
     * @return 反序列化后的对象
     * @throws NullValueException   空值异常
     * @throws DeserializeException 反序列化异常
     * @throws NotBaseException     非基础结点异常
     * @throws NotMatchException    类型不匹配异常
     */
    T deserialize(@Nonnull Type type, @Nonnull Node node) throws NullValueException, DeserializeException, NotBaseException, NotMatchException;

    /**
     * 序列化.
     *
     * @param type    实例类型
     * @param value   序列化对象
     * @param options 配置选项
     * @return 序列化后的结点
     * @throws NotMatchException  类型不匹配异常
     * @throws SerializeException 序列化异常
     */
    Node serialize(@Nonnull Type type, T value, @Nonnull Options options) throws NotMatchException, SerializeException;

    /**
     * 获取注册类型.
     *
     * @return 注册类型
     */
    @Nonnull
    Type getRegType();
}
