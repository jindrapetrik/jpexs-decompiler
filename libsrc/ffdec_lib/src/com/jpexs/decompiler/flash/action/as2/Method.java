/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class Method implements Trait {

    private final boolean isStatic;
    private final String name;
    private final String returnType;
    private final String className;

    public Method(boolean isStatic, String name, String returnType, String className) {
        this.isStatic = isStatic;
        this.name = name;
        this.returnType = returnType;
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return (isStatic ? "static " : "") + "function " + name + ": " + returnType;
    }

    @Override
    public String getCallType() {
        return returnType;
    }

    @Override
    public String getType() {
        return "Function";
    }
}
