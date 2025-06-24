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
 * Represents a variable trait in ActionScript 2.
 *
 * @author JPEXS
 */
public class Variable implements Trait {

    /**
     * Whether the variable is static
     */
    private final boolean isStatic;

    /**
     * Name of the variable
     */
    private final String name;

    /**
     * Type of the variable
     */
    private final String type;

    /**
     * Name of the class the variable is in
     */
    private final String className;

    /**
     * Constructs a new variable trait.
     *
     * @param isStatic Whether the variable is static
     * @param name Name of the variable
     * @param type Type of the variable
     * @param className Name of the class the variable is in
     */
    public Variable(boolean isStatic, String name, String type, String className) {
        this.isStatic = isStatic;
        this.name = name;
        this.type = type;
        this.className = className;
    }

    /**
     * Checks whether the variable is static
     *
     * @return Whether the variable is static
     */
    @Override
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Gets the name of the variable
     *
     * @return Name of the variable
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the variable
     *
     * @return Type of the variable
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Converts the variable to a string
     *
     * @return String representation of the variable
     */
    @Override
    public String toString() {
        return (isStatic ? "static " : "") + "var " + name + ": " + getType();
    }

    /**
     * Gets the call type of the variable
     *
     * @return Call type of the variable
     */
    @Override
    public String getCallType() {
        return null;
    }

    /**
     * Gets the class name of the variable
     *
     * @return Class name of the variable
     */
    @Override
    public String getClassName() {
        return className;
    }
}
