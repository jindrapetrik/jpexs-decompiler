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
package com.jpexs.helpers.utf8.charset;

import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GB2312 to unicode and back conversion. Based on
 * https://github.com/MoarVM/MoarVM/blob/master/src/strings/gb2312_codeindex.h
 */
public class Gb2312 extends AbstractCharsetConverter {

    static final int GB2312_NULL = -1;

    /* Conversion tables are generated according to mapping from
     * unicode.org-mappings/EASTASIA/GB/GB2312.TXT
     * at https://haible.de/bruno/charsets/conversion-tables/GB2312.html
     * The following tables use EUC form for GB2312 characters.

     * Unicode indexes 1106 - 8212, 9795 - 12287, 12842 - 19967,
     * and 40865 - 65280 don't correspond to gb2312 codepoint.
     * To reduce code length and save memory, these intervals are omitted
     * in the conversion table and indexes are shifted in the function. */
    private static int[][] gb2312_index_to_cp_record = new int[87][94];

    private static int[] gb2312_cp_to_index_record = new int[24380];

    static {
        //Since data is too long to save it directly into Java source, load it from bin
        InputStream is = Gb2312.class.getResourceAsStream("/com/jpexs/helpers/utf8/charset/Gb2312data.bin");
        if (is == null) {
            System.exit(0);
        }
        ActionScriptLexer lexer = new ActionScriptLexer(new InputStreamReader(is, Utf8Helper.charset));
        try {
            ParsedSymbol s;
            readTwoDimensionalInt(gb2312_index_to_cp_record, lexer);
            readOneDimensionalInt(gb2312_cp_to_index_record, lexer);
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(Gb2312.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int toUnicode(int codePoint) {
        if (codePoint < 128) {
            return codePoint;
        }

        int zone = codePoint / 256 - 161;
        int point = codePoint % 256 - 161;
        if (0 <= zone && zone < 87 && 0 <= point && point < 94) {
            return gb2312_index_to_cp_record[zone][point];
        } else {
            return GB2312_NULL;
        }
    }

    @Override
    public int fromUnicode(int codePoint) {

        int result = 0;
        if (0 <= codePoint && codePoint <= 1105) {
            result = gb2312_cp_to_index_record[codePoint];
        } else if (8213 <= codePoint && codePoint <= 9794) {
            result = gb2312_cp_to_index_record[codePoint - 7107];
        } else if (12288 <= codePoint && codePoint <= 12841) {
            result = gb2312_cp_to_index_record[codePoint - 9600];
        } else if (19968 <= codePoint && codePoint <= 40864) {
            result = gb2312_cp_to_index_record[codePoint - 16726];
        } else if (65281 <= codePoint && codePoint <= 65510) {
            result = gb2312_cp_to_index_record[codePoint - 41142];
        }
        return result == 0 ? GB2312_NULL : result;
    }
}
