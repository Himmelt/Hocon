package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.serializer.TypeSerializers;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * 配置选项类.
 */
public class Options {

    private int deep = 0;
    private int indent = 2;
    private boolean seal;
    private boolean debug = false;
    private String headLine = "---------------------------------------------";
    private Function<String, String> translator = key -> key;
    private final TypeSerializers[] serializers = new TypeSerializers[5];

    private static final Options defaults = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
        serializers[0] = TypeSerializers.build();
    }

    /**
     * 获取默认选项，不可修改.
     *
     * @return 默认选项
     */
    public static Options defaults() {
        return defaults;
    }

    /**
     * 获取一个新的配置选项.
     *
     * @return 配置选项
     */
    public static Options build() {
        return new Options(false);
    }

    /**
     * 封印配置.
     * 执行一次之后配置选项便不可以再修改选项.
     */
    public void seal() {
        this.seal = true;
    }

    /**
     * 获取缩进尺寸.
     *
     * @return 缩进尺寸
     */
    public int getIndent() {
        return indent;
    }

    /**
     * 设置缩进尺寸.
     * 如果配置已封印，则无效.
     *
     * @param indent 缩进尺寸
     */
    public void setIndent(int indent) {
        if (!seal) this.indent = indent;
    }

    /**
     * 是否调试模式.
     *
     * @return 是否调试模式
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置调试模式.
     * 如果配置已封印，则无效.
     *
     * @param debug 是否调试
     */
    public void setDebug(boolean debug) {
        if (!seal) this.debug = debug;
    }

    /**
     * 获取文件头部注释的分隔线字符串.
     *
     * @return 分隔线
     */
    public String getHeadLine() {
        return headLine == null ? "" : headLine;
    }

    /**
     * 设置文件头部注释的分隔线字符串.
     * 请不要带有换行符和回车符.
     * 如果配置已封印，则无效.
     *
     * @param headLine 分隔线
     */
    public void setHeadLine(String headLine) {
        if (!seal) this.headLine = headLine.replaceAll("[\r\n]", "");
    }

    /**
     * 获取 {@link Setting} 注解注释的翻译器.
     *
     * @return 翻译器
     */
    public Function<String, String> getTranslator() {
        return translator;
    }

    /**
     * 设置 {@link Setting} 注解注释的翻译器.
     * 如果配置已封印，则无效.
     *
     * @param function 翻译器
     */
    public void setTranslator(Function<String, String> function) {
        if (!seal && function != null) translator = function;
    }

    /**
     * 获取类型对应的序列化器.
     *
     * @param type 类型
     * @return 序列化器
     */
    public TypeSerializer getSerializer(Type type) {
        return serializers[deep].get(type);
    }

    /**
     * 注册类型和对应的序列化器.
     * 注册级别默认为 0
     *
     * @param <T>        类型
     * @param serializer 序列化器
     */
    public <T> void registerType(TypeSerializer<? super T> serializer) {
        if (!seal) serializers[0].registerType(serializer);
    }

    /**
     * 按级别注册类型和对应的序列化器.
     * 注册级别为 0 - 4
     * 0 为最高级，是 1级序列化器集合的父集合，
     * 同理，1级序列化器集合是1级序列化器集合的父集合.
     * 按类型搜索序列化器时会先从最低的等级开始搜寻，找不到再去父集合查找，
     * 直到根集合查找完毕.
     *
     * @param <T>        类型
     * @param serializer 序列化器
     * @param level      注册级别
     */
    public <T> void registerType(TypeSerializer<? super T> serializer, int level) {
        if (!seal && level >= 0) {
            level = level > 4 ? 4 : level;
            if (level > deep) {
                deep = level;
                for (int i = 1; i <= deep; i++) {
                    if (serializers[i] == null) serializers[i] = serializers[i - 1].newChild();
                }
                serializers[deep].registerType(serializer);
            } else serializers[level].registerType(serializer);
        }
    }
}
