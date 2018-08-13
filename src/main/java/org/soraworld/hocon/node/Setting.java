package org.soraworld.hocon.node;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Setting {
    String path() default "";

    String comment() default "";
}
