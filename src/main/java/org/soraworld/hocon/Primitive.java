package org.soraworld.hocon;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Primitive {

    String value() default "";

    String comment() default "";

}
