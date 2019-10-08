package org.soraworld.hocon.exception;

/**
 * 序列化反序列化异常.
 * @author Himmelt
 */
public class SerializerException extends HoconException {
    private static final long serialVersionUID = -171049263661664476L;

    /**
     * 实例化异常.
     *
     * @param e 引发异常来源
     */
    public SerializerException(Throwable e) {
        super(e);
    }

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     */
    public SerializerException(String message) {
        super(message);
    }
}
