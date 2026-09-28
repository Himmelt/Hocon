package org.soraworld.hocon.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置项注解：标在字段上表示该字段与文档中的某个配置项对应.
 *
 * <ul>
 *   <li>字段应当是非 {@code final}、具有可访问无参构造的类型；</li>
 *   <li>集合与映射字段建议声明为具体类型（{@code ArrayList}、{@code LinkedHashSet}、{@code LinkedHashMap}），
 *       声明为接口时也能工作（会退回到对应的常用实现）；</li>
 *   <li>{@code final} 字段会被识别但不写入，并在绑定报告里给出一条诊断——
 *       这是 JDK 16+ 的既定限制，不是静默忽略.</li>
 * </ul>
 *
 * @author Himmelt
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Setting {

    /**
     * 配置路径，点分形式；留空表示用字段名.
     *
     * @return 配置路径
     */
    String path() default "";

    /**
     * 注释键或注释文本.
     *
     * @return 注释
     */
    String comment() default "";

    /**
     * 注释是否交给翻译器处理（本地化）.
     *
     * @return 需要翻译返回 true
     */
    boolean translate() default true;
}
