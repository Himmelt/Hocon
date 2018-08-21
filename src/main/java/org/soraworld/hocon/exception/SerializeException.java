package org.soraworld.hocon.exception;

/**
 * 序列化异常.
 */
public class SerializeException extends Exception {
    /**
     * 实例化异常.
     *
     * @param e 异常来源
     */
    public SerializeException(Throwable e) {
        super(e);
    }
}
