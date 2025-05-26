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

import java.nio.charset.Charset;

/**
 * Shift JIS encoding.
 */
public class ShiftJis extends AbstractCharsetConverter {

    private static final Charset SHIFT_JIS_ENCODING = Charset.forName("Shift_JIS");

    @Override
    public int toUnicode(int codePoint) {
        byte[] b;
        if (codePoint > 0xff) {
            b = new byte[]{(byte) ((codePoint >> 8) & 0xff), (byte) (codePoint & 0xff)};
        } else {
            b = new byte[]{(byte) codePoint};
        }

        return new String(b, SHIFT_JIS_ENCODING).charAt(0);
    }

    @Override
    public int fromUnicode(int codePoint) {
        byte[] b = ("" + (char) codePoint).getBytes(SHIFT_JIS_ENCODING);
        int r = 0;
        for (int i = 0; i < b.length; i++) {
            int v = b[b.length - 1 - i] & 0xff;
            r = r + (v << (8 * i));
        }
        return r;
    }

}
