/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ShiftJis to unicode and back conversion.
 * Based on https://github.com/MoarVM/MoarVM/blob/master/src/strings/shiftjis_codeindex.h
 */
public class ShiftJis extends AbstractCharsetConverter {

    public static final int[][] shiftjis_offset_values = {
        {107, 11},
        {126, 8},
        {141, 11},
        {167, 7},
        {182, 4},
        {187, 15},
        {212, 7},
        {245, 6},
        {277, 4},
        {364, 11},
        {461, 8},
        {493, 8},
        {525, 38},
        {596, 15},
        {644, 13},
        {689, 438},
        {1157, 1},
        {1181, 8},
        {1219, 190},
        {4374, 43},
        {7807, 2908}
    };

    public static final int SHIFTJIS_OFFSET_VALUES_ELEMS = 21;
    public static final int SHIFTJIS_INDEX_TO_CP_CODEPOINTS_ELEMS = 7350;
    public static final int SHIFTJIS_MAX_INDEX = 11103;

    private static final int SHIFTJIS_NULL = -1;

    private static int[] shiftjis_index_to_cp_codepoints = new int[7350];
    private static Map<Integer, Integer> shiftjis_cp_to_index = new HashMap<>();

    static {
        //Since data is too long to save it directly into Java source, load it from bin
        
        InputStream is = Gb2312.class.getResourceAsStream("/com/jpexs/helpers/utf8/charset/ShiftJisdata.bin");
        if (is == null) {
            System.exit(0);
        }
        ActionScriptLexer lexer = new ActionScriptLexer(new InputStreamReader(is, Utf8Helper.charset));
        try {
            ParsedSymbol s;
            readOneDimensionalInt(shiftjis_index_to_cp_codepoints, lexer);
            readMap(shiftjis_cp_to_index, lexer);

        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(ShiftJis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int toUnicode(int codePoint) {
        if (codePoint < 128) {
            return codePoint;
        }
        
        if (shiftjis_cp_to_index.containsKey(codePoint)) {
            return shiftjis_cp_to_index.get(codePoint);
        }
        return SHIFTJIS_NULL;
    }

    @Override
    public int fromUnicode(int codePoint) {
        if (codePoint < shiftjis_index_to_cp_codepoints.length) {
            return shiftjis_index_to_cp_codepoints[codePoint];
        }
        return SHIFTJIS_NULL;
    }
}
