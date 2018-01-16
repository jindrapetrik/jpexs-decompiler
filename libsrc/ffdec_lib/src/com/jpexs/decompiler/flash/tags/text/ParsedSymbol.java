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
package com.jpexs.decompiler.flash.tags.text;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    public Object[] values;

    public SymbolType type;

    public ParsedSymbol(SymbolType type) {
        this.type = type;
        this.values = new Object[0];
    }

    public ParsedSymbol(SymbolType type, Object... values) {
        this.type = type;
        this.values = values;
    }

    @Override
    public String toString() {
        String v = "";
        boolean first = true;
        for (Object o : values) {
            if (!first) {
                v += ", ";
            }
            first = false;
            v += o.toString();
        }
        return type.toString() + " " + v;
    }

    public boolean isType(SymbolType... types) {
        for (SymbolType t : types) {
            if (type == t) {
                return true;
            }
        }
        return false;
    }
}
