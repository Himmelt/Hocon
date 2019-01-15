package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

/**
 * 非映射参数化类型异常.
 */
public class NotParamMapException extends Exception {
    private static final long serialVersionUID = 1724228867021216440L;

    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NotParamMapException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
