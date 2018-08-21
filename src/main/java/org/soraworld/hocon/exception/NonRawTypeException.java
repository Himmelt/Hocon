package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

/**
 * 无原始类型异常.
 */
public class NonRawTypeException extends Exception {
    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NonRawTypeException(Type type) {
        super(type.getTypeName() + " has non raw type");
    }
}
