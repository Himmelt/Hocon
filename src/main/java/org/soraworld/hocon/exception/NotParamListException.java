package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

/**
 * 非集合参数化类型异常.
 */
public class NotParamListException extends Exception {
    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NotParamListException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
