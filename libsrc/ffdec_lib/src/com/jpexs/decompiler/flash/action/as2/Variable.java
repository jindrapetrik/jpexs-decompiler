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
public class Variable implements Trait {

    private final boolean isStatic;
    private final String name;
    private final String type;
    private final String className;

    public Variable(boolean isStatic, String name, String type, String className) {
        this.isStatic = isStatic;
        this.name = name;
        this.type = type;
        this.className = className;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return (isStatic ? "static " : "") + "var " + name + ": " + getType();
    }

    @Override
    public String getCallType() {
        return null;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
