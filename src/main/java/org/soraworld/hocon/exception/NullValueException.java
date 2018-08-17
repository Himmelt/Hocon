package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

public class NullValueException extends Exception {
    public NullValueException(Type type) {
        super("Null node value for " + type.getTypeName());
    }
}
