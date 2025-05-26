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
 * Represents a method trait in ActionScript 2.
 *
 * @author JPEXS
 */
public class Method implements Trait {

    /**
     * Whether the method is static
     */
    private final boolean isStatic;

    /**
     * Name of the method
     */
    private final String name;

    /**
     * Return type of the method
     */
    private final String returnType;

    /**
     * Name of the class the method is in
     */
    private final String className;

    /**
     * Constructs a new method trait.
     *
     * @param isStatic Whether the method is static
     * @param name Name of the method
     * @param returnType Return type of the method
     * @param className Name of the class the method is in
     */
    public Method(boolean isStatic, String name, String returnType, String className) {
        this.isStatic = isStatic;
        this.name = name;
        this.returnType = returnType;
        this.className = className;
    }

    /**
     * Gets the name of the class the method is in.
     *
     * @return Class name
     */
    @Override
    public String getClassName() {
        return className;
    }

    /**
     * Gets whether the method is static.
     *
     * @return Whether the method is static
     */
    @Override
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Gets the name of the method.
     *
     * @return Name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the return type of the method.
     *
     * @return Return type
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Converts the method to a string.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return (isStatic ? "static " : "") + "function " + name + ": " + returnType;
    }

    /**
     * Gets the call type of the method.
     *
     * @return Call type
     */
    @Override
    public String getCallType() {
        return returnType;
    }

    /**
     * Gets the type of the trait.
     *
     * @return Trait type
     */
    @Override
    public String getType() {
        return "Function";
    }
}
