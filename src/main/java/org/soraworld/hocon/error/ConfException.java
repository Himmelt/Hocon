package org.soraworld.hocon.error;

/**
 * 配置操作异常基类.
 *
 * <p>词法/语法/绑定/IO 四类失败都继承自这里，且各自携带足够的定位信息，
 * 不再用裸的 {@code StringIndexOutOfBoundsException} 或 {@code throws Exception} 混同表达.</p>
 *
 * @author Himmelt
 */
public class ConfException extends Exception {

    private static final long serialVersionUID = 4501938428903481237L;

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     */
    public ConfException(String message) {
        super(message);
    }

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     * @param cause   引发异常来源
     */
    public ConfException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 实例化异常.
     *
     * @param cause 引发异常来源
     */
    public ConfException(Throwable cause) {
        super(cause);
    }
}
