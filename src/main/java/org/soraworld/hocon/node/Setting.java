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
     * @return 路径树
     */
    String path() default "";

    /**
     * 注释.
     *
     * @return 注释
     */
    String comment() default "";
}
