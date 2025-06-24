/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Target ABC version.
 *
 * @author JPEXS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ABCVersionRequirements {

    /**
     * Gets minimum minor version.
     *
     * @return minimum minor version
     */
    int minMinor() default 0;

    /**
     * Gets maximum minor version.
     *
     * @return maximum minor version
     */
    int maxMinor() default 0;

    /**
     * Gets maximum major version.
     *
     * @return maximum major version
     */
    int maxMajor() default 0;

    /**
     * Get minimum major version.
     *
     * @return minimum major version
     */
    int minMajor() default 0;

    /**
     * Gets exact minor version.
     *
     * @return Exact minor version
     */
    int exactMinor() default 0;
}
