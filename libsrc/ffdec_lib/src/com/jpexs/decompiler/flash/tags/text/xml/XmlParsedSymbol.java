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
package com.jpexs.decompiler.flash.tags.text.xml;

/**
 *
 * @author JPEXS
 */
public class XmlParsedSymbol {
    public Object value;

    public XmlSymbolType type;
    
    public String rawText;
    
    public int position;

    public XmlParsedSymbol(XmlSymbolType type, String rawText, int position) {
        this.type = type;
        this.value = "";
        this.rawText = rawText;
        this.position = position;
    }

    public XmlParsedSymbol(XmlSymbolType type, Object value, String rawText, int position) {
        this.type = type;
        this.value = value;
        this.rawText = rawText;
        this.position = position;
    }

    @Override
    public String toString() {
        return type.toString() + " " + value.toString();
    }

    public boolean isType(XmlSymbolType... types) {
        for (XmlSymbolType t : types) {
            if (type == t) {
                return true;
            }
        }
        return false;
    }
}
