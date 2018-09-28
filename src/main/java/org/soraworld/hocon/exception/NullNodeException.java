package org.soraworld.hocon.exception;

/**
 * Node 空异常.
 */
public class NullNodeException extends HoconException {
    /**
     * 实例化 Node 空异常.
     */
    public NullNodeException() {
        super("Node is Null");
    }
}
