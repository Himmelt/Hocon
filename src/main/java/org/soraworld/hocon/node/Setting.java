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
     * 结点是否可以 {@link NodeMap#modify(java.lang.Object)} 为 null
     *
     * @return 是否可以 modify 为 null
     */
    boolean nullable() default false;

    /**
     * 注释.
     *
     * @return 注释
     */
    String comment() default "";
}
