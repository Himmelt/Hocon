package org.soraworld.hocon.error;

import java.io.File;

/**
 * 配置文件读写异常.
 *
 * @author Himmelt
 */
public class ConfIoException extends ConfException {

    private static final long serialVersionUID = 5512083477192048231L;

    private final File file;

    /**
     * 实例化异常.
     *
     * @param message 异常消息
     * @param file    文件
     * @param cause   引发异常来源
     */
    public ConfIoException(String message, File file, Throwable cause) {
        super((file == null ? "" : file + ": ") + message, cause);
        this.file = file;
    }

    /**
     * 获取相关文件.
     *
     * @return 文件，可能为 null
     */
    public File file() {
        return file;
    }
}
