package org.soraworld.hocon.exception;

/**
 * 反序列化异常.
 */
public class DeserializeException extends Exception {
    /**
     * 实例化异常.
     *
     * @param e 引发异常来源
     */
    public DeserializeException(Throwable e) {
        super(e);
    }

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     */
    public DeserializeException(String message) {
        super(message);
    }
}
