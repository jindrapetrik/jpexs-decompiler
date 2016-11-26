package com.jpexs.decompiler.flash.iggy.annotations;

import com.jpexs.decompiler.flash.iggy.DataType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IggyFieldType {

    /// Type of value
    DataType value() default DataType.unknown;

    /// Alternate type when condition is met
    DataType alternateValue() default DataType.unknown;

    /// Condition for alternate type
    String alternateCondition() default "";

    /// Count - used primarily for bit fields UB,SB,FB to specify number of bits
    int count() default -1;

    /// Field name on which Count depends
    String countField() default "";

    //Count to add to countField
    int countAdd() default 0;
}
