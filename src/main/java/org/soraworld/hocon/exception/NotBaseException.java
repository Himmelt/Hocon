package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

/**
 * 非基础结点异常.
 */
public class NotBaseException extends Exception {
    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NotBaseException(Type type) {
        super("Node is not NodeBase for " + type.getTypeName());
    }
}
