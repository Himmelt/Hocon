package org.soraworld.hocon.node;

import java.lang.annotation.*;

/**
 * 序列化配置项注解类.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Setting {
    /**
     * 路径树, 以 . 分隔
     *
     * @return 路径树 string
     */
    String path() default "";

    /**
     * 注释.
     *
     * @return 注释 string
     */
    String comment() default "";

    /**
     * 字段翻译配置字, 默认 0b0001.<br>
     * bit_0:是否翻译注释<br>
     * bit_1:是否翻译读取内容<br>
     * bit_2:是否翻译写入内容<br>
     * bit_3:是否全部翻译.
     *
     * @return 配置字
     */
    byte trans() default 0b0001;
}
