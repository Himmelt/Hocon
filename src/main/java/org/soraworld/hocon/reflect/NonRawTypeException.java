package org.soraworld.hocon.reflect;

import java.lang.reflect.Type;

public class NonRawTypeException extends Exception {
    public NonRawTypeException(Type type) {
        // TODO class type & class name dose not has raw type
        super(type.getTypeName());
    }
}
