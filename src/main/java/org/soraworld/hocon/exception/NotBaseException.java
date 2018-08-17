package org.soraworld.hocon.exception;

import java.lang.reflect.Type;

public class NotBaseException extends Exception {
    public NotBaseException(Type type) {
        super("Node is not NodeBase for " + type.getTypeName());
    }
}
