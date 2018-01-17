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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;

/**
 *
 * @author JPEXS
 */
public class FontImporter {

    public static int getFontTagType(String format) {
        int res = 0;
        switch (format) {
            case "font":
                res = DefineFontTag.ID;
                break;
            case "font2":
                res = DefineFont2Tag.ID;
                break;
            case "font3":
                res = DefineFont3Tag.ID;
                break;
            case "font4":
                res = DefineFont4Tag.ID;
                break;
        }

        return res;
    }
}
