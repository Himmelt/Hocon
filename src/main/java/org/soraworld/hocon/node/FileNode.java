package org.soraworld.hocon.node;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 文件结点类.
 */
public class FileNode extends NodeMap {

    private final File file;
    private List<String> heads;

    private static final Field FD_VALUE;

    static {
        Field field = null;
        try {
            field = AbstractNode.class.getDeclaredField("value");
            field.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        FD_VALUE = field;
    }

    /**
     * 实例化一个新的文件结点.
     * 使用默认配置，此配置不可修改.
     *
     * @param file 文件
     */
    public FileNode(File file) {
        super(Options.defaults());
        this.file = file;
    }

    /**
     * 实例化一个新的文件结点.
     *
     * @param file    文件
     * @param options 配置选项
     */
    public FileNode(File file, Options options) {
        super(options);
        this.file = file;
    }

    /**
     * 保存配置到文件.
     *
     * @throws Exception 保存异常
     */
    public void save() throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

        if (heads != null && !heads.isEmpty()) {
            writer.write("# " + options.getHeadLine());
            writer.newLine();
            for (String head : heads) {
                writer.write("# " + head);
                writer.newLine();
            }
            writer.write("# " + options.getHeadLine());
            writer.newLine();
            if (notEmpty()) writer.newLine();
        }

        writeValue(0, writer);
        writer.flush();
        writer.close();
    }

    /**
     * 从文件加载配置.
     *
     * @param backup 是否在失败时还原
     * @throws Exception 加载异常
     */
    public void load(boolean backup) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        if (backup && FD_VALUE != null) {
            Object value = this.value;
            try {
                FD_VALUE.set(this, new LinkedHashMap<>());
                readValue(reader);
            } catch (IllegalAccessException ignored) {
                System.out.println("IllegalAccessException backup will not work !!!");
                readValue(reader);
            } catch (Exception e) {
                try {
                    FD_VALUE.set(this, value);
                    if (options.isDebug()) System.out.println("Fail-Recover success.");
                } catch (IllegalAccessException ignored) {
                    System.out.println("IllegalAccessException recover failed !!!");
                    throw e;
                }
            }
        } else readValue(reader);
        reader.close();
    }

    /**
     * 设置文件头部多行注释.
     *
     * @param heads 头部多行注释
     */
    public void setHeads(List<String> heads) {
        if (heads != null && !heads.isEmpty()) {
            this.heads = new ArrayList<>();
            heads.forEach(s -> this.heads.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.heads.removeIf(String::isEmpty);
        } else this.heads = null;
    }

    /**
     * 添加一条文件头部注释.
     *
     * @param head 头部注释
     */
    public void addHead(String head) {
        if (head != null && !head.isEmpty()) {
            if (heads == null) heads = new ArrayList<>();
            heads.addAll(Arrays.asList(head.split("[\n\r]")));
            heads.removeIf(String::isEmpty);
        }
    }

    /**
     * 清空头部注释.
     */
    public void clearHeads() {
        this.heads = null;
    }
}
