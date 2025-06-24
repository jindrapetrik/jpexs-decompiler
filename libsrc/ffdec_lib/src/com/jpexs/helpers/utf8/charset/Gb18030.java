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
 * Gb18030 encoding.
 *
 * @author JPEXS
 */
public class Gb18030 extends AbstractCharsetConverter {

    private static int[][] gb18030_index_to_cp_len2_record = new int[126][191];

    private static final int GB18030_NULL = 0;

    private static int[] gb18030_len4_record_shift = new int[]{0, -1546, -2806, -4066, -5326, -6586,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -41987, -43247, -44507, -45767, -47027, -48287, -49547, -50807,
        -52067, -53327, 11, -59963, -61223, -62483, 12, 13};

    private static int[][] gb18030_index_to_cp_len4_record = new int[14][1260];

    private static int[] gb18030_cp_to_index_record = new int[61339];

    static {
        //Since data is too long to save it directly into Java source, load it from bin
        InputStream is = Gb18030.class.getResourceAsStream("/com/jpexs/helpers/utf8/charset/Gb18030data.bin");
        if (is == null) {
            System.exit(0);
        }
        ActionScriptLexer lexer = new ActionScriptLexer(new InputStreamReader(is, Utf8Helper.charset));
        try {
            ParsedSymbol s;
            readTwoDimensionalInt(gb18030_index_to_cp_len2_record, lexer);
            readTwoDimensionalInt(gb18030_index_to_cp_len4_record, lexer);
            readOneDimensionalInt(gb18030_cp_to_index_record, lexer);
        } catch (IOException | ActionParseException ex) {
            Logger.getLogger(Gb2312.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int gb18030_index_to_cp_len2(int byte1, int byte2) {
        if (0x81 <= byte1 && byte1 <= 0xfe && 0x40 <= byte2 && byte2 <= 0xfe) {
            return gb18030_index_to_cp_len2_record[byte1 - 0x81][byte2 - 0x40];
        } else {
            return 0;
        }
    }

    public static int gb18030_index_to_cp_len4(int byte1, int byte2, int byte3, int byte4) {
        int pos_1;
        int pos_2;
        byte1 -= 0x81;
        byte2 -= 0x30;
        byte3 -= 0x81;
        byte4 -= 0x30;
        pos_1 = byte1 * 10 + byte2;
        pos_2 = byte3 * 10 + byte4;
        if (pos_1 <= 31 && pos_2 <= 1259) {
            if (gb18030_len4_record_shift[pos_1] < 0) {
                return pos_2 - gb18030_len4_record_shift[pos_1];
            } else {
                return gb18030_index_to_cp_len4_record[gb18030_len4_record_shift[pos_1]][pos_2];
            }
        } else {
            return 0;
        }
    }

    @Override
    public int toUnicode(int codepoint) {
        int result = 0;
        if (0 <= codepoint && codepoint <= 55295) {
            result = (int) gb18030_cp_to_index_record[codepoint];
        } else if (59493 <= codepoint && codepoint <= 65535) {
            result = (int) gb18030_cp_to_index_record[codepoint - 4197];
        }
        return result == 0 ? GB18030_NULL : result;
    }

    @Override
    public int fromUnicode(int codePoint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
