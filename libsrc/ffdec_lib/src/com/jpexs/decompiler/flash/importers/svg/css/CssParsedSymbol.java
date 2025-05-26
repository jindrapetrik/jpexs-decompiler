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
package com.jpexs.decompiler.flash.importers.svg.css;

/**
 * Css parsed symbol.
 *
 * @author JPEXS
 */
public class CssParsedSymbol {

    /**
     * Value
     */
    public String value;
    /**
     * Type
     */
    public CssSymbolType type;

    /**
     * Constructor.
     *
     * @param value Value
     * @param type Type
     */
    public CssParsedSymbol(String value, CssSymbolType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Constructor.
     * @param ss Value
     * @return CssParsedSymbol
     */
    public boolean isType(String... ss) {
        if (type == CssSymbolType.OTHER) {
            for (String s : ss) {
                if (s.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if type is in types.
     * @param types Types
     * @return True if type is in types
     */
    public boolean isType(CssSymbolType... types) {
        for (CssSymbolType type : types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return type.toString() + ": " + value;
    }

}
