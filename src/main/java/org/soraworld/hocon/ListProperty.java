package org.soraworld.hocon;

public @interface ListProperty {

    Class<?> type() default String.class;

}
