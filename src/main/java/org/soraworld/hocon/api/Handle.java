package org.soraworld.hocon.api;

import org.soraworld.hocon.bind.BindReport;
import org.soraworld.hocon.bind.Binder;
import org.soraworld.hocon.bind.BindingPolicy;
import org.soraworld.hocon.bind.Schema;
import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.error.ConfException;

import java.io.File;

/**
 * 一个配置文件句柄：把"文件 + 结构 + 对象 + 选项"绑在一起，提供加载与保存的闭环.
 *
 * <pre>
 * Handle&lt;Config&gt; handle = Hocon.of(Config.class, confFile);
 * Config config = handle.load();
 * handle.lastReport().unknown().forEach(log::warn);
 * config.language = "en_us";
 * handle.save();     // 注释、空行、键顺序、用户自己加的键都还在
 * </pre>
 *
 * @param <T> 配置对象类型
 * @author Himmelt
 */
public final class Handle<T> {

    private final T target;
    private final Schema schema;
    private final File file;
    private final Options options;
    private final BindReport report = new BindReport();
    private Doc doc;

    Handle(T target, Schema schema, File file, Options options) {
        this.target = target;
        this.schema = schema;
        this.file = file;
        this.options = options;
    }

    /**
     * 获取配置对象.
     *
     * @return 配置对象
     */
    public T target() {
        return target;
    }

    /**
     * 获取配置文件.
     *
     * @return 文件
     */
    public File file() {
        return file;
    }

    /**
     * 获取当前文档；从未加载/保存过时返回 null.
     *
     * @return 文档，可能为 null
     */
    public Doc document() {
        return doc;
    }

    /**
     * 获取最近一次加载/保存产生的诊断.
     * 每次 {@link #load()} 会清空重来，{@link #save()} 的结果追加进去.
     *
     * @return 诊断报告
     */
    public BindReport lastReport() {
        return report;
    }

    /**
     * 从文件加载到对象.
     *
     * <p>加载是原子的：解析或绑定失败会抛出异常，且内存中的文档与磁盘文件都不受影响.
     * 若策略为 {@link BindingPolicy.Missing#WRITE_BACK} 且确实发现了缺失项，会立即回写文件补齐.</p>
     *
     * @return 配置对象
     * @throws ConfException 读取、解析或绑定异常
     */
    public T load() throws ConfException {
        Doc loaded = Hocon.read(file);
        report.clear();
        Binder.read(loaded, schema, target, options.adapters(), options.policy(), report);
        this.doc = loaded;
        if (options.policy().missing() == BindingPolicy.Missing.WRITE_BACK && !report.missing().isEmpty()) {
            save();
        }
        return target;
    }

    /**
     * 把对象保存回文件.
     *
     * <p>默认按 {@link BindingPolicy.SaveMode#MERGE} 在已有文档上原地更新，因此用户手写的注释、
     * 空行、键顺序与额外键都不会被破坏；写盘走"同目录临时文件 + 原子改名".</p>
     *
     * @throws ConfException 绑定或写出异常
     */
    public void save() throws ConfException {
        Doc current = doc == null ? new Doc() : doc;
        Binder.write(target, schema, current.root(), options.adapters(), options.policy(), report);
        Hocon.write(current, file, options.printOptions());
        this.doc = current;
    }
}
