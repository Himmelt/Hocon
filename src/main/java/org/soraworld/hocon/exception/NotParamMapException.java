package org.soraworld.hocon.exception;

import java.lang.reflect.ParameterizedType;

public class NotParamMapException extends Exception {
    public NotParamMapException(ParameterizedType type) {
        super("Not Parameterized Map Type " + type.getTypeName());
    }
}
