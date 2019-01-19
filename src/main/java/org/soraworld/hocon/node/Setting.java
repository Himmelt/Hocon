package org.soraworld.hocon.node;

import java.lang.annotation.*;

/**
 * 序列化配置项注解类.<br>
 * 引用标有此注解的字段，不应复制引用，<br>
 * 而应始终使用所在对象的引用.<br>
 * 这是因为 {@link NodeMap#modify(Object)} 方法会用新的对象替换字段.<br>
 * !!! 注意:<br>
 * 1. 本注解修饰的字段请尽量使用具有 公开无参构造器 的 非抽象类.<br>
 * 2. 请不要使用 Immutable 的容器.<br>
 * 3. 由于数组可能定长, 所以请使用列表等可变容器.
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
