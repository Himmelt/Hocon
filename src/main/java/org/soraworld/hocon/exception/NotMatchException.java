package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

public class NotMatchException extends Exception {
    public NotMatchException(Type regType, Type type) {
        super("Instance type " + type.getTypeName() + " is not match the registered type " + regType.getTypeName());
    }
}
