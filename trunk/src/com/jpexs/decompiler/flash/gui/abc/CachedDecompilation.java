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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CachedDecompilation implements Serializable {

    public String text;
    public String hilightedText;

    public List<Highlighting> getHighlights() {
        return Highlighting.getInstrHighlights(hilightedText);
    }

    public List<Highlighting> getTraitHighlights() {
        return Highlighting.getTraitHighlights(hilightedText);
    }

    public List<Highlighting> getMethodHighlights() {
        return Highlighting.getMethodHighlights(hilightedText);
    }

    public List<Highlighting> getClassHighlights() {
        return Highlighting.getClassHighlights(hilightedText);
    }

    public List<Highlighting> getSpecialHighligths() {
        return Highlighting.getSpecialHighlights(hilightedText);
    }

    public CachedDecompilation(String hilightedText) {
        this.hilightedText = hilightedText;
        this.text = Highlighting.stripHilights(hilightedText);
    }
}
