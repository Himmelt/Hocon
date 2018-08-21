package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

/**
 * 序列化类型不匹配异常.
 */
public class NotMatchException extends Exception {
    /**
     * 实例化异常.
     *
     * @param regType 注册类型
     * @param type    实例类型
     */
    public NotMatchException(Type regType, Type type) {
        super("Instance type " + type.getTypeName() + " is not match the registered type " + regType.getTypeName());
    }
}
