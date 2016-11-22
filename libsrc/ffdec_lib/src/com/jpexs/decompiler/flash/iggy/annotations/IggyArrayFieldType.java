package com.jpexs.decompiler.flash.iggy.annotations;

import com.jpexs.decompiler.flash.iggy.DataType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IggyArrayFieldType {

    /// Type of value
    DataType value() default DataType.unknown;

    int count() default -1;

    /// Field name on which Count depends
    String countField() default "";

    //Count to add to countField
    int countAdd() default 0;
}
