package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

/**
 * 非集合参数化类型异常.
 */
public class NotParamListException extends Exception {
    private static final long serialVersionUID = -3299009973171243552L;

    /**
     * 实例化异常.
     *
     * @param type 类型
     */
    public NotParamListException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
