/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.types.annotations;

import com.jpexs.decompiler.flash.types.BasicType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps basic java types (int,float,double) to SWF types (UI8,UI16...FLOAT)
 *
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SWFType {

    /// Type of value
    BasicType value() default BasicType.OTHER;

    /// Alternate type when condition is met
    BasicType alternateValue() default BasicType.NONE;

    /// Condition for alternate type
    String alternateCondition() default "";

    /// Count - used primarily for bit fields UB,SB,FB to specify number of bits
    int count() default -1;

    /// Field name on which Count depends
    String countField() default "";

    //Count to add to countField
    int countAdd() default 0;
}
