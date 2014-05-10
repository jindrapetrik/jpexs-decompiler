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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.helpers.HilightedText;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CachedDecompilation implements Serializable {

    public String text;
    public List<Highlighting> traitHilights;
    public List<Highlighting> classHilights;
    public List<Highlighting> methodHilights;
    public List<Highlighting> instructionHilights;
    public List<Highlighting> specialHilights;

    public List<Highlighting> getInstructionHighlights() {
        return instructionHilights;
    }

    public List<Highlighting> getTraitHighlights() {
        return traitHilights;
    }

    public List<Highlighting> getMethodHighlights() {
        return methodHilights;
    }

    public List<Highlighting> getClassHighlights() {
        return classHilights;
    }

    public List<Highlighting> getSpecialHighligths() {
        return specialHilights;
    }

    public CachedDecompilation(HilightedText hilightedText) {
        this.text = hilightedText.text;
        this.traitHilights = hilightedText.traitHilights;
        this.classHilights = hilightedText.classHilights;
        this.methodHilights = hilightedText.methodHilights;
        this.instructionHilights = hilightedText.instructionHilights;
        this.specialHilights = hilightedText.specialHilights;
    }
}
