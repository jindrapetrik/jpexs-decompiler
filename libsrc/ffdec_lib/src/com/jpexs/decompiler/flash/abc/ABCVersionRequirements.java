package com.jpexs.decompiler.flash.abc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Target ABC version
 *
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ABCVersionRequirements {

    int minMinor() default 0;

    int maxMinor() default 0;

    int maxMajor() default 0;

    int minMajor() default 0;

    int exactMinor() default 0;
}
