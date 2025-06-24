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
package com.jpexs.decompiler.flash.action.as2;

/**
 * Represents a trait in ActionScript 2.
 *
 * @author JPEXS
 */
public interface Trait {

    /**
     * Whether the trait is static
     *
     * @return Whether the trait is static
     */
    public boolean isStatic();

    /**
     * Gets the name of the trait
     *
     * @return Name of the trait
     */
    public String getName();

    /**
     * Gets the type of the trait
     *
     * @return Type of the trait
     */
    public String getType();

    /**
     * Gets the call type of the trait
     *
     * @return Call type of the trait
     */
    public String getCallType();

    /**
     * Gets the class name of the trait
     *
     * @return Class name of the trait
     */
    public String getClassName();
}
