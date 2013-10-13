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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.GraphSourceItem;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class HilightedTextWriter {
    
    private StringBuilder sb = new StringBuilder();
    private boolean hilight;

    public HilightedTextWriter() {
    }
    
    public HilightedTextWriter(boolean hilight) {
        this.hilight = hilight;
    }

    public boolean getIsHighlighted() {
        return hilight;
    }
    
    public HilightedTextWriter append(String str, GraphSourceItem src, int pos) {
        if (src != null && hilight) {
            sb.append(Highlighting.hilighOffset(str, src.getOffset() + pos + 1));
        } else {
            sb.append(str);
        }
        return this;
    }

    public HilightedTextWriter appendNoHilight(String str) {
        sb.append(str);
        return this;
    }

    public HilightedTextWriter appendNewLine() {
        sb.append("\r\n");
        return this;
    }

    public HilightedTextWriter stripSemicolon() {
        // hack
        if (sb.charAt(sb.length() - 1) == ';') {
            sb.setLength(sb.length() - 1);
        }
        return this;
    }

    public int getLength() {
        return sb.length();
    }
    
    public String toString() {
        return sb.toString();
    }
}
