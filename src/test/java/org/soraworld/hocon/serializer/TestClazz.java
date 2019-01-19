package org.soraworld.hocon.serializer;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import java.lang.reflect.Type;

public class TestClazz {

    public interface A {
    }

    public interface B extends A {
    }

    public static class C implements B {
    }

    public static class D extends C {
    }

    public static class SA extends TypeSerializer<A, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SA() throws SerializerException {
        }

        @NotNull
        public A deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull A value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SB extends TypeSerializer<B, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SB() throws SerializerException {
        }

        @NotNull
        public B deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull B value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SC extends TypeSerializer<C, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SC() throws SerializerException {
        }

        @NotNull
        public C deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull C value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SD extends TypeSerializer<D, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SD() throws SerializerException {
        }

        @NotNull
        public D deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull D value, @NotNull Options options) throws HoconException {
            return null;
        }
    }
}
