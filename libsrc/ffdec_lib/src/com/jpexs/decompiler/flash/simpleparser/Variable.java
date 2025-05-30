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
package com.jpexs.decompiler.flash.simpleparser;

/**
 *
 * @author JPEXS
 */
public class Variable implements VariableOrScope {

    public boolean definition;
    public String name;
    public int position;
    public Boolean isStatic;

    public Variable(boolean definition, String name, int position) {
        this(definition, name, position, null);
    }
    
    public Variable(boolean definition, String name, int position, Boolean isStatic) {
        this.definition = definition;
        this.name = name;
        this.position = position;
        this.isStatic = isStatic;
    }
    
    @Override
    public String toString() {
        return (definition ? "definition of " : "") + (isStatic ? "static " : "") + name + " at " + position;
    }        
    
    public String getLastName() {
        if (name.contains(".")) {
            return name.substring(name.lastIndexOf(".") + 1);
        }
        return name;
    }
    
    public String getFirstName() {
        if (name.contains(".")) {
            return name.substring(0, name.indexOf("."));
        }
        return name;
    }
}
