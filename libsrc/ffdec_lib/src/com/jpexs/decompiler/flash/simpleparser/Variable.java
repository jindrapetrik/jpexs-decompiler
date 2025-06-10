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

import java.util.Arrays;
import java.util.List;

/**
 * Variable.
 * @author JPEXS
 */
public class Variable implements VariableOrScope {

    public boolean definition;
    public String name;
    public int position;
    public Boolean isStatic;
    public String type;
    public String callType;
    public Variable subType;
    public Variable callSubType;

    public Variable(boolean definition, String name, int position) {
        this(definition, name, position, null);
    }
    
    public Variable(boolean definition, String name, int position, Boolean isStatic) {
        this(definition, name, position, isStatic, null, null);
    }
    
    public Variable(boolean definition, String name, int position, Boolean isStatic, String type, String callType) {
        this(definition, name, position, isStatic, type, callType, null, null);
    }
    
    public Variable(boolean definition, String name, int position, Boolean isStatic, String type, String callType, Variable subType, Variable callSubType) {
        this.definition = definition;
        this.name = name;
        this.position = position;
        this.isStatic = isStatic;
        this.type = type;
        this.callType = callType;
        this.subType = subType;
        this.callSubType = callSubType;
    }
    
    @Override
    public String toString() {
        return (definition ? "definition of " : "") + (isStatic == Boolean.TRUE ? "static " : "") + name + " at " + position;
    }        
    
    public String getLastName() {
        String ret = name;
        if (ret.contains(".")) {
            ret = ret.substring(ret.lastIndexOf(".") + 1);
        }        
        return ret;
    }
    
    public String getFirstName() {
        String ret = name;
        if (ret.contains(".")) {
            ret = ret.substring(0, ret.indexOf("."));
        }       
        return ret;
    }
   
    public String getParentName() {
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf("."));
        }
        return null;
    }
    
    public List<String> getParts() {
        return Arrays.asList(name.split("\\.", -1));
    }
    
    public boolean hasParent() {
        return name.contains(".");
    }    
}
