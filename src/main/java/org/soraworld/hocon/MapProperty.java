package org.soraworld.hocon;

public @interface MapProperty {

    Class<?> key() default String.class;

    Class<?> val() default String.class;
}
