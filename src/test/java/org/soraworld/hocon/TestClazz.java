package org.soraworld.hocon;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.serializer.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TestClazz {

    public interface IA {
    }

    public interface IB extends IA {
    }

    public interface AList<ALT, ALM> extends List<ALT> {
    }

    public interface AMap<AMT> extends Map<String, AMT> {
    }

    public static class CC implements IB {
    }

    public static class CD extends CC {
    }

    public static class SA extends TypeSerializer<IA, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SA() throws SerializerException {
        }

        @NotNull
        public TestClazz.IA deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull TestClazz.IA value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SB extends TypeSerializer<IB, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SB() throws SerializerException {
        }

        @NotNull
        public TestClazz.IB deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull TestClazz.IB value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SC extends TypeSerializer<CC, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SC() throws SerializerException {
        }

        @NotNull
        public TestClazz.CC deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull TestClazz.CC value, @NotNull Options options) throws HoconException {
            return null;
        }
    }

    public static class SD extends TypeSerializer<CD, NodeBase> {

        /**
         * 实例化,并计算类型标记.
         *
         * @throws SerializerException 序列化器实例化异常
         */
        public SD() throws SerializerException {
        }

        @NotNull
        public TestClazz.CD deserialize(@NotNull Type fieldType, @NotNull NodeBase node) throws HoconException {
            return null;
        }

        @NotNull
        public NodeBase serialize(@NotNull Type fieldType, @NotNull TestClazz.CD value, @NotNull Options options) throws HoconException {
            return null;
        }
    }
}
