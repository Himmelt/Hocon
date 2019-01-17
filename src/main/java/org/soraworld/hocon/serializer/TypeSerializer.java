package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.util.Reflects;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 序列化器接口.<br>
 * 1. 实现类应当用 {@code final} 修饰<br>
 * 2. 实现类不得含有参数类型<br>
 * 3. 预作为 Map 的键类型，理应实现有效的hashcode和equals方法.
 *
 * @param <T> 序列化类型参数
 * @param <N> 序列化结点类型参数
 */
public abstract class TypeSerializer<T, N extends Node> implements Comparable<TypeSerializer> {

    private Type[] types = new Type[2];

    /**
     * 实例化,并计算类型标记.
     *
     * @throws SerializerException 序列化器实例化异常
     */
    public TypeSerializer() throws SerializerException {
        // 必须获取两个有效类型，否则抛出异常
        ParameterizedType type = Reflects.getGenericType(TypeSerializer.class, getClass());
        if (type != null) {
            Type[] types = type.getActualTypeArguments();
            if (types.length == 2) {
                this.types[0] = types[0];
                this.types[1] = types[1];
                return;
            }
        }
        throw new SerializerException("Type " + getClass().getName() + " MUST has 2 parameters extends 'TypeSerializer<T, N extends Node>'");
    }

    /**
     * 反序列化.
     * 此方法第一行应该检查 node 是否为空
     * {@code if (node == null) throw new NullNodeException();}
     *
     * @param actualType 实例类型
     * @param node       结点
     * @return 反序列化后的对象
     * @throws HoconException Hocon操作异常
     */
    @NotNull
    public abstract T deserialize(@NotNull Type actualType, @NotNull N node) throws HoconException;

    /**
     * 序列化.
     * 此方法可以抛出异常，但不应该返回空值.
     *
     * @param actualType 实际类型
     * @param value      序列化对象
     * @param options    配置选项
     * @return 序列化后的结点
     * @throws HoconException Hocon操作异常
     */
    @NotNull
    public abstract N serialize(@NotNull Type actualType, @NotNull T value, @NotNull Options options) throws HoconException;

    final boolean keyAble() {
        return types[1] == NodeBase.class;
    }

    /**
     * 获取注册类型.
     *
     * @return 注册类型
     */
    @NotNull
    final Type getType() {
        if (this instanceof SerializableSerializer) return Serializable.class;
        return types[0];
    }

    public int hashCode() {
        return Arrays.hashCode(types);
    }

    public String toString() {
        return types[0].getTypeName() + " <-> " + types[1].getTypeName();
    }

    public int compareTo(@NotNull TypeSerializer another) {
        if (this == another) return 0;
        if (Reflects.isAssignableFrom(types[0], another.types[0])) return 1;
        return -1;
    }
}
