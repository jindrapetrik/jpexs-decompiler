/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.parser;


public class ParsedSymbol {
    public int type;
    public Object value;

    public static final int TYPE_STRING = 1;
    public static final int TYPE_MULTINAME = 2;
    public static final int TYPE_INSTRUCTION_NAME = 3;
    public static final int TYPE_INTEGER = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_IDENTIFIER = 6;
    public static final int TYPE_EOF = 7;
    public static final int TYPE_LABEL = 8;
    public static final int TYPE_COMMENT = 9;

    public ParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ParsedSymbol(int type) {
        this.type = type;
    }

}
