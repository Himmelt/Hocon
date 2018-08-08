package org.soraworld.hocon;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Setting {

    String value() default "";

    String comment() default "";

}
