/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.decompiler.flash.action.parser.script;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {
    public SymbolGroup group;
    public Object value;
    public SymbolType type;

    public ParsedSymbol(SymbolGroup group,SymbolType type){
        this.group = group;
        this.type = type;
        this.value = null;
    }
    public ParsedSymbol(SymbolGroup group,SymbolType type, Object value) {
        this.group = group;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return group.toString()+" "+type.toString()+" "+(value!=null?value.toString():"");
    }
    public boolean isType(SymbolType... types){
        for(SymbolType t:types){
            if(type==t){
                return true;
            }
        }
        return false;
    }
    
    
}
