package org.soraworld.hocon.exception;

/**
 * Hocon 操作异常.
 */
public class HoconException extends Exception {
    /**
     * 实例化异常.
     *
     * @param e 引发异常来源
     */
    public HoconException(Throwable e) {
        super(e);
    }

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     */
    public HoconException(String message) {
        super(message);
    }
}