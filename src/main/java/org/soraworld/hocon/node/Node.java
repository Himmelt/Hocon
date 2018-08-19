package org.soraworld.hocon.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * The interface Node.
 */
public interface Node {

    /**
     * Check whether the node is not empty.
     *
     * @return the node is not empty
     */
    boolean notEmpty();

    /**
     * Check node cycle reference.
     *
     * @param node the other node
     * @return true if there is no cycle reference
     */
    boolean checkCycle(Node node);

    /**
     * Read value from data.
     *
     * @param reader the reader
     * @throws Exception the exception
     */
    void readValue(BufferedReader reader) throws Exception;

    /**
     * Write value to data.
     *
     * @param indent the indent
     * @param writer the writer
     * @throws Exception the exception
     */
    void writeValue(int indent, BufferedWriter writer) throws Exception;

    /**
     * Add comment.
     *
     * @param comment the comment
     */
    void addComment(String comment);

    /**
     * Sets comments.
     *
     * @param comments the comments
     */
    void setComments(List<String> comments);

    /**
     * Write comment.
     *
     * @param indent the indent
     * @param writer the writer
     * @throws IOException the io exception
     */
    void writeComment(int indent, BufferedWriter writer) throws IOException;

    /**
     * Options options.
     *
     * @return the options
     */
    Options options();

    /**
     * Write indent.
     *
     * @param indent the indent
     * @param writer the writer
     * @throws IOException the io exception
     */
    void writeIndent(int indent, BufferedWriter writer) throws IOException;
}
