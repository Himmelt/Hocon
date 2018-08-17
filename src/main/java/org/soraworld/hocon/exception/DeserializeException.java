package org.soraworld.hocon.exception;

public class DeserializeException extends Exception {
    public DeserializeException(Throwable e) {
        super(e);
    }

    public DeserializeException(String message) {
        super(message);
    }
}
