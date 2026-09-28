package org.soraworld.hocon.bind;

import org.soraworld.hocon.text.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 绑定诊断报告.
 *
 * <p>这是现有实现最缺的一块：配置里把 {@code language} 拼成 {@code langauge}，
 * 现在的表现是"改了没反应"，有了报告就能在启动日志里直接指出是哪个配置项.</p>
 *
 * @author Himmelt
 */
public final class BindReport {

    /**
     * 诊断类型.
     */
    public enum Code {
        /**
         * 文档里缺少该配置项，字段保留了默认值.
         */
        MISSING,
        /**
         * 文档里有该键，但不属于任何已知配置项，通常是拼写错误.
         */
        UNKNOWN,
        /**
         * 配置项类型与字段类型不符，字段保留了默认值.
         */
        TYPE_MISMATCH,
        /**
         * 字段无法写入（例如 {@code final} 字段），已跳过.
         */
        NOT_WRITABLE,
        /**
         * 一般提示.
         */
        NOTE
    }

    /**
     * 一条诊断.
     */
    public static final class Diagnostic {

        private final Code code;
        private final String path;
        private final Position position;
        private final String message;

        Diagnostic(Code code, String path, Position position, String message) {
            this.code = code;
            this.path = path;
            this.position = position;
            this.message = message;
        }

        /**
         * 获取诊断类型.
         *
         * @return 诊断类型
         */
        public Code code() {
            return code;
        }

        /**
         * 获取配置路径.
         *
         * @return 配置路径，可能为 null
         */
        public String path() {
            return path;
        }

        /**
         * 获取位置.
         *
         * @return 位置，可能为 null
         */
        public Position position() {
            return position;
        }

        /**
         * 获取消息.
         *
         * @return 消息
         */
        public String message() {
            return message;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[").append(code).append("] ");
            if (position != null && !position.isUnknown()) {
                builder.append(position.describe()).append(' ');
            }
            if (path != null && !path.isEmpty()) {
                builder.append("配置项 '").append(path).append("' ");
            }
            return builder.append(message).toString();
        }
    }

    private final List<Diagnostic> all = new ArrayList<>();

    /**
     * 添加一条诊断.
     *
     * @param code     类型
     * @param path     配置路径，可为 null
     * @param position 位置，可为 null
     * @param message  消息
     */
    public void add(Code code, String path, Position position, String message) {
        all.add(new Diagnostic(code, path, position, message));
    }

    /**
     * 获取全部诊断.
     *
     * @return 诊断列表（不可变）
     */
    public List<Diagnostic> all() {
        return Collections.unmodifiableList(all);
    }

    /**
     * 筛选指定类型的诊断.
     *
     * @param codes 目标类型
     * @return 诊断列表
     */
    public List<Diagnostic> of(Code... codes) {
        if (codes == null || codes.length == 0) {
            return all();
        }
        List<Diagnostic> result = new ArrayList<>();
        for (Diagnostic item : all) {
            for (Code code : codes) {
                if (item.code() == code) {
                    result.add(item);
                    break;
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 缺失的配置项.
     *
     * @return 诊断列表
     */
    public List<Diagnostic> missing() {
        return of(Code.MISSING);
    }

    /**
     * 文档里有、但不认识的键.
     *
     * @return 诊断列表
     */
    public List<Diagnostic> unknown() {
        return of(Code.UNKNOWN);
    }

    /**
     * 类型不符的配置项.
     *
     * @return 诊断列表
     */
    public List<Diagnostic> mismatched() {
        return of(Code.TYPE_MISMATCH);
    }

    /**
     * 是否没有任何诊断.
     *
     * @return 没有诊断返回 true
     */
    public boolean isEmpty() {
        return all.isEmpty();
    }

    /**
     * 清空.
     */
    public void clear() {
        all.clear();
    }

    @Override
    public String toString() {
        if (all.isEmpty()) {
            return "（无诊断）";
        }
        StringBuilder builder = new StringBuilder();
        for (Diagnostic item : all) {
            builder.append(item).append('\n');
        }
        return builder.toString();
    }
}
