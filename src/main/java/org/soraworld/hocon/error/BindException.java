package org.soraworld.hocon.error;

/**
 * 对象绑定异常.
 * 消息里带上配置路径与字段信息，便于直接定位.
 *
 * @author Himmelt
 */
public class BindException extends ConfException {

    private static final long serialVersionUID = 3095847120394812653L;

    private final String path;

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     * @param path    配置路径，可为 null
     */
    public BindException(String message, String path) {
        super(path == null || path.isEmpty() ? message : "配置项 '" + path + "' " + message);
        this.path = path;
    }

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     * @param path    配置路径，可为 null
     * @param cause   引发异常来源
     */
    public BindException(String message, String path, Throwable cause) {
        super(path == null || path.isEmpty() ? message : "配置项 '" + path + "' " + message, cause);
        this.path = path;
    }

    /**
     * 获取配置路径.
     *
     * @return 配置路径，可能为 null
     */
    public String path() {
        return path;
    }
}
