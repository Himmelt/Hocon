package org.soraworld.hocon.bind;

/**
 * 绑定策略.
 *
 * @author Himmelt
 */
public final class BindingPolicy {

    /**
     * 文档里缺少配置项时的行为.
     */
    public enum Missing {
        /**
         * 保留字段当前的默认值（默认）.
         */
        KEEP_FIELD_DEFAULT,
        /**
         * 保留默认值，并把该配置项用默认值补写回文档（适合插件升级后自动补齐配置）.
         */
        WRITE_BACK,
        /**
         * 抛异常.
         */
        ERROR
    }

    /**
     * 文档里有、但字段里没有对应配置项时的行为.
     */
    public enum Unknown {
        /**
         * 原样保留（默认，避免吃掉用户手写的额外内容）.
         */
        KEEP,
        /**
         * 移除.
         */
        REMOVE,
        /**
         * 抛异常.
         */
        ERROR
    }

    /**
     * 类型不符时的行为.
     */
    public enum Mismatch {
        /**
         * 保留默认值并记诊断（默认）.
         */
        KEEP_DEFAULT,
        /**
         * 抛异常.
         */
        ERROR
    }

    /**
     * 保存文档时的行为.
     */
    public enum SaveMode {
        /**
         * 在已有文档上按路径更新：已有条目连注释一起保留，新条目追加到末尾（默认）.
         */
        MERGE,
        /**
         * 丢弃已有文档，完全按对象重建.
         */
        REPLACE
    }

    private Missing missing = Missing.KEEP_FIELD_DEFAULT;
    private Unknown unknown = Unknown.KEEP;
    private Mismatch mismatch = Mismatch.KEEP_DEFAULT;
    private SaveMode saveMode = SaveMode.MERGE;

    /**
     * 获取默认策略.
     *
     * @return 默认策略
     */
    public static BindingPolicy defaults() {
        return new BindingPolicy();
    }

    /**
     * 获取缺项行为.
     *
     * @return 缺项行为
     */
    public Missing missing() {
        return missing;
    }

    /**
     * 设置缺项行为.
     *
     * @param missing 缺项行为
     * @return 本策略
     */
    public BindingPolicy missing(Missing missing) {
        this.missing = missing == null ? Missing.KEEP_FIELD_DEFAULT : missing;
        return this;
    }

    /**
     * 获取多余键行为.
     *
     * @return 多余键行为
     */
    public Unknown unknown() {
        return unknown;
    }

    /**
     * 设置多余键行为.
     *
     * @param unknown 多余键行为
     * @return 本策略
     */
    public BindingPolicy unknown(Unknown unknown) {
        this.unknown = unknown == null ? Unknown.KEEP : unknown;
        return this;
    }

    /**
     * 获取类型不符行为.
     *
     * @return 类型不符行为
     */
    public Mismatch mismatch() {
        return mismatch;
    }

    /**
     * 设置类型不符行为.
     *
     * @param mismatch 类型不符行为
     * @return 本策略
     */
    public BindingPolicy mismatch(Mismatch mismatch) {
        this.mismatch = mismatch == null ? Mismatch.KEEP_DEFAULT : mismatch;
        return this;
    }

    /**
     * 获取保存模式.
     *
     * @return 保存模式
     */
    public SaveMode saveMode() {
        return saveMode;
    }

    /**
     * 设置保存模式.
     *
     * @param saveMode 保存模式
     * @return 本策略
     */
    public BindingPolicy saveMode(SaveMode saveMode) {
        this.saveMode = saveMode == null ? SaveMode.MERGE : saveMode;
        return this;
    }
}
