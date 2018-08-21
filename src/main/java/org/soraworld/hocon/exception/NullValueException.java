package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

/**
 * 空值异常.
 */
public class NullValueException extends Exception {
    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NullValueException(Type type) {
        super("Null node value for " + type.getTypeName());
    }
}
