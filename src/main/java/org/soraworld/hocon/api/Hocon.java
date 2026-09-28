package org.soraworld.hocon.api;

import org.soraworld.hocon.bind.Schema;
import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.error.ConfException;
import org.soraworld.hocon.error.ConfIoException;
import org.soraworld.hocon.mirror.Mirror;
import org.soraworld.hocon.printer.PrintOptions;
import org.soraworld.hocon.printer.Printer;
import org.soraworld.hocon.syntax.Lexer;
import org.soraworld.hocon.syntax.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 业务门面：加载 / 保存 / 解析 / 打印的入口.
 *
 * @author Himmelt
 */
public final class Hocon {

    private Hocon() {
    }

    /**
     * 用默认选项为一个对象建立句柄.
     *
     * @param <T>      类型
     * @param instance 配置对象
     * @param file     配置文件
     * @return 句柄
     */
    public static <T> Handle<T> of(T instance, File file) {
        return of(instance, file, Options.defaults());
    }

    /**
     * 为一个对象建立句柄.
     *
     * @param <T>      类型
     * @param instance 配置对象
     * @param file     配置文件
     * @param options  选项
     * @return 句柄
     */
    public static <T> Handle<T> of(T instance, File file, Options options) {
        if (instance == null) {
            throw new IllegalArgumentException("配置对象不能为 null");
        }
        if (file == null) {
            throw new IllegalArgumentException("配置文件不能为 null");
        }
        Schema schema = Schema.of(instance.getClass());
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("类 " + instance.getClass().getName() + " 没有任何 @Setting 字段");
        }
        return new Handle<T>(instance, schema, file, options == null ? Options.defaults() : options);
    }

    /**
     * 为一个类建立句柄（自动构造实例；若只有静态 {@link org.soraworld.hocon.bind.Setting} 字段则走静态模式）.
     *
     * @param <T>   类型
     * @param type  配置类
     * @param file  配置文件
     * @return 句柄
     */
    public static <T> Handle<T> of(Class<T> type, File file) {
        return of(type, file, Options.defaults());
    }

    /**
     * 为一个类建立句柄.
     *
     * @param <T>     类型
     * @param type    配置类
     * @param file    配置文件
     * @param options 选项
     * @return 句柄
     */
    @SuppressWarnings("unchecked")
    public static <T> Handle<T> of(Class<T> type, File file, Options options) {
        if (type == null) {
            throw new IllegalArgumentException("配置类不能为 null");
        }
        Schema instanceSchema = Schema.of(type);
        if (!instanceSchema.isEmpty()) {
            Object instance;
            try {
                instance = Mirror.newInstance(type);
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new IllegalArgumentException("类 " + type.getName() + " 必须具有可访问的无参构造器", e);
            }
            return new Handle<T>((T) instance, instanceSchema, file, options == null ? Options.defaults() : options);
        }
        Schema staticSchema = Schema.of(type, true);
        if (!staticSchema.isEmpty()) {
            return new Handle<T>((T) type, staticSchema, file, options == null ? Options.defaults() : options);
        }
        throw new IllegalArgumentException("类 " + type.getName() + " 没有任何 @Setting 字段");
    }

    /**
     * 解析文本.
     *
     * @param text       文本
     * @param sourceName 来源名，用于报错定位，可为 null
     * @return 文档
     * @throws ConfException 解析异常
     */
    public static Doc parse(String text, String sourceName) throws ConfException {
        return Parser.parse(text, sourceName);
    }

    /**
     * 读取配置文件.
     *
     * @param file 文件
     * @return 文档
     * @throws ConfException 读取或解析异常
     */
    public static Doc read(File file) throws ConfException {
        if (!file.isFile()) {
            throw new ConfIoException("配置文件不存在或不是普通文件", file, null);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            return Parser.parse(Lexer.readAll(reader), file.getPath());
        } catch (IOException e) {
            throw new ConfIoException("读取失败", file, e);
        }
    }

    /**
     * 打印文档.
     *
     * @param doc     文档
     * @param options 打印策略，可为 null
     * @return 文本
     */
    public static String print(Doc doc, PrintOptions options) {
        return options == null ? Printer.print(doc) : Printer.print(doc, options);
    }

    /**
     * 原子地写出文档.
     * 先写同目录临时文件，再改名覆盖；任一步失败都不会留下半截配置.
     *
     * @param doc     文档
     * @param file    目标文件
     * @param options 打印策略，可为 null
     * @throws ConfException 写出异常
     */
    public static void write(Doc doc, File file, PrintOptions options) throws ConfException {
        Path target = file.toPath();
        Path parent = target.getParent();
        if (parent != null && !Files.isDirectory(parent)) {
            throw new ConfIoException("配置目录不存在", file, null);
        }
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write(print(doc, options));
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new ConfIoException("保存失败", file, e);
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // 临时文件清理失败不影响主流程
            }
        }
    }
}
