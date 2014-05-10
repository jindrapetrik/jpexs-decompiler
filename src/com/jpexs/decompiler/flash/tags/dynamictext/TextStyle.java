/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags.dynamictext;

import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.RGBA;

/**
 *
 *
 * @author JPEXS
 */
public class TextStyle {

    public FontTag font;

    public int fontHeight;

    public int fontLeading;

    public boolean bold;

    public boolean italic;

    public boolean underlined;

    public RGBA textColor;

    @Override
    public TextStyle clone() {
        TextStyle result = new TextStyle();
        result.font = font;
        result.fontHeight = fontHeight;
        result.bold = bold;
        result.italic = italic;
        result.underlined = underlined;
        result.textColor = textColor;
        return result;
    }
}
