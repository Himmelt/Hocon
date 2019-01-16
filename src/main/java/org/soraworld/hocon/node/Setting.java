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
     * Content trans boolean.
     *
     * @return the boolean
     */
    boolean contentTrans() default false;

    /**
     * Serialize trans boolean.
     *
     * @return the boolean
     */
    boolean serializeTrans() default false;

    /**
     * Deserialize trans boolean.
     *
     * @return the boolean
     */
    boolean deserializeTrans() default false;
}
