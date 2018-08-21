package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

/**
 * 非映射参数化类型异常.
 */
public class NotParamMapException extends Exception {
    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NotParamMapException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
