/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.types.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark for field that it is available only when certain field (value) is set.
 *
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Conditional {

    ///Name of field on which this depends
    String[] value() default {};

    ///Tag IDs which this field must be in
    int[] tags() default {};

    ///Minimum SWF version for this field
    int minSwfVersion() default 1;

    ///Maximum SWF version for this field
    int maxSwfVersion() default Integer.MAX_VALUE;

    ///List of values for condition (if true/false is not enough)
    int[] options() default {};

    ///Revert condition (if false...)
    boolean revert() default false;
}
