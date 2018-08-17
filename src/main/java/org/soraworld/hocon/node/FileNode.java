package org.soraworld.hocon.node;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

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

    public FileNode(File file) {
        super(Options.defaults());
        this.file = file;
    }

    public FileNode(File file, Options options) {
        super(options);
        this.file = file;
    }

    public void save() throws IOException {
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
                } catch (IllegalAccessException ignored) {
                    System.out.println("IllegalAccessException recover failed !!!");
                    throw e;
                }
            }
        } else readValue(reader);
    }

    public void setHeads(List<String> heads) {
        if (heads != null && !heads.isEmpty()) {
            this.heads = new ArrayList<>();
            heads.forEach(s -> this.heads.addAll(Arrays.asList(s.split("[\n\r]"))));
            this.heads.removeIf(String::isEmpty);
        } else this.heads = null;
    }

    public void addHead(String head) {
        if (head != null && !head.isEmpty()) {
            if (heads == null) heads = new ArrayList<>();
            heads.addAll(Arrays.asList(head.split("[\n\r]")));
            heads.removeIf(String::isEmpty);
        }
    }

    public void clearHeads() {
        this.heads = null;
    }
}
