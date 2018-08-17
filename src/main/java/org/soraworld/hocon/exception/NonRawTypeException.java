package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

public class NonRawTypeException extends Exception {
    public NonRawTypeException(Type type) {
        super(type.getTypeName() + " has non raw type");
    }
}
