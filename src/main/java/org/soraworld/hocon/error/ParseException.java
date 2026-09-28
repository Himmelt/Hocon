package org.soraworld.hocon.error;

import org.soraworld.hocon.text.Position;

/**
 * 词法/语法解析异常.
 * 消息形如 {@code conf/server.conf:12:5 键 'port' 之后缺少 '=' 或 '{'}.
 *
 * @author Himmelt
 */
public class ParseException extends ConfException {

    private static final long serialVersionUID = 7824310095612398402L;

    private final Position position;

    /**
     * 实例化异常.
     *
     * @param message  异常消息
     * @param position 出错位置，可为 null
     */
    public ParseException(String message, Position position) {
        super(position == null || position.isUnknown() ? message : position.describe() + " " + message);
        this.position = position;
    }

    /**
     * 获取出错位置.
     *
     * @return 出错位置，可能为 null
     */
    public Position position() {
        return position;
    }
}
