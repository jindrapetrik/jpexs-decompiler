/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TEXTRECORD {

    public boolean styleFlagsHasFont;
    public boolean styleFlagsHasColor;
    public boolean styleFlagsHasYOffset;
    public boolean styleFlagsHasXOffset;
    public int fontId;
    public RGB textColor;
    public RGBA textColorA;
    public int xOffset;
    public int yOffset;
    public int textHeight;
    public GLYPHENTRY glyphEntries[];

    public String getText(List<Tag> tags, FontTag font) {
        String ret = "";
        for (GLYPHENTRY ge : glyphEntries) {
            ret += font.glyphToChar(tags, ge.glyphIndex);
        }
        return ret;
    }
}
