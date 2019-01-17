package org.soraworld.hocon.node;

import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.serializer.TypeSerializers;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * 配置选项类.
 */
public class Options {

    private int indent = 2;
    private boolean seal;
    private boolean debug = false;
    private Function<String, String> commentTranslator;
    private Function<String, String> serializeTranslator;
    private Function<String, String> deserializeTranslator;
    private final TypeSerializers serializers = new TypeSerializers();

    private static final Options defaults = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
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
     * 翻译 {@link Setting} 注解的注释.
     *
     * @param comment the comment
     * @return 翻译后的注释 string
     */
    public String transComment(String comment) {
        if (comment != null && commentTranslator != null) return commentTranslator.apply(comment);
        return comment;
    }

    /**
     * 设置 {@link Setting} 注解注释的翻译器.
     * 如果配置已封印，则无效.
     *
     * @param function 翻译器
     */
    public void setCommentTranslator(Function<String, String> function) {
        if (!seal) commentTranslator = function;
    }

    /**
     * 翻译 {@link Setting} 注解的注释.
     *
     * @param content the content
     * @return 翻译后的注释 string
     */
    public String transSerializeText(String content) {
        if (content != null && serializeTranslator != null) return serializeTranslator.apply(content);
        return content;
    }

    /**
     * 设置 {@link Setting} 注解注释的翻译器.
     * 如果配置已封印，则无效.
     *
     * @param function 翻译器
     */
    public void setSerializeTranslator(Function<String, String> function) {
        if (!seal) serializeTranslator = function;
    }

    /**
     * 翻译 {@link Setting} 注解的注释.
     *
     * @param content the content
     * @return 翻译后的注释 string
     */
    public String transDeserializeText(String content) {
        if (content != null && deserializeTranslator != null) return deserializeTranslator.apply(content);
        return content;
    }

    /**
     * 设置 {@link Setting} 注解注释的翻译器.
     * 如果配置已封印，则无效.
     *
     * @param function 翻译器
     */
    public void setDeserializeTranslator(Function<String, String> function) {
        if (!seal) deserializeTranslator = function;
    }

    /**
     * 获取类型对应的序列化器.
     *
     * @param type 类型
     * @return 序列化器
     */
    public TypeSerializer getSerializer(Type type) {
        return serializers.get(type);
    }

    /**
     * 注册序列化器.
     *
     * @param serializer 序列化器
     */
    public void registerType(TypeSerializer serializer) {
        if (!seal) {
            try {
                serializers.registerType(serializer);
            } catch (SerializerException e) {
                System.out.println(e.getMessage());
                if (debug) e.printStackTrace();
            }
        }
    }
}
