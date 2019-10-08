package org.soraworld.hocon.node;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * Node 接口.
 * @author Himmelt
 */
public interface Node {

    /**
     * 检查 node 是否非空.
     *
     * @return node 非空 返回 true，空 返回 false
     */
    boolean notEmpty();

    /**
     * 从数据 reader 读取值到 node.
     *
     * @param reader reader
     * @throws Exception 读取异常
     */
    default void readValue(BufferedReader reader) throws Exception {
        readValue(reader, false);
    }

    /**
     * 读取值
     *
     * @param reader       读
     * @param keepComments 是否保持注释
     * @throws Exception 异常
     */
    void readValue(BufferedReader reader, boolean keepComments) throws Exception;

    /**
     * 从 node 值写数据到 writer.
     *
     * @param indent 缩进级别
     * @param writer writer
     * @throws Exception 写入异常
     */
    void writeValue(int indent, BufferedWriter writer) throws Exception;

    /**
     * 获取 node 的多行注释.
     *
     * @return 多行注释
     */
    List<String> getComments();

    /**
     * 为 node 添加一条新注释.
     * 如果注释含有'\r' 或 '\n' 会被解析成多行.
     *
     * @param comment 注释
     */
    void addComment(String comment);

    /**
     * 为 node 设置单行注释,<br>
     * 原来的注释会被替换.
     *
     * @param comment 新注释
     */
    void setComment(String comment);

    /**
     * 为 node 设置多行注释.
     * 原来的注释会被替换.
     *
     * @param comments 新注释列表
     */
    void setComments(List<String> comments);

    /**
     * 写注释内容到数据 writer.
     *
     * @param indent 缩进级别
     * @param writer writer
     * @throws IOException 写入异常
     */
    void writeComment(int indent, BufferedWriter writer) throws IOException;

    /**
     * 翻译
     * @param cfg 配置字
     * @return 节点
     */
    @NotNull
    Node translate(byte cfg);

    /**
     * 获取 配置选项 options.
     *
     * @return options
     */
    Options options();

    /**
     * 写缩进到数据 writer.
     *
     * @param indent 缩进级别
     * @param writer writer
     * @throws IOException 写入异常
     */
    void writeIndent(int indent, BufferedWriter writer) throws IOException;
}
