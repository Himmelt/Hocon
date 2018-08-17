package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

public class NotParamListException extends Exception {
    public NotParamListException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
