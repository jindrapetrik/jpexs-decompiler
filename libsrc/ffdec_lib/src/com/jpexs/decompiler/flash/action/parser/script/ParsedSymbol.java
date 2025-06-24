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
package com.jpexs.decompiler.flash.action.parser.script;

/**
 * ActionScript 1/2 parsed symbol.
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    /**
     * Position (characters) in source text
     */
    public int position;
    
    public SymbolGroup group;

    public Object value;

    public SymbolType type;

    public ParsedSymbol(int position, SymbolGroup group, SymbolType type) {
        this.position = position;
        this.group = group;
        this.type = type;
        this.value = null;
    }

    public ParsedSymbol(int position, SymbolGroup group, SymbolType type, Object value) {
        this.position = position;
        this.group = group;
        this.type = type;
        this.value = value;
    }  
    
    @Override
    public String toString() {
        return group.toString() + " " + type.toString() + " " + (value != null ? value.toString() : "");
    }

    public boolean isType(Object... types) {
        for (Object t : types) {
            if (t instanceof SymbolGroup) {
                if (group == t) {
                    return true;
                }
            }
            if (t instanceof SymbolType) {
                if (type == t) {
                    return true;
                }
            }
        }
        return false;
    }
}
