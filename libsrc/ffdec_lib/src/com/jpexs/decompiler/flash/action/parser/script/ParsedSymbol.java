/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.parser.script;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    public SymbolGroup group;

    public Object value;

    public SymbolType type;

    public ParsedSymbol(SymbolGroup group, SymbolType type) {
        this.group = group;
        this.type = type;
        this.value = null;
    }

    public ParsedSymbol(SymbolGroup group, SymbolType type, Object value) {
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
